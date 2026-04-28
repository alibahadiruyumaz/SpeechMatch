package com.example.speechmatch.data.recognizer

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.example.speechmatch.domain.repository.VoiceToTextParser
import com.example.speechmatch.domain.repository.VoiceToTextParserState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class VoiceToTextParserImpl @Inject constructor(
    private val app: Application
) : VoiceToTextParser, RecognitionListener {

    private val _state = MutableStateFlow(VoiceToTextParserState())
    override val state = _state.asStateFlow()

    // Android'in çekirdek ses tanıma servisi
    private val recognizer = SpeechRecognizer.createSpeechRecognizer(app)

    override fun startListening(languageCode: String) {
        _state.update { VoiceToTextParserState(isSpeaking = true) }

        if (!SpeechRecognizer.isRecognitionAvailable(app)) {
            _state.update { it.copy(error = "Ses tanıma bu cihazda mevcut değil.") }
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
            // UÇ BİLİŞİMİN KALBİ: İnterneti yasaklıyoruz
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            // Kelime kelime (Partial) sonuçları anlık almak için
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        recognizer.setRecognitionListener(this)
        recognizer.startListening(intent)
    }

    override fun stopListening() {
        _state.update { it.copy(isSpeaking = false) }
        recognizer.stopListening()
    }

    override fun destroy() {
        recognizer.destroy()
    }

    // --- RecognitionListener Callback'leri ---

    override fun onResults(results: Bundle?) {
        val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.getOrNull(0)
        _state.update { it.copy(spokenText = text ?: "", isSpeaking = false) }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val text = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.getOrNull(0)
        text?.let { partialText ->
            _state.update { it.copy(spokenText = partialText) }
        }
    }

    override fun onError(error: Int) {
        _state.update { it.copy(error = "Hata kodu: $error", isSpeaking = false) }
    }

    // Gereksiz callback'leri boş bırakıyoruz ancak implement etmek zorundayız
    override fun onReadyForSpeech(params: Bundle?) { _state.update { it.copy(error = null) } }
    override fun onBeginningOfSpeech() {}
    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEndOfSpeech() { _state.update { it.copy(isSpeaking = false) } }
    override fun onEvent(eventType: Int, params: Bundle?) {}
}