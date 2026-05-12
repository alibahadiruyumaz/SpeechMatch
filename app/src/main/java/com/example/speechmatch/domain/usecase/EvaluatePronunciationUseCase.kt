package com.example.speechmatch.domain.usecase

import com.example.speechmatch.data.local.entity.ReviewLogEntity
import com.example.speechmatch.data.local.entity.VocabularyEntity
import com.example.speechmatch.domain.repository.SpeechMatchRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/** Telaffuz değerlendirmesinin sonucunu ve başarı durumunu arayüze ileten veri sınıfı. */
data class EvaluationResult(
    /** 0 ile 5 arasında hesaplanan Levenshtein doğruluk skoru. */
    val qualityScore: Int,
    /** Skorun 4 veya 5 olması durumunda telaffuzun başarılı (perfect) sayılıp sayılmadığı. */
    val isPerfect: Boolean
)

/** * Levenshtein fonetik analizini ve SM-2 aralıklı tekrar algoritmalarını
 * koordine eden (Orchestrator) ana UseCase.
 */
class EvaluatePronunciationUseCase @Inject constructor(
    private val repository: SpeechMatchRepository
) {
    private val calculateLevenshtein = CalculateLevenshteinScoreUseCase()
    private val updateSm2 = UpdateSm2SpacedRepetitionUseCase()

    /**
     * Kullanıcının telaffuzunu analiz eder, SM-2 algoritmasını işletir ve ilerlemeyi veritabanına kaydeder.
     * Bu işlem veritabanı G/Ç (I/O) içerdiği için IO Dispatcher üzerinde asenkron olarak çalışır.
     * * @param targetWord Okunması beklenen hedef kelimenin veritabanı nesnesi.
     * @param spokenText STT motorundan dönen kullanıcı telaffuzu.
     * @return Analiz sonucunu barındıran [EvaluationResult] nesnesi.
     */
    suspend operator fun invoke(
        targetWord: VocabularyEntity,
        spokenText: String
    ): EvaluationResult = withContext(Dispatchers.IO) {

        // 1. Levenshtein algoritması ile kalite skoru (q) hesaplanır.
        val qScore = calculateLevenshtein(targetWord.text, spokenText)

        // 2. Kelimenin mevcut SM-2 kaydı çekilir; kayıt yoksa varsayılan (default) değerlerle oluşturulur.
        val currentLog = repository.getReviewLogForWord(targetWord.id) ?: ReviewLogEntity(
            logId = 0,
            vocabId = targetWord.id,
            lastAccuracyScore = 0,
            easeFactor = 2.5,
            intervalDays = 0,
            nextReviewDate = System.currentTimeMillis()
        )

        // 3. SM-2 için ardışık doğru bilme (consecutive) serisi hesaplanır.
        val syntheticConsecutive = if (currentLog.intervalDays > 0) 2 else 0

        // 4. SM-2 algoritması çalıştırılarak yeni zamanlamalar (Interval, EF, Date) belirlenir.
        val sm2Result = updateSm2(
            qualityScore = qScore,
            previousEaseFactor = currentLog.easeFactor,
            previousInterval = currentLog.intervalDays,
            consecutiveCorrect = syntheticConsecutive
        )

        // 5. Güncellenen SM-2 logu veritabanına (Room) UPSERT edilir.
        val newLog = currentLog.copy(
            lastAccuracyScore = qScore,
            easeFactor = sm2Result.easeFactor,
            intervalDays = sm2Result.intervalDays,
            nextReviewDate = sm2Result.nextReviewDate
        )
        repository.insertOrUpdateReviewLog(newLog)

        // 6. Değerlendirme sonucu arayüz (UI) katmanına döndürülür.
        return@withContext EvaluationResult(
            qualityScore = qScore,
            isPerfect = qScore >= 4
        )
    }
}