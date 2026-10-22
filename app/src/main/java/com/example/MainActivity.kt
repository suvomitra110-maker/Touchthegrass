package com.example

import android.Manifest
import android.app.Activity
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notifications.MatchNotificationHelper
import com.example.ui.components.DynamicIslandScoreboard
import com.example.ui.components.MessiAndAbstractBackground
import com.example.ui.components.OledCardBorder
import com.example.ui.components.OledCardSurface
import com.example.ui.components.OledMutedText
import com.example.ui.components.TouchTheGrassMenuDialog
import com.example.ui.components.TouchTheGrassProfileAndSettingsScreen
import com.example.ui.components.UserAvatarCircle
import com.example.ui.components.VerifiedPillGreen
import com.example.ui.components.attemptAutoSignIn
import com.example.ui.screens.AiScoutAndVisionScreen
import com.example.ui.screens.AlertsAndSavedScreen
import com.example.ui.screens.CinemaHubScreen
import com.example.ui.screens.LiveFootballScreen
import com.example.ui.screens.SecurityCenterScreen
import com.example.ui.screens.StandingsScreen
import com.example.ui.screens.TouchTheGrassWebPortalDialog
import com.example.ui.theme.FrameXTVTheme
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.FrameXViewModel

enum class FrameXTab(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector,
    val testTag: String
) {
    LIVE_FOOTBALL("live_football", R.string.nav_live_football, Icons.Outlined.Home, "nav_tab_football"),
    STANDINGS("standings", R.string.nav_standings, Icons.Outlined.EmojiEvents, "nav_tab_standings"),
    CINEMA("cinema", R.string.nav_cinema, Icons.Outlined.Movie, "nav_tab_cinema"),
    AI_HUB("ai_hub", R.string.nav_ai_hub, Icons.Outlined.AutoAwesome, "nav_tab_ai"),
    ALERTS("alerts", R.string.nav_alerts, Icons.Outlined.Notifications, "nav_tab_alerts"),
    PROFILE("profile", R.string.nav_security, Icons.Outlined.Person, "nav_tab_profile")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Ensure FLAG_SECURE display block is removed so live match streams are visible on all devices
        window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        enableEdgeToEdge()
        setContent {
            FrameXTVTheme {
                FrameXTVApp()
            }
        }
    }
}

