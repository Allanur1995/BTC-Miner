package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.remote.OperationType
import com.example.data.remote.handleFirestoreError
import com.example.model.LeaderboardEntry
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlin.math.roundToLong

class LeaderboardRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    constructor(context: Context) : this(
        firestore = FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        auth = Firebase.auth
    )

    fun observeTopMiners(limit: Int = 50): Flow<List<LeaderboardEntry>> {
        val safeLimit = limit.coerceIn(1, 100).toLong()
        return firestore.collection(COLLECTION_LEADERBOARD)
            .whereGreaterThanOrEqualTo(FIELD_TOTAL_SATS, 0L)
            .orderBy(FIELD_TOTAL_SATS, Query.Direction.DESCENDING)
            .limit(safeLimit)
            .snapshots()
            .map { querySnapshot ->
                querySnapshot.documents.mapNotNull { doc ->
                    doc.toObject(
                        LeaderboardEntry::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
            }
            .catch { exception ->
                handleFirestoreError(exception, OperationType.LIST, COLLECTION_LEADERBOARD)
            }
    }

    suspend fun getTopMinersOnce(limit: Int = 50): Result<List<LeaderboardEntry>> {
        val safeLimit = limit.coerceIn(1, 100).toLong()
        return runCatching {
            val snapshot = firestore.collection(COLLECTION_LEADERBOARD)
                .whereGreaterThanOrEqualTo(FIELD_TOTAL_SATS, 0L)
                .orderBy(FIELD_TOTAL_SATS, Query.Direction.DESCENDING)
                .limit(safeLimit)
                .get()
                .await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(
                    LeaderboardEntry::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            }
        }.onFailure { exception ->
            handleFirestoreError(exception, OperationType.LIST, COLLECTION_LEADERBOARD)
        }
    }

    suspend fun getPlayerEntry(userId: String): Result<LeaderboardEntry?> {
        val path = "$COLLECTION_LEADERBOARD/$userId"
        return runCatching {
            val doc = firestore.collection(COLLECTION_LEADERBOARD)
                .document(userId)
                .get()
                .await()
            if (!doc.exists()) {
                null
            } else {
                doc.toObject(
                    LeaderboardEntry::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            }
        }.onFailure { exception ->
            handleFirestoreError(exception, OperationType.GET, path)
        }
    }

    suspend fun syncPlayerScore(
        displayName: String,
        totalBtcMined: Double,
        hashRateGh: Double,
        activeMiners: Int
    ): Result<Unit> {
        val currentUser = auth.currentUser
            ?: return Result.failure(IllegalStateException("Must be signed in to sync leaderboard score."))

        val uid = currentUser.uid
        val path = "$COLLECTION_LEADERBOARD/$uid"
        val cleanName = displayName.trim()
            .ifBlank { currentUser.displayName?.trim().orEmpty() }
            .ifBlank { "Miner-${uid.take(5)}" }
            .take(40)
        val safeBtc = totalBtcMined.coerceIn(0.0, 21_000_000.0)
        val safeSats = (safeBtc * 100_000_000.0).roundToLong().coerceIn(0L, 2_100_000_000_000_000L)
        val safeHashRate = hashRateGh.coerceIn(0.0, 1_000_000_000.0)
        val safeMiners = activeMiners.coerceIn(0, 1000)

        val docRef = firestore.collection(COLLECTION_LEADERBOARD).document(uid)

        return runCatching {
            firestore.runTransaction { transaction ->
                val existing = transaction.get(docRef)
                if (!existing.exists()) {
                    val createPayload = hashMapOf<String, Any>(
                        FIELD_USER_ID to uid,
                        FIELD_DISPLAY_NAME to cleanName,
                        FIELD_TOTAL_SATS to safeSats,
                        FIELD_TOTAL_BTC to safeBtc,
                        FIELD_HASH_RATE to safeHashRate,
                        FIELD_ACTIVE_MINERS to safeMiners,
                        FIELD_CREATED_AT to FieldValue.serverTimestamp(),
                        FIELD_UPDATED_AT to FieldValue.serverTimestamp()
                    )
                    transaction.set(docRef, createPayload)
                } else {
                    val updatePayload = hashMapOf<String, Any>(
                        FIELD_DISPLAY_NAME to cleanName,
                        FIELD_TOTAL_SATS to safeSats,
                        FIELD_TOTAL_BTC to safeBtc,
                        FIELD_HASH_RATE to safeHashRate,
                        FIELD_ACTIVE_MINERS to safeMiners,
                        FIELD_UPDATED_AT to FieldValue.serverTimestamp()
                    )
                    transaction.update(docRef, updatePayload)
                }
            }.await()
            Unit
        }.onFailure { exception ->
            handleFirestoreError(exception, OperationType.WRITE, path)
        }
    }

    suspend fun deleteMyEntry(): Result<Unit> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("Must be signed in."))
        val path = "$COLLECTION_LEADERBOARD/$uid"
        return runCatching {
            firestore.collection(COLLECTION_LEADERBOARD).document(uid).delete().await()
            Unit
        }.onFailure { exception ->
            handleFirestoreError(exception, OperationType.DELETE, path)
        }
    }

    companion object {
        const val COLLECTION_LEADERBOARD = "leaderboard"
        const val FIELD_USER_ID = "userId"
        const val FIELD_DISPLAY_NAME = "displayName"
        const val FIELD_TOTAL_SATS = "totalSatsMined"
        const val FIELD_TOTAL_BTC = "totalBtcMined"
        const val FIELD_HASH_RATE = "hashRateGh"
        const val FIELD_ACTIVE_MINERS = "activeMiners"
        const val FIELD_CREATED_AT = "createdAt"
        const val FIELD_UPDATED_AT = "updatedAt"
    }
}
