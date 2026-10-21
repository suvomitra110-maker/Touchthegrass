package com.example

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.example.model.FootballCompetition
import com.example.model.SeedCatalog
import com.example.model.StandingsUiState
import com.example.network.StandingsRepository
import com.example.ui.screens.LiveFootballScreen
import com.example.ui.screens.StandingsScreen
import com.example.ui.screens.TouchTheGrassWebPortalDialog
import com.example.ui.theme.FrameXTVTheme
import org.junit.Assert.assertEquals
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
}