@Composable
fun FrameXTVApp(
    viewModel: FrameXViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    var currentTab by rememberSaveable { mutableStateOf(FrameXTab.LIVE_FOOTBALL) }
    var isUpperRightMenuOpen by rememberSaveable { mutableStateOf(false) }
    var menuInitialSettings by rememberSaveable { mutableStateOf(false) }
    var showSecurityCyberScreen by rememberSaveable { mutableStateOf(false) }
    var isWebPortalOpen by rememberSaveable { mutableStateOf(false) }
    var webPortalTitle by rememberSaveable { mutableStateOf("Touchthegrass.com • Official Live Hub") }
    var webPortalUrl by rememberSaveable { mutableStateOf("https://touchthegrass.com") }

    LaunchedEffect(Unit) {
        attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = { uid, displayName, email, photoUrl ->
                viewModel.onGoogleUserAuthenticated(uid, displayName, email, photoUrl)
            },
            onUnauthenticated = { },
            scope = coroutineScope
        )
    }

    var hasNotifPermission by remember {
        mutableStateOf(MatchNotificationHelper.hasNotificationPermission(context))
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotifPermission = granted
    }

    val ensureNotificationPermissionAndRun: (() -> Unit) -> Unit = { action ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !MatchNotificationHelper.hasNotificationPermission(context)
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        action()
    }

    BackHandler(enabled = showSecurityCyberScreen || currentTab != FrameXTab.LIVE_FOOTBALL) {
        if (showSecurityCyberScreen) {
            showSecurityCyberScreen = false
        } else {
            currentTab = FrameXTab.LIVE_FOOTBALL
        }
    }

    // Collect User & Theme States
    val isGoogleSignedIn by viewModel.isGoogleSignedIn.collectAsStateWithLifecycle()
    val userEmail by viewModel.userEmail.collectAsStateWithLifecycle()
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val favoriteClub by viewModel.favoriteClub.collectAsStateWithLifecycle()
    val favoritedTeams by viewModel.favoritedTeams.collectAsStateWithLifecycle()
    val fcmToken by viewModel.fcmToken.collectAsStateWithLifecycle()
    val fcmSubscribedTopics by viewModel.fcmSubscribedTopics.collectAsStateWithLifecycle()
    val profilePictureId by viewModel.profilePictureId.collectAsStateWithLifecycle()
    val customAvatarBitmap by viewModel.customAvatarBitmap.collectAsStateWithLifecycle()
    val selectedTheme by viewModel.selectedTheme.collectAsStateWithLifecycle()
    val enabledLeagues by viewModel.enabledLeagues.collectAsStateWithLifecycle()

    // Collect Standings States
    val selectedStandingsLeague by viewModel.selectedStandingsLeague.collectAsStateWithLifecycle()
    val standingsSearchQuery by viewModel.standingsSearchQuery.collectAsStateWithLifecycle()
    val standingsUiState by viewModel.standingsUiState.collectAsStateWithLifecycle()

    // Collect Security & App States
    val flagSecureEnabled by viewModel.flagSecureEnabled.collectAsStateWithLifecycle()
    val wafFirewallEnabled by viewModel.wafFirewallEnabled.collectAsStateWithLifecycle()
    val securityAuditReport by viewModel.securityAuditReport.collectAsStateWithLifecycle()
    val activeStreamToken by viewModel.activeStreamToken.collectAsStateWithLifecycle()
    val customWafTestInput by viewModel.customWafTestInput.collectAsStateWithLifecycle()
    val securityAuditLogs by viewModel.securityAuditLogs.collectAsStateWithLifecycle()

    LaunchedEffect(flagSecureEnabled) {
        val activity = context as? Activity ?: return@LaunchedEffect
        // Always keep FLAG_SECURE cleared so live match streams are visible on all devices, external displays, and emulators
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }

    val matches by viewModel.matches.collectAsStateWithLifecycle()
    val selectedCompetition by viewModel.selectedCompetition.collectAsStateWithLifecycle()
    val matchSearchQuery by viewModel.matchSearchQuery.collectAsStateWithLifecycle()
    val selectedMatchId by viewModel.selectedMatchId.collectAsStateWithLifecycle()
    val selectedMatchServer by viewModel.selectedMatchServer.collectAsStateWithLifecycle()
    val pinnedMatchId by viewModel.pinnedMatchId.collectAsStateWithLifecycle()
    val isPinnedToNotificationBar by viewModel.isPinnedToNotificationBar.collectAsStateWithLifecycle()
    val dynamicIslandGoalFlash by viewModel.dynamicIslandGoalFlash.collectAsStateWithLifecycle()
    val subscribedAlerts by viewModel.matchAlerts.collectAsStateWithLifecycle()
    val notificationLogs by viewModel.notificationLogs.collectAsStateWithLifecycle()
    val watchlist by viewModel.watchlist.collectAsStateWithLifecycle()
    val inAppBanner by viewModel.inAppAlertBanner.collectAsStateWithLifecycle()

    val activeIslandMatch = remember(matches, pinnedMatchId, favoriteClub) {
        if (pinnedMatchId != null) {
            matches.find { it.id == pinnedMatchId }
        } else {
            null
        } ?: matches.find { match ->
            match.homeTeam.contains(favoriteClub, ignoreCase = true) ||
                match.awayTeam.contains(favoriteClub, ignoreCase = true) ||
                favoriteClub.contains(match.homeTeam, ignoreCase = true) ||
                favoriteClub.contains(match.awayTeam, ignoreCase = true)
        } ?: matches.first()
    }

    val selectedMediaCategory by viewModel.selectedMediaCategory.collectAsStateWithLifecycle()
    val cinemaSearchQuery by viewModel.cinemaSearchQuery.collectAsStateWithLifecycle()
    val selectedMedia by viewModel.selectedMedia.collectAsStateWithLifecycle()
    val selectedMediaServer by viewModel.selectedMediaServer.collectAsStateWithLifecycle()
    val isMediaPlaying by viewModel.isMediaPlaying.collectAsStateWithLifecycle()

    val ttsStatus by viewModel.ttsStatus.collectAsStateWithLifecycle()
    val isTtsLoading by viewModel.isTtsLoading.collectAsStateWithLifecycle()
    val selectedTtsVoice by viewModel.selectedTtsVoice.collectAsStateWithLifecycle()

    val searchGroundingQuery by viewModel.searchGroundingQuery.collectAsStateWithLifecycle()
    val isSearchingGrounding by viewModel.isSearchingGrounding.collectAsStateWithLifecycle()
    val searchGroundingResult by viewModel.searchGroundingResult.collectAsStateWithLifecycle()

    val selectedVisionBitmap by viewModel.selectedVisionBitmap.collectAsStateWithLifecycle()
    val visionPrompt by viewModel.visionPrompt.collectAsStateWithLifecycle()
    val isAnalyzingImage by viewModel.isAnalyzingImage.collectAsStateWithLifecycle()
    val visionAnalysisResult by viewModel.visionAnalysisResult.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        MessiAndAbstractBackground(
            theme = selectedTheme,
            modifier = Modifier.fillMaxSize()
        )

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                if (currentTab != FrameXTab.PROFILE || showSecurityCyberScreen) {
                    TouchTheGrassTopBar(
                        userName = userName,
                        favoriteClub = favoriteClub,
                        profilePictureId = profilePictureId,
                        customAvatarBitmap = customAvatarBitmap,
                        onOpenWebPortal = {
                            webPortalTitle = "Touchthegrass.com • Official Live Hub"
                            webPortalUrl = "https://touchthegrass.com"
                            isWebPortalOpen = true
                        },
                        onOpenProfileModal = {
                            menuInitialSettings = false
                            isUpperRightMenuOpen = true
                        },
                        onOpenSettingsModal = {
                            menuInitialSettings = true
                            isUpperRightMenuOpen = true
                        }
                    )
                }
            },
            bottomBar = {
                NavigationBar(
                    containerColor = Color(0xFF0E1014),
                    tonalElevation = 0.dp,
                    modifier = Modifier.border(
                        width = 0.8.dp,
                        color = OledCardBorder
                    )
                ) {
                    FrameXTab.entries.forEach { tab ->
                        val selected = tab == currentTab && !showSecurityCyberScreen
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                showSecurityCyberScreen = false
                                currentTab = tab
                            },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = stringResource(tab.labelRes),
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF38BDF8),
                                indicatorColor = Color.Transparent,
                                unselectedIconColor = OledMutedText
                            ),
                            modifier = Modifier.testTag(tab.testTag)
                        )
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                DynamicIslandScoreboard(
                    activeMatch = activeIslandMatch,
                    allMatches = matches,
                    favoriteClub = favoriteClub,
                    pinnedMatchId = pinnedMatchId,
                    isPinnedToNotificationBar = isPinnedToNotificationBar,
                    goalFlashMessage = dynamicIslandGoalFlash,
                    streamServerName = selectedMatchServer.name,
                    onPinMatch = { matchId ->
                        ensureNotificationPermissionAndRun {
                            viewModel.pinMatchToScoreboard(matchId, alsoPinNotificationBar = true)
                        }
                    },
                    onTogglePinToNotificationBar = {
                        ensureNotificationPermissionAndRun {
                            viewModel.togglePinScoreboardToNotificationBar()
                        }
                    },
                    onTriggerFavoriteTeamGoal = {
                        ensureNotificationPermissionAndRun {
                            viewModel.triggerFavoriteClubFcmPush(isGoal = true)
                        }
                    },
                    onJumpToMatchStream = { matchId ->
                        showSecurityCyberScreen = false
                        viewModel.selectMatch(matchId)
                        currentTab = FrameXTab.LIVE_FOOTBALL
                    }
                )

                AnimatedVisibility(visible = inAppBanner != null) {
                    Surface(
                        color = OledCardSurface,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp)
                            .border(1.dp, VerifiedPillGreen, RoundedCornerShape(14.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = inAppBanner.orEmpty(),
                                style = MaterialTheme.typography.labelLarge,
                                color = VerifiedPillGreen,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { viewModel.dismissBanner() },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss Banner",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    if (showSecurityCyberScreen) {
                        SecurityCenterScreen(
                            auditReport = securityAuditReport,
                            activeToken = activeStreamToken,
                            flagSecureEnabled = flagSecureEnabled,
                            wafEnabled = wafFirewallEnabled,
                            customWafInput = customWafTestInput,
                            securityLogs = securityAuditLogs,
                            onToggleFlagSecure = viewModel::toggleFlagSecure,
                            onToggleWaf = viewModel::toggleWafFirewall,
                            onRunDeepAudit = viewModel::runDeepSecurityAudit,
                            onRotateStreamToken = { viewModel.rotateStreamSessionToken() },
                            onUpdateWafInput = viewModel::updateCustomWafTestInput,
                            onTestWafPayload = viewModel::testWafAttackPayload,
                            onClearSecurityLogs = viewModel::clearSecurityAuditLogs
                        )
                    } else {
                        when (currentTab) {
                            FrameXTab.LIVE_FOOTBALL -> {
                                LiveFootballScreen(
                                    matches = matches,
                                    selectedCompetition = selectedCompetition,
                                    enabledLeagues = enabledLeagues,
                                    selectedTheme = selectedTheme,
                                    selectedMatchId = selectedMatchId,
                                    selectedServer = selectedMatchServer,
                                    subscribedAlerts = subscribedAlerts,
                                    ttsStatus = ttsStatus,
                                    isTtsLoading = isTtsLoading,
                                    onSelectCompetition = viewModel::selectCompetition,
                                    onToggleLeagueVisibility = viewModel::toggleLeagueVisibility,
                                    onEnableAllLeagues = viewModel::enableAllLeagues,
                                    onSelectMatch = viewModel::selectMatch,
                                    onSelectServer = viewModel::selectMatchServer,
                                    onSpeakCommentary = viewModel::speakTextWithGeminiTts,
                                    onStopTts = viewModel::stopTtsAudio,
                                    onToggleAlertSubscription = { match ->
                                        ensureNotificationPermissionAndRun {
                                            viewModel.toggleMatchAlertSubscription(match)
                                        }
                                    },
                                    onTriggerGoalAlert = { match, isHome ->
                                        ensureNotificationPermissionAndRun {
                                            viewModel.triggerLiveMajorGoalEvent(match, isHome)
                                        }
                                    },
                                    onTriggerKickoffAlert = { match ->
                                        ensureNotificationPermissionAndRun {
                                            viewModel.triggerMatchKickoffAlert(match)
                                        }
                                    },
                                    matchSearchQuery = matchSearchQuery,
                                    onUpdateMatchSearch = viewModel::updateMatchSearchQuery,
                                    favoriteClub = favoriteClub,
                                    pinnedMatchId = pinnedMatchId,
                                    isPinnedToNotificationBar = isPinnedToNotificationBar,
                                    onPinMatchToScoreboard = { matchId ->
                                        ensureNotificationPermissionAndRun {
                                            viewModel.pinMatchToScoreboard(matchId, alsoPinNotificationBar = true)
                                        }
                                    },
                                    onTogglePinToNotificationBar = {
                                        ensureNotificationPermissionAndRun {
                                            viewModel.togglePinScoreboardToNotificationBar()
                                        }
                                    },
                                    onTriggerLiveStatEvent = viewModel::triggerLiveStatEvent,
                                    onOpenStandings = { competition ->
                                        viewModel.selectStandingsLeague(competition)
                                        currentTab = FrameXTab.STANDINGS
                                    },
                                    onOpenWebPortal = { title, url ->
                                        webPortalTitle = title
                                        webPortalUrl = url
                                        isWebPortalOpen = true
                                    }
                                )
                            }

                            FrameXTab.STANDINGS -> {
                                StandingsScreen(
                                    selectedCompetition = selectedStandingsLeague,
                                    standingsUiState = standingsUiState,
                                    searchQuery = standingsSearchQuery,
                                    favoriteClub = favoriteClub,
                                    selectedTheme = selectedTheme,
                                    onSelectCompetition = viewModel::selectStandingsLeague,
                                    onUpdateSearchQuery = viewModel::updateStandingsSearchQuery,
                                    onRefreshStandings = { viewModel.refreshStandings() },
                                    onOpenWebPortal = { title, url ->
                                        webPortalTitle = title
                                        webPortalUrl = url
                                        isWebPortalOpen = true
                                    },
                                    onJumpToLiveMatch = { comp ->
                                        viewModel.selectCompetition(comp)
                                        currentTab = FrameXTab.LIVE_FOOTBALL
                                    }
                                )
                            }

                            FrameXTab.CINEMA -> {
                                CinemaHubScreen(
                                    selectedCategory = selectedMediaCategory,
                                    searchQuery = cinemaSearchQuery,
                                    selectedMedia = selectedMedia,
                                    selectedServer = selectedMediaServer,
                                    isPlaying = isMediaPlaying,
                                    watchlist = watchlist,
                                    onSelectCategory = viewModel::selectMediaCategory,
                                    onUpdateSearch = viewModel::updateCinemaSearchQuery,
                                    onSelectMedia = viewModel::selectMediaTitle,
                                    onSelectServer = viewModel::selectMediaServer,
                                    onTogglePlayback = viewModel::toggleMediaPlayback,
                                    onToggleWatchlist = viewModel::toggleWatchlist,
                                    onSpeakSynopsis = viewModel::speakTextWithGeminiTts,
                                    onJumpToLiveFootball = { currentTab = FrameXTab.LIVE_FOOTBALL }
                                )
                            }

                            FrameXTab.AI_HUB -> {
                                AiScoutAndVisionScreen(
                                    searchQuery = searchGroundingQuery,
                                    isSearching = isSearchingGrounding,
                                    searchResult = searchGroundingResult,
                                    selectedBitmap = selectedVisionBitmap,
                                    visionPrompt = visionPrompt,
                                    isAnalyzingImage = isAnalyzingImage,
                                    visionResult = visionAnalysisResult,
                                    selectedTtsVoice = selectedTtsVoice,
                                    ttsStatus = ttsStatus,
                                    isTtsLoading = isTtsLoading,
                                    onUpdateSearchQuery = viewModel::updateSearchGroundingQuery,
                                    onExecuteSearchGrounding = viewModel::executeGoogleSearchGrounding,
                                    onUpdateVisionBitmap = viewModel::updateVisionBitmap,
                                    onLoadSampleTacticalBitmap = viewModel::loadSampleTacticalBoardBitmap,
                                    onUpdateVisionPrompt = viewModel::updateVisionPrompt,
                                    onAnalyzeImage = viewModel::analyzeSelectedImage,
                                    onSelectVoice = viewModel::setTtsVoice,
                                    onSpeakText = viewModel::speakTextWithGeminiTts
                                )
                            }

                            FrameXTab.ALERTS -> {
                                AlertsAndSavedScreen(
                                    subscribedAlerts = subscribedAlerts,
                                    notificationLogs = notificationLogs,
                                    watchlist = watchlist,
                                    matches = matches,
                                    hasNotificationPermission = hasNotifPermission,
                                    favoriteClub = favoriteClub,
                                    favoritedTeams = favoritedTeams,
                                    fcmToken = fcmToken,
                                    fcmSubscribedTopics = fcmSubscribedTopics,
                                    onRequestNotificationPermission = {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        }
                                    },
                                    onToggleFavoriteClubFcm = { club ->
                                        ensureNotificationPermissionAndRun {
                                            viewModel.toggleFavoriteClubFcm(club)
                                        }
                                    },
                                    onTriggerFavoriteClubFcmPush = { isGoal ->
                                        ensureNotificationPermissionAndRun {
                                            viewModel.triggerFavoriteClubFcmPush(isGoal)
                                        }
                                    },
                                    onUnsubscribeMatch = viewModel::toggleMatchAlertSubscription,
                                    onTriggerGoalAlert = { match ->
                                        ensureNotificationPermissionAndRun {
                                            viewModel.triggerLiveMajorGoalEvent(match, true)
                                        }
                                    },
                                    onTriggerKickoffAlert = { match ->
                                        ensureNotificationPermissionAndRun {
                                            viewModel.triggerMatchKickoffAlert(match)
                                        }
                                    },
                                    onClearNotificationLogs = viewModel::clearNotificationHistory
                                )
                            }

                            FrameXTab.PROFILE -> {
                                TouchTheGrassProfileAndSettingsScreen(
                                    initialShowSettings = false,
                                    isSignedIn = isGoogleSignedIn,
                                    userName = userName,
                                    userEmail = userEmail,
                                    favoriteClub = favoriteClub,
                                    profilePictureId = profilePictureId,
                                    customAvatarBitmap = customAvatarBitmap,
                                    selectedTheme = selectedTheme,
                                    enabledLeagues = enabledLeagues,
                                    flagSecureEnabled = flagSecureEnabled,
                                    wafEnabled = wafFirewallEnabled,
                                    selectedTtsVoice = selectedTtsVoice,
                                    liveMatchCount = matches.size,
                                    savedWatchlistCount = subscribedAlerts.size + watchlist.size,
                                    securityScore = securityAuditReport?.overallScore ?: 100,
                                    onGoogleAuthSuccess = { uid, name, email, photo ->
                                        viewModel.onGoogleUserAuthenticated(uid, name, email, photo)
                                    },
                                    onGoogleSignedOut = {
                                        viewModel.onGoogleUserSignedOut()
                                    },
                                    onAuthError = { msg ->
                                        viewModel.showUserMessage(msg)
                                    },
                                    onSaveProfile = { name, club, avatarId ->
                                        viewModel.updateUserProfileDetails(name, club, avatarId)
                                    },
                                    onUploadCustomAvatar = { bmp ->
                                        viewModel.updateCustomAvatarBitmap(bmp)
                                    },
                                    onSelectTheme = { theme ->
                                        viewModel.selectAbstractTheme(theme)
                                    },
                                    onToggleLeague = { league ->
                                        viewModel.toggleLeagueVisibility(league)
                                    },
                                    onEnableAllLeagues = {
                                        viewModel.enableAllLeagues()
                                    },
                                    onToggleFlagSecure = viewModel::toggleFlagSecure,
                                    onToggleWaf = viewModel::toggleWafFirewall,
                                    onSelectVoice = viewModel::setTtsVoice,
                                    onOpenSecurityShield = {
                                        showSecurityCyberScreen = true
                                    },
                                    onOpenAlertsAndWatchlist = {
                                        currentTab = FrameXTab.ALERTS
                                    },
                                    onCloseIfModal = null
                                )
                            }
                        }
                    }
                }
            }
        }

        // Modal My Profile & Settings Menu when opened from Top-Right Header Bar
        if (isUpperRightMenuOpen) {
            TouchTheGrassMenuDialog(
                initialShowSettings = menuInitialSettings,
                isSignedIn = isGoogleSignedIn,
                userName = userName,
                userEmail = userEmail,
                favoriteClub = favoriteClub,
                profilePictureId = profilePictureId,
                customAvatarBitmap = customAvatarBitmap,
                selectedTheme = selectedTheme,
                enabledLeagues = enabledLeagues,
                flagSecureEnabled = flagSecureEnabled,
                wafEnabled = wafFirewallEnabled,
                selectedTtsVoice = selectedTtsVoice,
                liveMatchCount = matches.size,
                savedWatchlistCount = subscribedAlerts.size + watchlist.size,
                securityScore = securityAuditReport?.overallScore ?: 100,
                onDismiss = { isUpperRightMenuOpen = false },
                onGoogleAuthSuccess = { uid, name, email, photo ->
                    viewModel.onGoogleUserAuthenticated(uid, name, email, photo)
                },
                onGoogleSignedOut = {
                    viewModel.onGoogleUserSignedOut()
                },
                onAuthError = { msg ->
                    viewModel.showUserMessage(msg)
                },
                onSaveProfile = { name, club, avatarId ->
                    viewModel.updateUserProfileDetails(name, club, avatarId)
                },
                onUploadCustomAvatar = { bmp ->
                    viewModel.updateCustomAvatarBitmap(bmp)
                },
                onSelectTheme = { theme ->
                    viewModel.selectAbstractTheme(theme)
                },
                onToggleLeague = { league ->
                    viewModel.toggleLeagueVisibility(league)
                },
                onEnableAllLeagues = {
                    viewModel.enableAllLeagues()
                },
                onToggleFlagSecure = viewModel::toggleFlagSecure,
                onToggleWaf = viewModel::toggleWafFirewall,
                onSelectVoice = viewModel::setTtsVoice,
                onOpenSecurityShield = {
                    showSecurityCyberScreen = true
                },
                onOpenAlertsAndWatchlist = {
                    currentTab = FrameXTab.ALERTS
                }
            )
        }

        // Interactive Touchthegrass.com Web Portal Modal
        if (isWebPortalOpen) {
            TouchTheGrassWebPortalDialog(
                title = webPortalTitle,
                url = webPortalUrl,
                matches = matches,
                onDismiss = { isWebPortalOpen = false },
                onSelectLiveMatchInApp = { matchId ->
                    viewModel.selectMatch(matchId)
                    currentTab = FrameXTab.LIVE_FOOTBALL
                }
            )
        }
    }
}

