package com.example.speechmatch.presentation.placement

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.speechmatch.data.local.entity.UserProfileEntity
import com.example.speechmatch.data.local.entity.VocabularyEntity
import com.example.speechmatch.domain.repository.HeadsetStateObserver
import com.example.speechmatch.domain.repository.SpeechMatchRepository
import com.example.speechmatch.domain.repository.VoiceToTextParser
import com.example.speechmatch.domain.usecase.EvaluatePronunciationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Seviye belirleme sınavının (Placement Test) iş mantığını yöneten ViewModel.
 * Çoklu Profil (Multi-User) desteği ile güncellenmiştir.
 */
@HiltViewModel
class PlacementViewModel @Inject constructor(
    private val repository: SpeechMatchRepository,
    private val evaluateUseCase: EvaluatePronunciationUseCase,
    /** Çevrimdışı ses tanıma motoru (STT) sözleşmesi. */
    val voiceParser: VoiceToTextParser,
    /** Kulaklık takılıp çıkarılma durumunu dinleyen donanım gözlemcisi. */
    private val headsetObserver: HeadsetStateObserver,
    /** Navigasyon argümanlarını (Örn: userId) yakalamak için kullanılır. */
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    /** Navigasyondan gelen Aktif Kullanıcı Kimliği. Gelmezse güvenlik için 1 atanır. */
    private val currentUserId: Int = savedStateHandle.get<Int>("userId") ?: 1

    /** Sınav ekranının anlık durum (UI State) akışı. */
    private val _state = MutableStateFlow(PlacementUiState())
    val state = _state.asStateFlow()

    /** Mikrofonun dinleme durumu ve dönen metin akışını UI katmanına açar. */
    val voiceState = voiceParser.state

    /** Seviye bazlı (A1, B1 vb.) toplanan akustik doğruluk skorlarını tutan geçici bellek. */
    private val scoresPerLevel = mutableMapOf<String, MutableList<Int>>()

    init {
        loadTestWords()

        // Kulaklık gözlemcisini başlat ve State'i güncelle
        headsetObserver.startObserving()
        viewModelScope.launch {
            headsetObserver.isHeadsetConnected.collect { isConnected ->
                _state.update { it.copy(isHeadsetConnected = isConnected) }
            }
        }
    }

    /** * Veritabanından her CEFR seviyesi için rastgele örnek kelimeler seçerek
     * dengeli bir sınav seti oluşturur ve state'i günceller.
     */
    private fun loadTestWords() {
        viewModelScope.launch {
            // Kelimeleri çekerken artık aktif kullanıcının userId'sini gönderiyoruz
            val allWords = repository.getAllActiveWords(currentUserId)
            val testSet = allWords.groupBy { it.cefrLevel }
                .map { it.value.shuffled().take(2) }
                .flatten()

            _state.update { it.copy(testWords = testSet, isLoading = false) }
        }
    }

    /** Amerikan İngilizcesi dil paketiyle ses dinleme sürecini başlatır. */
    fun startListening() = voiceParser.startListening("en-US")

    /** Aktif ses dinleme sürecini durdurur. */
    fun stopListening() = voiceParser.stopListening()

    /** * Kullanıcıdan gelen ses verisini sanitize eder, Levenshtein algoritmasıyla
     * puanlar ve bir sonraki soruya geçiş veya sınav bitiş kontrolünü yapar.
     */
    fun onWordSpoken(spokenText: String) {
        val currentWord = _state.value.testWords.getOrNull(_state.value.currentWordIndex) ?: return
        if (spokenText.isBlank()) return

        // Regex Sanitization: Metni temizle ve sadece ilk kelimeyi al
        val sanitizedText = spokenText.trim().split("\\s+".toRegex()).firstOrNull()?.replace(Regex("[^A-Za-z]"), "") ?: ""
        if (sanitizedText.isBlank()) return

        viewModelScope.launch {
            // Analiz yapılırken hangi kullanıcının analiz edildiği (currentUserId) UseCase'e bildirilir
            val result = evaluateUseCase(currentWord, sanitizedText, currentUserId)
            scoresPerLevel.getOrPut(currentWord.cefrLevel) { mutableListOf() }.add(result.qualityScore)

            // UI Tutarlılığı: Bir sonraki kelimeye geçmeden önce ekrandaki metni temizler.
            voiceParser.reset()

            if (_state.value.currentWordIndex < _state.value.testWords.size - 1) {
                _state.update { it.copy(currentWordIndex = it.currentWordIndex + 1) }
            } else {
                calculateFinalLevelAndFinish()
            }
        }
    }

    /** * Toplanan skorların ortalamasını alarak kullanıcının dil seviyesini (CEFR)
     * teşhis eder ve kullanıcı profilini veritabanında günceller.
     */
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
            // Aktif kullanıcı profili çekilir
            val existingProfile = repository.getUserProfile(currentUserId)

            if (existingProfile != null) {
                // Güvenlik: Tüm skorların genel ortalaması hesaplanır (NaN ise 1.0 atanır)
                val totalAvg = scoresPerLevel.values.flatten().average().takeIf { !it.isNaN() } ?: 1.0

                // KRİTİK GÜNCELLEME: baselineScore sıfırdan büyük bir sayıya güncellenir ki
                // kullanıcı tekrar giriş yaptığında Router onu sınav yerine antrenmana yönlendirsin!
                val updatedProfile = existingProfile.copy(
                    currentLevel = finalLevel,
                    baselineScore = totalAvg
                )
                repository.upsertProfile(updatedProfile)
            }

            _state.update { it.copy(isTestFinished = true, calculatedLevel = finalLevel) }
        }
    }

    /** ViewModel yok edildiğinde donanım gözlemcisini kapatır. */
    override fun onCleared() {
        super.onCleared()
        headsetObserver.stopObserving()
    }
}