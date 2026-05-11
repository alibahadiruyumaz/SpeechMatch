package com.example.speechmatch.domain.repository

import kotlinx.coroutines.flow.StateFlow

// 1. Ekranların ve ViewModel'in donanımı yönetmek için kullanacağı Sözleşme (Interface)
interface VoiceToTextParser {
    val state: StateFlow<VoiceParserState>
    fun startListening(languageCode: String)
    fun stopListening()
    fun destroy()

    fun reset()
}

// 2. Sözleşmenin veri paketi: Mikrofonun anlık durumunu tutan Veri Sınıfı (Data Class)
// DİKKAT: İsim VoiceParserState olarak sabitlendi.
data class VoiceParserState(
    val spokenText: String = "",
    val isSpeaking: Boolean = false,
    val error: String? = null
)