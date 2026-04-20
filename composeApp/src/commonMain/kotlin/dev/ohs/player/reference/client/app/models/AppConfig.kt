package dev.ohs.player.reference.client.app.models

import kotlinx.serialization.Serializable

@Serializable
data class AppConfig(
    val appId: String,
    val configType: String,
    val appTitle: String,
    val remoteSyncPageSize: Int,
    val languages: List<String>,
    val syncInterval: Int,
    val loginConfig: LoginConfig
)

@Serializable
data class LoginConfig(
    val showLogo: Boolean,
    val enablePin: Boolean,
    val pinLength: Int
)