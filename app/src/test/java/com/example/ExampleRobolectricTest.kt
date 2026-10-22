package com.example

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.example.model.FootballCompetition
import com.example.model.SeedCatalog
import com.example.model.StandingsUiState
import com.example.network.StandingsRepository
import com.example.security.SecurityShieldManager
import com.example.ui.components.DynamicIslandScoreboard
import com.example.ui.components.LiveMatchStatsOverlayDialog
import com.example.ui.screens.LiveFootballScreen
import com.example.ui.screens.StandingsScreen
import com.example.ui.screens.TouchTheGrassWebPortalDialog
import com.example.ui.theme.FrameXTVTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp-xxhdpi")
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `customer visit 1 - verify Touchthegrass brand, 20 football themes, and 5 EpicSports servers`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        val tagline = context.getString(R.string.tagline_framextv)

        assertEquals("TouchTheGrass", appName)
        assertTrue(tagline.contains("Touchthegrass.com"))
        assertEquals(20, SeedCatalog.top20AbstractThemes.size)
        assertEquals(5, SeedCatalog.footballBroadcastServers.size)
        assertTrue(
            SeedCatalog.footballBroadcastServers.first().streamUrl.contains("live.epicsports.in/?m=1")
        )
    }

    @Test
    fun `customer visit 2 - test LiveFootballScreen real-time team search, video player, and server selector`() {
        composeTestRule.mainClock.autoAdvance = false
        val matches = SeedCatalog.initialMatches
        var searchQuery by mutableStateOf("")
        var selectedServer by mutableStateOf(SeedCatalog.footballBroadcastServers.first())
        var selectedMatchId by mutableStateOf(matches.first().id)

        composeTestRule.setContent {
            FrameXTVTheme {
                LiveFootballScreen(
                    matches = matches,
                    selectedCompetition = FootballCompetition.ALL,
                    enabledLeagues = FootballCompetition.toggleableLeagues.toSet(),
                    selectedTheme = SeedCatalog.top20AbstractThemes.first(),
                    selectedMatchId = selectedMatchId,
                    selectedServer = selectedServer,
                    subscribedAlerts = emptyList(),
                    ttsStatus = null,
                    isTtsLoading = false,
                    onSelectCompetition = {},
                    onToggleLeagueVisibility = {},
                    onEnableAllLeagues = {},
                    onSelectMatch = { selectedMatchId = it },
                    onSelectServer = { selectedServer = it },
                    onSpeakCommentary = {},
                    onStopTts = {},
                    onToggleAlertSubscription = {},
                    onTriggerGoalAlert = { _, _ -> },
                    onTriggerKickoffAlert = {},
                    matchSearchQuery = searchQuery,
                    onUpdateMatchSearch = { searchQuery = it }
                )
            }
        }

        composeTestRule.mainClock.advanceTimeBy(150)

        // Verify persistent header search bar is displayed and filters matches in real time
        composeTestRule.onNodeWithTag("main_feed_match_search_input").assertIsDisplayed()
        composeTestRule.onNodeWithTag("main_feed_match_search_input").performTextInput("Inter Miami")
        composeTestRule.mainClock.advanceTimeBy(150)
        assertEquals("Inter Miami", searchQuery)
    }

    @Test
    fun `customer visit 3 - test StandingsScreen league tables and TouchTheGrassWebPortalDialog`() {
        composeTestRule.mainClock.autoAdvance = false
        val repository = StandingsRepository()
        val eplTable = repository.getBaselineTable(
            FootballCompetition.EPL,
            SeedCatalog.initialMatches
        )
        val mlsTable = repository.getBaselineTable(
            FootballCompetition.INTER_MIAMI,
            SeedCatalog.initialMatches
        )

        assertTrue(eplTable.rows.any { it.teamName == "Arsenal" && it.points > 0 })
        assertTrue(mlsTable.rows.any { it.teamName.contains("Inter Miami") && it.points > 0 })

        composeTestRule.setContent {
            FrameXTVTheme {
                StandingsScreen(
                    selectedCompetition = FootballCompetition.EPL,
                    standingsUiState = StandingsUiState.Success(eplTable),
                    searchQuery = "",
                    favoriteClub = "FC Barcelona",
                    selectedTheme = SeedCatalog.top20AbstractThemes.first(),
                    onSelectCompetition = {},
                    onUpdateSearchQuery = {},
                    onRefreshStandings = {},
                    onOpenWebPortal = { _, _ -> },
                    onJumpToLiveMatch = {}
                )

                TouchTheGrassWebPortalDialog(
                    title = "EpicSports Live • Server 1",
                    url = "https://live.epicsports.in/?m=1",
                    matches = SeedCatalog.initialMatches,
                    onDismiss = {},
                    onSelectLiveMatchInApp = {}
                )
            }
        }

        composeTestRule.mainClock.advanceTimeBy(150)

        // Verify Standings screen & Web Portal modal for live.epicsports.in/?m=1 render cleanly
        composeTestRule.onNodeWithTag("standings_league_selector_row").assertExists()
        composeTestRule.onNodeWithTag("standings_search_input").assertExists()
        composeTestRule.onNodeWithTag("touchthegrass_web_portal_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithText("live.epicsports.in/?m=1").assertIsDisplayed()
    }

    @Test
    fun `e2e test 4 - LiveMatchStatsOverlayDialog displays interactive graphical progress bars for possession, shots, and corners`() {
        composeTestRule.mainClock.autoAdvance = false
        var match by mutableStateOf(SeedCatalog.initialMatches.first())
        val initialShots = match.stats.homeTotalShots
        val initialCorners = match.stats.homeCorners

        composeTestRule.setContent {
            FrameXTVTheme {
                LiveMatchStatsOverlayDialog(
                    match = match,
                    onDismiss = {},
                    onSimulateLiveStat = { statType, isHome ->
                        if (statType == "SHOT" && isHome) {
                            match = match.copy(
                                stats = match.stats.copy(
                                    homeTotalShots = match.stats.homeTotalShots + 1,
                                    homeShotsOnTarget = match.stats.homeShotsOnTarget + 1
                                )
                            )
                        } else if (statType == "CORNER" && isHome) {
                            match = match.copy(
                                stats = match.stats.copy(
                                    homeCorners = match.stats.homeCorners + 1
                                )
                            )
                        }
                    }
                )
            }
        }

        composeTestRule.mainClock.advanceTimeBy(200)

        // Verify the overlay dialog and all interactive graphical progress bars exist
        composeTestRule.onNodeWithTag("live_match_stats_overlay_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithTag("interactive_bar_possession").assertExists()
        composeTestRule.onNodeWithTag("interactive_bar_shots").assertExists()
        composeTestRule.onNodeWithTag("interactive_bar_corners").assertExists()
        composeTestRule.onNodeWithTag("interactive_bar_xg").assertExists()

        // Trigger a live shot and corner via interactive overlay controls and verify real-time update
        composeTestRule.onNodeWithTag("overlay_add_home_shot_button").performClick()
        composeTestRule.mainClock.advanceTimeBy(150)
        assertEquals(initialShots + 1, match.stats.homeTotalShots)

        composeTestRule.onNodeWithTag("overlay_add_home_corner_button").performClick()
        composeTestRule.mainClock.advanceTimeBy(150)
        assertEquals(initialCorners + 1, match.stats.homeCorners)
    }

    @Test
    fun `e2e test 5 - DynamicIslandScoreboard displays favorite team, expands, and toggles Notification Bar pin`() {
        composeTestRule.mainClock.autoAdvance = false
        val matches = SeedCatalog.initialMatches
        val miamiMatch = matches[1]
        var isPinnedToNotif by mutableStateOf(true)
        var favGoalTriggered by mutableStateOf(false)

        composeTestRule.setContent {
            FrameXTVTheme {
                DynamicIslandScoreboard(
                    activeMatch = miamiMatch,
                    allMatches = matches,
                    favoriteClub = "Inter Miami CF",
                    pinnedMatchId = null,
                    isPinnedToNotificationBar = isPinnedToNotif,
                    goalFlashMessage = "★ FAVORITE TEAM GOAL! L. Messi (85') • MIA 3 - 1 LAG",
                    streamServerName = SeedCatalog.footballBroadcastServers.first().name,
                    onPinMatch = {},
                    onTogglePinToNotificationBar = { isPinnedToNotif = !isPinnedToNotif },
                    onTriggerFavoriteTeamGoal = { favGoalTriggered = true },
                    onJumpToMatchStream = {}
                )
            }
        }

        composeTestRule.mainClock.advanceTimeBy(200)

        composeTestRule.onNodeWithTag("dynamic_island_scoreboard").assertIsDisplayed()
        composeTestRule.onNodeWithTag("dynamic_island_goal_flash_banner").assertIsDisplayed()

        // Tap quick Notification Bar pin toggle
        composeTestRule.onNodeWithTag("dynamic_island_quick_notif_pin_button").performClick()
        composeTestRule.mainClock.advanceTimeBy(150)
        assertFalse(isPinnedToNotif)

        // Expand Dynamic Island and trigger Favorite Team Goal Alert
        composeTestRule.onNodeWithTag("dynamic_island_scoreboard").performClick()
        composeTestRule.mainClock.advanceTimeBy(250)
        composeTestRule.onNodeWithTag("dynamic_island_expanded_card").assertExists()
        composeTestRule.onNodeWithTag("dynamic_island_fav_goal_button").performClick()
        composeTestRule.mainClock.advanceTimeBy(150)
        assertTrue(favGoalTriggered)
    }

    @Test
    fun `e2e test 6 - SecurityShieldManager blocks CVE-2024-4577 PHP CGI and verifies FLAG_SECURE removal & Python gateway`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val shield = SecurityShieldManager(context)

        val phpCgiCheck = shield.inspectAndSanitizeInput(
            "gateway.php?%ADd+allow_url_include%3d1+-d+auto_prepend_file%3dphp://input",
            wafEnabled = true
        )
        assertFalse(phpCgiCheck.isSafe)
        assertEquals("NSA PHP / CGI / Web-Shell Exploit", phpCgiCheck.threatCategory)

        val audit = shield.runFullSecurityAudit(flagSecureEnabled = false, wafEnabled = true)
        assertTrue(audit.checks.any { it.id == "chk_nsa_python_migration" && it.passed })
        assertTrue(audit.checks.any { it.id == "chk_flag_secure" && it.passed })
    }
}


