package dev.ohs.player.reference.client.app.auth.screens


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.ohs.player.reference.client.app.security.PinManager
import dev.ohs.player.reference.client.app.security.platformEncryptedKSafe
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val SETUP_PIN_LENGTH = 4

enum class PinSetupStep {
    SET_PIN,
    CONFIRM_PIN,
    SUCCESS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetPinLockScreen(
    appName: String = "App Name",
    showLogo: Boolean,
    onSetupComplete: () -> Unit = {}
) {
    val pinManager = remember { PinManager(platformEncryptedKSafe) }
    val scope = rememberCoroutineScope()

    var currentStep by remember { mutableStateOf(PinSetupStep.SET_PIN) }
    var firstPin by remember { mutableStateOf("") }
    var enteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    fun resetEntry() {
        enteredPin = ""
        errorMessage = null
    }

    fun addDigit(digit: String) {
        if (enteredPin.length < SETUP_PIN_LENGTH) {
            enteredPin += digit
            errorMessage = null

            if (enteredPin.length == SETUP_PIN_LENGTH) {
                when (currentStep) {
                    PinSetupStep.SET_PIN -> {
                        firstPin = enteredPin
                        enteredPin = ""
                        currentStep = PinSetupStep.CONFIRM_PIN
                    }

                    PinSetupStep.CONFIRM_PIN -> {
                        if (enteredPin == firstPin) {
                            // PINs match — save
                            isLoading = true
                            scope.launch {
                                val success = pinManager.createPin(enteredPin)
                                isLoading = false
                                if (success) {
                                    currentStep = PinSetupStep.SUCCESS
                                    delay(2000)
                                    onSetupComplete()
                                } else {
                                    errorMessage = "A PIN already exists. Please log in instead."
                                    delay(1000)
                                    resetEntry()
                                }
                            }
                        } else {
                            errorMessage = "PINs do not match. Try again."
                            scope.launch {
                                delay(800)
                                resetEntry()
                            }
                        }
                    }

                    else -> {}
                }
            }
        }
    }

    fun deleteDigit() {
        if (enteredPin.isNotEmpty()) {
            enteredPin = enteredPin.dropLast(1)
            errorMessage = null
        }
    }

    Scaffold(
        containerColor = Color.White
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> CircularProgressIndicator()

                currentStep == PinSetupStep.SUCCESS -> SuccessView()

                else -> PinSetupContent(
                    step = currentStep,
                    enteredPin = enteredPin,
                    errorMessage = errorMessage,
                    appName = appName,
                    onDigitClick = { addDigit(it) },
                    onDelete = { deleteDigit() },
                    onBack = {
                        if (currentStep == PinSetupStep.CONFIRM_PIN) {
                            currentStep = PinSetupStep.SET_PIN
                            firstPin = ""
                            resetEntry()
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun SuccessView() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(32.dp)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color(0xFF4CAF50)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Success",
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "PIN Set Successfully!",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Your PIN has been saved securely.",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PinSetupContent(
    step: PinSetupStep,
    enteredPin: String,
    errorMessage: String?,
    appName: String,
    onDigitClick: (String) -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(Color(0xFFE3F2FD)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "🔐", fontSize = 32.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = appName,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Step indicator
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StepDot(active = step == PinSetupStep.SET_PIN, done = step == PinSetupStep.CONFIRM_PIN)
            Spacer(modifier = Modifier.width(8.dp))
            HorizontalDivider(
                modifier = Modifier.width(32.dp),
                color = if (step == PinSetupStep.CONFIRM_PIN) Color(0xFF2196F3) else Color.LightGray
            )
            Spacer(modifier = Modifier.width(8.dp))
            StepDot(active = step == PinSetupStep.CONFIRM_PIN, done = false)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = when (step) {
                PinSetupStep.SET_PIN -> "Create a 4-digit PIN"
                PinSetupStep.CONFIRM_PIN -> "Confirm your PIN"
                else -> ""
            },
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // PIN dots
        Row(horizontalArrangement = Arrangement.Center) {
            repeat(SETUP_PIN_LENGTH) { index ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(
                            if (index < enteredPin.length) Color(0xFF2196F3) else Color.LightGray
                        )
                )
            }
        }

        // Error
        Box(modifier = Modifier.height(28.dp), contentAlignment = Alignment.Center) {
            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    fontSize = 13.sp,
                    color = Color.Red,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Keypad
        KeypadRow(
            listOf(
                "1" to { onDigitClick("1") },
                "2" to { onDigitClick("2") },
                "3" to { onDigitClick("3") })
        )
        Spacer(modifier = Modifier.height(8.dp))
        KeypadRow(
            listOf(
                "4" to { onDigitClick("4") },
                "5" to { onDigitClick("5") },
                "6" to { onDigitClick("6") })
        )
        Spacer(modifier = Modifier.height(8.dp))
        KeypadRow(
            listOf(
                "7" to { onDigitClick("7") },
                "8" to { onDigitClick("8") },
                "9" to { onDigitClick("9") })
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.width(70.dp + 16.dp))
            KeypadButton("0") { onDigitClick("0") }
            Spacer(modifier = Modifier.width(16.dp))
            KeypadButton("←") { onDelete() }
        }

        // Back button on confirm step
        if (step == PinSetupStep.CONFIRM_PIN) {
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onBack) {
                Text("Re-enter PIN", color = Color(0xFF2196F3), fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun StepDot(active: Boolean, done: Boolean) {
    Box(
        modifier = Modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(
                when {
                    done -> Color(0xFF2196F3)
                    active -> Color(0xFF2196F3)
                    else -> Color.LightGray
                }
            )
    )
}