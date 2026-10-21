package com.example.data.firestore

import android.content.Context
import android.util.Log
import com.example.R
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject

enum class OperationType(val value: String) {
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete"),
    LIST("list"),
    GET("get"),
    WRITE("write"),
}

fun handleFirestoreError(
    exception: Exception,
    operationType: OperationType,
    path: String?
): String {
    val auth = try {
        FirebaseAuth.getInstance()
    } catch (_: Exception) {
        null
    }
    val currentUser = auth?.currentUser

    val providerInfoList = currentUser?.providerData?.map { provider ->
        JSONObject().apply {
            put("providerId", provider.providerId)
            put("email", provider.email)
        }
    } ?: emptyList()

    val authInfoJson = JSONObject().apply {
        put("userId", currentUser?.uid)
        put("email", currentUser?.email)
        put("emailVerified", currentUser?.isEmailVerified)
        put("tenantId", currentUser?.tenantId)
        put("providerInfo", JSONArray(providerInfoList))
    }

    val errorInfoJson = JSONObject().apply {
        put("error", exception.message ?: exception.toString())
        put("operationType", operationType.value)
        put("path", path)
        put("authInfo", authInfoJson)
    }

    val jsonString = errorInfoJson.toString()
    Log.e("FirestoreError", "Firestore Error: $jsonString")
    return jsonString
}

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val username: String = "Leo Messi #10",
    val favoriteClub: String = "Inter Miami CF",
    val profilePictureId: String = "avatar_messi_goat",
    val photoUrl: String? = null,
    val selectedThemeId: String = "messi_monochrome_goat",
    val enabledLeagues: String = "UCL,EPL,LIGA,SERIE_A,BUNDESLIGA,INTER_MIAMI,INTL_FRIENDLY,INTL_CUP",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

class UserProfileRepository(private val db: FirebaseFirestore) {

    // Mandatory: Initialize with R.string.firestore_database_id
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth: FirebaseAuth
        get() = FirebaseAuth.getInstance()

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeUserProfile(userId: String): Flow<UserProfile?> {
        val path = "users/$userId"
        return db.collection("users").document(userId)
            .snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject(
                        UserProfile::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                } else {
                    null
                }
            }
            .catch { error ->
                if (error is Exception) {
                    handleFirestoreError(error, OperationType.GET, path)
                }
                throw error
            }
    }

    suspend fun getUserProfile(userId: String): Result<UserProfile?> {
        val docRef = db.collection("users").document(userId)
        return try {
            val snap = docRef.get(com.google.firebase.firestore.Source.SERVER).await()
            val profile = if (snap.exists()) {
                snap.toObject(
                    UserProfile::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            } else null
            Result.success(profile)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun createInitialProfile(
        username: String,
        favoriteClub: String,
        profilePictureId: String,
        photoUrl: String? = null,
        selectedThemeId: String = "messi_monochrome_goat",
        enabledLeagues: String = "UCL,EPL,LIGA,SERIE_A,BUNDESLIGA,INTER_MIAMI,INTL_FRIENDLY,INTL_CUP"
    ): Result<String> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        return try {
            val payload = mutableMapOf<String, Any>(
                "userId" to uid,
                "username" to username.take(60).ifBlank { "Football Fan" },
                "favoriteClub" to favoriteClub.take(60).ifBlank { "Inter Miami CF" },
                "profilePictureId" to profilePictureId.take(120).ifBlank { "avatar_messi_goat" },
                "selectedThemeId" to selectedThemeId.take(60).ifBlank { "messi_monochrome_goat" },
                "enabledLeagues" to enabledLeagues.take(300),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (!photoUrl.isNullOrBlank()) {
                payload["photoUrl"] = photoUrl.take(500)
            }
            docRef.set(payload).await()
            Result.success(uid)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun saveOrUpdateProfile(
        username: String,
        favoriteClub: String,
        profilePictureId: String,
        photoUrl: String? = null,
        selectedThemeId: String,
        enabledLeagues: String
    ): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        return try {
            val existingSnap = docRef.get().await()
            if (!existingSnap.exists()) {
                createInitialProfile(
                    username = username,
                    favoriteClub = favoriteClub,
                    profilePictureId = profilePictureId,
                    photoUrl = photoUrl,
                    selectedThemeId = selectedThemeId,
                    enabledLeagues = enabledLeagues
                ).map { }
            } else {
                val updates = mutableMapOf<String, Any>(
                    "username" to username.take(60).ifBlank { "Football Fan" },
                    "favoriteClub" to favoriteClub.take(60).ifBlank { "Inter Miami CF" },
                    "profilePictureId" to profilePictureId.take(120).ifBlank { "avatar_messi_goat" },
                    "selectedThemeId" to selectedThemeId.take(60).ifBlank { "messi_monochrome_goat" },
                    "enabledLeagues" to enabledLeagues.take(300),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                if (!photoUrl.isNullOrBlank()) {
                    updates["photoUrl"] = photoUrl.take(500)
                }
                docRef.update(updates).await()
                Result.success(Unit)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }
}
