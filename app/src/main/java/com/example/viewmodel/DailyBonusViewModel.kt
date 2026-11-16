package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.CryptoMinerApplication
import com.example.data.repository.ClaimResult
import com.example.data.repository.DailyBonusRepository
import com.example.di.DatabaseModule
import com.example.model.DailyBonusDay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

data class DailyBonusUiState(
    val days: List<DailyBonusDay> = emptyList(),
    val currentStreakDay: Int = 1,
    val canClaimToday: Boolean = true,
    val remainingTimeText: String = "Ready to Claim!",
    val remainingMillis: Long = 0L,
    val lastClaimTimestamp: Long = 0L,
    val statusBanner: String? = null,
    val isClaimSuccess: Boolean = false
)

class DailyBonusViewModel(
    application: Application,
    private val repository: DailyBonusRepository = (application as? CryptoMinerApplication)?.appContainer?.dailyBonusRepository
        ?: DatabaseModule.provideDailyBonusRepository(application)
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(DailyBonusUiState())
    val uiState: StateFlow<DailyBonusUiState> = _uiState.asStateFlow()

    init {
        observeCheckInData()
        startCountdownTimer()
    }

    private fun observeCheckInData() {
        viewModelScope.launch(Dispatchers.IO) {
            val initial = repository.getOrInitCheckIn()
            updateUiFromEntity(initial.currentStreakDay, initial.lastClaimTimestamp)

            repository.checkInFlow.collect { checkIn ->
                if (checkIn != null) {
                    updateUiFromEntity(checkIn.currentStreakDay, checkIn.lastClaimTimestamp)
                }
            }
        }
    }

    private fun updateUiFromEntity(streakDay: Int, lastClaimTimestamp: Long) {
        val now = System.currentTimeMillis()
        val canClaim = repository.canClaim(lastClaimTimestamp, now)
        val remaining = repository.getRemainingMillis(lastClaimTimestamp, now)

        val updatedDays = DailyBonusRepository.REWARD_SCHEDULE.map { scheduleDay ->
            val isClaimed = if (canClaim) {
                scheduleDay.day < streakDay
            } else {
                scheduleDay.day <= streakDay
            }
            val isCurrent = scheduleDay.day == streakDay

            scheduleDay.copy(
                isClaimed = isClaimed,
                isCurrent = isCurrent
            )
        }

        _uiState.update { current ->
            current.copy(
                days = updatedDays,
                currentStreakDay = streakDay,
                canClaimToday = canClaim,
                remainingMillis = remaining,
                remainingTimeText = formatRemainingTime(remaining),
                lastClaimTimestamp = lastClaimTimestamp
            )
        }
    }

    private fun startCountdownTimer() {
        viewModelScope.launch {
            while (isActive) {
                delay(1000) // update countdown every second
                val lastClaim = _uiState.value.lastClaimTimestamp
                val now = System.currentTimeMillis()
                val remaining = repository.getRemainingMillis(lastClaim, now)
                val canClaim = remaining <= 0L

                _uiState.update {
                    it.copy(
                        canClaimToday = canClaim,
                        remainingMillis = remaining,
                        remainingTimeText = formatRemainingTime(remaining)
                    )
                }
            }
        }
    }

    private fun formatRemainingTime(millis: Long): String {
        if (millis <= 0L) return "Ready to Claim!"
        val totalSecs = millis / 1000
        val hours = totalSecs / 3600
        val minutes = (totalSecs % 3600) / 60
        val seconds = totalSecs % 60
        return String.format(Locale.US, "Next bonus in %02d:%02d:%02d", hours, minutes, seconds)
    }

    fun claimTodayBonus() {
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = repository.claimDailyBonus()) {
                is ClaimResult.Success -> {
                    _uiState.update {
                        it.copy(
                            statusBanner = result.message,
                            isClaimSuccess = true
                        )
                    }
                }
                is ClaimResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            statusBanner = result.message,
                            isClaimSuccess = false
                        )
                    }
                }
            }
        }
    }

    fun clearBanner() {
        _uiState.update { it.copy(statusBanner = null) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as Application
                val repo = (application as? CryptoMinerApplication)?.appContainer?.dailyBonusRepository
                    ?: DatabaseModule.provideDailyBonusRepository(application)
                return DailyBonusViewModel(application, repo) as T
            }
        }
    }
}
