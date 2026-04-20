package dev.ohs.player.reference.client.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.ohs.player.reference.client.app.auth.screens.PinLockScreen
import dev.ohs.player.reference.client.app.auth.screens.SetPinLockScreen
import dev.ohs.player.reference.client.app.configuration.ConfigurationManager
import dev.ohs.player.reference.client.app.main.MainScreen
import dev.ohs.player.reference.client.app.models.AppConfig
import dev.ohs.player.reference.client.app.security.platformEncryptedKSafe
import kotlinx.coroutines.launch


@Composable
@Preview
fun App() {

    val configManager = remember { ConfigurationManager() }
    val config by configManager.config.collectAsState()

    LaunchedEffect(Unit) {
        configManager.loadConfiguration()
    }

    when {
        config == null -> {
            // Loading screen while config loads
            LoadingScreen()
        }

        else -> {
            // Main app with loaded configuration
            MainAppContent(config = config!!)
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent(config: AppConfig) {
    val scope = rememberCoroutineScope()
    var hasPin by remember { mutableStateOf<Boolean?>(null) }
    var isLoggedIn by remember { mutableStateOf(false) }
    var currentLanguage by remember { mutableStateOf(config.languages.first()) }

    // Determine if PIN login is enabled from config
    val isPinEnabled = config.loginConfig.enablePin
    val shouldShowLogo = config.loginConfig.showLogo

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    )
    {
        LaunchedEffect(Unit) {
            if (isPinEnabled) {
                val existingPin = platformEncryptedKSafe.get("user_pin_data", "")
                hasPin = existingPin.isNotEmpty()
            } else {
                hasPin = false
                isLoggedIn = true
            }

            val loginFlag = platformEncryptedKSafe.get("is_logged_in", "false")
            if (isPinEnabled) {
                isLoggedIn = loginFlag == "true"
            }
        }

        when {
            !isPinEnabled -> {
                // PIN disabled, go straight to main screen
                MainScreen(onLogout = {
                    scope.launch {
                        platformEncryptedKSafe.put("is_logged_in", "false")
                        isLoggedIn = false
                    }
                })
            }

            isLoggedIn -> {
                MainScreen(
                    onLogout = {
                        scope.launch {
                            platformEncryptedKSafe.put("is_logged_in", "false")
                            isLoggedIn = false
                        }
                    }
                )
            }

            hasPin == null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            hasPin == false -> {
                SetPinLockScreen(
                    appName = config.appTitle,
                    showLogo = shouldShowLogo,
                    pinLength=config.loginConfig.pinLength,
                    onSetupComplete = {
                        hasPin = true
                    }
                )
            }

            hasPin == true -> {
                PinLockScreen(
                    appName = config.appTitle,
                    deviceName = "Enter pin for ${config.appId}",
                    showLogo = shouldShowLogo,
                    pinLength=config.loginConfig.pinLength,
                    onSuccess = { pin ->
                        scope.launch {
                            platformEncryptedKSafe.put("is_logged_in", "true")
                            isLoggedIn = true
                        }
                    },
                    onAdminLogin = {
                        // Handle admin login
                    },
                    onSettings = {
                        // Handle settings
                    },
                    onForgotPin = {
                        // Handle forgot pin
                    }
                )
            }
        }
    }

}

@Composable
fun LanguageSelector(
    languages: List<String>,
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit
) {
    // Use your configured languages
    var expanded by remember { mutableStateOf(false) }

    IconButton(onClick = { expanded = true }) {
        Text(currentLanguage.uppercase())
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            languages.forEach { language ->
                DropdownMenuItem(
                    text = { Text(language.uppercase()) },
                    onClick = {
                        onLanguageSelected(language)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Loading configuration...")
        }
    }
}