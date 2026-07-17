package com.sanskar.eventhive.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy Policy") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Privacy Policy",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Last updated: 16 Aug 2025",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            SectionTitle("Who we are")
            Paragraph(
                "EventHive (\"we\", \"us\", \"our\") provides event discovery, ticketing, and community features. For privacy questions, contact support@eventhive.example."
            )

            SectionTitle("Information we collect")
            Bullet("Account info you provide (name, email, profile details).")
            Bullet("Authentication data from sign-in providers (e.g., Google).")
            Bullet("Content you submit: tickets, chat messages, bug reports (including screenshots), and related metadata.")
            Bullet("Device and diagnostics: app version, device model, OS version, crash and performance data.")
            Bullet("Usage data: interactions with screens and features (e.g., tickets, chats).")
            Bullet("Optional media access when you choose to upload images/screenshots.")

            SectionTitle("How we use information")
            Bullet("Provide core features (accounts, tickets, chat, notifications).")
            Bullet("Improve and secure the app, including debugging, analytics, and preventing abuse.")
            Bullet("Communicate important updates and service messages (e.g., reminders, tickets).")
            Bullet("Comply with legal obligations and enforce terms.")

            SectionTitle("Legal bases (EEA/UK)")
            Bullet("Contract: to provide requested services.")
            Bullet("Legitimate interests: to keep services safe and improve them.")
            Bullet("Consent: where required (e.g., certain notifications).")

            SectionTitle("Sharing and disclosure")
            Bullet("Service providers that help us run the app under contract (confidentiality and security obligations).")
            Bullet("Firebase services: Authentication, Firestore (database), Storage (media uploads), and Messaging (notifications).")
            Bullet("Google Sign-In to authenticate your account when you opt to use it.")
            Bullet("When required by law or to protect rights, safety, and security.")
            Bullet("With your direction or consent (for example, content you share).")

            SectionTitle("Data retention")
            Paragraph(
                "We retain personal data for as long as needed to operate features and for legitimate business or legal purposes. " +
                "Bug reports (and attachments) are kept for troubleshooting and audit; we remove or anonymize them when no longer necessary. " +
                "You can request deletion of your account as described below."
            )

            SectionTitle("Security")
            Paragraph(
                "We use technical and organizational safeguards to protect your data. No method of transmission or storage is 100% secure; residual risk may remain."
            )

            SectionTitle("Children’s privacy")
            Paragraph(
                "The app isn’t directed to children under the age where parental consent is required by law. If we learn we collected such data, we’ll take steps to delete it."
            )

            SectionTitle("Your rights")
            Bullet("Access and update your account information.")
            Bullet("Delete your account or request deletion of certain data, subject to legal limits.")
            Bullet("Object to or restrict certain processing and withdraw consent where processing is based on consent.")
            Bullet("Data portability where applicable.")
            Paragraph(
                "Depending on your region, you may have additional rights. Contact us to exercise these rights."
            )

            SectionTitle("Notifications")
            Paragraph(
                "You can control push notifications in your device’s system settings and within the app’s preferences."
            )

            SectionTitle("Cookies and similar technologies")
            Paragraph(
                "The mobile app doesn’t use web cookies. It may use device identifiers and an FCM token to deliver notifications and improve reliability. You can reset advertising identifiers in your device settings."
            )

            SectionTitle("International transfers")
            Paragraph(
                "Your information may be processed in countries other than your own. We take steps to ensure appropriate safeguards are in place."
            )

            SectionTitle("Managing your data")
            Paragraph(
                "You can manage or delete your account from Profile → Danger Zone. Deleting your account removes or anonymizes associated data unless we must retain it for legal reasons."
            )

            SectionTitle("Changes to this policy")
            Paragraph(
                "We may update this policy periodically. Material changes will be communicated within the app or by other appropriate means."
            )

            SectionTitle("Contact us")
            Paragraph(
                "Questions or requests? Contact: support@eventhive.example"
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
    )
}

@Composable
private fun Paragraph(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        lineHeight = 20.sp,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun Bullet(text: String) {
    Text(
        text = "• " + text,
        style = MaterialTheme.typography.bodyMedium,
        lineHeight = 20.sp,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}
