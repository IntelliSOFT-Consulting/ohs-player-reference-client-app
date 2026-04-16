package dev.ohs.player.reference.client.app.models


import kotlinx.serialization.Serializable
import kotlin.time.Clock

@Serializable
data class PinData(
    val hashedPin: String,
    val salt: String,
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    var attempts: Int = 0,
    var isLocked: Boolean = false,
    var lastAttemptAt: Long = 0,
    var lockedUntil: Long = 0
) {
    fun incrementAttempts(): PinData {
        return copy(
            attempts = attempts + 1,
            lastAttemptAt = Clock.System.now().toEpochMilliseconds(),
            isLocked = attempts + 1 >= MAX_ATTEMPTS
        )
    }

    fun resetAttempts(): PinData {
        return copy(
            attempts = 0,
            isLocked = false,
            lockedUntil = 0
        )
    }

    fun isCurrentlyLocked(): Boolean {
        return if (lockedUntil > Clock.System.now().toEpochMilliseconds()) {
            true
        } else if (isLocked && lockedUntil == 0L) {
            true
        } else {
            false
        }
    }

    companion object {
        const val MAX_ATTEMPTS = 5
        const val LOCKOUT_DURATION_MS = 30000 // 30 seconds
    }
}