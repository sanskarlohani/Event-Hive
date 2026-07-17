package com.sanskar.eventhive

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sanskar.eventhive.ui.theme.EventHiveTheme
import kotlinx.coroutines.delay

private const val SPLASH_DURATION_MS = 1200L

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EventHiveTheme {
                EventHiveSplash {
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                    finish()
                }
            }
        }
    }
}

@Composable
private fun EventHiveSplash(onDone: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "splashTransition")
    val driftA by transition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "driftA"
    )
    val driftB by transition.animateFloat(
        initialValue = 10f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "driftB"
    )
    val glow by transition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    LaunchedEffect(Unit) {
        delay(SPLASH_DURATION_MS)
        onDone()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF5B3CF5),
                        Color(0xFF9B59B6)
                    )
                )
            )
            .padding(24.dp)
    ) {
        FloatingCard(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 18.dp, y = (70 + driftA).dp)
                .rotate(-10f),
            title = "Design Meetup",
            subtitle = "Today • 6:00 PM",
            alpha = 0.28f
        )
        FloatingCard(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-8).dp, y = (130 + driftB).dp)
                .rotate(8f),
            title = "Hack Night",
            subtitle = "Tomorrow • 7:30 PM",
            alpha = 0.3f
        )
        FloatingCard(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = 4.dp, y = (110 - driftA).dp)
                .rotate(-4f),
            title = "Startup Club",
            subtitle = "Open Discussion",
            alpha = 0.25f
        )

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(Color.White.copy(alpha = 0.2f * glow)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "EH",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 30.sp
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Event Hive",
                color = Color.White,
                fontSize = 40.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Where clubs and events come alive",
                color = Color.White.copy(alpha = 0.86f),
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun FloatingCard(
    modifier: Modifier,
    title: String,
    subtitle: String,
    alpha: Float
) {
    Card(
        modifier = modifier
            .fillMaxWidth(0.56f)
            .alpha(alpha),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.26f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.87f),
                fontSize = 11.sp
            )
        }
    }
}
