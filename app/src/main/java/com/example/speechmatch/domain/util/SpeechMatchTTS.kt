package com.example.speechmatch.domain.util

import android.content.Context
import android.speech.tts.TextToSpeech
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Uygulama genelinde metin-ses dönüşümü (Text-to-Speech) işlemlerini yöneten tekil (Singleton) yardımcı sınıf.
 */
@Singleton
class SpeechMatchTTS @Inject constructor(
    @ApplicationContext context: Context
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context, this)
    private var isReady = false

    /** TTS motoru başlatıldığında varsayılan dil modeli olarak Amerikan İngilizcesini (Locale.US) yapılandırır. */
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                isReady = true
            }
        }
    }

    /**
     * Verilen metni seslendirir.
     * QUEUE_FLUSH bayrağı kullanılarak, mevcut bir okuma işlemi varsa kesilir ve anında yeni metne geçilir.
     * * @param text Seslendirilecek hedef metin.
     */
    fun speak(text: String) {
        if (isReady) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    /** Bellek sızıntılarını (Memory Leak) önlemek için TTS motorunu durdurur ve sistem kaynaklarını serbest bırakır. */
    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}