package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firestore.UserProfileRepository
import com.example.data.local.FrameXDatabase
import com.example.data.local.LocalRepository
import com.example.data.local.MatchAlertEntity
import com.example.data.local.NotificationLogEntity
import com.example.data.local.SecurityAuditLogEntity
import com.example.data.local.WatchlistMediaEntity
import com.example.model.CommentaryEvent
import com.example.model.CommentaryType
import com.example.model.FootballCompetition
import com.example.model.FootballMatch
import com.example.model.FootballThemeOption
import com.example.model.MediaCategory
import com.example.model.MediaTitle
import com.example.model.SearchGroundingResult
import com.example.model.SeedCatalog
import com.example.model.StandingsUiState
import com.example.model.StreamServer
import com.example.network.GeminiRepository
import com.example.network.StandingsRepository
import com.example.notifications.FcmPushManager
import com.example.notifications.MatchNotificationHelper
import com.example.security.EncryptedStreamToken
import com.example.security.SecurityAuditReport
import com.example.security.SecurityShieldManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class FrameXViewModel(application: Application) : AndroidViewModel(application) {

    private val database = FrameXDatabase.getInstance(application)
    private val localRepo = LocalRepository(database.frameXDao())
    private val userProfileRepo = UserProfileRepository(application.applicationContext)
    private val geminiRepo = GeminiRepository(application.applicationContext)
    private val standingsRepo = StandingsRepository()
    private val securityShield = SecurityShieldManager(application.applicationContext)

    private var profileObserverJob: Job? = null

    // --- User Auth, Profile, Club, Avatar & Theme State (Synced with Firestore when signed in) ---
    private val _isGoogleSignedIn = MutableStateFlow(false)
    val isGoogleSignedIn: StateFlow<Boolean> = _isGoogleSignedIn.asStateFlow()

    private val _userUid = MutableStateFlow<String?>(null)
    val userUid: StateFlow<String?> = _userUid.asStateFlow()

    private val _userEmail = MutableStateFlow<String?>(null)
    val userEmail: StateFlow<String?> = _userEmail.asStateFlow()

    private val _userName = MutableStateFlow("Leo Messi #10")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _favoriteClub = MutableStateFlow("Inter Miami CF")
    val favoriteClub: StateFlow<String> = _favoriteClub.asStateFlow()

    val fcmToken: StateFlow<String> = FcmPushManager.fcmToken
    val fcmSubscribedTopics: StateFlow<Set<String>> = FcmPushManager.subscribedTopics
    val favoritedTeams: StateFlow<Set<String>> = FcmPushManager.favoritedTeams

    private val _profilePictureId = MutableStateFlow("avatar_messi_goat")
    val profilePictureId: StateFlow<String> = _profilePictureId.asStateFlow()

    private val _userPhotoUrl = MutableStateFlow<String?>(null)
    val userPhotoUrl: StateFlow<String?> = _userPhotoUrl.asStateFlow()

    private val _customAvatarBitmap = MutableStateFlow<Bitmap?>(null)
    val customAvatarBitmap: StateFlow<Bitmap?> = _customAvatarBitmap.asStateFlow()

    private val _selectedTheme = MutableStateFlow(SeedCatalog.top20AbstractThemes.first())
    val selectedTheme: StateFlow<FootballThemeOption> = _selectedTheme.asStateFlow()

    private val _enabledLeagues = MutableStateFlow(FootballCompetition.toggleableLeagues.toSet())
    val enabledLeagues: StateFlow<Set<FootballCompetition>> = _enabledLeagues.asStateFlow()

    // --- Room Persisted Flows ---
    val matchAlerts: StateFlow<List<MatchAlertEntity>> = localRepo.matchAlerts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val watchlist: StateFlow<List<WatchlistMediaEntity>> = localRepo.watchlist
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notificationLogs: StateFlow<List<NotificationLogEntity>> = localRepo.notificationLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val securityAuditLogs: StateFlow<List<SecurityAuditLogEntity>> = localRepo.securityAuditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Cyber Shield Security State (Default false on startup so browser streaming emulator renders frames cleanly) ---
    private val _flagSecureEnabled = MutableStateFlow(false)
    val flagSecureEnabled: StateFlow<Boolean> = _flagSecureEnabled.asStateFlow()

    private val _wafFirewallEnabled = MutableStateFlow(true)
    val wafFirewallEnabled: StateFlow<Boolean> = _wafFirewallEnabled.asStateFlow()

    private val _securityAuditReport = MutableStateFlow<SecurityAuditReport?>(null)
    val securityAuditReport: StateFlow<SecurityAuditReport?> = _securityAuditReport.asStateFlow()

    private val _activeStreamToken = MutableStateFlow<EncryptedStreamToken?>(null)
    val activeStreamToken: StateFlow<EncryptedStreamToken?> = _activeStreamToken.asStateFlow()

    private val _customWafTestInput = MutableStateFlow("SELECT * FROM users WHERE '1'='1' UNION SELECT password FROM admins; <script>alert(1)</script>")
    val customWafTestInput: StateFlow<String> = _customWafTestInput.asStateFlow()

    // --- Cinema & Series State ---
    private val _selectedMediaCategory = MutableStateFlow(MediaCategory.ALL)
    val selectedMediaCategory: StateFlow<MediaCategory> = _selectedMediaCategory.asStateFlow()

    private val _cinemaSearchQuery = MutableStateFlow("")
    val cinemaSearchQuery: StateFlow<String> = _cinemaSearchQuery.asStateFlow()

    private val _selectedMedia = MutableStateFlow<MediaTitle?>(SeedCatalog.cinemaCatalog.first())
    val selectedMedia: StateFlow<MediaTitle?> = _selectedMedia.asStateFlow()

    private val _selectedMediaServer = MutableStateFlow<StreamServer>(SeedCatalog.defaultServers.first())
    val selectedMediaServer: StateFlow<StreamServer> = _selectedMediaServer.asStateFlow()

    private val _isMediaPlaying = MutableStateFlow(true)
    val isMediaPlaying: StateFlow<Boolean> = _isMediaPlaying.asStateFlow()

    // --- Live Football Match Center State ---
    private val _selectedCompetition = MutableStateFlow(FootballCompetition.ALL)
    val selectedCompetition: StateFlow<FootballCompetition> = _selectedCompetition.asStateFlow()

    private val _matchSearchQuery = MutableStateFlow("")
    val matchSearchQuery: StateFlow<String> = _matchSearchQuery.asStateFlow()

    private val _matches = MutableStateFlow(SeedCatalog.initialMatches)
    val matches: StateFlow<List<FootballMatch>> = _matches.asStateFlow()

    private val _selectedMatchId = MutableStateFlow(SeedCatalog.initialMatches.first().id)
    val selectedMatchId: StateFlow<String> = _selectedMatchId.asStateFlow()

    private val _selectedMatchServer = MutableStateFlow(SeedCatalog.footballBroadcastServers.first())
    val selectedMatchServer: StateFlow<StreamServer> = _selectedMatchServer.asStateFlow()

    private val _liveTickerEnabled = MutableStateFlow(true)
    val liveTickerEnabled: StateFlow<Boolean> = _liveTickerEnabled.asStateFlow()

    // --- Dynamic Island & Android Notification Bar Pinned Scoreboard State ---
    private val _pinnedMatchId = MutableStateFlow<String?>(null)
    val pinnedMatchId: StateFlow<String?> = _pinnedMatchId.asStateFlow()

    private val _isPinnedToNotificationBar = MutableStateFlow(true)
    val isPinnedToNotificationBar: StateFlow<Boolean> = _isPinnedToNotificationBar.asStateFlow()

    private val _dynamicIslandGoalFlash = MutableStateFlow<String?>(null)
    val dynamicIslandGoalFlash: StateFlow<String?> = _dynamicIslandGoalFlash.asStateFlow()

    private val _inAppAlertBanner = MutableStateFlow<String?>(null)
    val inAppAlertBanner: StateFlow<String?> = _inAppAlertBanner.asStateFlow()

    // --- AI Features State (TTS, Google Search Grounding, Image Understanding) ---
    private val _ttsStatus = MutableStateFlow<String?>(null)
    val ttsStatus: StateFlow<String?> = _ttsStatus.asStateFlow()

    private val _isTtsLoading = MutableStateFlow(false)
    val isTtsLoading: StateFlow<Boolean> = _isTtsLoading.asStateFlow()

    private val _selectedTtsVoice = MutableStateFlow("Kore")
    val selectedTtsVoice: StateFlow<String> = _selectedTtsVoice.asStateFlow()

    private val _searchGroundingQuery = MutableStateFlow("Latest UEFA Champions League, Premier League & Inter Miami live scores and standings")
    val searchGroundingQuery: StateFlow<String> = _searchGroundingQuery.asStateFlow()

    private val _isSearchingGrounding = MutableStateFlow(false)
    val isSearchingGrounding: StateFlow<Boolean> = _isSearchingGrounding.asStateFlow()

    private val _searchGroundingResult = MutableStateFlow<SearchGroundingResult?>(null)
    val searchGroundingResult: StateFlow<SearchGroundingResult?> = _searchGroundingResult.asStateFlow()

    private val _selectedVisionBitmap = MutableStateFlow<Bitmap?>(null)
    val selectedVisionBitmap: StateFlow<Bitmap?> = _selectedVisionBitmap.asStateFlow()

    private val _visionPrompt = MutableStateFlow("Analyze the tactical football formation, high-press lanes, key player positions, and scoring threat in this frame.")
    val visionPrompt: StateFlow<String> = _visionPrompt.asStateFlow()

    private val _isAnalyzingImage = MutableStateFlow(false)
    val isAnalyzingImage: StateFlow<Boolean> = _isAnalyzingImage.asStateFlow()

    private val _visionAnalysisResult = MutableStateFlow<String?>(null)
    val visionAnalysisResult: StateFlow<String?> = _visionAnalysisResult.asStateFlow()

    // --- League Standings State (EPL, LaLiga, UCL, Serie A, Bundesliga, MLS Inter Miami, Intl) ---
    private val _selectedStandingsLeague = MutableStateFlow(FootballCompetition.EPL)
    val selectedStandingsLeague: StateFlow<FootballCompetition> = _selectedStandingsLeague.asStateFlow()

    private val _standingsSearchQuery = MutableStateFlow("")
    val standingsSearchQuery: StateFlow<String> = _standingsSearchQuery.asStateFlow()

    private val _standingsUiState = MutableStateFlow<StandingsUiState>(
        StandingsUiState.Success(
            standingsRepo.getBaselineTable(FootballCompetition.EPL, SeedCatalog.initialMatches)
        )
    )
    val standingsUiState: StateFlow<StandingsUiState> = _standingsUiState.asStateFlow()

    init {
        MatchNotificationHelper.ensureChannelsCreated(application.applicationContext)
        FcmPushManager.initializeFcm(application.applicationContext, _favoriteClub.value)
        seedDefaultAlertsIfNeeded()
        initializeSecurityShield()
        _selectedVisionBitmap.value = createTacticalBoardBitmap()
        syncPinnedScoreboardNotification()
        startLiveFootballTicker()
        refreshStandings()
    }

    fun selectStandingsLeague(competition: FootballCompetition) {
        val target = if (competition == FootballCompetition.ALL) FootballCompetition.EPL else competition
        _selectedStandingsLeague.value = target
        refreshStandings(target)
    }

    fun updateStandingsSearchQuery(query: String) {
        _standingsSearchQuery.value = query
    }

    fun refreshStandings(competition: FootballCompetition = _selectedStandingsLeague.value) {
        val target = if (competition == FootballCompetition.ALL) FootballCompetition.EPL else competition
        val preview = standingsRepo.getBaselineTable(target, _matches.value)
        _standingsUiState.value = StandingsUiState.Loading(preview)

        viewModelScope.launch {
            val table = standingsRepo.fetchLeagueStandings(target, _matches.value)
            _standingsUiState.value = StandingsUiState.Success(table)
        }
    }

    // --- Google Sign-In & Firestore Profile Sync ---
    fun onGoogleUserAuthenticated(
        uid: String,
        displayName: String?,
        email: String?,
        photoUrl: String?
    ) {
        _isGoogleSignedIn.value = true
        _userUid.value = uid
        _userEmail.value = email
        if (!displayName.isNullOrBlank()) {
            _userName.value = displayName
        }
        if (!photoUrl.isNullOrBlank()) {
            _userPhotoUrl.value = photoUrl
        }

        profileObserverJob?.cancel()
        profileObserverJob = viewModelScope.launch {
            // Check if profile exists, if not create initial
            val existing = userProfileRepo.getUserProfile(uid).getOrNull()
            if (existing == null) {
                userProfileRepo.saveOrUpdateProfile(
                    username = _userName.value,
                    favoriteClub = _favoriteClub.value,
                    profilePictureId = _profilePictureId.value,
                    photoUrl = _userPhotoUrl.value,
                    selectedThemeId = _selectedTheme.value.id,
                    enabledLeagues = _enabledLeagues.value.joinToString(",") { it.code }
                )
            }
            userProfileRepo.observeUserProfile(uid)
                .catch { /* Handled by handleFirestoreError in repository */ }
                .collect { profile ->
                    if (profile != null) {
                        _userName.value = profile.username
                        _favoriteClub.value = profile.favoriteClub
                        FcmPushManager.syncPrimaryFavoriteClub(profile.favoriteClub)
                        autoSubscribeMatchesForClub(profile.favoriteClub)
                        _profilePictureId.value = profile.profilePictureId
                        if (!profile.photoUrl.isNullOrBlank()) {
                            _userPhotoUrl.value = profile.photoUrl
                        }
                        SeedCatalog.top20AbstractThemes.find { it.id == profile.selectedThemeId }?.let {
                            _selectedTheme.value = it
                        }
                        val codes = profile.enabledLeagues.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
                        val parsedLeagues = FootballCompetition.toggleableLeagues.filter { it.code in codes }.toSet()
                        if (parsedLeagues.isNotEmpty()) {
                            _enabledLeagues.value = parsedLeagues
                        }
                    }
                }
        }
        showBanner("✅ Signed in to Touchthegrass.com as ${_userName.value}")
    }

    fun onGoogleUserSignedOut() {
        profileObserverJob?.cancel()
        profileObserverJob = null
        _isGoogleSignedIn.value = false
        _userUid.value = null
        _userEmail.value = null
        showBanner("Signed out of Google account")
    }

    fun updateUserProfileDetails(
        newUsername: String,
        newFavoriteClub: String,
        newAvatarId: String
    ) {
        val cleanName = securityShield.inspectAndSanitizeInput(newUsername, _wafFirewallEnabled.value)
            .sanitizedText.trim().ifBlank { "Leo Messi #10" }
        val cleanClub = securityShield.inspectAndSanitizeInput(newFavoriteClub, _wafFirewallEnabled.value)
            .sanitizedText.trim().ifBlank { "Inter Miami CF" }

        _userName.value = cleanName
        _favoriteClub.value = cleanClub
        _profilePictureId.value = newAvatarId
        FcmPushManager.syncPrimaryFavoriteClub(cleanClub)
        autoSubscribeMatchesForClub(cleanClub)
        syncProfileToFirestoreIfSignedIn()
        showBanner("Updated Profile & FCM Alerts: $cleanName • ★ $cleanClub")
    }

    fun toggleFavoriteClubFcm(clubName: String) {
        val nowFavorited = FcmPushManager.toggleFavoriteTeamFcm(clubName)
        if (nowFavorited) {
            _favoriteClub.value = clubName
            autoSubscribeMatchesForClub(clubName)
            syncProfileToFirestoreIfSignedIn()
            showBanner("★ FCM Subscribed: $clubName (Goals & Kickoffs)")
        } else {
            showBanner("Unsubscribed FCM topic for $clubName")
        }
    }

    private fun autoSubscribeMatchesForClub(clubName: String) {
        viewModelScope.launch {
            _matches.value.filter { match ->
                match.homeTeam.contains(clubName, ignoreCase = true) ||
                    match.awayTeam.contains(clubName, ignoreCase = true) ||
                    clubName.contains(match.homeTeam, ignoreCase = true) ||
                    clubName.contains(match.awayTeam, ignoreCase = true)
            }.forEach { match ->
                localRepo.upsertMatchAlert(
                    MatchAlertEntity(
                        matchId = match.id,
                        matchLabel = "${match.homeTeam} vs ${match.awayTeam}",
                        competitionName = "${match.competition.displayName} • ★ Favorite Club",
                        notifyMajorGoals = true,
                        notifyKickoff = true
                    )
                )
            }
        }
    }

    fun triggerFavoriteClubFcmPush(isGoal: Boolean) {
        val favClub = _favoriteClub.value
        val favMatch = _matches.value.find { match ->
            FcmPushManager.isTeamFavorited(match.homeTeam, favClub) ||
                FcmPushManager.isTeamFavorited(match.awayTeam, favClub)
        } ?: _matches.value.first()

        val isHomeFav = FcmPushManager.isTeamFavorited(favMatch.homeTeam, favClub)
        if (isGoal) {
            triggerLiveMajorGoalEvent(favMatch, scoringHome = isHomeFav)
        } else {
            triggerMatchKickoffAlert(favMatch)
        }
    }

    fun resolveDynamicIslandMatch(): FootballMatch {
        val pinnedId = _pinnedMatchId.value
        if (pinnedId != null) {
            _matches.value.find { it.id == pinnedId }?.let { return it }
        }
        val favClub = _favoriteClub.value
        return _matches.value.find { match ->
            FcmPushManager.isTeamFavorited(match.homeTeam, favClub) ||
                FcmPushManager.isTeamFavorited(match.awayTeam, favClub)
        } ?: _matches.value.first()
    }

    fun pinMatchToScoreboard(matchId: String?, alsoPinNotificationBar: Boolean = true) {
        _pinnedMatchId.value = matchId
        if (alsoPinNotificationBar) {
            _isPinnedToNotificationBar.value = true
        }
        syncPinnedScoreboardNotification()
        val targetMatch = resolveDynamicIslandMatch()
        if (matchId == null) {
            showBanner("★ Dynamic Island locked to Favorite Club: ${targetMatch.homeShort} vs ${targetMatch.awayShort}")
        } else {
            showBanner("📌 Pinned ${targetMatch.homeShort} ${targetMatch.homeScore}-${targetMatch.awayScore} ${targetMatch.awayShort} to Dynamic Island & Notification Bar")
        }
    }

    fun togglePinScoreboardToNotificationBar() {
        val next = !_isPinnedToNotificationBar.value
        _isPinnedToNotificationBar.value = next
        if (next) {
            syncPinnedScoreboardNotification()
            val m = resolveDynamicIslandMatch()
            showBanner("📌 Live Scoreboard Pinned to Android Notification Bar (${m.homeShort} ${m.homeScore}-${m.awayScore} ${m.awayShort})")
        } else {
            MatchNotificationHelper.cancelPinnedScoreboardNotification(getApplication())
            showBanner("Unpinned Live Scoreboard from Android Notification Bar")
        }
    }

    fun syncPinnedScoreboardNotification() {
        if (!_isPinnedToNotificationBar.value) return
        val match = resolveDynamicIslandMatch()
        val favClub = _favoriteClub.value
        val isFav = FcmPushManager.isTeamFavorited(match.homeTeam, favClub) ||
            FcmPushManager.isTeamFavorited(match.awayTeam, favClub)
        MatchNotificationHelper.showOrUpdatePinnedScoreboardNotification(
            context = getApplication(),
            match = match,
            isFavoriteClub = isFav,
            streamServerName = _selectedMatchServer.value.name
        )
    }

    fun updateCustomAvatarBitmap(bitmap: Bitmap) {
        _customAvatarBitmap.value = bitmap
        _profilePictureId.value = "custom_photo"
        syncProfileToFirestoreIfSignedIn()
        showBanner("Updated circular profile picture")
    }

    fun selectAbstractTheme(theme: FootballThemeOption) {
        _selectedTheme.value = theme
        syncProfileToFirestoreIfSignedIn()
        showBanner("Theme applied: #${theme.rank} ${theme.name}")
    }

    fun toggleLeagueVisibility(competition: FootballCompetition) {
        if (competition == FootballCompetition.ALL) return
        _enabledLeagues.update { current ->
            val next = if (competition in current) {
                if (current.size > 1) current - competition else current
            } else {
                current + competition
            }
            next
        }
        val activeLeagues = _enabledLeagues.value
        if (_selectedCompetition.value != FootballCompetition.ALL && _selectedCompetition.value !in activeLeagues) {
            _selectedCompetition.value = FootballCompetition.ALL
        }
        val visibleMatches = _matches.value.filter { it.competition in activeLeagues }
        if (visibleMatches.isNotEmpty() && visibleMatches.none { it.id == _selectedMatchId.value }) {
            _selectedMatchId.value = visibleMatches.first().id
        }
        syncProfileToFirestoreIfSignedIn()
    }

    fun enableAllLeagues() {
        _enabledLeagues.value = FootballCompetition.toggleableLeagues.toSet()
        _selectedCompetition.value = FootballCompetition.ALL
        syncProfileToFirestoreIfSignedIn()
    }

    private fun syncProfileToFirestoreIfSignedIn() {
        if (!_isGoogleSignedIn.value || _userUid.value == null) return
        viewModelScope.launch {
            userProfileRepo.saveOrUpdateProfile(
                username = _userName.value,
                favoriteClub = _favoriteClub.value,
                profilePictureId = _profilePictureId.value,
                photoUrl = _userPhotoUrl.value,
                selectedThemeId = _selectedTheme.value.id,
                enabledLeagues = _enabledLeagues.value.joinToString(",") { it.code }
            )
        }
    }

    private fun initializeSecurityShield() {
        viewModelScope.launch {
            val report = securityShield.runFullSecurityAudit(
                flagSecureEnabled = _flagSecureEnabled.value,
                wafEnabled = _wafFirewallEnabled.value
            )
            _securityAuditReport.value = report
            val token = securityShield.rotateAndEncryptStreamToken(_selectedMatchServer.value.name)
            _activeStreamToken.value = token

            localRepo.logSecurityEvent(
                SecurityAuditLogEntity(
                    eventType = "RASP_AUDIT",
                    severity = "VERIFIED",
                    title = "Zero-Trust RASP & TLS 1.3 Boot Verification (${report.overallScore}%)",
                    detail = "APK Cert SHA-256 verified • Cleartext blocked • Keystore AES-256-GCM token ${token.tokenId} active."
                )
            )
        }
    }

    // --- Cyber Shield Security Actions ---
    fun toggleFlagSecure(enabled: Boolean) {
        // Keep FLAG_SECURE display block removed so live match streams are always visible on all devices
        _flagSecureEnabled.value = false
        val updated = securityShield.runFullSecurityAudit(
            flagSecureEnabled = false,
            wafEnabled = _wafFirewallEnabled.value
        )
        _securityAuditReport.value = updated
        viewModelScope.launch {
            localRepo.logSecurityEvent(
                SecurityAuditLogEntity(
                    eventType = "SHIELD_CONFIG",
                    severity = "VERIFIED",
                    title = "Universal Stream Visibility Active (FLAG_SECURE Block Removed)",
                    detail = "WindowManager.LayoutParams.FLAG_SECURE is cleared so live match streams render on all devices and displays."
                )
            )
        }
        showBanner("✅ Universal Stream Display Active (FLAG_SECURE Block Removed for All Devices)")
    }

    fun toggleWafFirewall(enabled: Boolean) {
        _wafFirewallEnabled.value = enabled
        val updated = securityShield.runFullSecurityAudit(
            flagSecureEnabled = _flagSecureEnabled.value,
            wafEnabled = enabled
        )
        _securityAuditReport.value = updated
        viewModelScope.launch {
            localRepo.logSecurityEvent(
                SecurityAuditLogEntity(
                    eventType = "SHIELD_CONFIG",
                    severity = "HIGH_SECURITY",
                    title = if (enabled) "WAF SQLi/XSS/Prompt Firewall Armed" else "WAF Firewall Paused",
                    detail = "Active inspection for SQL injection, XSS, SSRF, and AI prompt injection."
                )
            )
        }
        showBanner(
            if (enabled) "🛡️ Zero-Trust WAF Firewall Armed (SQLi/XSS/Prompt Guard)"
            else "⚠️ WAF Firewall Paused"
        )
    }

    fun runDeepSecurityAudit() {
        viewModelScope.launch {
            val report = securityShield.runFullSecurityAudit(
                flagSecureEnabled = _flagSecureEnabled.value,
                wafEnabled = _wafFirewallEnabled.value
            )
            _securityAuditReport.value = report
            localRepo.logSecurityEvent(
                SecurityAuditLogEntity(
                    eventType = "RASP_AUDIT",
                    severity = "VERIFIED",
                    title = "Deep 8-Layer RASP & Network Audit Complete: ${report.overallScore}% (${report.postureGrade})",
                    detail = "Verified APK Signature (${report.apkSignatureSha256.take(23)}...), TLS 1.3, Keystore AES-256-GCM, and Anti-Root checks."
                )
            )
            showBanner("🛡️ Security Audit Complete: ${report.overallScore}% — ${report.postureGrade}")
        }
    }

    fun rotateStreamSessionToken(serverName: String = _selectedMatchServer.value.name) {
        viewModelScope.launch {
            val newToken = securityShield.rotateAndEncryptStreamToken(serverName)
            _activeStreamToken.value = newToken
            localRepo.logSecurityEvent(
                SecurityAuditLogEntity(
                    eventType = "KEYSTORE_ROTATION",
                    severity = "HIGH_SECURITY",
                    title = "Rotated AES-256-GCM Stream Session Token (${newToken.tokenId})",
                    detail = "Server: $serverName • IV: ${newToken.ivBase64} • SHA-256: ${newToken.sha256Fingerprint.take(24)}..."
                )
            )
            showBanner("🔐 Switched to $serverName (${newToken.tokenId})")
        }
    }

    fun updateCustomWafTestInput(input: String) {
        _customWafTestInput.value = input
    }

    fun testWafAttackPayload(payload: String = _customWafTestInput.value) {
        viewModelScope.launch {
            val inspection = securityShield.inspectAndSanitizeInput(payload, wafEnabled = true)
            if (!inspection.isSafe) {
                localRepo.logSecurityEvent(
                    SecurityAuditLogEntity(
                        eventType = "WAF_BLOCKED",
                        severity = "CRITICAL_BLOCKED",
                        title = "WAF Neutralized ${inspection.threatCategory} Attack",
                        detail = "Rule: ${inspection.ruleId} • Blocked Payload: \"${inspection.rawBlockedSnippet}\""
                    )
                )
                showBanner("🛑 WAF BLOCKED ${inspection.threatCategory}: ${inspection.ruleId}")
            } else {
                localRepo.logSecurityEvent(
                    SecurityAuditLogEntity(
                        eventType = "WAF_VERIFIED",
                        severity = "VERIFIED",
                        title = "WAF Inspected Clean Payload",
                        detail = "Sanitized input passed all SQLi, XSS, SSRF, and Prompt Injection rules."
                    )
                )
                showBanner("✅ WAF Inspection Passed: Clean Payload")
            }
        }
    }

    fun clearSecurityAuditLogs() {
        viewModelScope.launch {
            localRepo.clearSecurityLogs()
        }
    }

    private fun seedDefaultAlertsIfNeeded() {
        viewModelScope.launch {
            val uclMatch = SeedCatalog.initialMatches.first()
            val miamiMatch = SeedCatalog.initialMatches[1]
            localRepo.upsertMatchAlert(
                MatchAlertEntity(
                    matchId = uclMatch.id,
                    matchLabel = "${uclMatch.homeTeam} vs ${uclMatch.awayTeam}",
                    competitionName = uclMatch.competition.displayName,
                    notifyMajorGoals = true,
                    notifyKickoff = true
                )
            )
            localRepo.upsertMatchAlert(
                MatchAlertEntity(
                    matchId = miamiMatch.id,
                    matchLabel = "${miamiMatch.homeTeam} vs ${miamiMatch.awayTeam}",
                    competitionName = miamiMatch.competition.displayName,
                    notifyMajorGoals = true,
                    notifyKickoff = true
                )
            )
        }
    }

    private fun startLiveFootballTicker() {
        viewModelScope.launch {
            var tickCount = 0
            while (isActive) {
                delay(8_000L)
                if (!_liveTickerEnabled.value) continue
                tickCount++

                _matches.update { currentList ->
                    currentList.mapIndexed { index, match ->
                        val nextMin = if (match.minute < 90) match.minute + 1 else 90
                        val nextAdded = if (match.minute >= 90) (match.addedTime + 1).coerceAtMost(6) else 0
                        val momentumShift = ((tickCount + index) % 5 - 2) * 0.03f
                        val newMomentum = (match.stats.homeMomentum + momentumShift).coerceIn(0.22f, 0.82f)

                        val updatedCommentary = if (tickCount % 2 == 0 && match.id == _selectedMatchId.value) {
                            val liveNote = CommentaryEvent(
                                id = "live_${match.id}_${System.currentTimeMillis()}",
                                minute = nextMin,
                                type = CommentaryType.MAJOR_CHANCE,
                                headline = "${match.homeTeam} surge forward into the final third ($nextMin')",
                                detail = "High-tempo build-up play sees ${match.homeKeyPlayers.first()} combine on the edge of the box as xG climbs to ${String.format("%.2f", match.stats.homeXg + 0.08f)}."
                            )
                            listOf(liveNote) + match.commentary.take(14)
                        } else {
                            match.commentary
                        }

                        val possShift = ((tickCount + index) % 3 - 1)
                        val newHomePoss = (match.stats.homePossession + possShift).coerceIn(32, 68)
                        val newAwayPoss = 100 - newHomePoss
                        val addHomeShot = if ((tickCount + index) % 3 == 0) 1 else 0
                        val addAwayShot = if ((tickCount + index) % 4 == 0) 1 else 0
                        val addHomeCorner = if ((tickCount + index) % 5 == 0) 1 else 0

                        match.copy(
                            minute = nextMin,
                            addedTime = nextAdded,
                            stats = match.stats.copy(
                                homePossession = newHomePoss,
                                awayPossession = newAwayPoss,
                                homeTotalShots = match.stats.homeTotalShots + addHomeShot,
                                awayTotalShots = match.stats.awayTotalShots + addAwayShot,
                                homeCorners = match.stats.homeCorners + addHomeCorner,
                                homeMomentum = newMomentum,
                                homeXg = (match.stats.homeXg + 0.02f).coerceAtMost(4.5f)
                            ),
                            commentary = updatedCommentary
                        )
                    }
                }
                syncPinnedScoreboardNotification()
            }
        }
    }

    // --- Cinema Actions (WAF-Protected) ---
    fun selectMediaCategory(category: MediaCategory) {
        _selectedMediaCategory.value = category
    }

    fun updateCinemaSearchQuery(query: String) {
        val check = securityShield.inspectAndSanitizeInput(query, _wafFirewallEnabled.value)
        if (!check.isSafe) {
            testWafAttackPayload(query)
            _cinemaSearchQuery.value = check.sanitizedText
        } else {
            _cinemaSearchQuery.value = query
        }
    }

    fun selectMediaTitle(media: MediaTitle) {
        _selectedMedia.value = media
        _selectedMediaServer.value = media.servers.first()
        _isMediaPlaying.value = true
        rotateStreamSessionToken(media.servers.first().name)
    }

    fun selectMediaServer(server: StreamServer) {
        _selectedMediaServer.value = server
        rotateStreamSessionToken(server.name)
    }

    fun toggleMediaPlayback() {
        _isMediaPlaying.update { !it }
    }

    fun toggleWatchlist(media: MediaTitle) {
        viewModelScope.launch {
            val isSaved = watchlist.value.any { it.mediaId == media.id }
            if (isSaved) {
                localRepo.removeFromWatchlist(media.id)
                showBanner("Removed \"${media.title}\" from TouchTheGrass Watchlist")
            } else {
                localRepo.addToWatchlist(
                    WatchlistMediaEntity(
                        mediaId = media.id,
                        title = media.title,
                        categoryLabel = media.category.label,
                        qualityBadge = media.qualityBadge,
                        rating = media.rating
                    )
                )
                showBanner("Saved \"${media.title}\" to TouchTheGrass Watchlist")
            }
        }
    }

    // --- Football Actions ---
    fun selectCompetition(competition: FootballCompetition) {
        _selectedCompetition.value = competition
        if (competition != FootballCompetition.ALL && competition !in _enabledLeagues.value) {
            _enabledLeagues.update { it + competition }
        }
        val activeLeagues = _enabledLeagues.value
        val filtered = if (competition == FootballCompetition.ALL) {
            _matches.value.filter { it.competition in activeLeagues }
        } else {
            _matches.value.filter { it.competition == competition }
        }
        if (filtered.isNotEmpty() && filtered.none { it.id == _selectedMatchId.value }) {
            _selectedMatchId.value = filtered.first().id
        }
    }

    fun selectMatch(matchId: String) {
        _selectedMatchId.value = matchId
    }

    fun updateMatchSearchQuery(query: String) {
        _matchSearchQuery.value = query
    }

    fun selectMatchServer(server: StreamServer) {
        _selectedMatchServer.value = server
        rotateStreamSessionToken(server.name)
    }

    fun toggleLiveTicker() {
        _liveTickerEnabled.update { !it }
    }

    fun toggleMatchAlertSubscription(match: FootballMatch) {
        viewModelScope.launch {
            val existing = matchAlerts.value.find { it.matchId == match.id }
            if (existing != null) {
                localRepo.deleteMatchAlert(match.id)
                showBanner("Muted goal & kickoff alerts for ${match.homeShort} vs ${match.awayShort}")
            } else {
                localRepo.upsertMatchAlert(
                    MatchAlertEntity(
                        matchId = match.id,
                        matchLabel = "${match.homeTeam} vs ${match.awayTeam}",
                        competitionName = match.competition.displayName,
                        notifyMajorGoals = true,
                        notifyKickoff = true
                    )
                )
                showBanner("Subscribed to Major Goal & Kickoff Push Alerts for ${match.homeTeam} vs ${match.awayTeam}")
            }
        }
    }

    fun triggerLiveMajorGoalEvent(match: FootballMatch, scoringHome: Boolean = true) {
        viewModelScope.launch {
            val newHomeScore = if (scoringHome) match.homeScore + 1 else match.homeScore
            val newAwayScore = if (!scoringHome) match.awayScore + 1 else match.awayScore
            val scoringTeam = if (scoringHome) match.homeTeam else match.awayTeam
            val scorer = if (scoringHome) match.homeKeyPlayers.first() else match.awayKeyPlayers.first()
            val minute = match.minute

            val headline = "MAJOR GOAL! $scorer scores for $scoringTeam ($minute')!"
            val detail = "$scoringTeam break through in the ${match.competition.displayName}! $scorer unleashes an unstoppable finish into the top corner to make it ${match.homeShort} $newHomeScore - $newAwayScore ${match.awayShort}!"

            val goalEvent = CommentaryEvent(
                id = "goal_${System.currentTimeMillis()}",
                minute = minute,
                type = CommentaryType.GOAL,
                headline = headline,
                detail = detail,
                scorerOrPlayer = "$scorer ($minute')"
            )

            _matches.update { list ->
                list.map { item ->
                    if (item.id == match.id) {
                        item.copy(
                            homeScore = newHomeScore,
                            awayScore = newAwayScore,
                            stats = item.stats.copy(
                                homeShotsOnTarget = if (scoringHome) item.stats.homeShotsOnTarget + 1 else item.stats.homeShotsOnTarget,
                                awayShotsOnTarget = if (!scoringHome) item.stats.awayShotsOnTarget + 1 else item.stats.awayShotsOnTarget,
                                homeTotalShots = if (scoringHome) item.stats.homeTotalShots + 1 else item.stats.homeTotalShots,
                                awayTotalShots = if (!scoringHome) item.stats.awayTotalShots + 1 else item.stats.awayTotalShots,
                                homeXg = if (scoringHome) item.stats.homeXg + 0.42f else item.stats.homeXg,
                                awayXg = if (!scoringHome) item.stats.awayXg + 0.42f else item.stats.awayXg
                            ),
                            commentary = listOf(goalEvent) + item.commentary
                        )
                    } else item
                }
            }

            val scoreline = "${match.homeTeam} $newHomeScore - $newAwayScore ${match.awayTeam} ($minute')"
            val favClub = _favoriteClub.value
            val isFavoriteTeamGoal = FcmPushManager.isTeamFavorited(scoringTeam, favClub) ||
                FcmPushManager.isTeamFavorited(match.homeTeam, favClub) ||
                FcmPushManager.isTeamFavorited(match.awayTeam, favClub)

            MatchNotificationHelper.sendMajorGoalNotification(
                context = getApplication(),
                competition = match.competition.displayName,
                scoreline = scoreline,
                headline = headline,
                detail = detail,
                isFavoriteTeam = isFavoriteTeamGoal
            )

            syncPinnedScoreboardNotification()

            val flashText = if (isFavoriteTeamGoal) {
                "★ FAVORITE TEAM GOAL! $scorer ($minute') • ${match.homeShort} $newHomeScore - $newAwayScore ${match.awayShort}"
            } else {
                "⚽ GOAL! $scorer ($minute') • ${match.homeShort} $newHomeScore - $newAwayScore ${match.awayShort}"
            }
            viewModelScope.launch {
                _dynamicIslandGoalFlash.value = flashText
                delay(5_500L)
                if (_dynamicIslandGoalFlash.value == flashText) {
                    _dynamicIslandGoalFlash.value = null
                }
            }

            localRepo.logNotification(
                NotificationLogEntity(
                    alertType = if (isFavoriteTeamGoal) "FAV_TEAM_GOAL" else "MAJOR_GOAL",
                    competition = match.competition.displayName,
                    headline = if (isFavoriteTeamGoal) "★ FAV GOAL! $scoreline" else "⚽ GOAL! $scoreline",
                    body = "$headline — $detail"
                )
            )

            showBanner(
                if (isFavoriteTeamGoal) "★ FAVORITE TEAM SCORED: $scoreline — $scorer!"
                else "⚽ GOAL ALERT SENT: $scoreline — $scorer!"
            )
        }
    }

    fun triggerMatchKickoffAlert(match: FootballMatch) {
        viewModelScope.launch {
            val matchTitle = "${match.homeTeam} vs ${match.awayTeam}"
            val serverName = _selectedMatchServer.value.name

            MatchNotificationHelper.sendKickoffNotification(
                context = getApplication(),
                competition = match.competition.displayName,
                matchTitle = matchTitle,
                venue = match.venue,
                streamServerName = serverName
            )

            localRepo.logNotification(
                NotificationLogEntity(
                    alertType = "KICKOFF",
                    competition = match.competition.displayName,
                    headline = "🔔 KICKOFF: $matchTitle",
                    body = "${match.competition.displayName} live from ${match.venue} on $serverName."
                )
            )

            showBanner("🔔 KICKOFF PUSH ALERT SENT: $matchTitle (${match.competition.shortTag})")
        }
    }

    fun triggerLiveStatEvent(matchId: String, statType: String, isHome: Boolean) {
        _matches.update { currentList ->
            currentList.map { match ->
                if (match.id != matchId) return@map match
                val teamName = if (isHome) match.homeTeam else match.awayTeam
                val keyPlayer = if (isHome) match.homeKeyPlayers.first() else match.awayKeyPlayers.first()

                when (statType.uppercase()) {
                    "POSSESSION" -> {
                        val nextHomePoss = if (isHome) {
                            (match.stats.homePossession + 3).coerceIn(25, 75)
                        } else {
                            (match.stats.homePossession - 3).coerceIn(25, 75)
                        }
                        val nextAwayPoss = 100 - nextHomePoss
                        match.copy(
                            stats = match.stats.copy(
                                homePossession = nextHomePoss,
                                awayPossession = nextAwayPoss,
                                homeMomentum = (nextHomePoss / 100f).coerceIn(0.2f, 0.8f)
                            )
                        )
                    }
                    "CORNER" -> {
                        val nextHomeCorners = if (isHome) match.stats.homeCorners + 1 else match.stats.homeCorners
                        val nextAwayCorners = if (!isHome) match.stats.awayCorners + 1 else match.stats.awayCorners
                        val cornerNote = CommentaryEvent(
                            id = "corner_${System.currentTimeMillis()}",
                            minute = match.minute,
                            type = CommentaryType.TACTICAL,
                            headline = "Corner Kick awarded to $teamName (${match.minute}')",
                            detail = "$keyPlayer steps up to whip an in-swinging corner delivery into the six-yard box."
                        )
                        match.copy(
                            stats = match.stats.copy(
                                homeCorners = nextHomeCorners,
                                awayCorners = nextAwayCorners,
                                homeXg = if (isHome) match.stats.homeXg + 0.06f else match.stats.homeXg,
                                awayXg = if (!isHome) match.stats.awayXg + 0.06f else match.stats.awayXg
                            ),
                            commentary = listOf(cornerNote) + match.commentary.take(14)
                        )
                    }
                    else -> {
                        // SHOT attempt
                        val nextHomeTotal = if (isHome) match.stats.homeTotalShots + 1 else match.stats.homeTotalShots
                        val nextAwayTotal = if (!isHome) match.stats.awayTotalShots + 1 else match.stats.awayTotalShots
                        val nextHomeOT = if (isHome) match.stats.homeShotsOnTarget + 1 else match.stats.homeShotsOnTarget
                        val nextAwayOT = if (!isHome) match.stats.awayShotsOnTarget + 1 else match.stats.awayShotsOnTarget
                        val shotNote = CommentaryEvent(
                            id = "shot_${System.currentTimeMillis()}",
                            minute = match.minute,
                            type = CommentaryType.MAJOR_CHANCE,
                            headline = "Shot Attempt on Target by $keyPlayer for $teamName (${match.minute}')",
                            detail = "$keyPlayer fires a stinging drive from 20 yards forcing a fingertip save!"
                        )
                        match.copy(
                            stats = match.stats.copy(
                                homeTotalShots = nextHomeTotal,
                                awayTotalShots = nextAwayTotal,
                                homeShotsOnTarget = nextHomeOT,
                                awayShotsOnTarget = nextAwayOT,
                                homeXg = if (isHome) match.stats.homeXg + 0.14f else match.stats.homeXg,
                                awayXg = if (!isHome) match.stats.awayXg + 0.14f else match.stats.awayXg
                            ),
                            commentary = listOf(shotNote) + match.commentary.take(14)
                        )
                    }
                }
            }
        }
        syncPinnedScoreboardNotification()
    }

    fun clearNotificationHistory() {
        viewModelScope.launch {
            localRepo.clearNotificationLogs()
        }
    }

    // --- Gemini AI Feature 1: Convert Text to Speech (gemini-3.8-flash-tts) ---
    fun setTtsVoice(voiceName: String) {
        _selectedTtsVoice.value = voiceName
    }

    fun speakTextWithGeminiTts(text: String) {
        if (!securityShield.checkRateLimitAllowed()) {
            showBanner("🛑 Anti-DDoS Rate Limiter: Please wait a moment before next request.")
            return
        }
        viewModelScope.launch {
            _isTtsLoading.value = true
            _ttsStatus.value = "Synthesizing live broadcast audio via ${GeminiRepository.MODEL_TTS_PRIMARY}..."
            val result = geminiRepo.speakCommentaryWithGeminiTts(
                textToSpeak = text,
                voiceName = _selectedTtsVoice.value
            )
            _isTtsLoading.value = false
            _ttsStatus.value = result.getOrElse { it.message ?: "TTS playback finished" }
        }
    }

    fun stopTtsAudio() {
        geminiRepo.stopSpeaking()
        _ttsStatus.value = "Audio broadcast paused"
    }

    // --- Gemini AI Feature 2: Google Search Grounding (gemini-3.8-flash) ---
    fun updateSearchGroundingQuery(query: String) {
        _searchGroundingQuery.value = query
    }

    fun executeGoogleSearchGrounding(customQuery: String? = null) {
        val rawQuery = (customQuery ?: _searchGroundingQuery.value).trim()
        if (rawQuery.isEmpty()) return

        val wafCheck = securityShield.inspectAndSanitizeInput(rawQuery, _wafFirewallEnabled.value)
        if (!wafCheck.isSafe) {
            testWafAttackPayload(rawQuery)
            return
        }
        if (!securityShield.checkRateLimitAllowed()) {
            showBanner("🛑 Anti-DDoS Rate Limiter: Request throttled.")
            return
        }

        _searchGroundingQuery.value = wafCheck.sanitizedText

        viewModelScope.launch {
            _isSearchingGrounding.value = true
            val result = geminiRepo.searchWithGoogleGrounding(wafCheck.sanitizedText)
            _searchGroundingResult.value = result
            _isSearchingGrounding.value = false
        }
    }

    // --- Gemini AI Feature 3: Analyze Images (gemini-4-argon) ---
    fun updateVisionBitmap(bitmap: Bitmap) {
        _selectedVisionBitmap.value = bitmap
    }

    fun loadSampleTacticalBoardBitmap() {
        _selectedVisionBitmap.value = createTacticalBoardBitmap()
    }

    fun updateVisionPrompt(prompt: String) {
        _visionPrompt.value = prompt
    }

    fun analyzeSelectedImage() {
        val wafCheck = securityShield.inspectAndSanitizeInput(_visionPrompt.value, _wafFirewallEnabled.value)
        if (!wafCheck.isSafe) {
            testWafAttackPayload(_visionPrompt.value)
            return
        }
        if (!securityShield.checkRateLimitAllowed()) {
            showBanner("🛑 Anti-DDoS Rate Limiter: Request throttled.")
            return
        }

        val bitmap = _selectedVisionBitmap.value ?: createTacticalBoardBitmap().also {
            _selectedVisionBitmap.value = it
        }
        viewModelScope.launch {
            _isAnalyzingImage.value = true
            val output = geminiRepo.analyzeImageWithGemini(
                bitmap = bitmap,
                userPrompt = wafCheck.sanitizedText
            )
            _visionAnalysisResult.value = output
            _isAnalyzingImage.value = false
        }
    }

    fun showUserMessage(message: String) {
        showBanner(message)
    }

    private fun showBanner(message: String) {
        viewModelScope.launch {
            _inAppAlertBanner.value = message
            delay(4_500L)
            if (_inAppAlertBanner.value == message) {
                _inAppAlertBanner.value = null
            }
        }
    }

    fun dismissBanner() {
        _inAppAlertBanner.value = null
    }

    private fun createTacticalBoardBitmap(): Bitmap {
        val width = 640
        val height = 380
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val turfPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, width.toFloat(), height.toFloat(),
                0xFF064E3B.toInt(), 0xFF042F2E.toInt(), Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), turfPaint)

        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0x88FFFFFF.toInt()
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        val pad = 28f
        canvas.drawRect(pad, pad, width - pad, height - pad, linePaint)
        canvas.drawLine(width / 2f, pad, width / 2f, height - pad, linePaint)
        canvas.drawCircle(width / 2f, height / 2f, 52f, linePaint)
        canvas.drawRect(pad, height * 0.25f, pad + 80f, height * 0.75f, linePaint)
        canvas.drawRect(width - pad - 80f, height * 0.25f, width - pad, height * 0.75f, linePaint)

        val homePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF00FF87.toInt()
            style = Paint.Style.FILL
        }
        val homePositions = listOf(
            60f to 190f,
            150f to 80f, 140f to 150f, 140f to 230f, 150f to 300f,
            260f to 110f, 240f to 190f, 260f to 270f,
            380f to 85f, 410f to 190f, 380f to 295f
        )
        homePositions.forEach { (x, y) ->
            canvas.drawCircle(x, y, 12f, homePaint)
        }

        val awayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF00E5FF.toInt()
            style = Paint.Style.FILL
        }
        val awayPositions = listOf(
            580f to 190f,
            490f to 85f, 505f to 155f, 505f to 225f, 490f to 295f,
            420f to 145f, 420f to 235f,
            330f to 95f, 340f to 190f, 330f to 285f,
            220f to 190f
        )
        awayPositions.forEach { (x, y) ->
            canvas.drawCircle(x, y, 12f, awayPaint)
        }

        val hudPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xCC070B14.toInt()
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(RectF(200f, 6f, 440f, 36f), 8f, 8f, hudPaint)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFFFFF.toInt()
            textSize = 18f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("UCL LIVE • RMA 2 - 2 MCI (78')", width / 2f, 27f, textPaint)

        return bitmap
    }

    override fun onCleared() {
        super.onCleared()
        geminiRepo.stopSpeaking()
    }
}
