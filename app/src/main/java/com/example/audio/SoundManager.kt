package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

enum class SoundEffect {
    MINE_TAP,
    UPGRADE_COMPLETE,
    REWARD_CHIME,
    ERROR_THUD,
    UI_TICK
}

interface SoundController {
    fun setEnabled(enabled: Boolean)
    fun isEnabled(): Boolean
    fun playMineTap(comboCount: Int = 0)
    fun playUpgradeComplete()
    fun playRewardChime()
    fun playError()
    fun playUiTick()
    fun release()
}

/**
 * Low-latency game sound manager powered by Android SoundPool and procedurally
 * synthesized WAV effects cached locally on app initialization.
 */
class SoundManager(
    context: Context,
    initialEnabled: Boolean = true
) : SoundController {

    private val appContext = context.applicationContext
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @Volatile
    private var enabled: Boolean = initialEnabled

    @Volatile
    private var lastTapPlayedMs: Long = 0L

    private val soundIds = ConcurrentHashMap<SoundEffect, Int>()
    private val loadedSoundIds = ConcurrentHashMap<Int, Boolean>()

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(8)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()
        .apply {
            setOnLoadCompleteListener { _, sampleId, status ->
                if (status == 0) {
                    loadedSoundIds[sampleId] = true
                }
            }
        }

    init {
        prepareAndLoadSounds()
    }

    private fun prepareAndLoadSounds() {
        scope.launch {
            runCatching {
                val sfxDir = File(appContext.cacheDir, "sfx").apply { mkdirs() }
                loadSound(sfxDir, "mine_tap.wav", SoundEffect.MINE_TAP) {
                    SoundSynthesizer.wrapPcmAsWav(SoundSynthesizer.generateMineTapPcm())
                }
                loadSound(sfxDir, "upgrade_complete.wav", SoundEffect.UPGRADE_COMPLETE) {
                    SoundSynthesizer.wrapPcmAsWav(SoundSynthesizer.generateUpgradeCompletePcm())
                }
                loadSound(sfxDir, "reward_chime.wav", SoundEffect.REWARD_CHIME) {
                    SoundSynthesizer.wrapPcmAsWav(SoundSynthesizer.generateRewardChimePcm())
                }
                loadSound(sfxDir, "error_thud.wav", SoundEffect.ERROR_THUD) {
                    SoundSynthesizer.wrapPcmAsWav(SoundSynthesizer.generateErrorThudPcm())
                }
                loadSound(sfxDir, "ui_tick.wav", SoundEffect.UI_TICK) {
                    SoundSynthesizer.wrapPcmAsWav(SoundSynthesizer.generateUiTickPcm())
                }
            }
        }
    }

    private fun loadSound(
        dir: File,
        fileName: String,
        effect: SoundEffect,
        wavFactory: () -> ByteArray
    ) {
        val file = File(dir, fileName)
        val wavBytes = wavFactory()
        if (!file.exists() || file.length() != wavBytes.size.toLong()) {
            file.writeBytes(wavBytes)
        }
        val id = soundPool.load(file.absolutePath, 1)
        if (id > 0) {
            soundIds[effect] = id
        }
    }

    override fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }

    override fun isEnabled(): Boolean = enabled

    override fun playMineTap(comboCount: Int) {
        if (!enabled) return
        val now = System.currentTimeMillis()
        // Prevent double-triggering within 25ms while supporting rapid multi-taps
        if (now - lastTapPlayedMs < 25L) return
        lastTapPlayedMs = now

        // Subtle pitch rise with combo + tiny organic variation
        val comboBoost = (comboCount.coerceIn(0, 20)) * 0.009f
        val jitter = (Random.nextFloat() - 0.5f) * 0.04f
        val rate = (0.98f + comboBoost + jitter).coerceIn(0.85f, 1.25f)
        playInternal(SoundEffect.MINE_TAP, volume = 0.85f, rate = rate)
    }

    override fun playUpgradeComplete() {
        if (!enabled) return
        playInternal(SoundEffect.UPGRADE_COMPLETE, volume = 0.9f, rate = 1.0f)
    }

    override fun playRewardChime() {
        if (!enabled) return
        playInternal(SoundEffect.REWARD_CHIME, volume = 0.9f, rate = 1.0f)
    }

    override fun playError() {
        if (!enabled) return
        playInternal(SoundEffect.ERROR_THUD, volume = 0.7f, rate = 1.0f)
    }

    override fun playUiTick() {
        if (!enabled) return
        playInternal(SoundEffect.UI_TICK, volume = 0.6f, rate = 1.0f)
    }

    private fun playInternal(effect: SoundEffect, volume: Float, rate: Float) {
        val id = soundIds[effect] ?: return
        if (loadedSoundIds[id] != true) return
        soundPool.play(id, volume, volume, 1, 0, rate)
    }

    override fun release() {
        soundIds.clear()
        loadedSoundIds.clear()
        soundPool.release()
    }
}
