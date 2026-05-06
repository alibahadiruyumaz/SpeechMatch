package com.example.speechmatch.presentation.placement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.speechmatch.data.local.entity.UserProfileEntity
import com.example.speechmatch.data.local.entity.VocabularyEntity
import com.example.speechmatch.domain.repository.SpeechMatchRepository
import com.example.speechmatch.domain.repository.VoiceToTextParser
import com.example.speechmatch.domain.usecase.EvaluatePronunciationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlacementViewModel @Inject constructor(
    private val repository: SpeechMatchRepository,
    private val evaluateUseCase: EvaluatePronunciationUseCase,
    val voiceParser: VoiceToTextParser // SPRINT 9: Mikrofon motoru eklendi
) : ViewModel() {

    private val _state = MutableStateFlow(PlacementUiState())
    val state = _state.asStateFlow()

    // Mikrofonun anlık durumunu UI'a açıyoruz
    val voiceState = voiceParser.state

    private val scoresPerLevel = mutableMapOf<String, MutableList<Int>>()

    init {
        loadTestWords()
    }

    private fun loadTestWords() {
        viewModelScope.launch {
            val allWords = repository.getAllActiveWords()
            // Her seviyeden RASTGELE 2 kelime seçerek sınavı daha dengeli yapıyoruz
            val testSet = allWords.groupBy { it.cefrLevel }
                .map { it.value.shuffled().take(2) } // .shuffled() eklendi
                .flatten()

            _state.update { it.copy(testWords = testSet, isLoading = false) }
        }
    }

    // Mikrofon tetikleyicileri
    fun startListening() = voiceParser.startListening("en-US")
    fun stopListening() = voiceParser.stopListening()

    fun onWordSpoken(spokenText: String) {
        val currentWord = _state.value.testWords.getOrNull(_state.value.currentWordIndex) ?: return
        if (spokenText.isBlank()) return

        // Ham metni temizle (Sanitizasyon)
        val sanitizedText = spokenText.trim().split("\\s+".toRegex()).firstOrNull()?.replace(Regex("[^A-Za-z]"), "") ?: ""
        if (sanitizedText.isBlank()) return

        viewModelScope.launch {
            val result = evaluateUseCase(currentWord, sanitizedText)
            scoresPerLevel.getOrPut(currentWord.cefrLevel) { mutableListOf() }.add(result.qualityScore)

            if (_state.value.currentWordIndex < _state.value.testWords.size - 1) {
                _state.update { it.copy(currentWordIndex = it.currentWordIndex + 1) }
            } else {
                calculateFinalLevelAndFinish()
            }
        }
    }

    private fun calculateFinalLevelAndFinish() {
        val levels = listOf("A1", "A2", "B1", "B2", "C1", "C2")
        var finalLevel = "A1"

        for (level in levels) {
            val avg = scoresPerLevel[level]?.average() ?: 0.0
            if (avg >= 3.5) {
                finalLevel = level
            } else {
                break
            }
        }

        viewModelScope.launch {
            val existingProfile = repository.getUserProfile()
            val updatedProfile = if (existingProfile != null) {
                existingProfile.copy(currentLevel = finalLevel)
            } else {
                // Not: userId ve baselineScore senin sınıfına uygun olmalı
                UserProfileEntity(userId = 1, currentLevel = finalLevel, baselineScore = 0.0, chronicErrorPhonemes = "")
            }
            repository.upsertProfile(updatedProfile)
            _state.update { it.copy(isTestFinished = true, calculatedLevel = finalLevel) }
        }
    }
}