package com.sanskar.eventhive.ui.screen.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import com.sanskar.eventhive.ui.components.ClayButton
import com.sanskar.eventhive.ui.components.ClayCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun IntroScreen(
    onGetStarted: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFEEF6FF),
                        Color(0xFFF7FBFF),
                        Color(0xFFEAF3FF),
                    ),
                ),
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        ClayCard(
            cornerRadius = 40.dp,
            elevation = 16.dp,
            backgroundColor = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Text(
                    text = "EventHive",
                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Manage college events, clubs, roles, and chats in one app.",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                
                Spacer(Modifier.height(10.dp))
                
                ClayButton(
                    text = "Get Started",
                    onClick = onGetStarted,
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                )
            }
        }
    }
}
