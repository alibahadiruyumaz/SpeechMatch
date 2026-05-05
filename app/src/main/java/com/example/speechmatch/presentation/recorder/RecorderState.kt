package com.example.speechmatch.presentation.recorder

// Ekranın o anki durumunu temsil eden katı kurallar bütünü.
sealed class RecorderState {
    object Idle : RecorderState() // Bekleme
    object Listening : RecorderState() // Mikrofon açık, dinliyor
    object Analyzing : RecorderState() // Levenshtein ve SM-2 devrede
    data class Success(val qualityScore: Int, val isPerfect: Boolean) : RecorderState() // Sonuç
    data class Error(val message: String) : RecorderState() // Hata
}