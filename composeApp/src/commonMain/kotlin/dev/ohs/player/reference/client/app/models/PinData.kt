package dev.ohs.player.reference.client.app.models

import kotlinx.serialization.Serializable


@Serializable
data class PinData(
    val hashedPin: String,
    val salt: String
)