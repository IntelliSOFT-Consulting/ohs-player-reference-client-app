package dev.ohs.player.reference.client.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.ohs.player.reference.client.app.auth.screens.PinLockScreen
import dev.ohs.player.reference.client.app.auth.screens.SetPinLockScreen
import dev.ohs.player.reference.client.app.security.platformEncryptedKSafe


@Composable
@Preview
fun App() {
    val scope = rememberCoroutineScope()
    var hasPin by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(Unit) {
        val existingPin = platformEncryptedKSafe.get("user_pin_data", "")
        hasPin = existingPin.isNotEmpty()
    }

    when (hasPin) {
        null -> {

            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        false -> {

            SetPinLockScreen(
                appName = "App Name",
                onSetupComplete = {
                    hasPin = true
                }
            )
        }

        true -> {

            PinLockScreen(
                appName = "App Name",
                deviceName = "Enter pin for Test-001",
                onSuccess = { pin ->

                },
                onAdminLogin = {

                },
                onSettings = {

                },
                onForgotPin = {

                }
            )
        }
    }
}