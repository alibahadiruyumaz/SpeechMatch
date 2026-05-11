package com.example.speechmatch.data.recognizer

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.example.speechmatch.domain.repository.VoiceParserState
import com.example.speechmatch.domain.repository.VoiceToTextParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class VoiceToTextParserImpl @Inject constructor(
    private val app: Application
) : VoiceToTextParser, RecognitionListener {

    private val _state = MutableStateFlow(VoiceParserState())
    override val state = _state.asStateFlow()

    // Nesne yaratılır yaratılmaz değil, sadece ihtiyaç anında oluşturulacak
    private var recognizer: SpeechRecognizer? = null

    override fun startListening(languageCode: String) {
        _state.update { it.copy(error = null, isSpeaking = true, spokenText = "") }

        if (!SpeechRecognizer.isRecognitionAvailable(app)) {
            _state.update { it.copy(error = "Ses tanıma bu cihazda mevcut değil.", isSpeaking = false) }
            return
        }

        // Lazy Initialization: Sadece dinleme başladığında ve Main Thread'de oluşturulur
        if (recognizer == null) {
            recognizer = SpeechRecognizer.createSpeechRecognizer(app)
            recognizer?.setRecognitionListener(this)
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
            // UÇ BİLİŞİMİN KALBİ: İnterneti yasaklıyoruz (Sıfır Gecikme)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            // Kelime kelime (Partial) sonuçları anlık almak için
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        recognizer?.startListening(intent)
    }

    override fun stopListening() {
        _state.update { it.copy(isSpeaking = false) }
        recognizer?.stopListening()
    }

    override fun destroy() {
        // Zombi süreç bırakmamak için bellekten tamamen kazıyoruz
        recognizer?.destroy()
        recognizer = null
    }

    // --- RecognitionListener Callback'leri ---

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
            SpeechRecognizer.ERROR_NETWORK -> "Çevrimdışı dil paketi bulunamadı!"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Ağ zaman aşımı."
            SpeechRecognizer.ERROR_NO_MATCH -> "Söylediğiniz anlaşılamadı."
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Motor zaten meşgul."
            SpeechRecognizer.ERROR_SERVER -> "Sunucu hatası."
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Konuşmadığınız için dinleme durduruldu."
            else -> "Bilinmeyen bir hata oluştu: $error"
        }
        _state.update { it.copy(error = errorMessage, isSpeaking = false) }
    }

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

    override fun onEvent(eventType: Int, params: Bundle?) {}

    override fun reset() {
        _state.update { it.copy(spokenText = "", error = null) }
    }
}
