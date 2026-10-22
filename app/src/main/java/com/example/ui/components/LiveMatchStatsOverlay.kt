package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.FootballMatch
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LiveCrimson
import kotlin.math.roundToInt

enum class StatOverlayMetric(
    val id: String,
    val title: String,
    val icon: ImageVector
) {
    POSSESSION("possession", "Real-Time Possession %", Icons.Default.PieChart),
    SHOT_ATTEMPTS("shots", "Shot Attempts & On Target", Icons.Default.SportsSoccer),
    CORNER_KICKS("corners", "Corner Kicks & Set Pieces", Icons.Default.Flag),
    EXPECTED_GOALS("xg", "Expected Goals (xG) & Passing", Icons.Default.Timeline)
}

enum class StatTimeframeWindow(val label: String, val scaleFactor: Float) {
    FULL_LIVE("Full Match LIVE", 1.0f),
    FIRST_HALF("1st Half (1'-45')", 0.58f),
    LAST_15_MIN("Last 15m Pressure", 0.32f)
}

/**
 * Full-featured Translucent Glassmorphic Overlay Detail View Dialog for Live Matches.
 * Displays real-time Possession Percentage, Shot Attempts, and Corner Kicks using
 * interactive graphical progress bars with touch inspection, timeframe scrubbing, and live stat triggers.
 */
@Composable
fun LiveMatchStatsOverlayDialog(
    match: FootballMatch,
    onDismiss: () -> Unit,
    onSimulateLiveStat: (statType: String, isHome: Boolean) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = Color(0xFF07090D).copy(alpha = 0.94f),
            shape = RoundedCornerShape(26.dp),
            border = BorderStroke(1.2.dp, VerifiedPillGreen.copy(alpha = 0.65f)),
            shadowElevation = 20.dp,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .testTag("live_match_stats_overlay_dialog")
        ) {
            LiveMatchStatsOverlayContent(
                match = match,
                isCompactInPlayer = false,
                onClose = onDismiss,
                onSimulateLiveStat = onSimulateLiveStat
            )
        }
    }
}

/**
 * Reusable Interactive Live Match Stats Overlay Content (can be rendered either inside the
 * Live Video Broadcast viewport or inside the full-screen Translucent Overlay Dialog).
 */
