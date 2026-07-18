package com.sanskar.eventhive.ui.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.sanskar.eventhive.data.Resource
import com.sanskar.eventhive.data.model.Category
import com.sanskar.eventhive.data.model.Club
import com.sanskar.eventhive.data.model.Event
import com.sanskar.eventhive.ui.components.BottomBarScaffold
import com.sanskar.eventhive.ui.components.ClayCard
import com.sanskar.eventhive.ui.navigation.NavigationItem
import com.sanskar.eventhive.ui.theme.AppDimens
import com.sanskar.eventhive.ui.viewModel.ClubCategoryViewModel
import com.sanskar.eventhive.ui.viewModel.ClubViewModel
import com.sanskar.eventhive.ui.viewModel.EventViewModel
import com.sanskar.eventhive.ui.viewModel.UserViewModel
import com.google.firebase.Timestamp
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
private fun Timestamp.toLocalDateTime(): LocalDateTime =
    LocalDateTime.ofInstant(toDate().toInstant(), ZoneId.systemDefault())

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    navController: NavController,
    categoryVm: ClubCategoryViewModel = hiltViewModel(),
    clubVm: ClubViewModel = hiltViewModel(),
    eventVm: EventViewModel = hiltViewModel(),
    userVm: UserViewModel = hiltViewModel(),
) {
    val categories by categoryVm.categories.collectAsStateWithLifecycle()
    val clubs by clubVm.allClubs.collectAsStateWithLifecycle()
    val events by eventVm.allEvents.collectAsStateWithLifecycle()
    val userResource by userVm.observeUser.collectAsStateWithLifecycle(initialValue = Resource.Loading)

    val userName = remember(userResource) {
        (userResource as? Resource.Success)?.data?.name?.takeIf { it.isNotBlank() } ?: "Member"
    }

    var selectedCategory by rememberSaveable { mutableStateOf<String?>(null) }
    val now = remember { LocalDateTime.now() }
    val clubsById = remember(clubs) { clubs.associateBy { it.clubId } }
    val filteredClubs by remember(clubs, selectedCategory) {
        derivedStateOf {
            if (selectedCategory.isNullOrBlank()) clubs else clubs.filter { it.categoryId == selectedCategory }
        }
    }
    val upcomingEvents by remember(events, selectedCategory, now) {
        derivedStateOf {
            events
                .asSequence()
                .filter { event ->
                    val startsAfterNow = event.startTime?.toLocalDateTime()?.isAfter(now) == true
                    val categoryMatches = selectedCategory.isNullOrBlank() || (event.categoryId == selectedCategory)
                    startsAfterNow && categoryMatches
                }
                .sortedBy { it.startTime?.toLocalDateTime() }
                .toList()
        }
    }
    val featuredEvents = remember(upcomingEvents) { upcomingEvents.take(6) }
    val listState = rememberLazyListState()
    val activeFeaturedIndex by remember {
        derivedStateOf { listState.firstVisibleItemIndex.coerceAtMost((featuredEvents.size - 1).coerceAtLeast(0)) }
    }

    BottomBarScaffold(navController = navController) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding),
            contentPadding = PaddingValues(
                start = AppDimens.ScreenHorizontalPadding,
                end = AppDimens.ScreenHorizontalPadding,
                top = 18.dp,
                bottom = 28.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(AppDimens.SectionGap),
        ) {
            item {
                HomeHeader(
                    userName = userName,
                ) {
                    navController.navigate(NavigationItem.Notifications.route)
                }
            }
            item {
                SearchField()
            }
            item {
                SectionHeading("Featured Events")
                FeaturedEventCarousel(
                    events = featuredEvents,
                    clubsById = clubsById,
                    state = listState
                ) { event ->
                    navController.navigate(
                        NavigationItem.EventDetail.createRoute(event.categoryId, event.clubId, event.eventId)
                    )
                }
                if (featuredEvents.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(featuredEvents.size) { index ->
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 3.dp)
                                    .size(width = if (index == activeFeaturedIndex) 18.dp else 6.dp, height = 6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (index == activeFeaturedIndex) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                                    )
                            )
                        }
                    }
                }
            }
            item {
                SectionHeading(
                    title = "Upcoming For You",
                    action = "See all",
                    onActionClick = { navController.navigate(NavigationItem.AllClubs.route) }
                )
                if (upcomingEvents.isEmpty()) {
                    EmptySectionCard("No upcoming events in this category yet.")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        upcomingEvents.take(6).forEach { event ->
                            UpcomingEventCard(
                                event = event,
                                clubName = clubsById[event.clubId]?.name.orEmpty()
                            ) {
                                navController.navigate(
                                    NavigationItem.EventDetail.createRoute(
                                        event.categoryId,
                                        event.clubId,
                                        event.eventId
                                    )
                                )
                            }
                        }
                    }
                }
            }
            item {
                SectionHeading("Browse by Category")
                if (categories.isEmpty()) {
                    EmptySectionCard("Categories will appear here when admins publish them.")
                } else {
                    CategoryChipRow(
                        categories = categories,
                        selectedCategory = selectedCategory,
                        onSelect = { selectedCategory = it }
                    )
                }
            }
            item {
                SectionHeading(
                    title = "Recommended Clubs",
                    action = "Explore",
                    onActionClick = { navController.navigate(NavigationItem.AllClubs.route) }
                )
                if (filteredClubs.isEmpty()) {
                    EmptySectionCard("No clubs found for this category.")
                } else {
                    ClubGrid(
                        clubs = filteredClubs.take(6),
                        onClubClick = { club ->
                            navController.navigate(
                                NavigationItem.ClubDetail.createRoute(club.categoryId, club.clubId)
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(
    userName: String,
    onNotificationClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Good morning, $userName \uD83D\uDC4B",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Find new events and clubs around you",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Box {
            IconButton(
                onClick = onNotificationClick,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444))
            )
        }
    }
}

@Composable
private fun SearchField() {
    OutlinedTextField(
        value = "",
        onValueChange = {},
        enabled = false,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(50.dp),
        textStyle = MaterialTheme.typography.bodyMedium,
        placeholder = { Text("Search events, clubs...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            disabledContainerColor = MaterialTheme.colorScheme.surface,
            disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f),
            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledTextColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
private fun SectionHeading(
    title: String,
    action: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium
        )
        if ((action != null) && (onActionClick != null)) {
            Text(
                text = "$action \u2192",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.clickable(onClick = onActionClick)
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun FeaturedEventCarousel(
    events: List<Event>,
    clubsById: Map<String, Club>,
    state: LazyListState,
    onClick: (Event) -> Unit
) {
    if (events.isEmpty()) {
        EmptySectionCard("No featured events available right now.")
        return
    }

    LazyRow(
        state = state,
        contentPadding = PaddingValues(end = 24.dp, bottom = 12.dp, top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(events, key = { it.eventId }) { event ->
            ClayCard(
                modifier = Modifier
                    .widthIn(min = 260.dp, max = 260.dp)
                    .height(320.dp),
                onClick = { onClick(event) },
                cornerRadius = 28.dp,
                elevation = 10.dp,
                backgroundColor = Color.Transparent
            ) {
                Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(28.dp))) {
                    if (event.posterUrl.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.95f),
                                            Color(0xFF9B59B6),
                                            Color(0xFF1F1B2F)
                                        )
                                    )
                                )
                        )
                    } else {
                        AsyncImage(
                            model = event.posterUrl,
                            contentDescription = event.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.08f),
                                        Color.Black.copy(alpha = 0.68f)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val dateText = event.startTime
                            ?.toLocalDateTime()
                            ?.format(DateTimeFormatter.ofPattern("dd MMM"))
                            ?: "Date TBD"
                        Text(
                            text = dateText,
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(Color.White.copy(alpha = 0.2f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                        Text(
                            text = event.title.ifBlank { "Untitled Event" },
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = clubsById[event.clubId]?.name ?: "Independent Club",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.84f)
                        )
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun UpcomingEventCard(
    event: Event,
    clubName: String,
    onClick: () -> Unit
) {
    val start = event.startTime?.toLocalDateTime()
    val dateText = start?.format(DateTimeFormatter.ofPattern("EEE, dd MMM")) ?: "Date TBD"
    val timeText = start?.format(DateTimeFormatter.ofPattern("hh:mm a")) ?: "Time TBD"

    ClayCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        cornerRadius = 20.dp,
        elevation = 8.dp,
        backgroundColor = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!event.posterUrl.isNullOrBlank()) {
                AsyncImage(
                    model = event.posterUrl,
                    contentDescription = event.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = event.title.ifBlank { "Untitled Event" },
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$dateText • $timeText",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Groups,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = clubName.ifBlank { "Unknown club" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryChipRow(
    categories: List<Category>,
    selectedCategory: String?,
    onSelect: (String?) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            CategoryChip(
                title = "All",
                selected = selectedCategory == null,
                onClick = { onSelect(null) }
            )
        }
        items(categories, key = { it.categoryId }) { category ->
            CategoryChip(
                title = category.name,
                selected = selectedCategory == category.categoryId,
                onClick = { onSelect(category.categoryId) }
            )
        }
    }
}

@Composable
private fun CategoryChip(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primary 
                else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
private fun ClubGrid(
    clubs: List<Club>,
    onClubClick: (Club) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        clubs.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { club ->
                    ClayCard(
                        modifier = Modifier.weight(1f),
                        onClick = { onClubClick(club) },
                        cornerRadius = 20.dp,
                        elevation = 6.dp,
                        backgroundColor = MaterialTheme.colorScheme.surface
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(88.dp)
                                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                            ) {
                                if (!club.bannerUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = club.bannerUrl,
                                        contentDescription = club.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.linearGradient(
                                                    colors = listOf(
                                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.95f),
                                                        Color(0xFF8B74FF)
                                                    )
                                                )
                                            )
                                    )
                                }
                                if (!club.logoUrl.isNullOrBlank()) {
                                    Box(
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AsyncImage(
                                            model = club.logoUrl,
                                            contentDescription = "${club.name} logo",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(26.dp)
                                                .clip(CircleShape)
                                        )
                                    }
                                }
                            }
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(
                                    text = club.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${club.members.size} members",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (club.isPublic) "Open" else "Closed",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (club.isPublic) Color(0xFF0D8A4B) else Color(0xFFB45309),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(MaterialTheme.colorScheme.secondary)
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
                if (row.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun EmptySectionCard(message: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
