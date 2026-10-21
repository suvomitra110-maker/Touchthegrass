package com.example.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.model.FootballMatch

object MatchNotificationHelper {

    const val CHANNEL_MAJOR_GOALS = "framextv_major_goals_channel"
    const val CHANNEL_KICKOFF_ALERTS = "framextv_kickoff_channel"
    const val CHANNEL_PINNED_SCOREBOARD = "framextv_pinned_scoreboard_channel"
    const val PINNED_SCOREBOARD_NOTIFICATION_ID = 91010

    fun ensureChannelsCreated(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val goalChannel = NotificationChannel(
                CHANNEL_MAJOR_GOALS,
                "Major Goal & Favorite Team Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Real-time push alerts when your favorite team scores or major goals happen across UCL, EPL, LaLiga, Serie A, Bundesliga & MLS"
                enableVibration(true)
            }

            val kickoffChannel = NotificationChannel(
                CHANNEL_KICKOFF_ALERTS,
                "Match Kickoff & Lineup Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when subscribed live football matches are kicking off"
                enableVibration(true)
            }

            val pinnedScoreboardChannel = NotificationChannel(
                CHANNEL_PINNED_SCOREBOARD,
                "Pinned Live Scoreboard (Dynamic Island & Status Bar)",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Persistent live scoreboard pinned to the Android notification bar for your favorite team or any pinned match"
                enableVibration(false)
                setSound(null, null)
            }

            manager.createNotificationChannel(goalChannel)
            manager.createNotificationChannel(kickoffChannel)
            manager.createNotificationChannel(pinnedScoreboardChannel)
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun sendMajorGoalNotification(
        context: Context,
        competition: String,
        scoreline: String,
        headline: String,
        detail: String,
        isFavoriteTeam: Boolean = false
    ): Boolean {
        ensureChannelsCreated(context)
        if (!hasNotificationPermission(context)) return false

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            scoreline.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val titlePrefix = if (isFavoriteTeam) "★ FAVORITE TEAM GOAL!" else "⚽ GOAL!"
        val summaryPrefix = if (isFavoriteTeam) "Touchthegrass.com • ★ Favorite Club Alert" else "Touchthegrass.com Live Football"

        val notification = NotificationCompat.Builder(context, CHANNEL_MAJOR_GOALS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("$titlePrefix [$competition] $scoreline")
            .setContentText(headline)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$headline\n$detail")
                    .setSummaryText("$summaryPrefix • $competition")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        return try {
            NotificationManagerCompat.from(context)
                .notify((System.currentTimeMillis() % 100000).toInt(), notification)
            true
        } catch (e: SecurityException) {
            false
        }
    }

    fun showOrUpdatePinnedScoreboardNotification(
        context: Context,
        match: FootballMatch,
        isFavoriteClub: Boolean,
        streamServerName: String
    ): Boolean {
        ensureChannelsCreated(context)
        if (!hasNotificationPermission(context)) return false

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            PINNED_SCOREBOARD_NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val minuteBadge = if (match.addedTime > 0) "${match.minute}+${match.addedTime}'" else "${match.minute}'"
        val pinBadge = if (isFavoriteClub) "★ FAV LIVE" else "📌 PINNED LIVE"
        val title = "$pinBadge • ${match.homeShort} ${match.homeScore} - ${match.awayScore} ${match.awayShort} ($minuteBadge)"
        val latestEvent = match.commentary.firstOrNull()?.headline ?: "Live at ${match.venue}"
        val statsLine = "${match.competition.shortTag} • Poss: ${match.stats.homePossession}%-${match.stats.awayPossession}% • xG: ${String.format("%.2f", match.stats.homeXg)}-${String.format("%.2f", match.stats.awayXg)}"

        val notification = NotificationCompat.Builder(context, CHANNEL_PINNED_SCOREBOARD)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText("$statsLine • $latestEvent")
            .setSubText(match.competition.shortTag)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .setBigContentTitle("${match.homeTeam} ${match.homeScore} - ${match.awayScore} ${match.awayTeam} ($minuteBadge)")
                    .bigText("$statsLine\nLatest: $latestEvent\nStream: $streamServerName (live.epicsports.in/?m=1)")
                    .setSummaryText("$pinBadge • Touchthegrass.com Dynamic Scoreboard")
            )
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)
            .build()

        return try {
            NotificationManagerCompat.from(context)
                .notify(PINNED_SCOREBOARD_NOTIFICATION_ID, notification)
            true
        } catch (e: SecurityException) {
            false
        }
    }

    fun cancelPinnedScoreboardNotification(context: Context) {
        try {
            NotificationManagerCompat.from(context).cancel(PINNED_SCOREBOARD_NOTIFICATION_ID)
        } catch (_: SecurityException) {
        }
    }

    fun sendKickoffNotification(
        context: Context,
        competition: String,
        matchTitle: String,
        venue: String,
        streamServerName: String
    ): Boolean {
        ensureChannelsCreated(context)
        if (!hasNotificationPermission(context)) return false

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            matchTitle.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_KICKOFF_ALERTS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("🔔 KICKOFF ALERT: $matchTitle")
            .setContentText("$competition is kicking off at $venue • Streaming in 4K on $streamServerName")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$matchTitle is now LIVE at $venue!\nStream live in 4K HDR on $streamServerName with real-time commentary.")
                    .setSummaryText("FrameXTV Kickoff Alert • $competition")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        return try {
            NotificationManagerCompat.from(context)
                .notify(((System.currentTimeMillis() + 500) % 100000).toInt(), notification)
            true
        } catch (e: SecurityException) {
            false
        }
    }
}
