package dev.ohs.player.reference.client.app.utils

/**
 * Utility class for hexadecimal encoding and decoding operations.
 * Works across all KMP targets (Android, iOS, Desktop, JS, Wasm).
 */
object HexUtils {

    private const val HEX_CHARS_LOWER = "0123456789abcdef"
    private const val HEX_CHARS_UPPER = "0123456789ABCDEF"

    /**
     * Encodes a byte array to a lowercase hexadecimal string.
     *
     * @param bytes The byte array to encode
     * @return Hexadecimal string representation
     * @throws IllegalArgumentException if bytes is null
     */
    fun encode(bytes: ByteArray): String {
        require(bytes.isNotEmpty()) { "Byte array cannot be empty" }

        return buildString(capacity = bytes.size * 2) {
            for (byte in bytes) {
                val value = byte.toInt() and 0xFF
                append(HEX_CHARS_LOWER[value ushr 4])
                append(HEX_CHARS_LOWER[value and 0x0F])
            }
        }
    }

    /**
     * Encodes a byte array to an uppercase hexadecimal string.
     *
     * @param bytes The byte array to encode
     * @return Uppercase hexadecimal string representation
     */
    fun encodeUpper(bytes: ByteArray): String {
        require(bytes.isNotEmpty()) { "Byte array cannot be empty" }

        return buildString(capacity = bytes.size * 2) {
            for (byte in bytes) {
                val value = byte.toInt() and 0xFF
                append(HEX_CHARS_UPPER[value ushr 4])
                append(HEX_CHARS_UPPER[value and 0x0F])
            }
        }
    }

    /**
     * Decodes a hexadecimal string to a byte array.
     * Supports both uppercase and lowercase hex strings.
     *
     * @param hexString The hexadecimal string to decode
     * @return Byte array representation
     * @throws IllegalArgumentException if hex string is invalid
     */
    fun decode(hexString: String): ByteArray {
        val clean = hexString.trim().replace(" ", "").replace("-", "")

        require(clean.isNotEmpty()) { "Hex string cannot be empty" }
        require(clean.length % 2 == 0) { "Hex string must have even length: ${clean.length}" }
        require(clean.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }) {
            "Hex string contains invalid characters: $hexString"
        }

        return ByteArray(clean.length / 2) { index ->
            val start = index * 2
            val byteValue = clean.substring(start, start + 2).toInt(16)
            byteValue.toByte()
        }
    }

    /**
     * Validates whether a string is a valid hexadecimal string.
     *
     * @param hexString The string to validate
     * @return true if valid hex string, false otherwise
     */
    fun isValid(hexString: String): Boolean {
        val clean = hexString.trim().replace(" ", "").replace("-", "")
        if (clean.isEmpty() || clean.length % 2 != 0) return false
        return clean.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }
    }

    /**
     * Encodes a single byte to a hex string.
     *
     * @param byte The byte to encode
     * @return Two-character hex string
     */
    fun encodeByte(byte: Byte): String {
        val value = byte.toInt() and 0xFF
        return "${HEX_CHARS_LOWER[value ushr 4]}${HEX_CHARS_LOWER[value and 0x0F]}"
    }

    /**
     * Decodes a two-character hex string to a byte.
     *
     * @param hex Two-character hex string
     * @return Decoded byte
     * @throws IllegalArgumentException if input is not a valid two-character hex
     */
    fun decodeByte(hex: String): Byte {
        require(hex.length == 2) { "Hex byte must be exactly 2 characters: $hex" }
        return hex.toInt(16).toByte()
    }

    /**
     * Converts a hex string to a readable format with spaces.
     * Useful for debugging and logging.
     *
     * @param hexString The hex string to format
     * @param groupSize Number of bytes per group (default: 2)
     * @return Formatted hex string
     */
    fun formatReadable(hexString: String, groupSize: Int = 2): String {
        val clean = hexString.trim().replace(" ", "").replace("-", "")
        if (clean.isEmpty()) return ""

        val bytes = clean.chunked(2)
        return bytes.chunked(groupSize)
            .joinToString("  ") { group -> group.joinToString(" ") }
    }

    /**
     * Converts a byte array to a readable hex format.
     *
     * @param bytes The byte array to format
     * @param groupSize Number of bytes per group (default: 4)
     * @return Formatted hex string
     */
    fun formatReadable(bytes: ByteArray, groupSize: Int = 4): String {
        val hex = encode(bytes)
        return hex.chunked(groupSize * 2)
            .joinToString(" ")
    }

    /**
     * Creates a random hex string of the specified byte length.
     *
     * @param byteLength Number of random bytes to generate
     * @return Random hex string
     */
    fun randomHexString(byteLength: Int): String {
        require(byteLength > 0) { "Byte length must be positive" }
        val randomBytes = ByteArray(byteLength)
        // Note: Platform-specific random implementation needed
        // For now, use Kotlin's Random
        randomBytes.indices.forEach { i ->
            randomBytes[i] = kotlin.random.Random.nextInt(0, 256).toByte()
        }
        return encode(randomBytes)
    }

    /**
     * Converts a hex string to a compact representation (removes all whitespace and separators).
     *
     * @param hexString The hex string to compact
     * @return Compact hex string
     */
    fun compact(hexString: String): String {
        return hexString.trim()
            .replace(" ", "")
            .replace("-", "")
            .replace(":", "")
            .lowercase()
    }
}