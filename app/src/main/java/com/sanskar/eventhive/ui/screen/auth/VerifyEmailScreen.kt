package com.sanskar.eventhive.ui.screen.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.sanskar.eventhive.data.Resource
import com.sanskar.eventhive.ui.navigation.NavigationItem
import com.sanskar.eventhive.ui.viewModel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun VerifyEmailScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val verifyState by authViewModel.verifyState.collectAsState()
    val resendState by authViewModel.sendEmailLinkState.collectAsState()
    val snackbarHost = remember { SnackbarHostState() }

    val otp = remember { mutableStateListOf("", "", "", "", "", "") }
    var timeLeft by remember { mutableStateOf(45) }
    var canResend by remember { mutableStateOf(false) }
    val email = authViewModel.getCurrentUser()?.email.orEmpty()

    LaunchedEffect(Unit) {
        canResend = false
        timeLeft = 45
        while (timeLeft > 0) {
            delay(1000L)
            timeLeft--
        }
        canResend = true
    }

    LaunchedEffect(verifyState) {
        when (verifyState) {
            is Resource.Loading -> snackbarHost.showSnackbar("Verifying...")
            is Resource.Error -> snackbarHost.showSnackbar(
                (verifyState as Resource.Error).exception.localizedMessage ?: "Verification failed"
            )
            is Resource.Success -> {
                navController.navigate(NavigationItem.Home.route) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
            else -> Unit
        }
    }
    LaunchedEffect(resendState) {
        if (resendState is Resource.Success) {
            snackbarHost.showSnackbar("Verification link resent")
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.36f)
                        )
                    )
                )
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Default.MarkEmailRead,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(54.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Check your inbox",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "We sent a verification link to ${maskEmail(email)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        repeat(6) { index ->
                            OutlinedTextField(
                                value = otp[index],
                                onValueChange = {
                                    otp[index] = it.take(1)
                                },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                textStyle = MaterialTheme.typography.titleLarge.copy(textAlign = TextAlign.Center),
                                shape = RoundedCornerShape(10.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    if (!canResend) {
                        Text(
                            text = "Resend in 0:${timeLeft.toString().padStart(2, '0')}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        TextButton(
                            onClick = {
                                authViewModel.resendVerification()
                                canResend = false
                                timeLeft = 45
                            }
                        ) {
                            Text("Resend verification link")
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(999.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary,
                                        Color(0xFF7B5EEA)
                                    )
                                )
                            )
                            .padding(vertical = 14.dp)
                            .align(Alignment.CenterHorizontally),
                        contentAlignment = Alignment.Center
                    ) {
                        TextButton(onClick = { authViewModel.checkEmailVerified() }) {
                            Text(
                                "Verify",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun maskEmail(email: String): String {
    if (!email.contains("@")) return email
    val parts = email.split("@")
    val name = parts[0]
    val domain = parts[1]
    val maskedName = if (name.length <= 2) "${name.first()}*" else "${name.take(2)}***"
    return "$maskedName@$domain"
}
