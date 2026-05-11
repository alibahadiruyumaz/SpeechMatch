package com.example.speechmatch.presentation.recorder

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.speechmatch.data.local.entity.VocabularyEntity
import com.example.speechmatch.domain.repository.HeadsetStateObserver
import com.example.speechmatch.domain.repository.SpeechMatchRepository
import com.example.speechmatch.domain.repository.VoiceToTextParser
import com.example.speechmatch.domain.usecase.EvaluatePronunciationUseCase
import com.example.speechmatch.domain.usecase.EvaluationResult
import com.example.speechmatch.domain.util.SpeechMatchTTS
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import javax.inject.Inject

@HiltViewModel
class RecorderViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val voiceParser: VoiceToTextParser,
    private val headsetObserver: HeadsetStateObserver,
    private val repository: SpeechMatchRepository,
    private val evaluatePronunciationUseCase: EvaluatePronunciationUseCase,
    private val tts: SpeechMatchTTS
) : ViewModel() {

    private val _viewState = MutableStateFlow(RecorderViewState())

    val state = combine(
        voiceParser.state,
        headsetObserver.isHeadsetConnected,
        _viewState
    ) { parserState, isHeadsetConnected, viewState ->

        val priorityError = if (viewState.errorMessage == "BITTI" || viewState.errorMessage?.contains("hatası") == true) {
            viewState.errorMessage
        } else {
            parserState.error ?: viewState.errorMessage
        }

        RecorderUiState(
            spokenText = parserState.spokenText,
            isSpeaking = parserState.isSpeaking,
            error = priorityError,
            isHeadsetConnected = isHeadsetConnected,
            activeWord = viewState.activeWord,
            evaluationResult = viewState.evaluationResult,
            sessionResults = viewState.sessionResults,
            isSessionFinished = viewState.isSessionFinished
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RecorderUiState()
    )

    init {
        voiceParser.reset()
        headsetObserver.startObserving()
        seedDatabaseFromJson()
    }

    private fun seedDatabaseFromJson() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (repository.getAllActiveWords().isNotEmpty()) {
                    loadNextWordFromDatabase()
                    return@launch
                }

                val inputStream = context.assets.open("vocabulary_seed.json")
                val jsonString = inputStream.bufferedReader().use { it.readText() }
                val jsonArray = JSONArray(jsonString)

                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)

                    val entity = VocabularyEntity(
                        id = 0,
                        text = jsonObject.getString("text"),
                        targetPhoneme = jsonObject.getString("targetPhoneme"),
                        cefrLevel = jsonObject.getString("cefrLevel"),
                        minimalPairId = jsonObject.getInt("minimalPairId"),
                        isArchived = false
                    )
                    repository.insertWord(entity)
                }
                loadNextWordFromDatabase()

            } catch (e: Exception) {
                _viewState.update { it.copy(errorMessage = "JSON Ayrıştırma (Parsing) Hatası: ${e.message}") }
            }
        }
    }

    private fun loadNextWordFromDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                delay(300)

                val wordsToReview = repository.getWordsToReview(System.currentTimeMillis())

                if (wordsToReview.isNotEmpty()) {
                    _viewState.update {
                        it.copy(
                            activeWord = wordsToReview.random(),
                            errorMessage = null,
                            evaluationResult = null
                        )
                    }
                } else {
                    _viewState.update {
                        it.copy(
                            activeWord = null,
                            evaluationResult = null,
                            errorMessage = "BITTI"
                        )
                    }
                }
            } catch (e: Exception) {
                _viewState.update { it.copy(errorMessage = "Veritabanı okuma hatası: ${e.message}") }
            }
        }
    }

    fun playActiveWord() {
        state.value.activeWord?.let { word ->
            tts.speak(word.text)
        }
    }

    fun startListening() {
        // KULAKLIK ZORUNLULUĞU KALDIRILDI! Sadece kelime var mı diye bakıyoruz.
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

    fun evaluateSpeech(finalSpokenText: String) {
        val targetEntity = state.value.activeWord ?: return
        if (finalSpokenText.isBlank()) return

        val sanitizedText = finalSpokenText
            .trim()
            .split("\\s+".toRegex())
            .firstOrNull()
            ?.replace(Regex("[^A-Za-z]"), "")
            ?: ""

        if (sanitizedText.isBlank()) return

        viewModelScope.launch {
            try {
                val result = evaluatePronunciationUseCase(targetEntity, sanitizedText)

                _viewState.update {
                    it.copy(
                        evaluationResult = result,
                        sessionResults = it.sessionResults + SessionWordResult(
                            targetWord = targetEntity.text,
                            spokenWord = sanitizedText,
                            score = result.qualityScore
                        )
                    )
                }
            } catch (e: Exception) {
                _viewState.update { it.copy(errorMessage = "Analiz hatası: ${e.message}") }
            }
        }
    }

    fun proceedToNextWord() {
        voiceParser.reset()
        loadNextWordFromDatabase()
    }

    fun finishSession() {
        _viewState.update { it.copy(isSessionFinished = true) }
    }
    // SPRINT 14: Yanlışlıkla dersi bitirenler için geri dönüş fonksiyonu
    fun resumeSession() {
        _viewState.update { it.copy(isSessionFinished = false) }
    }
    override fun onCleared() {
        super.onCleared()
        headsetObserver.stopObserving()
        voiceParser.destroy()
        tts.shutdown()
    }
}

data class SessionWordResult(
    val targetWord: String,
    val spokenWord: String,
    val score: Int
)

data class RecorderUiState(
    val spokenText: String = "",
    val isSpeaking: Boolean = false,
    val error: String? = null,
    val isHeadsetConnected: Boolean = false,
    val activeWord: VocabularyEntity? = null,
    val evaluationResult: EvaluationResult? = null,
    val sessionResults: List<SessionWordResult> = emptyList(),
    val isSessionFinished: Boolean = false
)

data class RecorderViewState(
    val errorMessage: String? = null,
    val activeWord: VocabularyEntity? = null,
    val evaluationResult: EvaluationResult? = null,
    val sessionResults: List<SessionWordResult> = emptyList(),
    val isSessionFinished: Boolean = false
)