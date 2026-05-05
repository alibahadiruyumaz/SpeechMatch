package com.example.speechmatch.presentation.recorder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.speechmatch.data.local.entity.VocabularyEntity
import com.example.speechmatch.domain.repository.HeadsetStateObserver
import com.example.speechmatch.domain.repository.SpeechMatchRepository
import com.example.speechmatch.domain.repository.VoiceToTextParser
import com.example.speechmatch.domain.usecase.EvaluatePronunciationUseCase
import com.example.speechmatch.domain.usecase.EvaluationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecorderViewModel @Inject constructor(
    private val voiceParser: VoiceToTextParser,
    private val headsetObserver: HeadsetStateObserver,
    private val repository: SpeechMatchRepository,
    private val evaluatePronunciationUseCase: EvaluatePronunciationUseCase // SPRINT 7 BEYNİ ENJEKTE EDİLDİ
) : ViewModel() {

    // Lokal State: Artık sadece String değil, veritabanı ID'sini kaybetmemek için tüm Entity'i tutuyoruz.
    private val _viewState = MutableStateFlow(RecorderViewState())

    // REAKTİF BİRLEŞTİRME MOTORU (Single Source of Truth)
    val state = combine(
        voiceParser.state,
        headsetObserver.isHeadsetConnected,
        _viewState
    ) { parserState, isHeadsetConnected, viewState ->
        RecorderUiState(
            spokenText = parserState.spokenText,
            isSpeaking = parserState.isSpeaking,
            error = parserState.error ?: viewState.errorMessage,
            isHeadsetConnected = isHeadsetConnected,
            activeWord = viewState.activeWord, // Bütün nesne UI'a aktarılıyor
            evaluationResult = viewState.evaluationResult // Levenshtein Skoru
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RecorderUiState()
    )

    init {
        headsetObserver.startObserving()
        seedDatabase()
    }

    private fun seedDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (repository.getAllActiveWords().isEmpty()) {
                    repository.insertWord(VocabularyEntity(id = 0, text = "Think", targetPhoneme = "θɪŋk", difficultyLevel = "Hard", minimalPairId = 2, isArchived = false))
                    repository.insertWord(VocabularyEntity(id = 0, text = "Sink", targetPhoneme = "sɪŋk", difficultyLevel = "Easy", minimalPairId = 1, isArchived = false))
                }
                loadNextWordFromDatabase()
            } catch (e: Exception) {
                _viewState.update { it.copy(errorMessage = "Tohumlama hatası: ${e.message}") }
            }
        }
    }

    private fun loadNextWordFromDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val wordsToReview = repository.getWordsToReview(System.currentTimeMillis())
                if (wordsToReview.isNotEmpty()) {
                    _viewState.update {
                        it.copy(
                            activeWord = wordsToReview.first(), // ID ve tüm veriler korundu
                            errorMessage = null,
                            evaluationResult = null // Yeni kelimeye geçildiğinde eski skoru sıfırla
                        )
                    }
                } else {
                    _viewState.update { it.copy(errorMessage = "Şu an test edilecek kelime bulunamadı.") }
                }
            } catch (e: Exception) {
                _viewState.update { it.copy(errorMessage = "Veritabanı okuma hatası: ${e.message}") }
            }
        }
    }

    // DONANIM YÖNETİMİ VE DİNLEME
    fun startListening() {
        if (!state.value.isHeadsetConnected) {
            _viewState.update { it.copy(errorMessage = "Yankıyı önlemek için kulaklık takın!") }
            return
        }

        if (state.value.activeWord == null) {
            _viewState.update { it.copy(errorMessage = "Önce hedef kelimeyi yüklemelisiniz.") }
            return
        }

        _viewState.update { it.copy(errorMessage = null, evaluationResult = null) }
        voiceParser.startListening("en-US")
    }

    fun stopListening() {
        voiceParser.stopListening()
    }

    // SPRINT 7: ALGORİTMİK DEĞERLENDİRME TETİKLEYİCİSİ
    // Kullanıcı konuşmayı bitirdiğinde UI bu fonksiyonu çağırıp okunan metni içeri atmalıdır.
    fun evaluateSpeech(finalSpokenText: String) {
        val targetEntity = state.value.activeWord ?: return
        if (finalSpokenText.isBlank()) return

        viewModelScope.launch {
            try {
                // Levenshtein ve SM-2 çalışır, sonuç veritabanına yazılır
                val result = evaluatePronunciationUseCase(targetEntity, finalSpokenText)

                // UI'a kalite skorunu ve başarı durumunu yansıt
                _viewState.update { it.copy(evaluationResult = result) }

            } catch (e: Exception) {
                _viewState.update { it.copy(errorMessage = "Analiz hatası: ${e.message}") }
            }
        }
    }

    fun proceedToNextWord() {
        loadNextWordFromDatabase()
    }

    override fun onCleared() {
        super.onCleared()
        headsetObserver.stopObserving()
        voiceParser.destroy()
    }
}

// UI Tarafından Tüketilecek Nihai ve Tekil Veri Paketi
data class RecorderUiState(
    val spokenText: String = "",
    val isSpeaking: Boolean = false,
    val error: String? = null,
    val isHeadsetConnected: Boolean = false,
    val activeWord: VocabularyEntity? = null,
    val evaluationResult: EvaluationResult? = null
)

// ViewModel'in Kendi İçinde Kullandığı Lokal Veri Paketi
data class RecorderViewState(
    val errorMessage: String? = null,
    val activeWord: VocabularyEntity? = null,
    val evaluationResult: EvaluationResult? = null
)