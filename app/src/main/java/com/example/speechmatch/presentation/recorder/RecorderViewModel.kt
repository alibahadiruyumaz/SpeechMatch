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

/**
 * Antrenman ekranının iş mantığını, ses analizini ve asenkron veri akışlarını yöneten ana kontrol merkezi.
 * * Bu ViewModel; ses tanıma (STT), metin seslendirme (TTS), kulaklık durumu ve veritabanı işlemlerini
 * 'combine' operatörü ile birleştirerek tek bir UI State üzerinden sunar.
 */
@HiltViewModel
class RecorderViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val voiceParser: VoiceToTextParser,
    private val headsetObserver: HeadsetStateObserver,
    private val repository: SpeechMatchRepository,
    private val evaluatePronunciationUseCase: EvaluatePronunciationUseCase,
    private val tts: SpeechMatchTTS
) : ViewModel() {

    /** ViewModel içi içsel durum yönetimi (View State). */
    private val _viewState = MutableStateFlow(RecorderViewState())

    /** * Üç farklı veri kaynağını (Ses motoru, Donanım gözlemcisi, Dahili durum) birleştirerek
     * UI katmanına tutarlı bir [RecorderUiState] sağlayan ana akış.
     */
    val state = combine(
        voiceParser.state,
        headsetObserver.isHeadsetConnected,
        _viewState
    ) { parserState, isHeadsetConnected, viewState ->

        // Hata önceliklendirme mantığı: Kritik sistem hataları, parser hatalarına göre önceliklidir.
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

    /** * Veritabanı boşsa JSON üzerinden ön yükleme (Seed) yapar,
     * doluysa doğrudan çalışma listesini (Next Word) hazırlar.
     */
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
                _viewState.update { it.copy(errorMessage = "JSON Ayrıştırma Hatası: ${e.message}") }
            }
        }
    }

    /** SM-2 zamanlamasına göre bugün tekrar edilmesi gereken kelimelerden rastgele birini yükler. */
    private fun loadNextWordFromDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                delay(300) // UI geçişleri için kısa bekleme
                val wordsToReview = repository.getWordsToReview(System.currentTimeMillis())

                if (wordsToReview.isNotEmpty()) {
                    _viewState.update {
                        it.copy(activeWord = wordsToReview.random(), errorMessage = null, evaluationResult = null)
                    }
                } else {
                    _viewState.update { it.copy(activeWord = null, evaluationResult = null, errorMessage = "BITTI") }
                }
            } catch (e: Exception) {
                _viewState.update { it.copy(errorMessage = "Veritabanı okuma hatası: ${e.message}") }
            }
        }
    }

    /** TTS motorunu kullanarak hedef kelimenin doğru Amerikan İngilizcesi telaffuzunu seslendirir. */
    fun playActiveWord() {
        state.value.activeWord?.let { word -> tts.speak(word.text) }
    }

    /** Ses dinleme sürecini başlatır ve ekran durumunu temizler. */
    fun startListening() {
        if (state.value.activeWord == null) {
            _viewState.update { it.copy(errorMessage = "Önce hedef kelimeyi yüklemelisiniz.") }
            return
        }
        _viewState.update { it.copy(errorMessage = null, evaluationResult = null) }
        voiceParser.startListening("en-US")
    }

    /** Ses dinleme sürecini durdurur ve STT analizini sonlandırır. */
    fun stopListening() {
        voiceParser.stopListening()
    }

    /** * Kullanıcının telaffuzunu Levenshtein ve SM-2 metrikleri ile analiz eder
     * ve oturum sonuçlarına (Session Results) kaydeder.
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

    /** Mevcut kelimeyi ve ses motorunu sıfırlayarak bir sonraki kelimeye geçer. */
    fun proceedToNextWord() {
        voiceParser.reset()
        loadNextWordFromDatabase()
    }

    /** Mevcut çalışma oturumunu sonlandırır ve analiz raporu ekranına geçişi sağlar. */
    fun finishSession() {
        _viewState.update { it.copy(isSessionFinished = true) }
    }

    /** Rapor ekranından mevcut antrenmana geri dönülmesini sağlar. */
    fun resumeSession() {
        _viewState.update { it.copy(isSessionFinished = false) }
    }

    /** ViewModel yok edildiğinde sistem kaynaklarını (Donanım gözlemcisi, TTS, STT) serbest bırakır. */
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