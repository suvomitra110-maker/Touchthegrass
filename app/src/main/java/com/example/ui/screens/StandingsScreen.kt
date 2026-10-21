package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.model.FootballCompetition
import com.example.model.FootballMatch
import com.example.model.FootballThemeOption
import com.example.model.LeagueStandingsTable
import com.example.model.QualificationZone
import com.example.model.StandingRow
import com.example.model.StandingsUiState
import com.example.ui.components.OledBlack
import com.example.ui.components.OledCardBorder
import com.example.ui.components.OledCardSurface
import com.example.ui.components.OledMutedText
import com.example.ui.components.VerifiedPillGreen
import com.example.ui.theme.JetBrainsMonoFontFamily

@Composable
fun StandingsScreen(
    selectedCompetition: FootballCompetition,
    standingsUiState: StandingsUiState,
    searchQuery: String,
    favoriteClub: String,
    selectedTheme: FootballThemeOption,
    onSelectCompetition: (FootballCompetition) -> Unit,
    onUpdateSearchQuery: (String) -> Unit,
    onRefreshStandings: () -> Unit,
    onOpenWebPortal: (String, String) -> Unit,
    onJumpToLiveMatch: (FootballCompetition) -> Unit
) {
    val activeTable: LeagueStandingsTable = when (standingsUiState) {
        is StandingsUiState.Loading -> standingsUiState.previewTable
        is StandingsUiState.Success -> standingsUiState.table
        is StandingsUiState.Error -> standingsUiState.fallbackTable
    }
    val isRefreshing = standingsUiState is StandingsUiState.Loading
    val themeAccent = Color(selectedTheme.accentHex)

    val filteredRows = remember(activeTable.rows, searchQuery) {
        if (searchQuery.isBlank()) {
            activeTable.rows
        } else {
            val q = searchQuery.trim().lowercase()
            activeTable.rows.filter {
                it.teamName.lowercase().contains(q) || it.shortCode.lowercase().contains(q)
            }
        }
    }

    val leagueOptions = remember {
        listOf(
            FootballCompetition.EPL,
            FootballCompetition.LALIGA,
            FootballCompetition.UCL,
            FootballCompetition.SERIE_A,
            FootballCompetition.BUNDESLIGA,
            FootballCompetition.INTER_MIAMI,
            FootballCompetition.INTL_CUP,
            FootballCompetition.INTL_FRIENDLY
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("standings_screen_list"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Editorial Header + Live Sync & Website Open Actions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "League Standings",
                        fontFamily = FontFamily.Serif,
                        fontSize = 24.sp,
                        color = Color.White
                    )
                    Text(
                        text = activeTable.seasonLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = OledMutedText,
                        fontSize = 12.sp
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = OledCardSurface.copy(alpha = 0.82f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, OledCardBorder),
                        modifier = Modifier
                            .clickable { onRefreshStandings() }
                            .testTag("refresh_standings_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isRefreshing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(13.dp),
                                    strokeWidth = 1.8.dp,
                                    color = VerifiedPillGreen
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh Standings",
                                    tint = VerifiedPillGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (isRefreshing) "Syncing" else "Refresh",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Surface(
                        color = VerifiedPillGreen.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, VerifiedPillGreen.copy(alpha = 0.65f)),
                        modifier = Modifier
                            .clickable {
                                onOpenWebPortal(
                                    "${activeTable.competition.displayName} • Touchthegrass.com",
                                    activeTable.officialWebUrl
                                )
                            }
                            .testTag("open_standings_web_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "Open Web Table",
                                tint = VerifiedPillGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Web Table",
                                style = MaterialTheme.typography.labelSmall,
                                color = VerifiedPillGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 2. All Major Leagues Selector Bar (EPL, LaLiga, UCL, Serie A, Bundesliga, Inter Miami MLS, Intl)
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.testTag("standings_league_selector_row")
            ) {
                items(leagueOptions) { comp ->
                    val selected = comp == activeTable.competition
                    val badgeColor = Color(comp.badgeColorHex)
                    FilterChip(
                        selected = selected,
                        onClick = { onSelectCompetition(comp) },
                        label = {
                            Text(
                                text = comp.shortTag,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (selected) VerifiedPillGreen else badgeColor)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = VerifiedPillGreen.copy(alpha = 0.20f),
                            selectedLabelColor = VerifiedPillGreen,
                            containerColor = OledCardSurface.copy(alpha = 0.78f),
                            labelColor = OledMutedText
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selected,
                            borderColor = OledCardBorder,
                            selectedBorderColor = VerifiedPillGreen
                        ),
                        modifier = Modifier.testTag("standings_league_chip_${comp.code.lowercase()}")
                    )
                }
            }
        }

        // 3. Glass Search Filter + Live Telemetry Status Card
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onUpdateSearchQuery,
                singleLine = true,
                placeholder = {
                    Text(
                        text = stringResource(R.string.search_placeholder_standings),
                        color = OledMutedText,
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Clubs",
                        tint = OledMutedText,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onUpdateSearchQuery("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear Search",
                                tint = OledMutedText,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = OledCardSurface.copy(alpha = 0.76f),
                    unfocusedContainerColor = OledCardSurface.copy(alpha = 0.68f),
                    focusedBorderColor = VerifiedPillGreen,
                    unfocusedBorderColor = OledCardBorder,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("standings_search_input")
            )
        }

        // 4. Compact Glass-Transparent League Table Card (Rank, Club, MP, GD, PTS)
        item {
            Surface(
                color = OledCardSurface.copy(alpha = 0.78f),
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.dp, OledCardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("standings_table_card")
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Table Metadata Top Banner
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(activeTable.competition.badgeColorHex).copy(alpha = 0.16f),
                                        Color.Transparent
                                    )
                                )
                            )
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = activeTable.competition.displayName,
                                tint = Color(activeTable.competition.badgeColorHex),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = activeTable.competition.displayName,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${activeTable.sourceLabel} • Updated ${activeTable.lastUpdatedLabel}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OledMutedText,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Surface(
                            color = VerifiedPillGreen.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, VerifiedPillGreen.copy(alpha = 0.5f)),
                            modifier = Modifier.clickable {
                                onJumpToLiveMatch(activeTable.competition)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Live Match Feed",
                                    tint = VerifiedPillGreen,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "LIVE FEED",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VerifiedPillGreen
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = OledCardBorder, thickness = 0.8.dp)

                    // Compact Column Headers: # | CLUB | MP | GD | PTS
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.03f))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "#",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = OledMutedText,
                            modifier = Modifier.width(28.dp)
                        )
                        Text(
                            text = "CLUB",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = OledMutedText,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "MP",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = OledMutedText,
                            textAlign = TextAlign.End,
                            modifier = Modifier.width(36.dp)
                        )
                        Text(
                            text = "GD",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = OledMutedText,
                            textAlign = TextAlign.End,
                            modifier = Modifier.width(44.dp)
                        )
                        Text(
                            text = "PTS",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.End,
                            modifier = Modifier.width(42.dp)
                        )
                    }

                    HorizontalDivider(color = OledCardBorder, thickness = 0.8.dp)

                    if (filteredRows.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No club matching \"$searchQuery\" in ${activeTable.competition.shortTag}.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = OledMutedText
                            )
                        }
                    } else {
                        filteredRows.forEachIndexed { index, row ->
                            val isFavorite = favoriteClub.isNotBlank() &&
                                (row.teamName.equals(favoriteClub, ignoreCase = true) ||
                                    favoriteClub.contains(row.teamName, ignoreCase = true) ||
                                    row.teamName.contains(favoriteClub, ignoreCase = true))

                            CompactStandingRowItem(
                                row = row,
                                isFavorite = isFavorite,
                                themeAccent = themeAccent,
                                onRowClick = {
                                    if (row.isLivePlaying) {
                                        onJumpToLiveMatch(activeTable.competition)
                                    }
                                }
                            )

                            if (index < filteredRows.lastIndex) {
                                HorizontalDivider(
                                    color = OledCardBorder.copy(alpha = 0.55f),
                                    thickness = 0.6.dp
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = OledCardBorder, thickness = 0.8.dp)

                    // Qualification Zone Legend Footer
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ZoneLegendPill(
                            color = Color(QualificationZone.CHAMPIONS_OR_TITLE.indicatorColorHex),
                            label = if (activeTable.competition == FootballCompetition.INTER_MIAMI) {
                                "MLS Shield / Top 4"
                            } else {
                                "Champions League"
                            }
                        )
                        ZoneLegendPill(
                            color = Color(QualificationZone.CONTINENTAL_PLAYOFF.indicatorColorHex),
                            label = "Playoff / Europa"
                        )
                        ZoneLegendPill(
                            color = Color(QualificationZone.RELEGATION.indicatorColorHex),
                            label = "Relegation"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactStandingRowItem(
    row: StandingRow,
    isFavorite: Boolean,
    themeAccent: Color,
    onRowClick: () -> Unit
) {
    val zoneColor = if (row.qualificationZone != QualificationZone.NONE) {
        Color(row.qualificationZone.indicatorColorHex)
    } else {
        Color.Transparent
    }

    val rowBg = when {
        isFavorite -> themeAccent.copy(alpha = 0.12f)
        row.isLivePlaying -> VerifiedPillGreen.copy(alpha = 0.07f)
        else -> Color.Transparent
    }

    val gdText = if (row.goalDifference > 0) "+${row.goalDifference}" else "${row.goalDifference}"
    val gdColor = when {
        row.goalDifference > 0 -> VerifiedPillGreen
        row.goalDifference < 0 -> Color(0xFFF87171)
        else -> OledMutedText
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(rowBg)
            .clickable(onClick = onRowClick)
            .padding(end = 14.dp)
            .testTag("standing_row_${row.shortCode.lowercase()}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Qualification Zone Left Accent Bar
        Box(
            modifier = Modifier
                .width(3.5.dp)
                .height(48.dp)
                .background(zoneColor)
        )

        Spacer(modifier = Modifier.width(10.5.dp))

        // Rank #
        Text(
            text = "${row.rank}",
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 12.sp,
            fontWeight = if (row.rank <= 4) FontWeight.Bold else FontWeight.Medium,
            color = if (row.rank <= 4) Color.White else OledMutedText,
            modifier = Modifier.width(28.dp)
        )

        // Club Badge + Name + Favorite / Live Pill
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color(row.badgeColorHex).copy(alpha = 0.22f),
                shape = CircleShape,
                border = BorderStroke(1.dp, Color(row.badgeColorHex).copy(alpha = 0.85f)),
                modifier = Modifier.size(26.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = row.shortCode.take(3),
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.width(9.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = row.teamName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isFavorite || row.rank <= 4) FontWeight.Bold else FontWeight.Medium,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 13.sp
                    )
                    if (isFavorite) {
                        Spacer(modifier = Modifier.width(5.dp))
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Favorite Club",
                            tint = Color(0xFFFACC15),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                AnimatedVisibility(visible = row.isLivePlaying && !row.liveScoreSummary.isNullOrBlank()) {
                    Text(
                        text = "● LIVE: ${row.liveScoreSummary}",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = VerifiedPillGreen
                    )
                }
            }
        }

        // MP (Matches Played)
        Text(
            text = "${row.matchesPlayed}",
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 12.sp,
            color = OledMutedText,
            textAlign = TextAlign.End,
            modifier = Modifier.width(36.dp)
        )

        // GD (Goal Difference)
        Text(
            text = gdText,
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = gdColor,
            textAlign = TextAlign.End,
            modifier = Modifier.width(44.dp)
        )

        // PTS (Points)
        Text(
            text = "${row.points}",
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            textAlign = TextAlign.End,
            modifier = Modifier.width(42.dp)
        )
    }
}

@Composable
private fun ZoneLegendPill(
    color: Color,
    label: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = OledMutedText,
            fontSize = 10.sp
        )
    }
}

/**
 * Interactive Touchthegrass.com Web Portal Modal that renders both an interactive
 * live web view and safe external browser launcher so the website is always openable.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun TouchTheGrassWebPortalDialog(
    title: String,
    url: String,
    matches: List<FootballMatch>,
    onDismiss: () -> Unit,
    onSelectLiveMatchInApp: (String) -> Unit
) {
    val context = LocalContext.current
    val isTouchTheGrassRoot = url.contains("touchthegrass.com", ignoreCase = true)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = OledBlack,
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, OledCardBorder),
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.90f)
                .testTag("touchthegrass_web_portal_dialog")
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Browser Address Bar Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(OledCardSurface)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "HTTPS Verified",
                            tint = VerifiedPillGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = url,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                color = VerifiedPillGreen,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            runCatching {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VerifiedPillGreen,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(50),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("open_external_browser_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = "External Browser",
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Browser",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("close_web_portal_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Web Portal",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                HorizontalDivider(color = OledCardBorder)

                // Embedded WebView rendering the live Touchthegrass.com HTML5 Portal or External Official League URL
                Box(modifier = Modifier.weight(1f)) {
                    val htmlPortal = remember(matches) {
                        buildTouchTheGrassPortalHtml(matches)
                    }

                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            WebView(ctx).apply {
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                webViewClient = object : WebViewClient() {
                                    override fun shouldOverrideUrlLoading(
                                        view: WebView?,
                                        request: WebResourceRequest?
                                    ): Boolean = false
                                }
                                if (isTouchTheGrassRoot) {
                                    loadDataWithBaseURL(
                                        "https://touchthegrass.com",
                                        htmlPortal,
                                        "text/html",
                                        "UTF-8",
                                        null
                                    )
                                } else {
                                    loadUrl(url)
                                }
                            }
                        }
                    )
                }

                // Quick Jump Bar for Live Streams inside Touchthegrass.com
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(OledCardSurface)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Touchthegrass.com • 4K Live Match Streams Ready",
                        style = MaterialTheme.typography.labelSmall,
                        color = OledMutedText
                    )
                    Text(
                        text = "Watch in App →",
                        style = MaterialTheme.typography.labelSmall,
                        color = VerifiedPillGreen,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            matches.firstOrNull()?.let { onSelectLiveMatchInApp(it.id) }
                            onDismiss()
                        }
                    )
                }
            }
        }
    }
}

private fun buildTouchTheGrassPortalHtml(matches: List<FootballMatch>): String {
    val matchCardsHtml = matches.joinToString("\n") { m ->
        """
        <div class="card">
          <div class="badge-row">
            <span class="comp">${m.competition.shortTag} • ${m.stageLabel}</span>
            <span class="live">● LIVE ${m.minute}'</span>
          </div>
          <div class="score-row">
            <span class="team">${m.homeTeam}</span>
            <span class="score">${m.homeScore} - ${m.awayScore}</span>
            <span class="team">${m.awayTeam}</span>
          </div>
          <div class="meta">Venue: ${m.venue} • Server: FrameTV 4K / EpicSports HD</div>
        </div>
        """.trimIndent()
    }

    return """
    <!DOCTYPE html>
    <html>
    <head>
      <meta name="viewport" content="width=device-width, initial-scale=1.0">
      <style>
        body { background: #060709; color: #FFFFFF; font-family: -apple-system, BlinkMacSystemFont, sans-serif; margin: 0; padding: 18px; }
        .hero { border: 1px solid #26282E; background: #17181C; border-radius: 18px; padding: 18px; margin-bottom: 16px; }
        h1 { font-family: Georgia, serif; font-weight: normal; font-size: 26px; margin: 0 0 6px 0; }
        .sub { color: #8A8F99; font-size: 13px; margin-bottom: 12px; }
        .pill { display: inline-block; background: rgba(16,185,129,0.18); color: #10B981; border: 1px solid #10B981; border-radius: 99px; padding: 4px 12px; font-size: 11px; font-weight: bold; }
        .card { background: #17181C; border: 1px solid #26282E; border-radius: 16px; padding: 14px; margin-bottom: 12px; }
        .badge-row { display: flex; justify-content: space-between; font-size: 11px; margin-bottom: 10px; }
        .comp { color: #38BDF8; font-weight: bold; }
        .live { color: #10B981; font-weight: bold; }
        .score-row { display: flex; justify-content: space-between; align-items: center; font-size: 16px; font-weight: bold; margin-bottom: 8px; }
        .score { background: #060709; padding: 4px 12px; border-radius: 10px; border: 1px solid #26282E; color: #10B981; font-family: monospace; }
        .meta { color: #8A8F99; font-size: 11px; }
      </style>
    </head>
    <body>
      <div class="hero">
        <span class="pill">✓ VERIFIED STREAMING PORTAL</span>
        <h1>Touchthegrass.com</h1>
        <div class="sub">Official Clean Web Hub • FrameTV 4K & EpicSports Live Football</div>
      </div>
      $matchCardsHtml
    </body>
    </html>
    """.trimIndent()
}
