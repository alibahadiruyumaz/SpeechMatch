package com.example.speechmatch.presentation.recorder

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.speechmatch.data.local.entity.ReviewLogEntity
import com.example.speechmatch.data.local.entity.VocabularyEntity
import com.example.speechmatch.domain.repository.HeadsetStateObserver
import com.example.speechmatch.domain.repository.SpeechMatchRepository
import com.example.speechmatch.domain.repository.VoiceToTextParser
import com.example.speechmatch.domain.usecase.EvaluatePronunciationUseCase
import com.example.speechmatch.domain.usecase.EvaluationResult
import com.example.speechmatch.domain.usecase.UpdateSm2SpacedRepetitionUseCase
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
import kotlinx.coroutines.withContext
import org.json.JSONArray
import javax.inject.Inject

/**
 * Antrenman ekranının iş mantığını, ses analizini ve asenkron veri akışlarını yöneten ana kontrol merkezi.
 * Bu ViewModel; ses tanıma (STT), metin seslendirme (TTS), kulaklık durumu ve veritabanı işlemlerini
 * 'combine' operatörü ile birleştirerek tek bir UI State üzerinden sunar.
 */
@HiltViewModel
class RecorderViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val voiceParser: VoiceToTextParser,
    private val headsetObserver: HeadsetStateObserver,
    private val repository: SpeechMatchRepository,
    private val evaluatePronunciationUseCase: EvaluatePronunciationUseCase,
    private val updateSm2UseCase: UpdateSm2SpacedRepetitionUseCase,
    private val tts: SpeechMatchTTS
) : ViewModel() {

    /** ViewModel içi içsel durum yönetimi (View State). */
    private val _viewState = MutableStateFlow(RecorderViewState())

    /** Üç farklı veri kaynağını birleştirerek UI katmanına tutarlı bir state sağlayan ana akış. */
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

    /** Bu oturumda (session) daha önce sorulan kelimelerin ID'lerini tutar. */
    private val askedWordIds = mutableSetOf<Int>()

    init {
        voiceParser.reset()
        headsetObserver.startObserving()
        seedDatabaseFromJson()
    }

    /** Veritabanı boşsa JSON üzerinden ön yükleme (Seed) yapar. */
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
                        easyRead = jsonObject.getString("easyRead"),
                        cefrLevel = jsonObject.getString("cefrLevel"),
                        minimalPairId = jsonObject.getInt("minimalPairId"),
                        isArchived = false
                    )
                    repository.insertWord(entity)
                }
                loadNextWordFromDatabase()
            } catch (e: Exception) {
                _viewState.update { it.copy(errorMessage = "JSON Ayrıştırma Hatası: ${e.message}") }
            }
        }
    }

    /** * SM-2 zamanlamasına ve KULLANICININ CEFR SEVİYESİNE göre uygun kelimeleri filtreler ve yükler.
     */
    private fun loadNextWordFromDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                delay(300) // UI geçişleri için kısa bekleme

                // 1. Kullanıcının seviyesini al
                val userProfile = repository.getUserProfile()
                val userLevel = userProfile?.currentLevel ?: "A1"

                // 2. Seviye hiyerarşisi oluştur (Örn: B1 ise A1, A2, B1 kelimelerini görebilsin)
                val cefrLevels = listOf("A1", "A2", "B1", "B2", "C1", "C2")
                val levelIndex = cefrLevels.indexOf(userLevel).takeIf { it >= 0 } ?: 0
                val allowedLevels = cefrLevels.take(levelIndex + 1)

                // 3. Veritabanından kelimeleri çek ve FİLTRELE
                val wordsToReview = repository.getWordsToReview(System.currentTimeMillis())
                    .filter { it.id !in askedWordIds } // Oturumda sorulanları ele
                    .filter { it.cefrLevel in allowedLevels } // SADECE KENDİ SEVİYESİNDEKİLERİ GETİR

                if (wordsToReview.isNotEmpty()) {
                    val nextWord = wordsToReview.random()
                    askedWordIds.add(nextWord.id)

                    _viewState.update {
                        it.copy(
                            activeWord = nextWord,
                            errorMessage = null,
                            evaluationResult = null
                        )
                    }
                } else {
                    _viewState.update { it.copy(activeWord = null, evaluationResult = null, errorMessage = "BITTI") }
                }
            } catch (e: Exception) {
                _viewState.update { it.copy(errorMessage = "Veritabanı okuma hatası: ${e.message}") }
            }
        }
    }

    fun playActiveWord() {
        state.value.activeWord?.let { word -> tts.speak(word.text) }
    }

    fun playWord(wordText: String) {
        tts.speak(wordText)
    }

    fun startListening() {
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

    /** * Kullanıcının telaffuzunu analiz eder, rapora ekler ve SM-2 veritabanını GÜNCELLER.
     */
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
                // Akustik Analiz (Levenshtein Puanlaması)
                val result = evaluatePronunciationUseCase(targetEntity, sanitizedText)

                // SM-2 Tarihlerini Güncelleme ve Veritabanına Kaydetme (IO Thread içinde yapılır)
                withContext(Dispatchers.IO) {
                    val existingLog = repository.getReviewLogForWord(targetEntity.id)

                    val sm2Result = updateSm2UseCase(
                        qualityScore = result.qualityScore,
                        previousEaseFactor = existingLog?.easeFactor ?: 2.5,
                        previousInterval = existingLog?.intervalDays ?: 0,
                        consecutiveCorrect = 0
                    )

                    val newLog = ReviewLogEntity(
                        logId = existingLog?.logId ?: 0,
                        vocabId = targetEntity.id,
                        lastAccuracyScore = result.qualityScore,
                        easeFactor = sm2Result.easeFactor,
                        intervalDays = sm2Result.intervalDays,
                        nextReviewDate = sm2Result.nextReviewDate
                    )
                    repository.insertOrUpdateReviewLog(newLog)

                    // --- EKSİK OLAN KISIM: 180 GÜN KURALI VE TERFİ MANTIĞI EKLENDİ ---
                    if (sm2Result.intervalDays >= 180) {
                        // 1. Kelimeyi Kalıcı Hafızaya Geçtiği İçin Arşivle
                        repository.archiveWord(targetEntity.id)

                        // 2. Kullanıcının Seviyesini Kontrol Et
                        val userProfile = repository.getUserProfile()
                        if (userProfile != null) {
                            val currentLevel = userProfile.currentLevel

                            // 3. Bu seviyeden kaç kelime arşivlendiğini say (20 Kelime Barajı)
                            val archivedCount = repository.getArchivedWordCountByLevel(currentLevel)

                            if (archivedCount >= 20) {
                                val cefrLevels = listOf("A1", "A2", "B1", "B2", "C1", "C2")
                                val currentIndex = cefrLevels.indexOf(currentLevel)

                                // Zaten C2 değilse bir üst seviyeye terfi ettir
                                if (currentIndex in 0 until cefrLevels.size - 1) {
                                    val nextLevel = cefrLevels[currentIndex + 1]
                                    val updatedProfile = userProfile.copy(currentLevel = nextLevel)
                                    repository.upsertProfile(updatedProfile)
                                }
                            }
                        }
                    }
                    // -----------------------------------------------------------------
                }

                // UI State'ini (Arayüzü) Güncelleme
                _viewState.update { currentState ->
                    val filteredResults = currentState.sessionResults.filterNot { it.targetWord == targetEntity.text }

                    currentState.copy(
                        evaluationResult = result,
                        sessionResults = filteredResults + SessionWordResult(
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

/** Oturum sonu raporunda gösterilecek her bir kelime değerlendirmesini temsil eden veri sınıfı. */
data class SessionWordResult(
    val targetWord: String,
    val spokenWord: String,
    val score: Int
)

/** Kullanıcı arayüzüne (UI) sunulan, tüm veri kaynaklarının birleştiği nihai durum paketi. */
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

/** ViewModel içinde saklanan ve sadece dahili mantığı besleyen durum sınıfı. */
data class RecorderViewState(
    val errorMessage: String? = null,
    val activeWord: VocabularyEntity? = null,
    val evaluationResult: EvaluationResult? = null,
    val sessionResults: List<SessionWordResult> = emptyList(),
    val isSessionFinished: Boolean = false
)