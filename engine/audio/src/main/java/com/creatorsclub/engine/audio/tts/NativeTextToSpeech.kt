package com.creatorsclub.engine.audio.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class NativeTextToSpeech(private val context: Context) {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    suspend fun initialize(): Boolean = suspendCancellableCoroutine { continuation ->
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                tts?.language = Locale("en", "IN") // Default to Indian English, supports Hindi
                continuation.resume(true)
            } else {
                continuation.resume(false)
            }
        }
    }

    fun speak(text: String, languageCode: String = "en-IN") {
        if (!isInitialized) return
        val locale = if (languageCode.startsWith("hi")) Locale("hi", "IN") else Locale("en", "IN")
        tts?.language = locale
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts_${System.currentTimeMillis()}")
    }

    fun stop() {
        tts?.stop()
    }

    fun release() {
        tts?.shutdown()
        tts = null
    }
}
