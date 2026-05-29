package com.example.ui

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class EnglishSpeechManager(context: Context) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.US)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("EnglishSpeechManager", "TTS language not supported")
                } else {
                    isInitialized = true
                }
            } else {
                Log.e("EnglishSpeechManager", "TTS initialization failed")
            }
        }
    }

    fun speak(text: String) {
        if (isInitialized && text.isNotEmpty()) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "EnglishLearnerSpeechId")
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
