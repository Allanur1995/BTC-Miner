package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class LeaderboardRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun syncAndObserveTopMiners_authenticatedUsers_ranksDescendingByTotalSats() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = LeaderboardRepository(firestore, auth)
        withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.syncPlayerScore(
                displayName = "AliceMiner",
                totalBtcMined = 0.00001500,
                hashRateGh = 45.0,
                activeMiners = 2
            ).getOrThrow()
        }

        val bobUid = signInTestUser(BOB_EMAIL)
        val bobRepo = LeaderboardRepository(firestore, auth)
        withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.syncPlayerScore(
                displayName = "BobWhale",
                totalBtcMined = 0.00050000,
                hashRateGh = 320.0,
                activeMiners = 5
            ).getOrThrow()
        }

        val emitted = withTimeout(FLOW_TIMEOUT_MS) {
            bobRepo.observeTopMiners(50).first { list ->
                list.any { it.userId == aliceUid } && list.any { it.userId == bobUid }
            }
        }

        val bobIndex = emitted.indexOfFirst { it.userId == bobUid }
        val aliceIndex = emitted.indexOfFirst { it.userId == aliceUid }
        assertTrue("Bob should rank above Alice", bobIndex in 0 until aliceIndex)
        assertEquals(bobUid, emitted[bobIndex].id)
        assertEquals(50_000L, emitted[bobIndex].totalSatsMined)
        assertNotNull(emitted[bobIndex].updatedAt)
    }

    @Test
    fun syncPlayerScore_twice_updatesScoreWhilePreservingCreatedAt() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repo = LeaderboardRepository(firestore, auth)

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.syncPlayerScore(
                displayName = "AliceV1",
                totalBtcMined = 0.00002000,
                hashRateGh = 50.0,
                activeMiners = 2
            ).getOrThrow()
        }
        val firstEntry = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.getPlayerEntry(aliceUid).getOrThrow()
        }
        assertNotNull(firstEntry)
        val originalCreatedAt = firstEntry!!.createdAt

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.syncPlayerScore(
                displayName = "AliceV2",
                totalBtcMined = 0.00008000,
                hashRateGh = 110.0,
                activeMiners = 3
            ).getOrThrow()
        }
        val updatedEntry = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.getPlayerEntry(aliceUid).getOrThrow()
        }
        assertNotNull(updatedEntry)
        assertEquals("AliceV2", updatedEntry!!.displayName)
        assertEquals(8_000L, updatedEntry.totalSatsMined)
        assertEquals(originalCreatedAt, updatedEntry.createdAt)
    }

    @Test
    fun crossUserWrite_failsWithPermissionDenied() = runBlocking {
        val bobUid = signInTestUser(BOB_EMAIL)
        val aliceUid = signInTestUser(ALICE_EMAIL)

        val spoofPayload = hashMapOf<String, Any>(
            "userId" to aliceUid,
            "displayName" to "Hijacker",
            "totalSatsMined" to 9999999L,
            "totalBtcMined" to 0.09999999,
            "hashRateGh" to 9999.0,
            "activeMiners" to 10,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                firestore.collection(LeaderboardRepository.COLLECTION_LEADERBOARD)
                    .document(bobUid)
                    .set(spoofPayload)
                    .await()
            }
            fail("Expected PERMISSION_DENIED when Alice writes to Bob's document")
        } catch (e: Exception) {
            val fsEx = e.unwrapFirestoreException()
            assertNotNull("Expected FirebaseFirestoreException, got $e", fsEx)
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx!!.code)
        }
    }

    @Test
    fun redTeam_writeWithGhostField_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val ghostPayload = hashMapOf<String, Any>(
            "userId" to aliceUid,
            "displayName" to "GhostTester",
            "totalSatsMined" to 100L,
            "totalBtcMined" to 0.000001,
            "hashRateGh" to 5.0,
            "activeMiners" to 1,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
            "isAdmin" to true
        )

        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                firestore.collection(LeaderboardRepository.COLLECTION_LEADERBOARD)
                    .document(aliceUid)
                    .set(ghostPayload)
                    .await()
            }
            fail("Expected PERMISSION_DENIED when writing undeclared ghost field")
        } catch (e: Exception) {
            val fsEx = e.unwrapFirestoreException()
            assertNotNull("Expected FirebaseFirestoreException, got $e", fsEx)
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx!!.code)
        }
    }

    @Test
    fun observeTopMiners_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repo = LeaderboardRepository(firestore, auth)

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repo.observeTopMiners().first()
            }
            fail("Expected unauthenticated snapshot listener to fail with PERMISSION_DENIED")
        } catch (e: Exception) {
            val fsEx = e.unwrapFirestoreException()
            assertNotNull("Expected nested FirebaseFirestoreException, got $e", fsEx)
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx!!.code)
        }
    }

    private companion object {
        const val ALICE_EMAIL = "alice_miner@test.com"
        const val BOB_EMAIL = "bob_miner@test.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
