package com.example.speechmatch.domain.repository

import kotlinx.coroutines.flow.StateFlow

/** Ses tanıma (STT) motorunun yeteneklerini tanımlayan soyut arayüz (Contract). */
interface VoiceToTextParser {

    /** Motorun anlık durumunu (konuşma, metin, hata) reaktif olarak yayınlayan akış. */
    val state: StateFlow<VoiceParserState>

    /** Belirtilen dil kodu ile ses dinleme ve analiz sürecini başlatır. */
    fun startListening(languageCode: String)

    /** Aktif dinleme sürecini manuel olarak sonlandırır. */
    fun stopListening()

    /** Sistem kaynaklarını serbest bırakmak için motoru bellekten tamamen temizler. */
    fun destroy()

    /** Motorun mevcut metin çıktılarını ve hata geçmişini sıfırlar. */
    fun reset()
}

/** Ses tanıma motorunun kullanıcı arayüzüne ileteceği anlık durum (State) paketi. */
data class VoiceParserState(

    /** Motordan dönen analiz edilmiş nihai veya kısmi metin. */
    val spokenText: String = "",

    /** Kullanıcının o an konuşup konuşmadığını veya motorun dinlemede olup olmadığını belirtir. */
    val isSpeaking: Boolean = false,

    /** İşlem sırasında oluşan API, donanım veya ağ hatalarını barındırır. */
    val error: String? = null
)