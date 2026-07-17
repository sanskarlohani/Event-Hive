package com.sanskar.eventhive.ui.screen.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sanskar.eventhive.R


//@Preview(showBackground = true)
@Composable
fun EmailSent(navController: NavController) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Email image (replace with your actual image)
        Image(
            painter = painterResource(id = R.drawable.esent), // replace with your actual image name
            contentDescription = "Email sent icon",
            modifier = Modifier
                .size(120.dp)
                .padding(bottom = 24.dp),
            contentScale = ContentScale.Fit
        )

        // Message Text
        Text(
            text = "We Sent you an Email to reset your password.",
            style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )

//        // Return to Login Button
//        Button(
//            onClick = {
//                navController.navigate(Screen.LOGIN.route) {
//                    popUpTo(Screen.LOGIN.route) { inclusive = true }
//                }
//            },
//            modifier = Modifier
//                .width(180.dp)
//                .height(48.dp),
//            shape = RoundedCornerShape(24.dp),
//            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA26AFF))
//        ) {
//            Text("Return to Login", color = Color.White, fontSize = 14.sp)
//        }

    }
}
