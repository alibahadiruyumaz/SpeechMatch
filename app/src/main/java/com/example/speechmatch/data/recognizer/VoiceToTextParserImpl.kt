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

/** * Çevrimdışı (Edge Computing) odaklı, gecikmesiz ses-metin (STT) motoru.
 * Android'in yerel SpeechRecognizer API'sini kullanarak cihaz donanımında çalışır.
 */
class VoiceToTextParserImpl @Inject constructor(
    private val app: Application
) : VoiceToTextParser, RecognitionListener {

    /** Ses tanıma sürecinin anlık durumunu arayüze reaktif olarak ileten StateFlow. */
    private val _state = MutableStateFlow(VoiceParserState())
    override val state = _state.asStateFlow()

    /** Lazy Initialization: Kaynak tüketimini önlemek için sadece ihtiyaç anında oluşturulur. */
    private var recognizer: SpeechRecognizer? = null

    /** Çevrimdışı (Offline) işleme tercihiyle ses dinleme ve analiz sürecini başlatır. */
    override fun startListening(languageCode: String) {
        _state.update { it.copy(error = null, isSpeaking = true, spokenText = "") }

        if (!SpeechRecognizer.isRecognitionAvailable(app)) {
            _state.update { it.copy(error = "Ses tanıma bu cihazda mevcut değil.", isSpeaking = false) }
            return
        }

        if (recognizer == null) {
            recognizer = SpeechRecognizer.createSpeechRecognizer(app)
            recognizer?.setRecognitionListener(this)
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true) // Bulut bağımlılığını keser
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true) // Anlık akış sağlar
        }

        recognizer?.startListening(intent)
    }

    /** Dinleme işlemini manuel olarak durdurur. */
    override fun stopListening() {
        _state.update { it.copy(isSpeaking = false) }
        recognizer?.stopListening()
    }

    /** Bellek sızıntılarını (Memory Leak) önlemek için STT motorunu sistemden tamamen kazır. */
    override fun destroy() {
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

    /** API düzeyindeki hataları yakalayarak kullanıcı dostu Türkçe hata mesajlarına çevirir. */
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

    /** Analiz edilen kesinleşmiş (nihai) metin sonucunu iletir. */
    override fun onResults(results: Bundle?) {
        val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.getOrNull(0)
        _state.update { it.copy(spokenText = text ?: "", isSpeaking = false) }
    }

    /** Kullanıcı konuşurken canlı (anlık) parçalı metinleri asenkron olarak iletir. */
    override fun onPartialResults(partialResults: Bundle?) {
        val text = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.getOrNull(0)
        text?.let { partialText ->
            _state.update { it.copy(spokenText = partialText) }
        }
    }

    override fun onEvent(eventType: Int, params: Bundle?) {}

    /** Motorun state verilerini sıfırlayarak yeni bir kelime analizine hazırlar. */
    override fun reset() {
        _state.update { it.copy(spokenText = "", error = null) }
    }
}