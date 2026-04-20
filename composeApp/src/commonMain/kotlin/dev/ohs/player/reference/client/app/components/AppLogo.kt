package dev.ohs.player.reference.client.app.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import ohsplayerreferenceclientapp.composeapp.generated.resources.Res
import ohsplayerreferenceclientapp.composeapp.generated.resources.app_logo
import org.jetbrains.compose.resources.painterResource


@Composable
fun AppLogo(
    showLogo: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer
            )
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (showLogo) {
            Image(
                painter = painterResource(Res.drawable.app_logo),
                contentDescription = "App Logo",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Fit
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = "Security Icon",
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}