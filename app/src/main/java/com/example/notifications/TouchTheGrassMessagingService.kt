package com.example.notifications

import android.content.Context
import com.example.data.local.FrameXDatabase
import com.example.data.local.NotificationLogEntity
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Firebase Cloud Messaging (FCM) service that receives real-time match kickoff and goal
 * push notifications for teams favorited in the user's profile and subscribed matches.
 */
class TouchTheGrassMessagingService : FirebaseMessagingService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        FcmPushManager.updateRegistrationToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val data = remoteMessage.data
        val notif = remoteMessage.notification

        val eventType = (data["event_type"] ?: data["type"] ?: "MAJOR_GOAL").uppercase()
        val teamName = data["team"] ?: data["favorite_club"] ?: "Favorite Club"
        val competition = data["competition"] ?: "Touchthegrass.com Live"
        val scoreline = data["scoreline"] ?: notif?.title ?: "$teamName Live Update"
        val headline = data["headline"] ?: notif?.title ?: "⚽ $teamName Match Alert"
        val detail = data["detail"] ?: notif?.body ?: "Live broadcast streaming in 4K on live.epicsports.in/?m=1"
        val venue = data["venue"] ?: "Main Stadium"
        val serverName = data["server"] ?: "Server 1 • EpicSports Live (live.epicsports.in)"

        FcmPushManager.routeFcmPayload(
            context = applicationContext,
            scope = serviceScope,
            eventType = eventType,
            teamName = teamName,
            competition = competition,
            scoreline = scoreline,
            headline = headline,
            detail = detail,
            venue = venue,
            serverName = serverName
        )
    }
}

/**
 * Coordinates Firebase Cloud Messaging (FCM) topic subscriptions for teams favorited
 * in the user's profile (both `_goals` and `_kickoffs` topics) and token registration.
 */
object FcmPushManager {

    private val _fcmToken = MutableStateFlow("fcm_ttg_live_token_initializing")
    val fcmToken: StateFlow<String> = _fcmToken.asStateFlow()

    private val _subscribedTopics = MutableStateFlow<Set<String>>(emptySet())
    val subscribedTopics: StateFlow<Set<String>> = _subscribedTopics.asStateFlow()

    private val _favoritedTeams = MutableStateFlow(setOf("Inter Miami CF", "FC Barcelona"))
    val favoritedTeams: StateFlow<Set<String>> = _favoritedTeams.asStateFlow()

    fun initializeFcm(context: Context, initialFavoriteClub: String) {
        MatchNotificationHelper.ensureChannelsCreated(context)
        syncPrimaryFavoriteClub(initialFavoriteClub)

        runCatching {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    if (!token.isNullOrBlank()) {
                        _fcmToken.value = token
                    }
                } else {
                    _fcmToken.value = "fcm_ttg_${initialFavoriteClub.toTopicSlug()}_active"
                }
            }
        }.onFailure {
            _fcmToken.value = "fcm_ttg_${initialFavoriteClub.toTopicSlug()}_active"
        }
    }

    fun updateRegistrationToken(token: String) {
        if (token.isNotBlank()) {
            _fcmToken.value = token
        }
    }

    /**
     * Ensures the primary favorite club selected in the user's Profile is subscribed
     * to both FCM `_goals` and `_kickoffs` topics.
     */
    fun syncPrimaryFavoriteClub(clubName: String) {
        val clean = clubName.trim()
        if (clean.isEmpty()) return
        _favoritedTeams.update { current -> current + clean }
        subscribeClubToFcmTopics(clean)
    }

    fun toggleFavoriteTeamFcm(clubName: String): Boolean {
        val clean = clubName.trim()
        if (clean.isEmpty()) return false
        val isCurrentlyFavorited = _favoritedTeams.value.any { it.equals(clean, ignoreCase = true) }
        return if (isCurrentlyFavorited && _favoritedTeams.value.size > 1) {
            _favoritedTeams.update { set ->
                set.filterNot { it.equals(clean, ignoreCase = true) }.toSet()
            }
            unsubscribeClubFromFcmTopics(clean)
            false
        } else {
            _favoritedTeams.update { it + clean }
            subscribeClubToFcmTopics(clean)
            true
        }
    }

    fun isTeamFavorited(teamName: String, primaryFavoriteClub: String): Boolean {
        if (teamName.contains(primaryFavoriteClub, ignoreCase = true) ||
            primaryFavoriteClub.contains(teamName, ignoreCase = true)
        ) {
            return true
        }
        return _favoritedTeams.value.any { fav ->
            teamName.contains(fav, ignoreCase = true) || fav.contains(teamName, ignoreCase = true)
        }
    }

    private fun subscribeClubToFcmTopics(clubName: String) {
        val slug = clubName.toTopicSlug()
        val goalsTopic = "club_${slug}_goals"
        val kickoffsTopic = "club_${slug}_kickoffs"

        _subscribedTopics.update { it + goalsTopic + kickoffsTopic }

        runCatching {
            val messaging = FirebaseMessaging.getInstance()
            messaging.subscribeToTopic(goalsTopic)
            messaging.subscribeToTopic(kickoffsTopic)
        }
    }

    private fun unsubscribeClubFromFcmTopics(clubName: String) {
        val slug = clubName.toTopicSlug()
        val goalsTopic = "club_${slug}_goals"
        val kickoffsTopic = "club_${slug}_kickoffs"

        _subscribedTopics.update { it - goalsTopic - kickoffsTopic }

        runCatching {
            val messaging = FirebaseMessaging.getInstance()
            messaging.unsubscribeFromTopic(goalsTopic)
            messaging.unsubscribeFromTopic(kickoffsTopic)
        }
    }

    fun routeFcmPayload(
        context: Context,
        scope: CoroutineScope,
        eventType: String,
        teamName: String,
        competition: String,
        scoreline: String,
        headline: String,
        detail: String,
        venue: String,
        serverName: String
    ) {
        val isKickoff = eventType.contains("KICKOFF", ignoreCase = true)
        if (isKickoff) {
            MatchNotificationHelper.sendKickoffNotification(
                context = context,
                competition = competition,
                matchTitle = scoreline,
                venue = venue,
                streamServerName = serverName
            )
        } else {
            MatchNotificationHelper.sendMajorGoalNotification(
                context = context,
                competition = competition,
                scoreline = scoreline,
                headline = headline,
                detail = detail
            )
        }

        scope.launch {
            runCatching {
                val dao = FrameXDatabase.getInstance(context).frameXDao()
                dao.insertNotificationLog(
                    NotificationLogEntity(
                        alertType = if (isKickoff) "FCM_KICKOFF" else "FCM_GOAL",
                        competition = "$competition • ★ $teamName",
                        headline = if (isKickoff) "🔔 FCM KICKOFF: $scoreline" else "⚽ FCM GOAL: $scoreline",
                        body = "$headline — $detail"
                    )
                )
            }
        }
    }

    fun String.toTopicSlug(): String {
        return lowercase()
            .replace(Regex("[^a-z0-9]+"), "_")
            .trim('_')
            .ifBlank { "football_club" }
    }
}