@Composable
fun LiveMatchStatsOverlayContent(
    match: FootballMatch,
    isCompactInPlayer: Boolean = false,
    onClose: () -> Unit,
    onSimulateLiveStat: (statType: String, isHome: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedMetric by rememberSaveable { mutableStateOf(StatOverlayMetric.POSSESSION) }
    var selectedTimeframe by rememberSaveable { mutableStateOf(StatTimeframeWindow.FULL_LIVE) }
    var scrubbedMinuteRatio by rememberSaveable { mutableFloatStateOf(1f) }
    var inspectedSide by rememberSaveable { mutableStateOf("BOTH") } // "HOME", "AWAY", or "BOTH"

    val effectiveRatio = (selectedTimeframe.scaleFactor * scrubbedMinuteRatio).coerceIn(0.20f, 1.0f)
    val scrubbedMinute = (match.minute * scrubbedMinuteRatio).roundToInt().coerceAtLeast(1)

    // Compute real-time values scaled by interactive scrubber/window
    val homePoss = if (selectedTimeframe == StatTimeframeWindow.LAST_15_MIN) {
        (match.stats.homePossession + 4).coerceIn(20, 80)
    } else {
        match.stats.homePossession
    }
    val awayPoss = (100 - homePoss).coerceIn(20, 80)

    val homeTotalShots = (match.stats.homeTotalShots * effectiveRatio).roundToInt().coerceAtLeast(match.homeScore)
    val awayTotalShots = (match.stats.awayTotalShots * effectiveRatio).roundToInt().coerceAtLeast(match.awayScore)
    val homeShotsOT = (match.stats.homeShotsOnTarget * effectiveRatio).roundToInt().coerceAtLeast(match.homeScore)
    val awayShotsOT = (match.stats.awayShotsOnTarget * effectiveRatio).roundToInt().coerceAtLeast(match.awayScore)

    val homeCorners = (match.stats.homeCorners * effectiveRatio).roundToInt().coerceAtLeast(1)
    val awayCorners = (match.stats.awayCorners * effectiveRatio).roundToInt().coerceAtLeast(1)

    val homeXg = (match.stats.homeXg * effectiveRatio).coerceAtLeast(0.15f)
    val awayXg = (match.stats.awayXg * effectiveRatio).coerceAtLeast(0.12f)

    val homeColor = Color(match.homeColorHex)
    val awayColor = Color(match.awayColorHex)

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0A0E14).copy(alpha = 0.95f),
                        Color(0xFF111620).copy(alpha = 0.95f),
                        Color(0xFF080A0E).copy(alpha = 0.98f)
                    )
                )
            )
            .verticalScroll(scrollState)
            .padding(horizontal = if (isCompactInPlayer) 12.dp else 18.dp, vertical = if (isCompactInPlayer) 10.dp else 16.dp)
            .testTag("live_match_stats_overlay_view"),
        verticalArrangement = Arrangement.spacedBy(if (isCompactInPlayer) 8.dp else 12.dp)
    ) {
        // 1. Top Overlay Header Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = VerifiedPillGreen.copy(alpha = 0.18f),
                    shape = CircleShape,
                    border = BorderStroke(1.dp, VerifiedPillGreen)
                ) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = "Live Telemetry Overlay",
                        tint = VerifiedPillGreen,
                        modifier = Modifier
                            .padding(6.dp)
                            .size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "LIVE MATCH STATS OVERLAY",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = VerifiedPillGreen
                    )
                    Text(
                        text = "${match.competition.displayName} • Minute $scrubbedMinute'/${match.minute}' LIVE",
                        style = MaterialTheme.typography.bodySmall,
                        color = OledMutedText,
                        fontSize = 11.sp
                    )
                }
            }

            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E232B))
                    .testTag("close_stats_overlay_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Stats Overlay",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // 2. Live Match Scoreboard Banner inside Overlay
        Surface(
            color = OledCardSurface,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, OledCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            inspectedSide = if (inspectedSide == "HOME") "BOTH" else "HOME"
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(homeColor.copy(alpha = 0.25f))
                            .border(1.5.dp, homeColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = match.homeShort,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = match.homeTeam,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = match.homeFormation,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            color = VerifiedPillGreen
                        )
                    }
                }

                Surface(
                    color = Color(0xFF07090C),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, VerifiedPillGreen.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "${match.homeScore} - ${match.awayScore}",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            inspectedSide = if (inspectedSide == "AWAY") "BOTH" else "AWAY"
                        }
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = match.awayTeam,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = match.awayFormation,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            color = Color(0xFF38BDF8)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(awayColor.copy(alpha = 0.25f))
                            .border(1.5.dp, awayColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = match.awayShort,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // 3. Interactive Timeframe Chips & Minute Scrubber
        if (!isCompactInPlayer) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatTimeframeWindow.entries.forEach { window ->
                    val isSelected = selectedTimeframe == window
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedTimeframe = window },
                        label = {
                            Text(
                                text = window.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = VerifiedPillGreen.copy(alpha = 0.22f),
                            selectedLabelColor = VerifiedPillGreen,
                            containerColor = OledCardSurface,
                            labelColor = OledMutedText
                        ),
                        modifier = Modifier.testTag("stats_timeframe_${window.name.lowercase()}")
                    )
                }
            }

            // Interactive Timeline Progress Scrubber
            Surface(
                color = OledCardSurface,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, OledCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "INTERACTIVE MATCH MINUTE SCRUBBER",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = OledMutedText
                        )
                        Text(
                            text = "Inspecting at $scrubbedMinute' (Live ${match.minute}')",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = VerifiedPillGreen
                        )
                    }
                    Slider(
                        value = scrubbedMinuteRatio,
                        onValueChange = { scrubbedMinuteRatio = it.coerceIn(0.15f, 1f) },
                        colors = SliderDefaults.colors(
                            thumbColor = VerifiedPillGreen,
                            activeTrackColor = VerifiedPillGreen,
                            inactiveTrackColor = Color(0xFF232832)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(26.dp)
                            .testTag("stats_timeline_scrubber")
                    )
                }
            }
        }

        // 4. Interactive Graphical Progress Bars:
        //    (A) Real-Time Possession Percentage
        InteractiveGraphicalStatBarCard(
            metric = StatOverlayMetric.POSSESSION,
            isSelected = selectedMetric == StatOverlayMetric.POSSESSION,
            homeTeamShort = match.homeShort,
            awayTeamShort = match.awayShort,
            homeValueLabel = "$homePoss%",
            awayValueLabel = "$awayPoss%",
            subtitle = "Ball Control & Zone Dominance • Tap bar to inspect thirds",
            homeFraction = homePoss / 100f,
            homeSecondaryFraction = (homePoss / 100f * 0.68f),
            homeColor = VerifiedPillGreen,
            awayColor = Color(0xFF38BDF8),
            onSelectMetric = { selectedMetric = StatOverlayMetric.POSSESSION },
            onTapSide = { isHome ->
                selectedMetric = StatOverlayMetric.POSSESSION
                onSimulateLiveStat("POSSESSION", isHome)
            },
            testTag = "interactive_bar_possession"
        )

        //    (B) Shot Attempts (Total Shots + Shots On Target Dual-Layer Graphical Progress Bar)
        val totalShotsSum = (homeTotalShots + awayTotalShots).coerceAtLeast(1)
        val homeShotFraction = homeTotalShots.toFloat() / totalShotsSum.toFloat()
        val homeOnTargetRatio = (homeShotsOT.toFloat() / totalShotsSum.toFloat()).coerceIn(0.05f, homeShotFraction)

        InteractiveGraphicalStatBarCard(
            metric = StatOverlayMetric.SHOT_ATTEMPTS,
            isSelected = selectedMetric == StatOverlayMetric.SHOT_ATTEMPTS,
            homeTeamShort = match.homeShort,
            awayTeamShort = match.awayShort,
            homeValueLabel = "$homeTotalShots ($homeShotsOT OT)",
            awayValueLabel = "$awayTotalShots ($awayShotsOT OT)",
            subtitle = "Total Shot Attempts & On-Target Accuracy • Tap bar to add live shot",
            homeFraction = homeShotFraction,
            homeSecondaryFraction = homeOnTargetRatio,
            homeColor = Color(0xFFFACC15),
            awayColor = Color(0xFFF97316),
            onSelectMetric = { selectedMetric = StatOverlayMetric.SHOT_ATTEMPTS },
            onTapSide = { isHome ->
                selectedMetric = StatOverlayMetric.SHOT_ATTEMPTS
                onSimulateLiveStat("SHOT", isHome)
            },
            testTag = "interactive_bar_shots"
        )

        //    (C) Corner Kicks
        val totalCornersSum = (homeCorners + awayCorners).coerceAtLeast(1)
        val homeCornerFraction = homeCorners.toFloat() / totalCornersSum.toFloat()

        InteractiveGraphicalStatBarCard(
            metric = StatOverlayMetric.CORNER_KICKS,
            isSelected = selectedMetric == StatOverlayMetric.CORNER_KICKS,
            homeTeamShort = match.homeShort,
            awayTeamShort = match.awayShort,
            homeValueLabel = "$homeCorners",
            awayValueLabel = "$awayCorners",
            subtitle = "Corner Kicks & Set-Piece Threat • Tap bar to award live corner",
            homeFraction = homeCornerFraction,
            homeSecondaryFraction = homeCornerFraction * 0.75f,
            homeColor = Color(0xFFA855F7),
            awayColor = Color(0xFFEC4899),
            onSelectMetric = { selectedMetric = StatOverlayMetric.CORNER_KICKS },
            onTapSide = { isHome ->
                selectedMetric = StatOverlayMetric.CORNER_KICKS
                onSimulateLiveStat("CORNER", isHome)
            },
            testTag = "interactive_bar_corners"
        )

        //    (D) Expected Goals (xG) & Pass Accuracy
        val totalXgSum = (homeXg + awayXg).coerceAtLeast(0.1f)
        val homeXgFraction = (homeXg / totalXgSum).coerceIn(0.1f, 0.9f)

        InteractiveGraphicalStatBarCard(
            metric = StatOverlayMetric.EXPECTED_GOALS,
            isSelected = selectedMetric == StatOverlayMetric.EXPECTED_GOALS,
            homeTeamShort = match.homeShort,
            awayTeamShort = match.awayShort,
            homeValueLabel = "${String.format("%.2f", homeXg)} xG (${match.stats.homePassAccuracy}%)",
            awayValueLabel = "${String.format("%.2f", awayXg)} xG (${match.stats.awayPassAccuracy}%)",
            subtitle = "Expected Goals (xG) & Pass Completion Rate",
            homeFraction = homeXgFraction,
            homeSecondaryFraction = match.stats.homePassAccuracy / 100f * homeXgFraction,
            homeColor = VerifiedPillGreen,
            awayColor = LiveCrimson,
            onSelectMetric = { selectedMetric = StatOverlayMetric.EXPECTED_GOALS },
            onTapSide = { isHome ->
                selectedMetric = StatOverlayMetric.EXPECTED_GOALS
                onSimulateLiveStat("SHOT", isHome)
            },
            testTag = "interactive_bar_xg"
        )

        // 5. Interactive Selected Metric Deep-Dive Breakdown & Live Simulation Controls
        Surface(
            color = OledCardSurface,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, VerifiedPillGreen.copy(alpha = 0.45f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("stats_overlay_deep_dive_card")
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SELECTED TELEMETRY: ${selectedMetric.title.uppercase()}",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = VerifiedPillGreen
                    )
                    Text(
                        text = "LIVE INTERACTIVE",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 9.sp,
                        color = Color(0xFFFACC15)
                    )
                }

                val deepDiveText = when (selectedMetric) {
                    StatOverlayMetric.POSSESSION -> {
                        val leader = if (homePoss >= awayPoss) match.homeTeam else match.awayTeam
                        val diff = kotlin.math.abs(homePoss - awayPoss)
                        "$leader controls possession with a +$diff% margin ($homePoss% vs $awayPoss%). Final-third entries: ${match.homeShort} ${homePoss / 2} • ${match.awayShort} ${awayPoss / 2}. Pass accuracy: ${match.stats.homePassAccuracy}% vs ${match.stats.awayPassAccuracy}%."
                    }
                    StatOverlayMetric.SHOT_ATTEMPTS -> {
                        val homeConv = if (homeTotalShots > 0) (homeShotsOT * 100) / homeTotalShots else 0
                        val awayConv = if (awayTotalShots > 0) (awayShotsOT * 100) / awayTotalShots else 0
                        "${match.homeShort} has registered $homeTotalShots shot attempts ($homeShotsOT on target, $homeConv% accuracy) vs ${match.awayShort}'s $awayTotalShots attempts ($awayShotsOT on target, $awayConv% accuracy)."
                    }
                    StatOverlayMetric.CORNER_KICKS -> {
                        "${match.homeShort} leads set-piece pressure with $homeCorners corners vs $awayCorners for ${match.awayShort}. Box delivery threat: ${String.format("%.2f", homeCorners * 0.14f)} xG from corners."
                    }
                    StatOverlayMetric.EXPECTED_GOALS -> {
                        "${match.homeShort} xG is ${String.format("%.2f", homeXg)} (${match.homeScore} goals) vs ${match.awayShort} ${String.format("%.2f", awayXg)} xG (${match.awayScore} goals). Attacking momentum index: ${(match.stats.homeMomentum * 100).roundToInt()}%."
                    }
                }

                Text(
                    text = deepDiveText,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,
                    fontSize = 12.sp
                )

                HorizontalDivider(color = OledCardBorder)

                // Quick Interactive Live Event Simulator Buttons (Updates Graphical Progress Bars Instantly)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = { onSimulateLiveStat("SHOT", true) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1A2421),
                            contentColor = VerifiedPillGreen
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("overlay_add_home_shot_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${match.homeShort} Shot",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }

                    Button(
                        onClick = { onSimulateLiveStat("CORNER", true) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF231B2E),
                            contentColor = Color(0xFFD8B4FE)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("overlay_add_home_corner_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${match.homeShort} Corner",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }

                    Button(
                        onClick = { onSimulateLiveStat("SHOT", false) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF19232E),
                            contentColor = Color(0xFF38BDF8)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("overlay_add_away_shot_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${match.awayShort} Shot",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }

                    Button(
                        onClick = { onSimulateLiveStat("CORNER", false) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2B1924),
                            contentColor = Color(0xFFF472B6)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("overlay_add_away_corner_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${match.awayShort} Corner",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

/**
 * Interactive Graphical Dual-Team Progress Bar Card using custom Canvas rendering,
 * spring animation, and left/right touch-zone detection.
 */
@Composable
private fun InteractiveGraphicalStatBarCard(
    metric: StatOverlayMetric,
    isSelected: Boolean,
    homeTeamShort: String,
    awayTeamShort: String,
    homeValueLabel: String,
    awayValueLabel: String,
    subtitle: String,
    homeFraction: Float,
    homeSecondaryFraction: Float,
    homeColor: Color,
    awayColor: Color,
    onSelectMetric: () -> Unit,
    onTapSide: (isHome: Boolean) -> Unit,
    testTag: String
) {
    val animatedHomeFraction by animateFloatAsState(
        targetValue = homeFraction.coerceIn(0.08f, 0.92f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "stat_bar_${metric.id}"
    )

    val animatedSecondaryFraction by animateFloatAsState(
        targetValue = homeSecondaryFraction.coerceIn(0.04f, animatedHomeFraction),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "stat_sec_${metric.id}"
    )

    Surface(
        color = if (isSelected) Color(0xFF171C24) else OledCardSurface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) homeColor else OledCardBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelectMetric() }
            .testTag(testTag)
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp)) {
            // Metric Title + Home/Away Values
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$homeTeamShort  $homeValueLabel",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = homeColor
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = metric.icon,
                        contentDescription = null,
                        tint = if (isSelected) homeColor else OledMutedText,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = metric.title,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Text(
                    text = "$awayValueLabel  $awayTeamShort",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = awayColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Interactive Graphical Canvas Progress Bar (Tap left side for Home, right side for Away)
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .clip(RoundedCornerShape(50))
                    .pointerInput(metric.id) {
                        detectTapGestures { offset ->
                            val isLeftHome = offset.x <= size.width / 2f
                            onTapSide(isLeftHome)
                        }
                    }
                    .testTag("${testTag}_canvas")
            ) {
                val totalW = size.width
                val totalH = size.height
                val gapPx = 4.dp.toPx()
                val splitX = (totalW * animatedHomeFraction).coerceIn(12f, totalW - 12f)

                // Background Track
                drawRoundRect(
                    color = Color(0xFF0B0D12),
                    size = Size(totalW, totalH),
                    cornerRadius = CornerRadius(totalH / 2f, totalH / 2f)
                )

                // Home Graphical Progress Segment
                val homeWidth = (splitX - gapPx / 2f).coerceAtLeast(6f)
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(homeColor.copy(alpha = 0.65f), homeColor)
                    ),
                    topLeft = Offset(0f, 0f),
                    size = Size(homeWidth, totalH),
                    cornerRadius = CornerRadius(totalH / 2f, totalH / 2f)
                )

                // Inner High-Intensity Highlight Bar (e.g., Shots On Target / Final-Third Possession)
                val innerHighlightW = (totalW * animatedSecondaryFraction).coerceAtMost(homeWidth)
                if (innerHighlightW > 6f) {
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.28f),
                        topLeft = Offset(2.dp.toPx(), totalH * 0.25f),
                        size = Size(innerHighlightW - 4.dp.toPx(), totalH * 0.5f),
                        cornerRadius = CornerRadius(totalH / 4f, totalH / 4f)
                    )
                }

                // Away Graphical Progress Segment
                val awayStart = (splitX + gapPx / 2f).coerceAtMost(totalW - 6f)
                val awayWidth = (totalW - awayStart).coerceAtLeast(6f)
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(awayColor, awayColor.copy(alpha = 0.65f))
                    ),
                    topLeft = Offset(awayStart, 0f),
                    size = Size(awayWidth, totalH),
                    cornerRadius = CornerRadius(totalH / 2f, totalH / 2f)
                )

                // Center 50% Reference Tick Marker
                drawLine(
                    color = Color.White.copy(alpha = 0.45f),
                    start = Offset(totalW / 2f, 0f),
                    end = Offset(totalW / 2f, totalH),
                    strokeWidth = 1.5.dp.toPx()
                )

                // Split Divider Glow Dot
                drawCircle(
                    color = Color.White,
                    radius = (totalH * 0.36f),
                    center = Offset(splitX, totalH / 2f)
                )
            }

            Spacer(modifier = Modifier.height(5.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = OledMutedText,
                    fontSize = 10.sp
                )
                Text(
                    text = "${(animatedHomeFraction * 100).roundToInt()}% vs ${(100 - (animatedHomeFraction * 100).roundToInt())}%",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) homeColor else OledMutedText
                )
            }
        }
    }
}
