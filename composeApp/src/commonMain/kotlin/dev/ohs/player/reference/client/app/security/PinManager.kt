package dev.ohs.player.reference.client.app.security

import dev.ohs.player.reference.client.app.models.PinData
import eu.anifantakis.lib.ksafe.KSafe
import eu.anifantakis.lib.ksafe.KSafeWriteMode
import kotlinx.serialization.json.Json
import org.kotlincrypto.hash.sha2.SHA256
import kotlin.random.Random


class PinManager(
    private val secureStorage: KSafe
) {
    private val pinStorageKey = "user_pin_data"

    /**
     * Creates a new PIN for the user. Hashes it and stores it securely.
     * Returns true if successful, false if a PIN already exists.
     */
    suspend fun createPin(newPin: String): Boolean {
        val existingPinData = secureStorage.get(pinStorageKey, "")
        if (existingPinData.isNotEmpty()) {
            return false
        }
        val salt = generateSalt()
        val hashedPin = hashPinWithSalt(newPin, salt)
        val pinData = PinData(hashedPin = hashedPin, salt = salt)
        val jsonString = Json.encodeToString(pinData)
        secureStorage.put(pinStorageKey, jsonString, mode = KSafeWriteMode.Encrypted())

        return true
    }

    /**
     * Validates a user-entered PIN against the stored, hashed PIN.
     * Returns true if the PIN is correct.
     */
    suspend fun validatePin(enteredPin: String): Boolean {
        val jsonString = secureStorage.get(pinStorageKey, "") ?: return false

        if (jsonString.isEmpty()) {
            return false
        }
        val pinData = Json.decodeFromString<PinData>(jsonString)
        val hashedEnteredPin = hashPinWithSalt(enteredPin, pinData.salt)
        return hashedEnteredPin == pinData.hashedPin
    }

    private fun ByteArray.toHexString(): String {
        val hexChars = "0123456789abcdef"
        return buildString {
            for (byte in this@toHexString) {
                val value = byte.toInt() and 0xFF
                append(hexChars[value shr 4])
                append(hexChars[value and 0x0F])
            }
        }
    }

    private fun generateSalt(): String {
        return Random.nextBytes(32).toHexString()
    }

    private fun hashPinWithSalt(pin: String, salt: String): String {
        val input = "$salt:$pin".encodeToByteArray()
        val digest = SHA256().digest(input)
        return digest.toHexString()
    }
}