package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.MatchAlertEntity
import com.example.data.local.NotificationLogEntity
import com.example.data.local.WatchlistMediaEntity
import com.example.model.FootballMatch
import com.example.model.SeedCatalog
import com.example.ui.components.OledCardBorder
import com.example.ui.components.OledMutedText
import com.example.ui.components.VerifiedPillGreen
import com.example.ui.theme.BroadcastCard
import com.example.ui.theme.BroadcastCardElevated
import com.example.ui.theme.DividerSubtle
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.PitchEmerald
import com.example.ui.theme.StreamCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.UclGold
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AlertsAndSavedScreen(
    subscribedAlerts: List<MatchAlertEntity>,
    notificationLogs: List<NotificationLogEntity>,
    watchlist: List<WatchlistMediaEntity>,
    matches: List<FootballMatch>,
    hasNotificationPermission: Boolean,
    favoriteClub: String = "Inter Miami CF",
    favoritedTeams: Set<String> = setOf("Inter Miami CF", "FC Barcelona"),
    fcmToken: String = "fcm_ttg_live_token",
    fcmSubscribedTopics: Set<String> = emptySet(),
    onRequestNotificationPermission: () -> Unit,
    onToggleFavoriteClubFcm: (String) -> Unit = {},
    onTriggerFavoriteClubFcmPush: (Boolean) -> Unit = {},
    onUnsubscribeMatch: (FootballMatch) -> Unit,
    onTriggerGoalAlert: (FootballMatch) -> Unit,
    onTriggerKickoffAlert: (FootballMatch) -> Unit,
    onClearNotificationLogs: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("alerts_saved_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Firebase Cloud Messaging (FCM) Favorite Club Push Hub
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, VerifiedPillGreen.copy(alpha = 0.65f), RoundedCornerShape(18.dp))
                    .testTag("fcm_favorite_club_push_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = BroadcastCard)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = "Firebase Cloud Messaging",
                                tint = VerifiedPillGreen
                            )
                            Column {
                                Text(
                                    text = "FIREBASE CLOUD MESSAGING (FCM)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Profile Favorite Club: ★ $favoriteClub",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = VerifiedPillGreen
                                )
                            }
                        }

                        Surface(
                            color = VerifiedPillGreen.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, VerifiedPillGreen)
                        ) {
                            Text(
                                text = "${fcmSubscribedTopics.size} FCM TOPICS",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = VerifiedPillGreen,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text(
                        text = "FCM Token: ${fcmToken.take(36)}...",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        color = OledMutedText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "TAP CLUBS TO SUBSCRIBE FCM GOAL & KICKOFF TOPICS:",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.testTag("fcm_favorite_clubs_row")
                    ) {
                        items(SeedCatalog.popularClubs, key = { it }) { club ->
                            val isFav = favoritedTeams.any { it.equals(club, ignoreCase = true) } ||
                                club.equals(favoriteClub, ignoreCase = true)
                            FilterChip(
                                selected = isFav,
                                onClick = { onToggleFavoriteClubFcm(club) },
                                label = {
                                    Text(
                                        text = club,
                                        fontSize = 11.sp,
                                        fontWeight = if (isFav) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = if (isFav) Color(0xFFFACC15) else OledMutedText,
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = VerifiedPillGreen.copy(alpha = 0.20f),
                                    selectedLabelColor = Color.White,
                                    containerColor = BroadcastCardElevated,
                                    labelColor = OledMutedText
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isFav,
                                    borderColor = OledCardBorder,
                                    selectedBorderColor = VerifiedPillGreen
                                )
                            )
                        }
                    }

                    if (fcmSubscribedTopics.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            fcmSubscribedTopics.take(6).forEach { topic ->
                                Surface(
                                    color = Color(0xFF141820),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, OledCardBorder)
                                ) {
                                    Text(
                                        text = "/topics/$topic",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 9.sp,
                                        color = StreamCyan,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onTriggerFavoriteClubFcmPush(true) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VerifiedPillGreen,
                                contentColor = Color.Black
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("fcm_favorite_goal_push_button")
                        ) {
                            Text(
                                text = "★ Club Goal Push",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }
                        OutlinedButton(
                            onClick = { onTriggerFavoriteClubFcmPush(false) },
                            border = BorderStroke(1.dp, StreamCyan),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("fcm_favorite_kickoff_push_button")
                        ) {
                            Text(
                                text = "★ Club Kickoff Push",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = StreamCyan,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // 2. Push Notification Permission & Dispatch Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PitchEmerald.copy(alpha = 0.55f), RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = BroadcastCard)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Push Notifications Center",
                            tint = PitchEmerald
                        )
                        Column {
                            Text(
                                text = "PUSH NOTIFICATIONS: GOALS & KICKOFFS",
                                style = MaterialTheme.typography.headlineMedium,
                                color = TextPrimary
                            )
                            Text(
                                text = if (hasNotificationPermission) {
                                    "Status: Active • Android High-Priority Channels Enabled"
                                } else {
                                    "Status: Tap below to grant Android Notification Permission"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (hasNotificationPermission) PitchEmerald else UclGold
                            )
                        }
                    }

                    if (!hasNotificationPermission) {
                        Button(
                            onClick = onRequestNotificationPermission,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = UclGold,
                                contentColor = ObsidianBg
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("grant_notification_permission_button")
                        ) {
                            Text(
                                text = "Enable Push Notifications",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }

                    val featuredMatch = matches.firstOrNull()
                    if (featuredMatch != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onTriggerGoalAlert(featuredMatch) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PitchEmerald,
                                    contentColor = ObsidianBg
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("alerts_tab_goal_push_button")
                            ) {
                                Text(
                                    text = "Send Goal Push",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                            OutlinedButton(
                                onClick = { onTriggerKickoffAlert(featuredMatch) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("alerts_tab_kickoff_push_button")
                            ) {
                                Text(
                                    text = "Send Kickoff Push",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = StreamCyan
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Subscribed Match Alerts
        item {
            Text(
                text = "SUBSCRIBED MATCH ALERTS (${subscribedAlerts.size})",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )
        }

        if (subscribedAlerts.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BroadcastCard)
                ) {
                    Text(
                        text = "No active match subscriptions yet. Tap the bell icon on any live match in the Live Football tab to receive Major Goal and Kickoff alerts.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(subscribedAlerts, key = { it.matchId }) { sub ->
                val matchObj = matches.find { it.id == sub.matchId }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = BroadcastCardElevated)
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
                                text = sub.competitionName,
                                style = MaterialTheme.typography.labelSmall,
                                color = PitchEmerald
                            )
                            Text(
                                text = sub.matchLabel,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Text(
                                text = "FCM Major Goals: ON • Kickoff Alerts: ON",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                        if (matchObj != null) {
                            IconButton(
                                onClick = { onUnsubscribeMatch(matchObj) },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Unsubscribe match alert",
                                    tint = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Recent Push Notifications Log
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT PUSH ALERTS LOG (${notificationLogs.size})",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary
                )
                if (notificationLogs.isNotEmpty()) {
                    OutlinedButton(onClick = onClearNotificationLogs) {
                        Text(
                            text = "Clear Log",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        items(notificationLogs, key = { it.id }) { log ->
            val timeStr = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date(log.timestamp))
            val isGoal = log.alertType.contains("GOAL", ignoreCase = true)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = BroadcastCard)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            color = if (isGoal) PitchEmerald.copy(alpha = 0.2f) else UclGold.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "${log.alertType} • ${log.competition}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isGoal) PitchEmerald else UclGold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Text(
                            text = timeStr,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = log.headline,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = log.body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }

        // 5. Saved Watchlist ("My List")
        item {
            HorizontalDivider(color = DividerSubtle)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Bookmark,
                    contentDescription = "My List",
                    tint = StreamCyan
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "TOUCHTHEGRASS MY LIST (${watchlist.size})",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary
                )
            }
        }

        if (watchlist.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BroadcastCard)
                ) {
                    Text(
                        text = "Your TouchTheGrass watchlist is empty. Bookmark 4K movies, series, or anime in the Cinema & TV tab.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(watchlist, key = { it.mediaId }) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = BroadcastCardElevated)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${item.qualityBadge} • ★ ${item.rating}",
                                style = MaterialTheme.typography.labelSmall,
                                color = StreamCyan
                            )
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Text(
                                text = item.categoryLabel,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}
