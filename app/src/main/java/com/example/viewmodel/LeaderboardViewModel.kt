package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.R
import com.example.data.remote.formatUserFacingFirestoreError
import com.example.data.remote.unwrapFirestoreException
import com.example.data.repository.LeaderboardRepository
import com.example.model.LeaderboardEntry
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface LeaderboardUiState {
    data object Loading : LeaderboardUiState
    data class Success(
        val entries: List<LeaderboardEntry>,
        val currentUserId: String
    ) : LeaderboardUiState
    data class Error(val message: String) : LeaderboardUiState
}

sealed interface LeaderboardSyncStatus {
    data object Idle : LeaderboardSyncStatus
    data object Syncing : LeaderboardSyncStatus
    data class Synced(val timestampMs: Long) : LeaderboardSyncStatus
    data class Failed(val message: String) : LeaderboardSyncStatus
}

@OptIn(ExperimentalCoroutinesApi::class)
class LeaderboardViewModel(
    private val repository: LeaderboardRepository,
    private val currentUserId: String
) : ViewModel() {

    private val retryTrigger = MutableStateFlow(0)

    private val _syncStatus = MutableStateFlow<LeaderboardSyncStatus>(LeaderboardSyncStatus.Idle)
    val syncStatus: StateFlow<LeaderboardSyncStatus> = _syncStatus.asStateFlow()

    val uiState: StateFlow<LeaderboardUiState> = retryTrigger
        .flatMapLatest {
            repository.observeTopMiners(limit = 50)
                .map<List<LeaderboardEntry>, LeaderboardUiState> { entries ->
                    LeaderboardUiState.Success(
                        entries = entries,
                        currentUserId = currentUserId
                    )
                }
                .onStart { emit(LeaderboardUiState.Loading) }
                .catch { throwable ->
                    val firestoreEx = unwrapFirestoreException(throwable)
                    if (throwable is CancellationException && firestoreEx == null) {
                        throw throwable
                    }
                    emit(LeaderboardUiState.Error(formatUserFacingFirestoreError(throwable)))
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LeaderboardUiState.Loading
        )

    fun retry() {
        retryTrigger.value += 1
    }

    fun syncPlayerScore(
        displayName: String,
        totalBtcMined: Double,
        hashRateGh: Double,
        activeMiners: Int
    ) {
        if (_syncStatus.value is LeaderboardSyncStatus.Syncing) return
        viewModelScope.launch {
            _syncStatus.value = LeaderboardSyncStatus.Syncing
            try {
                repository.syncPlayerScore(
                    displayName = displayName,
                    totalBtcMined = totalBtcMined,
                    hashRateGh = hashRateGh,
                    activeMiners = activeMiners
                ).getOrThrow()
                _syncStatus.value = LeaderboardSyncStatus.Synced(System.currentTimeMillis())
            } catch (e: Exception) {
                val firestoreEx = unwrapFirestoreException(e)
                if (e is CancellationException && firestoreEx == null) throw e
                _syncStatus.value = LeaderboardSyncStatus.Failed(formatUserFacingFirestoreError(e))
            }
        }
    }

    companion object {
        fun provideFactory(context: Context, userId: String): ViewModelProvider.Factory {
            val appContext = context.applicationContext
            val databaseId = appContext.getString(R.string.firestore_database_id)
            val firestore = FirebaseFirestore.getInstance(databaseId)
            val auth = Firebase.auth
            return viewModelFactory {
                initializer {
                    LeaderboardViewModel(
                        repository = LeaderboardRepository(firestore = firestore, auth = auth),
                        currentUserId = userId
                    )
                }
            }
        }
    }
}
