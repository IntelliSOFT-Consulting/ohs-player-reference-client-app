package dev.ohs.player.reference.client.app.configuration

import dev.ohs.player.reference.client.app.models.AppConfig
import dev.ohs.player.reference.client.app.models.LoginConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import ohsplayerreferenceclientapp.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
class ConfigurationManager {

    private val _config = MutableStateFlow<AppConfig?>(null)
    val config: StateFlow<AppConfig?> = _config.asStateFlow()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = false
    }

    suspend fun loadConfiguration() {
        try {
            val jsonString = Res.readBytes("files/config.json").decodeToString()
            val appConfig = json.decodeFromString<AppConfig>(jsonString)
            _config.value = appConfig
        } catch (e: Exception) {
            _config.value = getDefaultConfig()
        }
    }

    fun getDefaultConfig(): AppConfig {
        return AppConfig(
            appId = "ohs-reference-app",
            configType = "application",
            appTitle = "OHS Player",
            remoteSyncPageSize = 100,
            languages = listOf("en", "sw", "fr"),
            syncInterval = 30,
            loginConfig = LoginConfig(
                showLogo = true,
                enablePin = true,
                pinLength = 4
            )
        )
    }

    fun getAppTitle(): String = _config.value?.appTitle ?: "OHS Player"

    fun getLanguages(): List<String> = _config.value?.languages ?: listOf("en")

    fun getSyncInterval(): Int = _config.value?.syncInterval ?: 30

    fun shouldShowLogo(): Boolean = _config.value?.loginConfig?.showLogo ?: true

    fun isPinEnabled(): Boolean = _config.value?.loginConfig?.enablePin ?: true

    fun getRemoteSyncPageSize(): Int = _config.value?.remoteSyncPageSize ?: 100
}