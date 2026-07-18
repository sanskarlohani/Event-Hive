package com.sanskar.eventhive.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.TheaterComedy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sanskar.eventhive.data.model.Category

fun imageVectorForName(name: String?): ImageVector =
    when (name) {
        "Person"         -> Icons.Default.Person
        "FlashOn"        -> Icons.Default.FlashOn
        "Lightbulb"      -> Icons.Default.Lightbulb
        "Laptop"         -> Icons.Default.Laptop
        "Handshake"      -> Icons.Default.Handshake
        "Language"       -> Icons.Default.Language
        "Palette"        -> Icons.Default.Palette
        "MusicNote"      -> Icons.Default.MusicNote
        "MenuBook"       -> Icons.AutoMirrored.Filled.MenuBook
        "EmojiEvents"    -> Icons.Default.EmojiEvents
        "TheaterComedy"  -> Icons.Default.TheaterComedy
        "Science"        -> Icons.Default.Science
        else             -> Icons.Default.Category
    }




@Composable
fun CategoryCard(
    category: Category,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    ClayCard(
        modifier = modifier
            .padding(12.dp)
            .fillMaxWidth(),
        onClick = onClick,
        cornerRadius = 24.dp,
        backgroundColor = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .padding(4.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon or placeholder
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.secondaryContainer
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                val iconName = category.iconName?.trim()
                val vector = imageVectorForName(iconName.orEmpty())
                if (!iconName.isNullOrBlank()) {
                    Icon(
                        imageVector   = vector,
                        contentDescription = category.name,
                        tint          = MaterialTheme.colorScheme.primary,
                        modifier      = Modifier.size(32.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = category.name,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(Modifier.width(20.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(4.dp))

                if (!category.description.isNullOrBlank()) {
                    Text(
                        text = category.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(8.dp))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Groups,
                        contentDescription = "Club Count",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "${category.clubs.size} clubs",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}
