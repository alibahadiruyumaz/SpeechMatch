package com.example.speechmatch.data.recognizer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer


import com.example.speechmatch.domain.repository.VoiceParserState
import com.example.speechmatch.domain.repository.VoiceToTextParser

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SpeechMatchRecognizer(
    private val context: Context
) : VoiceToTextParser, RecognitionListener {

    private val _state = MutableStateFlow(VoiceParserState())
    override val state: StateFlow<VoiceParserState> = _state.asStateFlow()

    private var recognizer: SpeechRecognizer? = null

    override fun startListening(languageCode: String) {
        _state.update { it.copy(error = null, isSpeaking = true, spokenText = "") }

        if (recognizer == null) {
            recognizer = SpeechRecognizer.createSpeechRecognizer(context)
            recognizer?.setRecognitionListener(this)
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
            // Bulutu reddeden çevrimdışı işleme emri
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        recognizer?.startListening(intent)
    }

    override fun stopListening() {
        _state.update { it.copy(isSpeaking = false) }
        recognizer?.stopListening()
    }

    override fun destroy() {
        recognizer?.destroy()
        recognizer = null
    }

    override fun onReadyForSpeech(params: Bundle?) {
        _state.update { it.copy(error = null) }
    }

    override fun onBeginningOfSpeech() {}
    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}

    override fun onEndOfSpeech() {
        _state.update { it.copy(isSpeaking = false) }
    }

    override fun onError(error: Int) {
        val errorMessage = when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Ses yakalanamadı. Mikrofonu kontrol edin."
            SpeechRecognizer.ERROR_CLIENT -> "İstemci tarafında bir hata oluştu."
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Mikrofon izni eksik!"
            SpeechRecognizer.ERROR_NETWORK -> "Ağ hatası (Çevrimdışı paketler eksik olabilir)."
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Ağ zaman aşımı."
            SpeechRecognizer.ERROR_NO_MATCH -> "Ne söylediğinizi anlayamadım."
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Motor zaten meşgul."
            SpeechRecognizer.ERROR_SERVER -> "Sunucu hatası."
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Konuşmadığınız için dinleme durduruldu."
            else -> "Bilinmeyen hata: $error"
        }
        _state.update { it.copy(error = errorMessage, isSpeaking = false) }
    }

    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            _state.update { it.copy(spokenText = matches[0], isSpeaking = false) }
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            _state.update { it.copy(spokenText = matches[0]) }
        }
    }

    override fun onEvent(eventType: Int, params: Bundle?) {}

    override fun reset() {
        _state.update { it.copy(spokenText = "", error = null) }
    }
}