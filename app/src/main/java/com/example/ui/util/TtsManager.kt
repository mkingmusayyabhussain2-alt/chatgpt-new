package com.example.ui.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class TtsManager(context: Context) {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _speakingMessageId = MutableStateFlow<String?>(null)
    val speakingMessageId: StateFlow<String?> = _speakingMessageId.asStateFlow()

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                tts?.language = Locale.US
            } else {
                Log.e("TtsManager", "TTS initialization failed: status $status")
            }
        }

        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _speakingMessageId.value = utteranceId
            }

            override fun onDone(utteranceId: String?) {
                if (_speakingMessageId.value == utteranceId) {
                    _speakingMessageId.value = null
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                if (_speakingMessageId.value == utteranceId) {
                    _speakingMessageId.value = null
                }
            }
        })
    }

    fun toggleSpeak(messageId: String, text: String) {
        if (_speakingMessageId.value == messageId) {
            stop()
        } else {
            speak(messageId, text)
        }
    }

    fun speak(messageId: String, text: String) {
        if (!isInitialized || tts == null) return
        stop()

        val cleanText = cleanMarkdownForSpeech(text)
        if (cleanText.isBlank()) return

        _speakingMessageId.value = messageId
        tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, messageId)
    }

    fun stop() {
        tts?.stop()
        _speakingMessageId.value = null
    }

    fun shutdown() {
        stop()
        tts?.shutdown()
        tts = null
    }

    private fun cleanMarkdownForSpeech(raw: String): String {
        return raw
            // Remove code blocks
            .replace(Regex("```[\\s\\S]*?```"), " Here is the code snippet. ")
            // Remove inline code
            .replace(Regex("`([^`]+)`"), "$1")
            // Remove headers
            .replace(Regex("^#+\\s*", RegexOption.MULTILINE), "")
            // Remove bold & italics
            .replace(Regex("\\*\\*([^*]+)\\*\\*"), "$1")
            .replace(Regex("\\*([^*]+)\\*"), "$1")
            .replace(Regex("__([^_]+)__"), "$1")
            .replace(Regex("_([^_]+)_"), "$1")
            // Clean extra whitespace
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
