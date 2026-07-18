package com.sanskar.eventhive.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sanskar.eventhive.data.model.Category


@Composable
fun HomeScreenCategoryCard(
    category: Category,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    ClayCard(
        modifier = modifier
            .size(width = 200.dp, height = 160.dp),
        onClick = onClick,
        cornerRadius = 24.dp,
        backgroundColor = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(PrimaryBrushBlue, MaterialTheme.colorScheme.secondary)
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // Club Icon and Name in same row
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val icon = imageVectorForName(category.iconName)
                    Icon(
                        imageVector = icon,
                        contentDescription = category.name,
                        tint = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier
                            .size(24.dp)
                            .padding(end = 8.dp)
                    )

                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Description
                category.description?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.85f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Club count at bottom
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Groups,
                        contentDescription = "Clubs",
                        tint = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${category.clubs.size} Clubs",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }
            }
        }
    }
}

