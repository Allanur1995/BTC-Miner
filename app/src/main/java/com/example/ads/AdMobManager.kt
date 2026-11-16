package com.example.ads

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import com.example.BuildConfig
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Centralized Google AdMob configuration.
 * Values are injected via BuildConfig from environment variables (e.g., GitHub Secrets)
 * and safely fall back to Google's official test Ad Unit IDs during development.
 */
object AdMobConfig {
    private const val SAMPLE_TEST_APP_ID = "ca-app-pub-3940256099942544~3347511713"

    val appId: String = BuildConfig.ADMOB_APP_ID
    val bannerUnitId: String = BuildConfig.ADMOB_BANNER_ID
    val rewardedUnitId: String = BuildConfig.ADMOB_REWARDED_ID
    val interstitialUnitId: String = BuildConfig.ADMOB_INTERSTITIAL_ID

    val isTestMode: Boolean
        get() = appId == SAMPLE_TEST_APP_ID
}

/**
 * Singleton manager responsible for initializing Google Mobile Ads SDK,
 * preloading Rewarded and Interstitial ads, and handling ad display with frequency capping.
 */
object AdMobManager {
    private const val TAG = "AdMobManager"
    private const val INTERSTITIAL_COOLDOWN_MS = 90_000L // 90 seconds minimum between interstitials

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var isInitialized = false
    private var rewardedAd: RewardedAd? = null
    private var interstitialAd: InterstitialAd? = null
    private var lastInterstitialShownTimeMs: Long = 0L

    private val _isRewardedReady = MutableStateFlow(false)
    val isRewardedReady: StateFlow<Boolean> = _isRewardedReady.asStateFlow()

    private val _isRewardedLoading = MutableStateFlow(false)
    val isRewardedLoading: StateFlow<Boolean> = _isRewardedLoading.asStateFlow()

    private val _isInterstitialReady = MutableStateFlow(false)
    val isInterstitialReady: StateFlow<Boolean> = _isInterstitialReady.asStateFlow()

    private var isInterstitialLoading = false

    fun initialize(context: Context) {
        if (isInitialized) return
        isInitialized = true
        val appContext = context.applicationContext

        // Google recommends calling MobileAds.initialize on a background thread
        CoroutineScope(Dispatchers.IO).launch {
            MobileAds.initialize(appContext) { initializationStatus ->
                Log.d(TAG, "AdMob SDK initialized: ${initializationStatus.adapterStatusMap.keys}")
                // Preload ads on the Main thread after initialization completes
                scope.launch {
                    loadRewardedAd(appContext)
                    loadInterstitialAd(appContext)
                }
            }
        }
    }

    fun loadRewardedAd(context: Context) {
        if (_isRewardedLoading.value || rewardedAd != null) return
        val appContext = context.applicationContext
        _isRewardedLoading.value = true

        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            appContext,
            AdMobConfig.rewardedUnitId,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "Rewarded ad loaded successfully")
                    rewardedAd = ad
                    _isRewardedLoading.value = false
                    _isRewardedReady.value = true
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "Rewarded ad failed to load: ${error.message} (code=${error.code})")
                    rewardedAd = null
                    _isRewardedLoading.value = false
                    _isRewardedReady.value = false
                }
            }
        )
    }

    fun loadInterstitialAd(context: Context) {
        if (isInterstitialLoading || interstitialAd != null) return
        val appContext = context.applicationContext
        isInterstitialLoading = true

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            appContext,
            AdMobConfig.interstitialUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Interstitial ad loaded successfully")
                    interstitialAd = ad
                    isInterstitialLoading = false
                    _isInterstitialReady.value = true
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "Interstitial ad failed to load: ${error.message} (code=${error.code})")
                    interstitialAd = null
                    isInterstitialLoading = false
                    _isInterstitialReady.value = false
                }
            }
        )
    }

    /**
     * Shows a Rewarded Video Ad if ready.
     * Triggers [onRewardEarned] once the user finishes watching the ad and earns the reward.
     * Automatically preloads the next rewarded ad after dismissal.
     */
    fun showRewardedAd(
        activity: Activity?,
        onRewardEarned: () -> Unit,
        onNotReady: (String) -> Unit
    ) {
        if (activity == null) {
            onNotReady("Unable to present ad right now.")
            return
        }

        val ad = rewardedAd
        if (ad == null) {
            loadRewardedAd(activity)
            onNotReady("📺 Loading rewarded video ad... Tap again in a few seconds!")
            return
        }

        var earnedReward = false
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Rewarded ad showed fullscreen")
                rewardedAd = null
                _isRewardedReady.value = false
            }

            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Rewarded ad dismissed (earned=$earnedReward)")
                loadRewardedAd(activity)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.w(TAG, "Rewarded ad failed to show: ${adError.message}")
                rewardedAd = null
                _isRewardedReady.value = false
                loadRewardedAd(activity)
                onNotReady("Ad unavailable right now. Reloading...")
            }
        }

        ad.show(activity) { rewardItem ->
            Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
            earnedReward = true
            onRewardEarned()
        }
    }

    /**
     * Shows an Interstitial Ad if loaded and the frequency cap cooldown has elapsed.
     */
    fun showInterstitialIfReady(activity: Activity?, ignoreCooldown: Boolean = false): Boolean {
        if (activity == null) return false
        val now = System.currentTimeMillis()
        if (!ignoreCooldown && (now - lastInterstitialShownTimeMs) < INTERSTITIAL_COOLDOWN_MS) {
            return false
        }

        val ad = interstitialAd ?: run {
            loadInterstitialAd(activity)
            return false
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                lastInterstitialShownTimeMs = System.currentTimeMillis()
                interstitialAd = null
                _isInterstitialReady.value = false
            }

            override fun onAdDismissedFullScreenContent() {
                loadInterstitialAd(activity)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                interstitialAd = null
                _isInterstitialReady.value = false
                loadInterstitialAd(activity)
            }
        }

        ad.show(activity)
        return true
    }
}

tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
