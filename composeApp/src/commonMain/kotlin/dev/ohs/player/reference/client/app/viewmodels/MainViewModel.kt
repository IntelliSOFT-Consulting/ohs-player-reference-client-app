package dev.ohs.player.reference.client.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.ohs.player.reference.client.app.configuration.ConfigurationManager
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val configManager = ConfigurationManager()

    val appConfig = configManager.config

    init {
        viewModelScope.launch {
            configManager.loadConfiguration()
        }
    }
    fun getWelcomeMessage(): String {
        val title = configManager.getAppTitle()
        val languages = configManager.getLanguages().joinToString(", ")
        return "Welcome to $title\nAvailable languages: $languages"
    }

    fun shouldShowLoginLogo(): Boolean = configManager.shouldShowLogo()

    fun getSyncIntervalInSeconds(): Int = configManager.getSyncInterval() * 1000

    fun isPinLoginAvailable(): Boolean = configManager.isPinEnabled()
}