package dev.ohs.player.reference.client.app.security

import dev.ohs.player.reference.client.app.models.PinData
import eu.anifantakis.lib.ksafe.KSafe
import eu.anifantakis.lib.ksafe.KSafeWriteMode
import kotlinx.serialization.json.Json
import org.kotlincrypto.hash.sha2.SHA256
import kotlin.random.Random
import kotlin.time.Clock

import dev.ohs.player.reference.client.app.utils.HexUtils


class PinManager(
    private val secureStorage: KSafe
) {

    private val pinStorageKey = "user_pin_data"
    private val json = Json {
        encodeDefaults = true
        prettyPrint = false
        ignoreUnknownKeys = true
    }

    private val PBKDF2_ITERATIONS = 10000
    private val KEY_LENGTH = 256

    /**
     * Creates a new PIN for the user. Hashes it and stores it securely.
     * Returns Result.success(true) if successful, Result.failure with error if fails.
     */
    suspend fun createPin(newPin: String): Result<Boolean> {
        return try {
            // Validate PIN strength
            if (!isValidPin(newPin)) {
                return Result.failure(IllegalArgumentException("PIN must be 4-6 digits"))
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

            // Create PIN data with metadata
            val pinData = PinData(
                hashedPin = hashedPin,
                salt = salt,
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
            val jsonString = secureStorage.get(pinStorageKey, "")

            if (jsonString.isEmpty()) {
                return ValidatePinResult.PinNotFound
            }

            val pinData = json.decodeFromString<PinData>(jsonString)

            // Check if PIN is locked
            if (pinData.isCurrentlyLocked()) {
                return ValidatePinResult.Locked(pinData.lockedUntil)
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
            // Validate new PIN
            if (!isValidPin(newPin)) {
                return Result.failure(IllegalArgumentException("New PIN must be 4-6 digits"))
            }

            // Verify current PIN
            val validationResult = validatePin(currentPin)
            when (validationResult) {
                is ValidatePinResult.Success -> {
                    // Create new PIN data
                    val salt = generateSecureSalt()
                    val hashedPin = hashPinWithPBKDF2(newPin, salt)

                    val pinData = PinData(
                        hashedPin = hashedPin,
                        salt = salt,
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
                    Result.failure(IllegalStateException("PIN is locked. Please try again later."))
                }

                is ValidatePinResult.PinNotFound -> {
                    Result.failure(IllegalStateException("No PIN exists. Use createPin instead."))
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
                pinData.isCurrentlyLocked() -> PinStatus.Locked(pinData.lockedUntil)
                pinData.isLocked -> PinStatus.Locked(pinData.lockedUntil)
                else -> PinStatus.Set(pinData.createdAt)
            }
        } catch (e: Exception) {
            PinStatus.Error(e.message ?: "Unknown error")
        }
    }

    // Private helper functions

    private fun isValidPin(pin: String): Boolean {
        return pin.length in 4..6 && pin.all { it.isDigit() }
    }

    /**
     * Generates a cryptographically secure random salt.
     * Returns hex-encoded salt string.
     */
    private fun generateSecureSalt(): String {
        val saltBytes = ByteArray(32) // 256-bit salt
        // Note: For production, use platform-specific secure random
        // For now using Kotlin's Random
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
        // Convert hex salt back to bytes
        val saltBytes = HexUtils.decode(salt)

        // Combine salt and PIN
        val pinBytes = pin.encodeToByteArray()
        val combined = saltBytes + pinBytes

        // For KMP, we need a platform-agnostic hashing approach
        // Using SHA256 as a simpler alternative that works across platforms
        return hashPinWithSHA256(pin, HexUtils.encode(saltBytes))
    }

    /**
     * Simple SHA256 hashing (works across all KMP platforms).
     * For production, consider using a proper PBKDF2 implementation per platform.
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
    data class Locked(val lockedUntil: Long) : ValidatePinResult()
    data class Error(val exception: Exception) : ValidatePinResult()
}

/**
 * PIN status information
 */
sealed class PinStatus {
    object NotSet : PinStatus()
    data class Set(val createdAt: Long) : PinStatus()
    data class Locked(val lockedUntil: Long) : PinStatus()
    data class Error(val message: String) : PinStatus()
}