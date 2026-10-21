package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FootballMatch
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LiveCrimson

/**
 * Interactive Dynamic Island Live Scoreboard for the user's Favorite Team or any Pinned Match.
 * Supports:
 * - Compact OLED Pill with live ticking minute, team badges, scoreline, and 1-tap Notification Bar Pin toggle.
 * - Animated Goal Celebration Flash when the user's favorite team or pinned match scores.
 * - Expanded Dynamic Island telemetry drawer with xG, possession bar, match switcher, and Favorite Team Goal simulator.
 */
@Composable
fun DynamicIslandScoreboard(
    activeMatch: FootballMatch,
    allMatches: List<FootballMatch>,
    favoriteClub: String,
    pinnedMatchId: String?,
    isPinnedToNotificationBar: Boolean,
    goalFlashMessage: String?,
    streamServerName: String,
    onPinMatch: (String?) -> Unit,
    onTogglePinToNotificationBar: () -> Unit,
    onTriggerFavoriteTeamGoal: () -> Unit,
    onJumpToMatchStream: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    val isFavoriteClubMatch = activeMatch.homeTeam.contains(favoriteClub, ignoreCase = true) ||
        activeMatch.awayTeam.contains(favoriteClub, ignoreCase = true) ||
        favoriteClub.contains(activeMatch.homeTeam, ignoreCase = true) ||
        favoriteClub.contains(activeMatch.awayTeam, ignoreCase = true)

    val isGoalFlashing = !goalFlashMessage.isNullOrBlank()

    val infiniteTransition = rememberInfiniteTransition(label = "dynamic_island_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700),
            repeatMode = RepeatMode.Reverse
        ),
        label = "island_pulse_alpha"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            isGoalFlashing -> Color(0xFFFACC15)
            isPinnedToNotificationBar -> VerifiedPillGreen.copy(alpha = 0.85f)
            isFavoriteClubMatch -> Color(0xFFFACC15).copy(alpha = 0.65f)
            else -> Color.White.copy(alpha = 0.22f)
        },
        label = "island_border_color"
    )

    val islandShape = RoundedCornerShape(if (isExpanded) 26.dp else 50.dp)
    val minuteLabel = if (activeMatch.addedTime > 0) {
        "${activeMatch.minute}+${activeMatch.addedTime}'"
    } else {
        "${activeMatch.minute}'"
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = Color(0xFF060709).copy(alpha = 0.96f),
            shape = islandShape,
            border = BorderStroke(if (isGoalFlashing) 1.8.dp else 1.2.dp, borderColor),
            shadowElevation = 12.dp,
            modifier = Modifier
                .fillMaxWidth(if (isExpanded || isGoalFlashing) 1f else 0.96f)
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
                .clickable { isExpanded = !isExpanded }
                .testTag("dynamic_island_scoreboard")
        ) {
            Column(
                modifier = Modifier
                    .background(
                        Brush.horizontalGradient(
                            colors = if (isGoalFlashing) {
                                listOf(
                                    Color(0xFF063726),
                                    Color(0xFF1E2912),
                                    Color(0xFF063726)
                                )
                            } else {
                                listOf(
                                    Color(0xFF07090C),
                                    Color(0xFF10141B),
                                    Color(0xFF07090C)
                                )
                            }
                        )
                    )
                    .padding(
                        horizontal = if (isExpanded) 16.dp else 14.dp,
                        vertical = if (isExpanded) 12.dp else 8.dp
                    )
            ) {
                // Top Compact Pill Row (Always Visible)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left Status Badge: ★ FAV or 📌 PINNED + Competition
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = if (isFavoriteClubMatch) {
                                Color(0xFFFACC15).copy(alpha = 0.18f)
                            } else {
                                VerifiedPillGreen.copy(alpha = 0.18f)
                            },
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(
                                1.dp,
                                if (isFavoriteClubMatch) Color(0xFFFACC15).copy(alpha = 0.65f)
                                else VerifiedPillGreen.copy(alpha = 0.65f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isFavoriteClubMatch && pinnedMatchId == null) {
                                        Icons.Default.Star
                                    } else {
                                        Icons.Default.PushPin
                                    },
                                    contentDescription = "Dynamic Island Mode",
                                    tint = if (isFavoriteClubMatch) Color(0xFFFACC15) else VerifiedPillGreen,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = if (pinnedMatchId != null) "PINNED" else "★ FAV",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isFavoriteClubMatch) Color(0xFFFACC15) else VerifiedPillGreen
                                )
                            }
                        }

                        // Live Pulsing Minute Dot
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(LiveCrimson.copy(alpha = pulseAlpha))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = minuteLabel,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = LiveCrimson
                            )
                        }
                    }

                    // Center Live Scoreboard Display
                    AnimatedContent(
                        targetState = "${activeMatch.homeShort} ${activeMatch.homeScore} - ${activeMatch.awayScore} ${activeMatch.awayShort}",
                        transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(180)) },
                        label = "island_score_anim"
                    ) { scoreText ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(9.dp)
                                    .clip(CircleShape)
                                    .background(Color(activeMatch.homeColorHex))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = scoreText,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(9.dp)
                                    .clip(CircleShape)
                                    .background(Color(activeMatch.awayColorHex))
                            )
                        }
                    }

                    // Right Controls: Notification Bar Pin Quick-Button + Expand Chevron
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = if (isPinnedToNotificationBar) {
                                VerifiedPillGreen.copy(alpha = 0.22f)
                            } else {
                                Color.White.copy(alpha = 0.08f)
                            },
                            shape = CircleShape,
                            border = BorderStroke(
                                1.dp,
                                if (isPinnedToNotificationBar) VerifiedPillGreen else OledCardBorder
                            ),
                            modifier = Modifier
                                .size(26.dp)
                                .clickable { onTogglePinToNotificationBar() }
                                .testTag("dynamic_island_quick_notif_pin_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isPinnedToNotificationBar) {
                                        Icons.Default.NotificationsActive
                                    } else {
                                        Icons.Default.NotificationsOff
                                    },
                                    contentDescription = "Pin Scoreboard to Android Notification Bar",
                                    tint = if (isPinnedToNotificationBar) VerifiedPillGreen else OledMutedText,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }

                        Icon(
                            imageVector = if (isExpanded) {
                                Icons.Default.KeyboardArrowUp
                            } else {
                                Icons.Default.KeyboardArrowDown
                            },
                            contentDescription = if (isExpanded) "Collapse Dynamic Island" else "Expand Dynamic Island",
                            tint = OledMutedText,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Goal Celebration Banner inside Dynamic Island when Favorite Team or Pinned Match Scores!
                AnimatedVisibility(visible = isGoalFlashing) {
                    Surface(
                        color = Color(0xFFFACC15).copy(alpha = 0.16f),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, Color(0xFFFACC15).copy(alpha = 0.7f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .testTag("dynamic_island_goal_flash_banner")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.SportsSoccer,
                                contentDescription = "Goal Celebration",
                                tint = Color(0xFFFACC15),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = goalFlashMessage.orEmpty(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Expanded Dynamic Island Live Telemetry & Pin Controls
                AnimatedVisibility(visible = isExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .testTag("dynamic_island_expanded_card")
                    ) {
                        HorizontalDivider(color = OledCardBorder, thickness = 0.8.dp)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Full Team Names & Competition Stage
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${activeMatch.homeTeam} vs ${activeMatch.awayTeam}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${activeMatch.competition.displayName} • ${activeMatch.venue}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OledMutedText,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Surface(
                                color = VerifiedPillGreen.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, VerifiedPillGreen.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .clickable { onJumpToMatchStream(activeMatch.id) }
                                    .testTag("dynamic_island_watch_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Watch Live",
                                        tint = VerifiedPillGreen,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Watch 4K",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = VerifiedPillGreen
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Live Possession & xG Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${activeMatch.homeShort} Poss ${activeMatch.stats.homePossession}% • xG ${String.format("%.2f", activeMatch.stats.homeXg)}",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                color = VerifiedPillGreen
                            )
                            Text(
                                text = "xG ${String.format("%.2f", activeMatch.stats.awayXg)} • Poss ${activeMatch.stats.awayPossession}% ${activeMatch.awayShort}",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                color = Color(0xFF38BDF8)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFF1F242D))
                        ) {
                            val homeWeight = (activeMatch.stats.homePossession / 100f).coerceIn(0.15f, 0.85f)
                            Box(
                                modifier = Modifier
                                    .weight(homeWeight)
                                    .height(5.dp)
                                    .background(VerifiedPillGreen)
                            )
                            Box(
                                modifier = Modifier
                                    .weight(1f - homeWeight)
                                    .height(5.dp)
                                    .background(Color(0xFF38BDF8))
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Latest Scorer / Commentary Line
                        val latestHeadline = activeMatch.commentary.firstOrNull()?.headline
                            ?: "Live broadcast streaming on $streamServerName"
                        Text(
                            text = "🎙️ $latestHeadline",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFD5D9E2),
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Match Switcher Row: Pin Favorite Team or Any Live Match to Dynamic Island
                        Text(
                            text = "PIN ANY MATCH TO DYNAMIC ISLAND & NOTIFICATION BAR:",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = OledMutedText
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.testTag("dynamic_island_match_switcher")
                        ) {
                            item {
                                val isAutoFav = pinnedMatchId == null
                                Surface(
                                    color = if (isAutoFav) Color(0xFFFACC15).copy(alpha = 0.20f) else OledCardSurface,
                                    shape = RoundedCornerShape(50),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isAutoFav) Color(0xFFFACC15) else OledCardBorder
                                    ),
                                    modifier = Modifier
                                        .clickable { onPinMatch(null) }
                                        .testTag("dynamic_island_pin_fav_chip")
                                ) {
                                    Text(
                                        text = "★ Fav ($favoriteClub)",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isAutoFav) Color(0xFFFACC15) else Color.White,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }

                            items(allMatches, key = { it.id }) { itemMatch ->
                                val isPinned = pinnedMatchId == itemMatch.id
                                Surface(
                                    color = if (isPinned) VerifiedPillGreen.copy(alpha = 0.22f) else OledCardSurface,
                                    shape = RoundedCornerShape(50),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isPinned) VerifiedPillGreen else OledCardBorder
                                    ),
                                    modifier = Modifier
                                        .clickable { onPinMatch(itemMatch.id) }
                                        .testTag("dynamic_island_switch_${itemMatch.id}")
                                ) {
                                    Text(
                                        text = "${itemMatch.homeShort} ${itemMatch.homeScore}-${itemMatch.awayScore} ${itemMatch.awayShort}",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 10.sp,
                                        fontWeight = if (isPinned) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isPinned) VerifiedPillGreen else Color.White,
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Action Buttons: Pin/Unpin to Android Notification Bar & Simulate Favorite Team Goal Push
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onTogglePinToNotificationBar,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isPinnedToNotificationBar) VerifiedPillGreen else Color(0xFF1E222A),
                                    contentColor = if (isPinnedToNotificationBar) Color.Black else Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("dynamic_island_pin_notif_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PushPin,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (isPinnedToNotificationBar) "Pinned to Notif Bar" else "Pin to Notif Bar",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }

                            OutlinedButton(
                                onClick = onTriggerFavoriteTeamGoal,
                                border = BorderStroke(1.dp, Color(0xFFFACC15)),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("dynamic_island_fav_goal_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFACC15),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Fav Team Goal Alert",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFACC15),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