/**
 * Editorial Serif Header matching IMG_8276 with one-tap "Open Touchthegrass.com" web portal action
 */
@Composable
private fun TouchTheGrassTopBar(
    userName: String,
    favoriteClub: String,
    profilePictureId: String,
    customAvatarBitmap: Bitmap?,
    onOpenWebPortal: () -> Unit,
    onOpenProfileModal: () -> Unit,
    onOpenSettingsModal: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onOpenWebPortal)
                .testTag("open_touchthegrass_website_header")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Touchthegrass.com",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Normal,
                    fontSize = 25.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    color = VerifiedPillGreen.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, VerifiedPillGreen.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Open Website",
                            tint = VerifiedPillGreen,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "OPEN",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = VerifiedPillGreen
                        )
                    }
                }
            }
            Text(
                text = "$userName • $favoriteClub",
                style = MaterialTheme.typography.bodySmall,
                color = OledMutedText,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            UserAvatarCircle(
                avatarId = profilePictureId,
                customBitmap = customAvatarBitmap,
                size = 40.dp,
                borderColor = OledCardBorder,
                modifier = Modifier
                    .clickable(onClick = onOpenProfileModal)
                    .testTag("open_upper_right_menu_button")
            )

            Surface(
                color = OledCardSurface,
                shape = CircleShape,
                border = BorderStroke(1.dp, OledCardBorder),
                modifier = Modifier
                    .size(40.dp)
                    .clickable(onClick = onOpenSettingsModal)
                    .testTag("top_bar_settings_gear_button")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Settings",
                        tint = Color.White,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }
    }
}
