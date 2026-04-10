package dev.ohs.player.reference.client.app.auth.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


enum class ValidationState {
    EMPTY,
    ENTERING,
    VALID,
    INVALID
}

@Composable
fun PinIndicators(
    pinLength: Int,
    enteredLength: Int,
    validationState: ValidationState
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(16.dp)
    ) {
        repeat(pinLength) { index ->
            val indicatorColor = when {
                validationState == ValidationState.VALID -> Color.Green
                validationState == ValidationState.INVALID -> Color.Red
                index < enteredLength -> Color.Blue
                else -> Color.Gray.copy(alpha = 0.3f)
            }

            Box(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .size(if (pinLength > 4) 20.dp else 18.dp)
                    .clip(CircleShape)
                    .background(indicatorColor)
            )
        }
    }
}

@Composable
fun ValidationMessage(validationState: ValidationState) {
    val message = when (validationState) {
        ValidationState.INVALID -> "Incorrect PIN. Please try again."
        ValidationState.VALID -> "Access Granted!"
        else -> ""
    }
    if (message.isNotEmpty()) {
        Text(
            text = message,
            color = when (validationState) {
                ValidationState.INVALID -> Color.Red
                ValidationState.VALID -> Color.Green
                else -> Color.Transparent
            },
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Composable
fun Keypad(
    onDigitClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    val digits = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("0", "DEL")
    )

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        for (row in digits) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (digit in row) {
                    KeypadButton(
                        label = digit,
                        onClick = {
                            if (digit == "DEL") onDeleteClick()
                            else onDigitClick(digit)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun KeypadButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = CircleShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = when {
                label == "⌫" -> 28.sp
                label == "," || label == "." -> 28.sp
                else -> 32.sp
            },
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = if (label == "⌫") FontWeight.Normal else FontWeight.Medium
        )
    }
}