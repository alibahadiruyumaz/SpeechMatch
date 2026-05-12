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

/** * Android'in yerel SpeechRecognizer API'sini kullanarak tamamen çevrimdışı
 * (Offline/Edge Computing) ses-metin dönüşümü (STT) yapan motor.
 */
class SpeechMatchRecognizer(
    private val context: Context
) : VoiceToTextParser, RecognitionListener {

    /** Ses tanıma motorunun anlık durumunu (konuşma, metin, hata) arayüze reaktif olarak iletir. */
    private val _state = MutableStateFlow(VoiceParserState())
    override val state: StateFlow<VoiceParserState> = _state.asStateFlow()

    private var recognizer: SpeechRecognizer? = null

    /** Bulut sunucularını reddederek, cihaz donanımı üzerinde ses dinleme ve analiz sürecini başlatır. */
    override fun startListening(languageCode: String) {
        _state.update { it.copy(error = null, isSpeaking = true, spokenText = "") }

        if (recognizer == null) {
            recognizer = SpeechRecognizer.createSpeechRecognizer(context)
            recognizer?.setRecognitionListener(this)
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
            // Bulutu reddeden çevrimdışı işleme emri (Zero-Latency hedefi)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        recognizer?.startListening(intent)
    }

    /** Dinleme işlemini manuel olarak durdurur ve motoru beklemeye alır. */
    override fun stopListening() {
        _state.update { it.copy(isSpeaking = false) }
        recognizer?.stopListening()
    }

    /** Bellek sızıntılarını (Memory Leak) önlemek için STT motorunu sistemden tamamen temizler. */
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

    /** API düzeyindeki hataları yakalayarak kullanıcı dostu Türkçe hata mesajlarına çevirir. */
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

    /** Ses analizinin kesinleşmiş nihai sonucunu State'e aktarır. */
    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            _state.update { it.copy(spokenText = matches[0], isSpeaking = false) }
        }
    }

    /** Kullanıcı konuşurken canlı (anlık) metin akışını asenkron olarak State'e iletir. */
    override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            _state.update { it.copy(spokenText = matches[0]) }
        }
    }

    override fun onEvent(eventType: Int, params: Bundle?) {}

    /** Motorun state verilerini (metin ve hata geçmişini) sıfırlayarak yeni kelime analizine hazırlar. */
    override fun reset() {
        _state.update { it.copy(spokenText = "", error = null) }
    }
}