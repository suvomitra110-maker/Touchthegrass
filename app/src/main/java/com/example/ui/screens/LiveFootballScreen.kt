package com.example.ui.screens

import android.annotation.SuppressLint
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.R
import com.example.data.local.MatchAlertEntity
import com.example.model.CommentaryEvent
import com.example.model.FootballCompetition
import com.example.model.FootballMatch
import com.example.model.FootballThemeOption
import com.example.model.SeedCatalog
import com.example.model.StreamServer
import com.example.ui.components.OledCardBorder
import com.example.ui.components.OledCardSurface
import com.example.ui.components.OledMutedText
import com.example.ui.components.OverviewStatTile
import com.example.ui.components.PremiumSlateBg
import com.example.ui.components.VerifiedPillGreen
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LiveCrimson
import com.example.ui.theme.PitchEmerald
import com.example.ui.theme.StreamCyan
import com.example.ui.theme.TextPrimary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LiveFootballScreen(
    matches: List<FootballMatch>,
    selectedCompetition: FootballCompetition,
    enabledLeagues: Set<FootballCompetition>,
    selectedTheme: FootballThemeOption,
    selectedMatchId: String,
    selectedServer: StreamServer,
    subscribedAlerts: List<MatchAlertEntity>,
    ttsStatus: String?,
    isTtsLoading: Boolean,
    onSelectCompetition: (FootballCompetition) -> Unit,
    onToggleLeagueVisibility: (FootballCompetition) -> Unit,
    onEnableAllLeagues: () -> Unit,
    onSelectMatch: (String) -> Unit,
    onSelectServer: (StreamServer) -> Unit,
    onSpeakCommentary: (String) -> Unit,
    onStopTts: () -> Unit,
    onToggleAlertSubscription: (FootballMatch) -> Unit,
    onTriggerGoalAlert: (FootballMatch, Boolean) -> Unit,
    onTriggerKickoffAlert: (FootballMatch) -> Unit,
    matchSearchQuery: String = "",
    onUpdateMatchSearch: (String) -> Unit = {},
    onOpenStandings: (FootballCompetition) -> Unit = {},
    onOpenWebPortal: (String, String) -> Unit = { _, _ -> }
) {
    var showLeagueToggleDrawer by rememberSaveable { mutableStateOf(false) }
    var showCommentaryAndTts by rememberSaveable { mutableStateOf(false) }

    val leagueFilteredPool = matches.filter { it.competition in enabledLeagues }
        .ifEmpty { matches }

    val competitionFilteredMatches = if (selectedCompetition == FootballCompetition.ALL) {
        leagueFilteredPool
    } else {
        leagueFilteredPool.filter { it.competition == selectedCompetition }
            .ifEmpty { leagueFilteredPool }
    }

    val filteredMatches = remember(competitionFilteredMatches, matchSearchQuery) {
        if (matchSearchQuery.isBlank()) {
            competitionFilteredMatches
        } else {
            val q = matchSearchQuery.trim()
            competitionFilteredMatches.filter { match ->
                match.homeTeam.contains(q, ignoreCase = true) ||
                    match.awayTeam.contains(q, ignoreCase = true) ||
                    match.homeShort.contains(q, ignoreCase = true) ||
                    match.awayShort.contains(q, ignoreCase = true)
            }
        }
    }

    val activeMatch = filteredMatches.find { it.id == selectedMatchId }
        ?: filteredMatches.firstOrNull()
        ?: competitionFilteredMatches.find { it.id == selectedMatchId }
        ?: competitionFilteredMatches.firstOrNull()
        ?: matches.first()

    val isSubscribed = subscribedAlerts.any { it.matchId == activeMatch.id }

    Column(modifier = Modifier.fillMaxSize()) {
        // Persistent Search Bar in the Header of the Main Feed
        Surface(
            color = Color(0xFF0B0D11).copy(alpha = 0.92f),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("persistent_main_feed_search_header")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = matchSearchQuery,
                    onValueChange = onUpdateMatchSearch,
                    singleLine = true,
                    placeholder = {
                        Text(
                            text = stringResource(R.string.search_placeholder_matches),
                            color = OledMutedText,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Matches by Team Name",
                            tint = VerifiedPillGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Surface(
                                color = VerifiedPillGreen.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(50),
                                border = BorderStroke(1.dp, VerifiedPillGreen.copy(alpha = 0.45f))
                            ) {
                                Text(
                                    text = "${filteredMatches.size} Live",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VerifiedPillGreen,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            if (matchSearchQuery.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = { onUpdateMatchSearch("") },
                                    modifier = Modifier
                                        .size(28.dp)
                                        .testTag("clear_match_search_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear Search",
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = OledCardSurface.copy(alpha = 0.88f),
                        unfocusedContainerColor = OledCardSurface.copy(alpha = 0.76f),
                        focusedBorderColor = VerifiedPillGreen,
                        unfocusedBorderColor = OledCardBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("main_feed_match_search_input")
                )
            }
        }

        HorizontalDivider(color = OledCardBorder.copy(alpha = 0.65f), thickness = 0.8.dp)

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag("live_football_list"),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        // 1. Top-Level League Filtering Bar (UCL, EPL, LaLiga, Serie A, Bundesliga, Inter Miami, etc.)
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                val visibleFilterTabs = listOf(FootballCompetition.ALL) +
                    FootballCompetition.toggleableLeagues.filter { it in enabledLeagues }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Live Broadcasts",
                        fontFamily = FontFamily.Serif,
                        fontSize = 20.sp,
                        color = Color.White
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = VerifiedPillGreen.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, VerifiedPillGreen.copy(alpha = 0.65f)),
                            modifier = Modifier
                                .clickable {
                                    val targetComp = if (selectedCompetition == FootballCompetition.ALL) {
                                        activeMatch.competition
                                    } else {
                                        selectedCompetition
                                    }
                                    onOpenStandings(targetComp)
                                }
                                .testTag("quick_link_standings_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WorkspacePremium,
                                    contentDescription = "Standings Table",
                                    tint = VerifiedPillGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Standings",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = VerifiedPillGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Surface(
                            color = OledCardSurface,
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, OledCardBorder),
                            modifier = Modifier
                                .clickable { showLeagueToggleDrawer = !showLeagueToggleDrawer }
                                .testTag("customize_leagues_toggle_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "Toggle Leagues",
                                    tint = VerifiedPillGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Leagues (${enabledLeagues.size}/8)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                AnimatedVisibility(visible = showLeagueToggleDrawer) {
                    Surface(
                        color = OledCardSurface,
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, OledCardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .testTag("league_visibility_toggles_bar")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Toggle visible leagues:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OledMutedText
                                )
                                Text(
                                    text = "Show All",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = VerifiedPillGreen,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable { onEnableAllLeagues() }
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                FootballCompetition.toggleableLeagues.forEach { comp ->
                                    val isVisible = comp in enabledLeagues
                                    FilterChip(
                                        selected = isVisible,
                                        onClick = { onToggleLeagueVisibility(comp) },
                                        label = {
                                            Text(
                                                text = comp.shortTag,
                                                fontSize = 11.sp,
                                                fontWeight = if (isVisible) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = VerifiedPillGreen.copy(alpha = 0.22f),
                                            selectedLabelColor = Color.White,
                                            containerColor = Color.White.copy(alpha = 0.04f),
                                            labelColor = OledMutedText
                                        ),
                                        modifier = Modifier.testTag("toggle_league_${comp.code.lowercase()}")
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.testTag("competition_filter_row")
                ) {
                    items(visibleFilterTabs) { comp ->
                        val selected = comp == selectedCompetition
                        FilterChip(
                            selected = selected,
                            onClick = { onSelectCompetition(comp) },
                            label = {
                                Text(
                                    text = comp.shortTag,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VerifiedPillGreen.copy(alpha = 0.22f),
                                selectedLabelColor = VerifiedPillGreen,
                                containerColor = OledCardSurface,
                                labelColor = OledMutedText
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selected,
                                borderColor = OledCardBorder,
                                selectedBorderColor = VerifiedPillGreen
                            ),
                            modifier = Modifier.testTag("filter_comp_${comp.code.lowercase()}")
                        )
                    }
                }
            }
        }

        // 2. Main Live Broadcast Video Player Card with EpicSports (live.epicsports.in/?m=1) & Server Choose Options
        item {
            LiveStadiumBroadcastCard(
                match = activeMatch,
                selectedServer = selectedServer,
                isSubscribed = isSubscribed,
                onSelectServer = onSelectServer,
                onToggleSubscription = { onToggleAlertSubscription(activeMatch) },
                onOpenEpicSportsPortal = { title, url ->
                    onOpenWebPortal(title, url)
                }
            )
        }

        // 3. Slate-Blue FrameTV & EpicSports VIP Banner Card (Exact IMG_8276 Premium Member Card Style)
        item {
            Surface(
                color = PremiumSlateBg,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.28f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color(0xFFD6E4F0),
                        shape = CircleShape,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = "FrameTV & EpicSports",
                                tint = Color.Black,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = selectedServer.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF101418),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Commentary, TTS Audio & Goal Alerts",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF1F272E)
                        )
                    }

                    Button(
                        onClick = { showCommentaryAndTts = !showCommentaryAndTts },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF090A0C),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("toggle_commentary_panel_button")
                    ) {
                        Text(
                            text = if (showCommentaryAndTts) "Hide Audio" else "Live Audio",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Expandable Audio Commentary & Goal Alert Drawer
        item {
            AnimatedVisibility(visible = showCommentaryAndTts) {
                BroadcastActionPanel(
                    match = activeMatch,
                    ttsStatus = ttsStatus,
                    isTtsLoading = isTtsLoading,
                    onSpeakMatchSummary = {
                        val latest = activeMatch.commentary.firstOrNull()
                        val script = buildString {
                            append("Live on Touchthegrass.com from ${activeMatch.venue} in the ${activeMatch.competition.displayName}! ")
                            append("The score is ${activeMatch.homeTeam} ${activeMatch.homeScore}, ${activeMatch.awayTeam} ${activeMatch.awayScore} in the ${activeMatch.minute}th minute. ")
                            if (latest != null) {
                                append("${latest.headline}. ${latest.detail}")
                            }
                        }
                        onSpeakCommentary(script)
                    },
                    onStopTts = onStopTts,
                    onTriggerHomeGoal = { onTriggerGoalAlert(activeMatch, true) },
                    onTriggerAwayGoal = { onTriggerGoalAlert(activeMatch, false) },
                    onTriggerKickoffAlert = { onTriggerKickoffAlert(activeMatch) }
                )
            }
        }

        // 4. "Match Overview" Serif Header + 4-Tile Metric Grid (Exact IMG_8276 Account Overview Style)
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Match Overview",
                    fontFamily = FontFamily.Serif,
                    fontSize = 20.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OverviewStatTile(
                        label = "Possession",
                        value = "${activeMatch.stats.homePossession}%",
                        modifier = Modifier.weight(1f)
                    )
                    OverviewStatTile(
                        label = "xG Threat",
                        value = String.format("%.2f", activeMatch.stats.homeXg),
                        modifier = Modifier.weight(1f)
                    )
                    OverviewStatTile(
                        label = "Ping",
                        value = "${selectedServer.pingMs}ms",
                        modifier = Modifier.weight(1f)
                    )
                    OverviewStatTile(
                        label = "Feed Rating",
                        value = "4.9",
                        showStar = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 5. Clean Rounded Match Row Cards Filtered by Team Name in Real-Time
        if (filteredMatches.isEmpty()) {
            item {
                Surface(
                    color = OledCardSurface,
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, OledCardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("empty_match_search_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No active matches found for \"$matchSearchQuery\"",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Try searching for Real Madrid, Manchester City, Liverpool, Barcelona, Inter Miami, AC Milan, or Bayern.",
                            style = MaterialTheme.typography.bodySmall,
                            color = OledMutedText,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { onUpdateMatchSearch("") },
                            border = BorderStroke(1.dp, VerifiedPillGreen)
                        ) {
                            Text(
                                text = "Reset Team Filter",
                                color = VerifiedPillGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        } else {
            items(filteredMatches, key = { it.id }) { match ->
                val isSelected = match.id == activeMatch.id
                CleanOledMatchRowCard(
                    match = match,
                    isSelected = isSelected,
                    onClick = { onSelectMatch(match.id) }
                )
            }
        }
        }
    }
}

@Composable
fun LivePulseBadge(text: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "live_pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(750),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )
    Surface(
        color = LiveCrimson.copy(alpha = 0.18f),
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, LiveCrimson.copy(alpha = alpha))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(LiveCrimson.copy(alpha = alpha))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CleanOledMatchRowCard(
    match: FootballMatch,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val compColor = Color(match.competition.badgeColorHex)
    Surface(
        color = OledCardSurface,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) VerifiedPillGreen else OledCardBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("match_card_${match.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Circular Icon (Matching IMG_8276 / IMG_8275 circular icon rows)
            Surface(
                color = Color(0xFF23252B),
                shape = CircleShape,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.SportsSoccer,
                        contentDescription = null,
                        tint = compColor,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${match.homeTeam}  ${match.homeScore} - ${match.awayScore}  ${match.awayTeam}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${match.competition.shortTag} • ${match.minute}' LIVE • ${match.venue}",
                    style = MaterialTheme.typography.bodySmall,
                    color = OledMutedText,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (isSelected) {
                Surface(
                    color = Color(0xFF063726),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = "Playing",
                        style = MaterialTheme.typography.labelSmall,
                        color = VerifiedPillGreen,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Watch Match",
                tint = OledMutedText
            )
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun LiveStadiumBroadcastCard(
    match: FootballMatch,
    selectedServer: StreamServer,
    isSubscribed: Boolean,
    onSelectServer: (StreamServer) -> Unit,
    onToggleSubscription: () -> Unit,
    onOpenEpicSportsPortal: (String, String) -> Unit
) {
    // Player mode: "LIVE_VIDEO" (HTML5 Video Stream + Scoreboard HUD), "EPICSPORTS_WEB" (live.epicsports.in/?m=1), "RADAR"
    var playerMode by rememberSaveable { mutableStateOf("LIVE_VIDEO") }
    var isVideoPlaying by rememberSaveable { mutableStateOf(true) }
    var isVideoMuted by rememberSaveable { mutableStateOf(true) }

    val infiniteTransition = rememberInfiniteTransition(label = "pitch_radar")
    val radarPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "radar_phase"
    )

    val servers = SeedCatalog.footballBroadcastServers

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("live_stadium_broadcast_card")
    ) {
        Surface(
            color = OledCardSurface,
            shape = RoundedCornerShape(22.dp),
            border = BorderStroke(1.dp, OledCardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
        ) {
            Column {
                // Top Player Mode Bar: Live Video Stream | EpicSports.in Server | Tactical Radar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF101216))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PlayerModeChip(
                            label = "Live Player",
                            selected = playerMode == "LIVE_VIDEO",
                            onClick = { playerMode = "LIVE_VIDEO" },
                            testTag = "mode_live_video_button"
                        )
                        PlayerModeChip(
                            label = "EpicSports.in Feed",
                            selected = playerMode == "EPICSPORTS_WEB",
                            onClick = { playerMode = "EPICSPORTS_WEB" },
                            testTag = "mode_epicsports_web_button"
                        )
                        PlayerModeChip(
                            label = "2D Radar",
                            selected = playerMode == "RADAR",
                            onClick = { playerMode = "RADAR" },
                            testTag = "mode_radar_button"
                        )
                    }

                    IconButton(
                        onClick = onToggleSubscription,
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF23252B))
                            .testTag("toggle_match_alert_button")
                    ) {
                        Icon(
                            imageVector = if (isSubscribed) Icons.Default.NotificationsActive else Icons.Default.NotificationsNone,
                            contentDescription = "Toggle Goal & Kickoff Push Notifications",
                            tint = if (isSubscribed) VerifiedPillGreen else OledMutedText,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                // Live Video Player / EpicSports.in Web Stream / Tactical Radar Viewport
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(235.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF0A0C10), Color(0xFF141820), Color(0xFF090B0E))
                            )
                        )
                        .testTag("live_video_player_viewport")
                ) {
                    when (playerMode) {
                        "LIVE_VIDEO" -> {
                            val playerHtml = remember(match.id, selectedServer.id, isVideoPlaying, isVideoMuted) {
                                buildLiveBroadcastVideoHtml(
                                    match = match,
                                    server = selectedServer,
                                    autoPlay = isVideoPlaying,
                                    muted = isVideoMuted
                                )
                            }
                            AndroidView(
                                modifier = Modifier.fillMaxSize(),
                                factory = { ctx ->
                                    WebView(ctx).apply {
                                        settings.javaScriptEnabled = true
                                        settings.domStorageEnabled = true
                                        settings.mediaPlaybackRequiresUserGesture = false
                                        setBackgroundColor(android.graphics.Color.BLACK)
                                        webViewClient = WebViewClient()
                                    }
                                },
                                update = { webView ->
                                    webView.loadDataWithBaseURL(
                                        selectedServer.streamUrl,
                                        playerHtml,
                                        "text/html",
                                        "UTF-8",
                                        null
                                    )
                                }
                            )
                        }

                        "EPICSPORTS_WEB" -> {
                            AndroidView(
                                modifier = Modifier.fillMaxSize(),
                                factory = { ctx ->
                                    WebView(ctx).apply {
                                        settings.javaScriptEnabled = true
                                        settings.domStorageEnabled = true
                                        settings.mediaPlaybackRequiresUserGesture = false
                                        setBackgroundColor(android.graphics.Color.BLACK)
                                        webViewClient = object : WebViewClient() {
                                            override fun shouldOverrideUrlLoading(
                                                view: WebView?,
                                                request: WebResourceRequest?
                                            ): Boolean = false
                                        }
                                        loadUrl(selectedServer.streamUrl)
                                    }
                                },
                                update = { webView ->
                                    if (webView.url != selectedServer.streamUrl) {
                                        webView.loadUrl(selectedServer.streamUrl)
                                    }
                                }
                            )
                        }

                        else -> {
                            // 2D Tactical Radar + Scoreboard Overlay
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val w = size.width
                                val h = size.height
                                val pad = 18.dp.toPx()
                                val lineStroke = Stroke(width = 1.2.dp.toPx())
                                val lineColor = Color.White.copy(alpha = 0.10f)

                                drawRect(
                                    color = lineColor,
                                    topLeft = Offset(pad, pad),
                                    size = Size(w - pad * 2, h - pad * 2),
                                    style = lineStroke
                                )
                                drawLine(
                                    color = lineColor,
                                    start = Offset(w / 2f, pad),
                                    end = Offset(w / 2f, h - pad),
                                    strokeWidth = 1.2.dp.toPx()
                                )
                                drawCircle(
                                    color = lineColor,
                                    radius = h * 0.22f,
                                    center = Offset(w / 2f, h / 2f),
                                    style = lineStroke
                                )

                                val ballX = w * (0.28f + 0.44f * radarPhase)
                                val ballY = h * (0.36f + 0.26f * kotlin.math.sin(radarPhase * 3.14f))
                                drawCircle(
                                    color = VerifiedPillGreen.copy(alpha = 0.24f),
                                    radius = 18.dp.toPx(),
                                    center = Offset(ballX, ballY)
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = 4.dp.toPx(),
                                    center = Offset(ballX, ballY)
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(Color(match.homeColorHex).copy(alpha = 0.22f))
                                            .border(1.5.dp, Color(match.homeColorHex), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = match.homeShort,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = match.homeTeam,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Color.White,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${match.homeScore}  -  ${match.awayScore}",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 28.sp,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${match.minute}' LIVE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = VerifiedPillGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(Color(match.awayColorHex).copy(alpha = 0.22f))
                                            .border(1.5.dp, Color(match.awayColorHex), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = match.awayShort,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = match.awayTeam,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Color.White,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    // Bottom Video Transport Controls Bar (Play/Pause, Audio Mute, Live Match Score Pill, Fullscreen Web Server)
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.86f))
                                )
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = VerifiedPillGreen,
                                shape = CircleShape,
                                modifier = Modifier
                                    .size(30.dp)
                                    .clickable { isVideoPlaying = !isVideoPlaying }
                                    .testTag("video_play_pause_button")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isVideoPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = if (isVideoPlaying) "Pause Stream" else "Play Stream",
                                        tint = Color.Black,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }

                            Surface(
                                color = Color(0xFF23252B),
                                shape = CircleShape,
                                modifier = Modifier
                                    .size(30.dp)
                                    .clickable { isVideoMuted = !isVideoMuted }
                                    .testTag("video_mute_toggle_button")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isVideoMuted) {
                                            Icons.AutoMirrored.Filled.VolumeOff
                                        } else {
                                            Icons.AutoMirrored.Filled.VolumeUp
                                        },
                                        contentDescription = "Toggle Stream Audio",
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = "${match.homeShort} ${match.homeScore}-${match.awayScore} ${match.awayShort} • ${match.minute}' LIVE",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "${selectedServer.providerTag} • ${selectedServer.pingMs}ms",
                                    fontSize = 10.sp,
                                    color = VerifiedPillGreen
                                )
                            }
                        }

                        Surface(
                            color = VerifiedPillGreen.copy(alpha = 0.20f),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, VerifiedPillGreen.copy(alpha = 0.7f)),
                            modifier = Modifier
                                .clickable {
                                    onOpenEpicSportsPortal(
                                        "${match.homeTeam} vs ${match.awayTeam} • ${selectedServer.name}",
                                        selectedServer.streamUrl
                                    )
                                }
                                .testTag("open_epicsports_fullscreen_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                    contentDescription = "Open EpicSports Live Server",
                                    tint = VerifiedPillGreen,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Full Server",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VerifiedPillGreen
                                )
                            }
                        }
                    }
                }

                // Server Choose Options Header + Connected URL Status
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Dns,
                                contentDescription = "Choose Server",
                                tint = VerifiedPillGreen,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "CHOOSE LIVE BROADCAST SERVER",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Text(
                            text = "● ${selectedServer.streamUrl.removePrefix("https://")}",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = VerifiedPillGreen,
                            modifier = Modifier.clickable {
                                onOpenEpicSportsPortal(
                                    "EpicSports Live Server • ${selectedServer.name}",
                                    "https://live.epicsports.in/?m=1"
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Server Choose Options Row (Server 1..5)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.testTag("server_choose_options_row")
                    ) {
                        items(servers, key = { it.id }) { srv ->
                            val active = srv.id == selectedServer.id
                            FilterChip(
                                selected = active,
                                onClick = { onSelectServer(srv) },
                                label = {
                                    Column(modifier = Modifier.padding(vertical = 2.dp)) {
                                        Text(
                                            text = srv.name,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
                                        )
                                        Text(
                                            text = "${srv.resolution} • ${srv.pingMs}ms",
                                            fontSize = 9.sp,
                                            color = if (active) VerifiedPillGreen else OledMutedText
                                        )
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (active) Icons.Default.Videocam else Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = VerifiedPillGreen.copy(alpha = 0.20f),
                                    selectedLabelColor = Color.White,
                                    selectedLeadingIconColor = VerifiedPillGreen,
                                    containerColor = Color(0xFF22242A),
                                    labelColor = OledMutedText
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = active,
                                    borderColor = OledCardBorder,
                                    selectedBorderColor = VerifiedPillGreen
                                ),
                                modifier = Modifier.testTag("football_server_${srv.id}")
                            )
                        }
                    }
                }
            }
        }

        // Floating Green "✓ Live" Badge overlapping top-right corner of the card (Exact IMG_8276 style)
        Surface(
            color = VerifiedPillGreen,
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-12).dp, y = (-2).dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Verified Stream",
                    tint = Color.White,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = selectedServer.resolution,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun PlayerModeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        color = if (selected) VerifiedPillGreen.copy(alpha = 0.22f) else Color(0xFF1C1E24),
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, if (selected) VerifiedPillGreen else OledCardBorder),
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) VerifiedPillGreen else OledMutedText,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

private fun buildLiveBroadcastVideoHtml(
    match: FootballMatch,
    server: StreamServer,
    autoPlay: Boolean,
    muted: Boolean
): String {
    val playAttr = if (autoPlay) "autoplay" else ""
    val muteAttr = if (muted) "muted" else ""
    return """
    <!DOCTYPE html>
    <html>
    <head>
      <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
      <style>
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body, html { width: 100%; height: 100%; background: #05070A; overflow: hidden; font-family: -apple-system, sans-serif; color: #fff; }
        .player-wrap { position: relative; width: 100%; height: 100%; background: radial-gradient(circle at center, #0e2419 0%, #05070a 100%); }
        video { width: 100%; height: 100%; object-fit: cover; opacity: 0.88; }
        .hud-top { position: absolute; top: 10px; left: 12px; right: 12px; display: flex; justify-content: space-between; align-items: center; pointer-events: none; }
        .score-bug { display: flex; align-items: center; background: rgba(6, 7, 9, 0.86); border: 1px solid rgba(255,255,255,0.16); border-radius: 10px; padding: 5px 10px; gap: 8px; }
        .live-dot { width: 8px; height: 8px; border-radius: 50%; background: #10B981; box-shadow: 0 0 8px #10B981; }
        .teams { font-weight: 800; font-size: 13px; letter-spacing: 0.4px; }
        .score { background: #10B981; color: #04120B; font-weight: 900; font-family: monospace; padding: 2px 7px; border-radius: 6px; font-size: 13px; }
        .minute { color: #10B981; font-family: monospace; font-size: 11px; font-weight: 700; }
        .srv-badge { background: rgba(16, 185, 129, 0.18); border: 1px solid #10B981; color: #10B981; font-size: 10px; font-weight: 700; padding: 3px 8px; border-radius: 99px; }
      </style>
    </head>
    <body>
      <div class="player-wrap">
        <video id="v" src="${server.videoStreamUrl}" $playAttr $muteAttr loop playsinline></video>
        <div class="hud-top">
          <div class="score-bug">
            <div class="live-dot"></div>
            <span class="teams">${match.homeShort}</span>
            <span class="score">${match.homeScore} - ${match.awayScore}</span>
            <span class="teams">${match.awayShort}</span>
            <span class="minute">${match.minute}'</span>
          </div>
          <div class="srv-badge">${server.providerTag}</div>
        </div>
      </div>
      <script>
        const v = document.getElementById('v');
        if (${if (autoPlay) "true" else "false"}) {
          v.play().catch(() => {});
        } else {
          v.pause();
        }
      </script>
    </body>
    </html>
    """.trimIndent()
}

@Composable
private fun BroadcastActionPanel(
    match: FootballMatch,
    ttsStatus: String?,
    isTtsLoading: Boolean,
    onSpeakMatchSummary: () -> Unit,
    onStopTts: () -> Unit,
    onTriggerHomeGoal: () -> Unit,
    onTriggerAwayGoal: () -> Unit,
    onTriggerKickoffAlert: () -> Unit
) {
    Surface(
        color = OledCardSurface,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, OledCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.RecordVoiceOver,
                    contentDescription = "Audio & Push Controls",
                    tint = VerifiedPillGreen,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Voice Broadcast (TTS) & Goal Push Alerts",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSpeakMatchSummary,
                    enabled = !isTtsLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VerifiedPillGreen,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("speak_live_commentary_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Speak Live Commentary",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isTtsLoading) "Synthesizing..." else "Voice Broadcast (TTS)",
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                OutlinedButton(
                    onClick = onStopTts,
                    border = BorderStroke(1.dp, OledCardBorder),
                    modifier = Modifier.testTag("stop_tts_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.StopCircle,
                        contentDescription = "Stop Audio",
                        tint = OledMutedText,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            AnimatedVisibility(visible = !ttsStatus.isNullOrBlank()) {
                Text(
                    text = ttsStatus.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = VerifiedPillGreen,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = OledCardBorder)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onTriggerHomeGoal,
                    border = BorderStroke(1.dp, PitchEmerald),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("simulate_home_goal_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = PitchEmerald,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${match.homeShort} Goal",
                        style = MaterialTheme.typography.labelSmall,
                        color = PitchEmerald
                    )
                }

                OutlinedButton(
                    onClick = onTriggerAwayGoal,
                    border = BorderStroke(1.dp, StreamCyan),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("simulate_away_goal_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = StreamCyan,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${match.awayShort} Goal",
                        style = MaterialTheme.typography.labelSmall,
                        color = StreamCyan
                    )
                }

                OutlinedButton(
                    onClick = onTriggerKickoffAlert,
                    border = BorderStroke(1.dp, VerifiedPillGreen),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("simulate_kickoff_alert_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = VerifiedPillGreen,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Kickoff",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            }
        }
    }
}
