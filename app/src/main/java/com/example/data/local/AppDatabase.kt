package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "match_alerts")
data class MatchAlertEntity(
    @PrimaryKey val matchId: String,
    val matchLabel: String,
    val competitionName: String,
    val notifyMajorGoals: Boolean = true,
    val notifyKickoff: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "watchlist_media")
data class WatchlistMediaEntity(
    @PrimaryKey val mediaId: String,
    val title: String,
    val categoryLabel: String,
    val qualityBadge: String,
    val rating: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notification_logs")
data class NotificationLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val alertType: String, // "MAJOR_GOAL" or "KICKOFF"
    val competition: String,
    val headline: String,
    val body: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "security_audit_logs")
data class SecurityAuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val eventType: String, // "WAF_BLOCKED", "KEYSTORE_ROTATION", "RASP_AUDIT", "SHIELD_CONFIG"
    val severity: String,  // "CRITICAL_BLOCKED", "HIGH_SECURITY", "VERIFIED"
    val title: String,
    val detail: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface FrameXDao {
    @Query("SELECT * FROM match_alerts ORDER BY updatedAt DESC")
    fun getAllMatchAlerts(): Flow<List<MatchAlertEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMatchAlert(alert: MatchAlertEntity)

    @Query("DELETE FROM match_alerts WHERE matchId = :matchId")
    suspend fun deleteMatchAlert(matchId: String)

    @Query("SELECT * FROM watchlist_media ORDER BY addedAt DESC")
    fun getAllWatchlistMedia(): Flow<List<WatchlistMediaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchlistMedia(item: WatchlistMediaEntity)

    @Query("DELETE FROM watchlist_media WHERE mediaId = :mediaId")
    suspend fun removeWatchlistMedia(mediaId: String)

    @Query("SELECT * FROM notification_logs ORDER BY timestamp DESC LIMIT 50")
    fun getNotificationLogs(): Flow<List<NotificationLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotificationLog(log: NotificationLogEntity)

    @Query("DELETE FROM notification_logs")
    suspend fun clearNotificationLogs()

    @Query("SELECT * FROM security_audit_logs ORDER BY timestamp DESC LIMIT 60")
    fun getSecurityAuditLogs(): Flow<List<SecurityAuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSecurityAuditLog(log: SecurityAuditLogEntity)

    @Query("DELETE FROM security_audit_logs")
    suspend fun clearSecurityAuditLogs()
}

@Database(
    entities = [
        MatchAlertEntity::class,
        WatchlistMediaEntity::class,
        NotificationLogEntity::class,
        SecurityAuditLogEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class FrameXDatabase : RoomDatabase() {
    abstract fun frameXDao(): FrameXDao

    companion object {
        @Volatile
        private var INSTANCE: FrameXDatabase? = null

        fun getInstance(context: Context): FrameXDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FrameXDatabase::class.java,
                    "framextv_local.db"
                ).fallbackToDestructiveMigration(true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class LocalRepository(private val dao: FrameXDao) {
    val matchAlerts: Flow<List<MatchAlertEntity>> = dao.getAllMatchAlerts()
    val watchlist: Flow<List<WatchlistMediaEntity>> = dao.getAllWatchlistMedia()
    val notificationLogs: Flow<List<NotificationLogEntity>> = dao.getNotificationLogs()
    val securityAuditLogs: Flow<List<SecurityAuditLogEntity>> = dao.getSecurityAuditLogs()

    suspend fun upsertMatchAlert(alert: MatchAlertEntity) = dao.upsertMatchAlert(alert)
    suspend fun deleteMatchAlert(matchId: String) = dao.deleteMatchAlert(matchId)
    suspend fun addToWatchlist(item: WatchlistMediaEntity) = dao.insertWatchlistMedia(item)
    suspend fun removeFromWatchlist(mediaId: String) = dao.removeWatchlistMedia(mediaId)
    suspend fun logNotification(log: NotificationLogEntity) = dao.insertNotificationLog(log)
    suspend fun clearNotificationLogs() = dao.clearNotificationLogs()
    suspend fun logSecurityEvent(log: SecurityAuditLogEntity) = dao.insertSecurityAuditLog(log)
    suspend fun clearSecurityLogs() = dao.clearSecurityAuditLogs()
}
