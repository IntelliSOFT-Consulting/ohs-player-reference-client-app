package dev.ohs.player.reference.client.app.utils

import kotlin.time.Clock


/**
 * Utility class for time-related operations in KMP applications.
 */
object TimeUtils {

    /**
     * Formats a duration in milliseconds to a human-readable string.
     *
     * @param millis Duration in milliseconds
     * @return Human-readable time string (e.g., "2 minutes", "1 hour", "30 seconds")
     */
    fun formatLockoutTime(millis: Long): String {
        return when {
            millis <= 0 -> "0 seconds"
            millis < 1000 -> "less than 1 second"
            millis < 60_000 -> {
                val seconds = (millis + 999) / 1000 // Round up to nearest second
                "${seconds} second${if (seconds != 1L) "s" else ""}"
            }
            millis < 3_600_000 -> {
                val minutes = (millis + 59_999) / 60_000 // Round up to nearest minute
                "${minutes} minute${if (minutes != 1L) "s" else ""}"
            }
            millis < 86_400_000 -> {
                val hours = (millis + 3_599_999) / 3_600_000 // Round up to nearest hour
                "${hours} hour${if (hours != 1L) "s" else ""}"
            }
            else -> {
                val days = (millis + 86_399_999) / 86_400_000 // Round up to nearest day
                "${days} day${if (days != 1L) "s" else ""}"
            }
        }
    }

    /**
     * Formats a duration in milliseconds to a detailed human-readable string.
     * Includes multiple units (e.g., "2 hours and 30 minutes").
     *
     * @param millis Duration in milliseconds
     * @return Detailed human-readable time string
     */
    fun formatDetailedTime(millis: Long): String {
        if (millis <= 0) return "0 seconds"

        val seconds = millis / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        val remainingHours = hours % 24
        val remainingMinutes = minutes % 60
        val remainingSeconds = seconds % 60

        return buildString {
            if (days > 0) {
                append("$days day${if (days != 1L) "s" else ""}")
                if (remainingHours > 0 || remainingMinutes > 0 || remainingSeconds > 0) {
                    append(" and ")
                }
            }
            if (remainingHours > 0) {
                append("$remainingHours hour${if (remainingHours != 1L) "s" else ""}")
                if (remainingMinutes > 0 || remainingSeconds > 0) {
                    append(" and ")
                }
            }
            if (remainingMinutes > 0) {
                append("$remainingMinutes minute${if (remainingMinutes != 1L) "s" else ""}")
                if (remainingSeconds > 0) {
                    append(" and ")
                }
            }
            if (remainingSeconds > 0) {
                append("$remainingSeconds second${if (remainingSeconds != 1L) "s" else ""}")
            }
        }
    }

    /**
     * Formats a duration in milliseconds to a compact time string.
     * Useful for UI elements with limited space.
     *
     * @param millis Duration in milliseconds
     * @return Compact time string (e.g., "2h 30m", "45s", "1d")
     */
    fun formatCompactTime(millis: Long): String {
        return when {
            millis <= 0 -> "0s"
            millis < 60_000 -> {
                val seconds = (millis + 999) / 1000
                "${seconds}s"
            }
            millis < 3_600_000 -> {
                val minutes = (millis + 59_999) / 60_000
                "${minutes}m"
            }
            millis < 86_400_000 -> {
                val hours = (millis + 3_599_999) / 3_600_000
                "${hours}h"
            }
            else -> {
                val days = (millis + 86_399_999) / 86_400_000
                "${days}d"
            }
        }
    }

    /**
     * Formats a duration in milliseconds to a natural language string.
     * Most user-friendly format.
     *
     * @param millis Duration in milliseconds
     * @return Natural language time string (e.g., "a few seconds", "about 2 minutes")
     */
    fun formatNaturalTime(millis: Long): String {
        if (millis <= 0) return "just a moment"

        val seconds = millis / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            days >= 7 -> "${days / 7} week${if (days / 7 != 1L) "s" else ""}"
            days >= 1 -> {
                when (days) {
                    1L -> "1 day"
                    else -> "$days days"
                }
            }
            hours >= 1 -> {
                when (hours) {
                    1L -> "about 1 hour"
                    else -> "$hours hours"
                }
            }
            minutes >= 1 -> {
                when (minutes) {
                    1L -> "about 1 minute"
                    in 2..4 -> "$minutes minutes"
                    else -> "${minutes} minutes"
                }
            }
            seconds >= 10 -> "$seconds seconds"
            seconds >= 5 -> "a few seconds"
            else -> "just a moment"
        }
    }

    /**
     * Gets the remaining time until a target timestamp.
     *
     * @param targetMillis Target timestamp in milliseconds
     * @return Remaining time in milliseconds (0 if already passed)
     */
    fun getRemainingTime(targetMillis: Long): Long {
        val now = Clock.System.now().toEpochMilliseconds()
        return (targetMillis - now).coerceAtLeast(0)
    }

    /**
     * Checks if a target timestamp has been reached.
     *
     * @param targetMillis Target timestamp in milliseconds
     * @return true if current time is past the target
     */
    fun isTimeReached(targetMillis: Long): Boolean {
        val now = Clock.System.now().toEpochMilliseconds()
        return now >= targetMillis
    }

    /**
     * Formats a timestamp to a readable date/time string.
     *
     * @param timestamp Timestamp in milliseconds
     * @return Formatted date/time string
     */
    fun formatTimestamp(timestamp: Long): String {
        val now = Clock.System.now().toEpochMilliseconds()
        val diff = now - timestamp

        return when {
            diff < 60_000 -> "just now"
            diff < 3_600_000 -> "${diff / 60_000} minute${if (diff / 60_000 != 1L) "s" else ""} ago"
            diff < 86_400_000 -> "${diff / 3_600_000} hour${if (diff / 3_600_000 != 1L) "s" else ""} ago"
            diff < 7 * 86_400_000 -> "${diff / 86_400_000} day${if (diff / 86_400_000 != 1L) "s" else ""} ago"
            else -> {
                // Fallback to simple format
                val days = diff / 86_400_000
                "${days} day${if (days != 1L) "s" else ""} ago"
            }
        }
    }
}

/**
 * Extension function to format Long milliseconds to human-readable time.
 */
fun Long.toHumanReadableTime(): String = TimeUtils.formatLockoutTime(this)

/**
 * Extension function to format Long milliseconds to detailed time.
 */
fun Long.toDetailedTime(): String = TimeUtils.formatDetailedTime(this)

/**
 * Extension function to format Long milliseconds to compact time.
 */
fun Long.toCompactTime(): String = TimeUtils.formatCompactTime(this)

/**
 * Extension function to format Long milliseconds to natural time.
 */
fun Long.toNaturalTime(): String = TimeUtils.formatNaturalTime(this)