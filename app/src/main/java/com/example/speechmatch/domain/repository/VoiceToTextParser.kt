package com.example.speechmatch.domain.repository

import kotlinx.coroutines.flow.StateFlow

// Bu arayüz, Android'in mikrofon sensörünün ne yapması gerektiğini tanımlayan SÖZLEŞMEDİR.
interface VoiceToTextParser {

    // Mikrofonun anlık durumunu tutacak asenkron bir akış (Flow)
    val state: StateFlow<VoiceToTextParserState>

    // Dinlemeyi başlatma emri
    fun startListening(languageCode: String = "en-US")

    // Dinlemeyi zorla durdurma emri
    fun stopListening()

    // Uygulama arka plana atıldığında sensörü RAM'den tamamen silme emri
    fun destroy()
}

// Mikrofonun anlık olarak bulunabileceği durumlar (Sonlu Durum Makinesi - FSM)
data class VoiceToTextParserState(
    val spokenText: String = "",       // Kullanıcının ağzından dökülen kelimeler
    val isSpeaking: Boolean = false,   // O an konuşuyor mu?
    val error: String? = null          // Çevrimdışı paket yoksa veya hata varsa
)