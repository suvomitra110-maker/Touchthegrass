package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.WatchlistMediaEntity
import com.example.model.MediaCategory
import com.example.model.MediaTitle
import com.example.model.SeedCatalog
import com.example.model.StreamServer
import com.example.ui.theme.BroadcastCard
import com.example.ui.theme.BroadcastCardElevated
import com.example.ui.theme.DividerSubtle
import com.example.ui.theme.MidnightSurface
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.PitchEmerald
import com.example.ui.theme.StreamCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.UclGold

@Composable
fun CinemaHubScreen(
    selectedCategory: MediaCategory,
    searchQuery: String,
    selectedMedia: MediaTitle?,
    selectedServer: StreamServer,
    isPlaying: Boolean,
    watchlist: List<WatchlistMediaEntity>,
    onSelectCategory: (MediaCategory) -> Unit,
    onUpdateSearch: (String) -> Unit,
    onSelectMedia: (MediaTitle) -> Unit,
    onSelectServer: (StreamServer) -> Unit,
    onTogglePlayback: () -> Unit,
    onToggleWatchlist: (MediaTitle) -> Unit,
    onSpeakSynopsis: (String) -> Unit,
    onJumpToLiveFootball: () -> Unit
) {
    val activeMedia = selectedMedia ?: SeedCatalog.cinemaCatalog.first()
    val isInWatchlist = watchlist.any { it.mediaId == activeMedia.id }

    val catalog = SeedCatalog.cinemaCatalog.filter { item ->
        val matchesCat = when (selectedCategory) {
            MediaCategory.ALL -> true
            MediaCategory.TRENDING -> item.isTrending
            else -> item.category == selectedCategory
        }
        val matchesSearch = searchQuery.isBlank() ||
            item.title.contains(searchQuery, ignoreCase = true) ||
            item.genres.any { it.contains(searchQuery, ignoreCase = true) } ||
            item.cast.any { it.contains(searchQuery, ignoreCase = true) }
        matchesCat && matchesSearch
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("cinema_hub_list"),
        contentPadding = PaddingValues(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. FrameXTV Header Banner + Quick Switch to Live Football
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .border(1.dp, PitchEmerald.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BroadcastCard)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "FRAMEXTV.CC • 4K DECRYPTED HUB",
                            style = MaterialTheme.typography.labelSmall,
                            color = StreamCyan
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Movies, Series, Anime + Live Football",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                    }
                    Button(
                        onClick = onJumpToLiveFootball,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PitchEmerald,
                            contentColor = ObsidianBg
                        ),
                        modifier = Modifier.testTag("banner_watch_live_football_button")
                    ) {
                        Text(
                            text = "Watch Live Football",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }

        // 2. Search Bar & Category Chips
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onUpdateSearch,
                    placeholder = {
                        Text(
                            text = "Search 4K movies, TV series, anime, cast…",
                            color = TextSecondary
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Cinema",
                            tint = StreamCyan
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StreamCyan,
                        unfocusedBorderColor = DividerSubtle,
                        focusedContainerColor = BroadcastCard,
                        unfocusedContainerColor = BroadcastCard
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("cinema_search_input")
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(MediaCategory.entries) { category ->
                        val selected = category == selectedCategory
                        FilterChip(
                            selected = selected,
                            onClick = { onSelectCategory(category) },
                            label = {
                                Text(
                                    text = category.label,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = StreamCyan.copy(alpha = 0.22f),
                                selectedLabelColor = StreamCyan,
                                containerColor = BroadcastCard,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }
            }
        }

        // 3. Active 4K Cinema Stream Player & Server Switcher
        item {
            ActiveCinemaPlayerCard(
                media = activeMedia,
                selectedServer = selectedServer,
                isPlaying = isPlaying,
                isInWatchlist = isInWatchlist,
                onSelectServer = onSelectServer,
                onTogglePlayback = onTogglePlayback,
                onToggleWatchlist = { onToggleWatchlist(activeMedia) },
                onSpeakSynopsis = {
                    onSpeakSynopsis("${activeMedia.title}, released in ${activeMedia.year}, rated ${activeMedia.rating}. ${activeMedia.synopsis}")
                }
            )
        }

        // 4. Catalog Grid / List
        item {
            Text(
                text = "FRAMEXTV 4K STREAMING CATALOG (${catalog.size})",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        items(catalog, key = { it.id }) { item ->
            val saved = watchlist.any { it.mediaId == item.id }
            CinemaCatalogItemCard(
                media = item,
                isSelected = item.id == activeMedia.id,
                isSaved = saved,
                onSelect = { onSelectMedia(item) },
                onToggleSave = { onToggleWatchlist(item) }
            )
        }
    }
}

@Composable
private fun ActiveCinemaPlayerCard(
    media: MediaTitle,
    selectedServer: StreamServer,
    isPlaying: Boolean,
    isInWatchlist: Boolean,
    onSelectServer: (StreamServer) -> Unit,
    onTogglePlayback: () -> Unit,
    onToggleWatchlist: () -> Unit,
    onSpeakSynopsis: () -> Unit
) {
    val primaryCol = Color(media.primaryColorHex)
    val secondaryCol = Color(media.secondaryColorHex)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .border(1.dp, DividerSubtle, RoundedCornerShape(20.dp))
            .testTag("active_cinema_player_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MidnightSurface)
    ) {
        Column {
            // Anamorphic 4K Stream Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(secondaryCol, ObsidianBg, primaryCol.copy(alpha = 0.38f))
                        )
                    )
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(primaryCol.copy(alpha = 0.35f), Color.Transparent),
                            center = Offset(size.width * 0.75f, size.height * 0.35f),
                            radius = size.width * 0.55f
                        ),
                        center = Offset(size.width * 0.75f, size.height * 0.35f),
                        radius = size.width * 0.55f
                    )
                }

                // Top Quality & Rating Badges
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            color = primaryCol.copy(alpha = 0.22f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, primaryCol)
                        ) {
                            Text(
                                text = media.qualityBadge,
                                style = MaterialTheme.typography.labelSmall,
                                color = primaryCol,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Surface(
                            color = ObsidianBg.copy(alpha = 0.75f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Rating",
                                    tint = UclGold,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = media.rating,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextPrimary
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onToggleWatchlist,
                        modifier = Modifier
                            .size(48.dp)
                            .background(ObsidianBg.copy(alpha = 0.7f), CircleShape)
                            .testTag("toggle_watchlist_button")
                    ) {
                        Icon(
                            imageVector = if (isInWatchlist) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Save to FrameXTV My List",
                            tint = if (isInWatchlist) PitchEmerald else TextPrimary
                        )
                    }
                }

                // Center Play / Pause Button
                IconButton(
                    onClick = onTogglePlayback,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(60.dp)
                        .background(primaryCol.copy(alpha = 0.9f), CircleShape)
                        .testTag("cinema_play_pause_button")
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause Stream" else "Play Stream",
                        tint = ObsidianBg,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Bottom Overlay Info
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, ObsidianBg.copy(alpha = 0.95f))
                            )
                        )
                        .padding(14.dp)
                ) {
                    Text(
                        text = media.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = "${media.year} • ${media.durationOrSeasons} • ${media.genres.joinToString(" / ")}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = media.synopsis,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                Text(
                    text = "Director: ${media.director} • Cast: ${media.cast.joinToString(", ")}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextPrimary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Dns,
                            contentDescription = "Decrypted Servers",
                            tint = StreamCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "DECRYPTED SERVERS (AD-FREE)",
                            style = MaterialTheme.typography.labelSmall,
                            color = StreamCyan
                        )
                    }

                    OutlinedButton(
                        onClick = onSpeakSynopsis,
                        modifier = Modifier.testTag("speak_synopsis_tts_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Read Synopsis via Gemini TTS",
                            tint = PitchEmerald,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Narrate (TTS)",
                            style = MaterialTheme.typography.labelSmall,
                            color = PitchEmerald
                        )
                    }
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(media.servers, key = { it.id }) { server ->
                        val active = server.id == selectedServer.id
                        FilterChip(
                            selected = active,
                            onClick = { onSelectServer(server) },
                            label = {
                                Text(
                                    text = "${server.name} • ${server.resolution}",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PitchEmerald.copy(alpha = 0.2f),
                                selectedLabelColor = PitchEmerald,
                                containerColor = BroadcastCard,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CinemaCatalogItemCard(
    media: MediaTitle,
    isSelected: Boolean,
    isSaved: Boolean,
    onSelect: () -> Unit,
    onToggleSave: () -> Unit
) {
    val accent = Color(media.primaryColorHex)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onSelect)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) accent else DividerSubtle,
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("cinema_item_${media.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) BroadcastCardElevated else BroadcastCard
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(accent.copy(alpha = 0.35f), Color(media.secondaryColorHex))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Movie,
                    contentDescription = media.title,
                    tint = accent,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = media.qualityBadge,
                        style = MaterialTheme.typography.labelSmall,
                        color = accent
                    )
                    Text(
                        text = "• ★ ${media.rating}",
                        style = MaterialTheme.typography.labelSmall,
                        color = UclGold
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = media.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${media.category.label} • ${media.durationOrSeasons}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            IconButton(
                onClick = onToggleSave,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = "Toggle My List",
                    tint = if (isSaved) PitchEmerald else TextSecondary
                )
            }
        }
    }
}
