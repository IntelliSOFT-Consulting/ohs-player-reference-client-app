package dev.ohs.player.reference.client.app

import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import dev.ohs.player.reference.client.app.auth.screens.PinLockScreen


@Composable
@Preview
fun App() {
    PinLockScreen(
        appName = "App Name",
        deviceName = "Enter pin for Test-001",
        onSuccess = { pin ->
            // Handle successful PIN entry
        },
        onAdminLogin = {
            // Navigate to admin login
        },
        onSettings = {
            // Open settings
        },
        onForgotPin = {
            // Handle dial number action
        },
        correctPin = "1234"
    )
}
