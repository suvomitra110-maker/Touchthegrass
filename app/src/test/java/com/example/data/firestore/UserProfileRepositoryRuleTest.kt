package com.example.data.firestore

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import java.io.File
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UserProfileRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createAndObserveUserProfile_authenticatedOwner_succeeds() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repo = UserProfileRepository(firestore)

        val saveResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.saveOrUpdateProfile(
                username = "Leo Messi #10",
                favoriteClub = "Inter Miami CF",
                profilePictureId = "avatar_messi_goat",
                photoUrl = null,
                selectedThemeId = "messi_monochrome_goat",
                enabledLeagues = "UCL,EPL,LIGA"
            )
        }
        assertTrue("Save failed: ${saveResult.exceptionOrNull()}", saveResult.isSuccess)

        val observed = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeUserProfile(aliceUid).first { it != null && it.username == "Leo Messi #10" }
        }
        assertNotNull(observed)
        assertEquals("Inter Miami CF", observed?.favoriteClub)
    }

    @Test
    fun getUserProfile_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = UserProfileRepository(firestore)
        withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.saveOrUpdateProfile(
                username = "Alice",
                favoriteClub = "FC Barcelona",
                profilePictureId = "avatar_messi_goat",
                photoUrl = null,
                selectedThemeId = "messi_monochrome_goat",
                enabledLeagues = "UCL,LIGA"
            ).getOrThrow()
        }

        val bobUid = signInTestUser(BOB_EMAIL)
        val bobRepo = UserProfileRepository(firestore)
        val crossRead = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.getUserProfile(aliceUid)
        }
        val logTail = try {
            File("/tmp/firebase-emulators.log").takeIf { it.exists() }?.readLines()?.takeLast(15)?.joinToString(" | ")
        } catch (_: Exception) {
            null
        }
        assertTrue(
            "Expected cross-user read to fail! aliceUid=$aliceUid, bobUid=$bobUid, currentAuth=${auth.currentUser?.uid}, db=$databaseId, result=${crossRead.getOrNull()}, log=$logTail",
            crossRead.isFailure
        )
        val exception = crossRead.exceptionOrNull() as? FirebaseFirestoreException
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, exception?.code)
    }

    @Test
    fun getUserProfile_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repo = UserProfileRepository(firestore)
        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.saveOrUpdateProfile(
                username = "Alice",
                favoriteClub = "FC Barcelona",
                profilePictureId = "avatar_messi_goat",
                photoUrl = null,
                selectedThemeId = "messi_monochrome_goat",
                enabledLeagues = "UCL"
            ).getOrThrow()
        }

        auth.signOut()
        delay(100)
        val unauthRead = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.getUserProfile(aliceUid)
        }
        assertTrue("Expected unauth read to fail! result=${unauthRead.getOrNull()}", unauthRead.isFailure)
        val exception = unauthRead.exceptionOrNull() as? FirebaseFirestoreException
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, exception?.code)
    }

    private companion object {
        const val ALICE_EMAIL = "alice_framex@test.com"
        const val BOB_EMAIL = "bob_framex@test.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
