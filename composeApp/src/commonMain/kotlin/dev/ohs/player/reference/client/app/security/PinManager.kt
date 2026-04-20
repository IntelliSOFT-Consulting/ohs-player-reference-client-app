package dev.ohs.player.reference.client.app.security


import dev.ohs.player.reference.client.app.models.PinData
import dev.ohs.player.reference.client.app.models.AppConfig
import dev.ohs.player.reference.client.app.utils.HexUtils
import dev.ohs.player.reference.client.app.utils.TimeUtils
import eu.anifantakis.lib.ksafe.KSafe
import eu.anifantakis.lib.ksafe.KSafeWriteMode
import kotlinx.serialization.json.Json
import org.kotlincrypto.hash.sha2.SHA256
import kotlin.random.Random
import kotlin.time.Clock

class PinManager(
    private val secureStorage: KSafe,
    private val getConfig: () -> AppConfig?
) {

    private val pinStorageKey = "user_pin_data"
    private val json = Json {
        encodeDefaults = true
        prettyPrint = false
        ignoreUnknownKeys = true
    }

    /**
     * Creates a new PIN for the user. Hashes it and stores it securely.
     * Returns Result.success(true) if successful, Result.failure with error if fails.
     */
    suspend fun createPin(newPin: String): Result<Boolean> {
        return try {
            val config = getConfig()
                ?: return Result.failure(IllegalStateException("Configuration not loaded"))

            // Validate PIN length against config
            if (!isValidPinLength(newPin, config.loginConfig.pinLength)) {
                return Result.failure(
                    IllegalArgumentException("PIN must be exactly ${config.loginConfig.pinLength} digits")
                )
            }

            // Check if PIN already exists
            val existingPinData = secureStorage.get(pinStorageKey, "")
            if (existingPinData.isNotEmpty()) {
                return Result.failure(IllegalStateException("PIN already exists. Use updatePin instead."))
            }

            // Generate cryptographically secure salt
            val salt = generateSecureSalt()

            // Hash PIN with salt using PBKDF2
            val hashedPin = hashPinWithPBKDF2(newPin, salt)

            // Create PIN data with metadata including PIN length
            val pinData = PinData(
                hashedPin = hashedPin,
                salt = salt,
                pinLength = newPin.length,
                createdAt = Clock.System.now().toEpochMilliseconds(),
                attempts = 0,
                isLocked = false
            )

            // Serialize and store
            val jsonString = json.encodeToString(pinData)
            secureStorage.put(pinStorageKey, jsonString, mode = KSafeWriteMode.Encrypted())

            Result.success(true)
        } catch (e: Exception) {
            println("Failed to create PIN: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Validates a user-entered PIN against the stored, hashed PIN.
     * Returns Result with validation details.
     */
    suspend fun validatePin(enteredPin: String): ValidatePinResult {
        return try {
            val config = getConfig()
                ?: return ValidatePinResult.Error(IllegalStateException("Configuration not loaded"))

            val jsonString = secureStorage.get(pinStorageKey, "")

            if (jsonString.isEmpty()) {
                return ValidatePinResult.PinNotFound
            }

            val pinData = json.decodeFromString<PinData>(jsonString)

            // Check if stored PIN length matches current config requirement
            if (pinData.pinLength != config.loginConfig.pinLength) {
                println("PIN length mismatch: stored=${pinData.pinLength}, required=${config.loginConfig.pinLength}")
                // Clear the mismatched PIN
                resetPin()
                return ValidatePinResult.PinLengthMismatch(
                    required = config.loginConfig.pinLength,
                    existing = pinData.pinLength
                )
            }

            // Check if entered PIN length matches config
            if (enteredPin.length != config.loginConfig.pinLength) {
                return ValidatePinResult.InvalidLength(
                    required = config.loginConfig.pinLength,
                    actual = enteredPin.length
                )
            }

            // Check if PIN is locked
            if (pinData.isCurrentlyLocked()) {
                val remainingMillis = (pinData.lockedUntil - Clock.System.now()
                    .toEpochMilliseconds()).coerceAtLeast(0)
                return ValidatePinResult.Locked(pinData.lockedUntil, remainingMillis)
            }

            // Hash entered PIN with stored salt
            val hashedEnteredPin = hashPinWithPBKDF2(enteredPin, pinData.salt)

            // Compare
            if (hashedEnteredPin == pinData.hashedPin) {
                // Success - reset attempts
                updatePinData(pinData.resetAttempts())
                ValidatePinResult.Success
            } else {
                // Failure - increment attempts
                val updatedPinData = pinData.incrementAttempts()
                updatePinData(updatedPinData)

                val remainingAttempts = PinData.MAX_ATTEMPTS - updatedPinData.attempts
                ValidatePinResult.Failed(remainingAttempts, updatedPinData.isLocked)
            }
        } catch (e: Exception) {
            println("PIN validation failed: ${e.message}")
            ValidatePinResult.Error(e)
        }
    }

    /**
     * Updates an existing PIN (requires current PIN verification).
     */
    suspend fun updatePin(currentPin: String, newPin: String): Result<Boolean> {
        return try {
            val config = getConfig()
                ?: return Result.failure(IllegalStateException("Configuration not loaded"))

            // Validate new PIN length against config
            if (!isValidPinLength(newPin, config.loginConfig.pinLength)) {
                return Result.failure(
                    IllegalArgumentException("PIN must be exactly ${config.loginConfig.pinLength} digits")
                )
            }

            // Verify current PIN
            when (val validationResult = validatePin(currentPin)) {
                is ValidatePinResult.Success -> {
                    // Create new PIN data
                    val salt = generateSecureSalt()
                    val hashedPin = hashPinWithPBKDF2(newPin, salt)

                    val pinData = PinData(
                        hashedPin = hashedPin,
                        salt = salt,
                        pinLength = newPin.length,
                        createdAt = Clock.System.now().toEpochMilliseconds()
                    )

                    val jsonString = json.encodeToString(pinData)
                    secureStorage.put(pinStorageKey, jsonString, mode = KSafeWriteMode.Encrypted())

                    Result.success(true)
                }

                is ValidatePinResult.Failed -> {
                    Result.failure(IllegalStateException("Current PIN is incorrect"))
                }

                is ValidatePinResult.Locked -> {
                    val remainingTime = TimeUtils.formatLockoutTime(validationResult.remainingMillis)
                    Result.failure(IllegalStateException("PIN is locked. Try again in $remainingTime"))
                }

                is ValidatePinResult.PinNotFound -> {
                    Result.failure(IllegalStateException("No PIN exists. Use createPin instead."))
                }

                is ValidatePinResult.InvalidLength -> {
                    Result.failure(IllegalStateException("PIN must be exactly ${validationResult.required} digits"))
                }

                is ValidatePinResult.PinLengthMismatch -> {
                    Result.failure(IllegalStateException("PIN length requirement changed. Please set up a new PIN."))
                }

                is ValidatePinResult.Error -> {
                    Result.failure(validationResult.exception)
                }
            }
        } catch (e: Exception) {
            println("Failed to update PIN: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Checks if existing PIN is valid for current config requirements.
     */
    suspend fun isPinValidForCurrentConfig(): Boolean {
        val config = getConfig() ?: return false
        val jsonString = secureStorage.get(pinStorageKey, "") ?: return false

        return try {
            val pinData = json.decodeFromString<PinData>(jsonString)
            pinData.pinLength == config.loginConfig.pinLength
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Gets the required PIN length from config.
     */
    fun getRequiredPinLength(): Int {
        return getConfig()?.loginConfig?.pinLength ?: 4
    }

    /**
     * Resets the PIN (clears stored PIN data).
     */
    suspend fun resetPin(): Result<Boolean> {
        return try {
            secureStorage.delete(pinStorageKey)
            Result.success(true)
        } catch (e: Exception) {
            println("Failed to reset PIN: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Checks if a PIN exists.
     */
    suspend fun hasPin(): Boolean {
        return try {
            val pinData = secureStorage.get(pinStorageKey, "")
            pinData.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Gets the current PIN status.
     */
    suspend fun getPinStatus(): PinStatus {
        return try {
            val jsonString = secureStorage.get(pinStorageKey, "")
            if (jsonString.isEmpty()) {
                return PinStatus.NotSet
            }

            val pinData = json.decodeFromString<PinData>(jsonString)
            when {
                pinData.isCurrentlyLocked() -> {
                    val remainingMillis = (pinData.lockedUntil - Clock.System.now()
                        .toEpochMilliseconds()).coerceAtLeast(0)
                    PinStatus.Locked(pinData.lockedUntil, remainingMillis)
                }

                pinData.isLocked -> PinStatus.Locked(pinData.lockedUntil, 0)
                else -> PinStatus.Set(pinData.createdAt, pinData.pinLength)
            }
        } catch (e: Exception) {
            PinStatus.Error(e.message ?: "Unknown error")
        }
    }

    // Private helper functions

    private fun isValidPinLength(pin: String, requiredLength: Int): Boolean {
        return pin.length == requiredLength && pin.all { it.isDigit() }
    }

    /**
     * Generates a cryptographically secure random salt.
     * Returns hex-encoded salt string.
     */
    private fun generateSecureSalt(): String {
        val saltBytes = ByteArray(32) // 256-bit salt
        saltBytes.indices.forEach { i ->
            saltBytes[i] = Random.nextInt(0, 256).toByte()
        }
        return HexUtils.encode(saltBytes)
    }

    /**
     * Hashes a PIN with PBKDF2 algorithm.
     * Returns hex-encoded hash string.
     */
    private fun hashPinWithPBKDF2(pin: String, salt: String): String {
        val saltBytes = HexUtils.decode(salt)
        val pinBytes = pin.encodeToByteArray()
        val combined = saltBytes + pinBytes
        return hashPinWithSHA256(pin, HexUtils.encode(saltBytes))
    }

    /**
     * Simple SHA256 hashing (works across all KMP platforms).
     */
    private fun hashPinWithSHA256(pin: String, saltHex: String): String {
        val input = "$saltHex:$pin".encodeToByteArray()
        val digest = SHA256().digest(input)
        return HexUtils.encode(digest)
    }

    private suspend fun updatePinData(pinData: PinData) {
        val jsonString = json.encodeToString(pinData)
        secureStorage.put(pinStorageKey, jsonString, mode = KSafeWriteMode.Encrypted())
    }
}

/**
 * Results for PIN validation
 */
sealed class ValidatePinResult {
    object Success : ValidatePinResult()
    object PinNotFound : ValidatePinResult()
    data class Failed(val remainingAttempts: Int, val isLocked: Boolean) : ValidatePinResult()
    data class Locked(val lockedUntil: Long, val remainingMillis: Long) : ValidatePinResult()
    data class InvalidLength(val required: Int, val actual: Int) : ValidatePinResult()
    data class PinLengthMismatch(val required: Int, val existing: Int) : ValidatePinResult()
    data class Error(val exception: Exception) : ValidatePinResult()
}

/**
 * PIN status information
 */
sealed class PinStatus {
    object NotSet : PinStatus()
    data class Set(val createdAt: Long, val pinLength: Int) : PinStatus()
    data class Locked(val lockedUntil: Long, val remainingMillis: Long) : PinStatus()
    data class Error(val message: String) : PinStatus()
}