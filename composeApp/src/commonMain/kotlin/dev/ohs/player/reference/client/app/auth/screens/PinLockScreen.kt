package dev.ohs.player.reference.client.app.auth.screens

// PinLockScreen.kt - Exact match to your screenshot design
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Dock
import androidx.compose.material.icons.filled.KeyboardBackspace
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.ohs.player.reference.client.app.security.PinManager
import dev.ohs.player.reference.client.app.security.platformEncryptedKSafe
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val PIN_LENGTH = 4

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinLockScreen(
    appName: String = "App Name",
    deviceName: String = "Enter pin for W4VV-01",
    showLogo: Boolean,
    onSuccess: (pin: String) -> Unit = {},
    onAdminLogin: () -> Unit = {},
    onSettings: () -> Unit = {},
    onForgotPin: () -> Unit = {}
) {
    var enteredPin by remember { mutableStateOf("") }
    var validationState by remember { mutableStateOf(ValidationState.EMPTY) }
    var showForgotDialog by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Assuming you have a way to get your PinManager instance (e.g., from a DI framework)
    val pinManager = remember { PinManager(platformEncryptedKSafe) }
    var isFirstTimeSetup by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        // Check if a PIN already exists when the screen loads
        val existingPin = platformEncryptedKSafe.get("user_pin_data", "")
        isFirstTimeSetup = existingPin.isEmpty()
        isLoading = false
    }
    fun resetPin() {
        enteredPin = ""
        validationState = ValidationState.EMPTY
    }

    fun validatePin() {
        scope.launch {
            if (isFirstTimeSetup) {
                val success = pinManager.createPin(enteredPin)
                validationState = if (success) ValidationState.VALID else ValidationState.INVALID
            } else {
                val isValid = pinManager.validatePin(enteredPin)
                validationState = if (isValid) ValidationState.VALID else ValidationState.INVALID
            }

            when (validationState) {
                ValidationState.VALID -> {
                    delay(300)
                    onSuccess(enteredPin)
                    resetPin()
                }

                ValidationState.INVALID -> {
                    delay(800)
                    resetPin()
                }

                else -> {}
            }
        }
    }

    fun addDigit(digit: String) {
        if (enteredPin.length < PIN_LENGTH && validationState != ValidationState.VALID) {
            enteredPin += digit
            if (enteredPin.length == PIN_LENGTH) {
                validatePin()
            } else {
                validationState = ValidationState.ENTERING
            }
        }
    }

    fun deleteDigit() {
        if (enteredPin.isNotEmpty() && validationState != ValidationState.VALID) {
            enteredPin = enteredPin.dropLast(1)
            validationState =
                if (enteredPin.isEmpty()) ValidationState.EMPTY else ValidationState.ENTERING
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = {

                },
                actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            modifier = Modifier.size(24.dp),
                            tint = Color.Gray
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .shadow(4.dp)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Admin Login", fontWeight = FontWeight.Medium) },
                            onClick = {
                                menuExpanded = false
                                onAdminLogin()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.AdminPanelSettings, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Settings", fontWeight = FontWeight.Medium) },
                            onClick = {
                                menuExpanded = false
                                onSettings()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Settings, contentDescription = null)
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    scrolledContainerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
            {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Logo / Icon at top
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE3F2FD)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🔒",
                            fontSize = 32.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // App Name
                    Text(
                        text = appName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Device Name / Instruction
                    Text(
                        text = deviceName,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // PIN Indicators
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(6.dp)
                    ) {
                        repeat(PIN_LENGTH) { index ->
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            validationState == ValidationState.VALID -> Color.Green
                                            validationState == ValidationState.INVALID -> Color.Red
                                            index < enteredPin.length -> Color(0xFF2196F3)
                                            else -> Color.LightGray
                                        }
                                    )
                            )
                        }
                    }

                    // Error Message
                    if (validationState == ValidationState.INVALID) {
                        Text(
                            text = "Invalid PIN",
                            fontSize = 14.sp,
                            color = Color.Red,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Forgot PIN Button
                    TextButton(
                        onClick = { showForgotDialog = true },
                        modifier = Modifier.padding(1.dp)
                    ) {
                        Text(
                            text = "Forgot PIN?",
                            fontSize = 14.sp,
                            color = Color(0xFF2196F3),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    KeypadRow(
                        listOf(
                            "1" to { addDigit("1") },
                            "2" to { addDigit("2") },
                            "3" to { addDigit("3") })
                    )
                    KeypadRow(
                        listOf(
                            "4" to { addDigit("4") },
                            "5" to { addDigit("5") },
                            "6" to { addDigit("6") })
                    )
                    KeypadRow(
                        listOf(
                            "7" to { addDigit("7") },
                            "8" to { addDigit("8") },
                            "9" to { addDigit("9") })
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Spacer(modifier = Modifier.width(70.dp + 16.dp)) // Width of one button + spacer
                        KeypadButton("0") { addDigit("0") }
                        Spacer(modifier = Modifier.width(16.dp))
                        KeypadButton("←") { deleteDigit() }
                    }



                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    if (showForgotDialog) {
        AlertDialog(
            onDismissRequest = { showForgotDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    text = "Forgot PIN",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            },
            text = {
                Column {
                    Text(
                        text = "Please call your supervisor at:",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "xxx-xxxx-xxx",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showForgotDialog = false
                        onForgotPin()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("DIAL NUMBER", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showForgotDialog = false }
                ) {
                    Text("CANCEL", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun KeypadRow(
    buttons: List<Pair<String, () -> Unit>>
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        buttons.forEachIndexed { index, (label, onClick) ->
            if (index > 0) Spacer(modifier = Modifier.width(16.dp))
            KeypadButton(label, onClick)
        }
    }
}

@Composable
fun KeypadButton(
    label: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .size(70.dp)
            .clip(CircleShape)
            .background(
                color = Color.White,
                shape = CircleShape
            )
            .shadow(
                elevation = 2.dp,
                shape = CircleShape,
                ambientColor = Color.Black.copy(alpha = 0.1f),
                spotColor = Color.Black.copy(alpha = 0.1f)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = if (label == "←") 24.sp else 28.sp,
            fontWeight = if (label == "←") FontWeight.Normal else FontWeight.Medium,
            color = Color.Black
        )
    }
}

enum class ValidationState {
    EMPTY, ENTERING, VALID, INVALID
}