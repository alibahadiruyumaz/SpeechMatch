package com.example.speechmatch.domain.usecase

import com.example.speechmatch.data.local.entity.ReviewLogEntity
import com.example.speechmatch.data.local.entity.VocabularyEntity
import com.example.speechmatch.domain.repository.SpeechMatchRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.max

/** Telaffuz değerlendirmesinin sonucunu ve başarı durumunu arayüze ileten veri sınıfı. */
data class EvaluationResult(
    /** SM-2 algoritmasının çalışması için gereken 0-5 arası kalite skoru. */
    val qualityScore: Int,
    /** UI (Arayüz) raporlarında gösterilecek %0 ile %100 arası net doğruluk yüzdesi. */
    val accuracyPercentage: Int,
    /** Skorun 4 veya 5 olması (Yüzde 75 ve üzeri) durumunda telaffuzun başarılı sayılıp sayılmadığı. */
    val isPerfect: Boolean
)

/** * Levenshtein fonetik analizini ve SM-2 aralıklı tekrar algoritmalarını
 * koordine eden (Orchestrator) ana UseCase. Çoklu profil desteği (userId) eklenmiştir.
 */
class EvaluatePronunciationUseCase @Inject constructor(
    private val repository: SpeechMatchRepository
) {
    // SM-2 algoritmasını başlatıyoruz
    private val updateSm2 = UpdateSm2SpacedRepetitionUseCase()

    suspend operator fun invoke(
        targetWord: VocabularyEntity,
        spokenText: String,
        userId: Int
    ): EvaluationResult = withContext(Dispatchers.IO) {

        // 1. Hocanın İstediği Yüzdelik Hesabı (Levenshtein Distance -> Yüzde Çevirimi)
        val target = targetWord.text.lowercase()
        val spoken = spokenText.lowercase()

        val distance = computeLevenshteinDistance(target, spoken)
        val maxLength = max(target.length, spoken.length)

        // Hata sıfıra bölünmeyi engellemek için
        val percentage = if (maxLength == 0) 0 else {
            (((maxLength - distance).toDouble() / maxLength) * 100).toInt()
        }

        // 2. Yüzdeyi SM-2'nin anladığı 0-5 sistemine gizlice çevirme
        val qScore = when {
            percentage >= 90 -> 5 // Neredeyse kusursuz
            percentage >= 75 -> 4 // Ufak bir hata var ama anlaşılır
            percentage >= 60 -> 3 // Hatalı ama geçer not
            percentage >= 40 -> 2 // Kötü
            percentage >= 20 -> 1 // Çok Kötü
            else -> 0             // Alakası yok
        }

        // 3. Kelimenin KULLANICIYA ÖZEL SM-2 kaydı çekilir; kayıt yoksa varsayılan değerlerle oluşturulur.
        val currentLog = repository.getReviewLogForWord(targetWord.id, userId) ?: ReviewLogEntity(
            logId = 0,
            userId = userId,
            vocabId = targetWord.id,
            lastAccuracyScore = 0,
            easeFactor = 2.5,
            intervalDays = 0,
            nextReviewDate = System.currentTimeMillis()
        )

        // 4. SM-2 için ardışık doğru bilme (consecutive) serisi hesaplanır.
        val syntheticConsecutive = if (currentLog.intervalDays > 0) 2 else 0

        // 5. SM-2 algoritması çalıştırılarak yeni zamanlamalar belirlenir.
        val sm2Result = updateSm2(
            qualityScore = qScore,
            previousEaseFactor = currentLog.easeFactor,
            previousInterval = currentLog.intervalDays,
            consecutiveCorrect = syntheticConsecutive
        )

        // 6. Güncellenen SM-2 logu veritabanına (Room) UPSERT edilir.
        val newLog = currentLog.copy(
            lastAccuracyScore = qScore,
            easeFactor = sm2Result.easeFactor,
            intervalDays = sm2Result.intervalDays,
            nextReviewDate = sm2Result.nextReviewDate
        )
        repository.insertOrUpdateReviewLog(newLog)

        // 7. Değerlendirme sonucu arayüz (UI) katmanına döndürülür.
        return@withContext EvaluationResult(
            qualityScore = qScore,
            accuracyPercentage = percentage,
            isPerfect = qScore >= 4
        )
    }

    /** * İki metin arasındaki minimum düzenleme mesafesini (Levenshtein Distance) hesaplayan yardımcı fonksiyon.
     * Bu fonksiyon "Update" ve "Updat" kelimeleri arasındaki 1 harflik farkı bulur.
     */
    private fun computeLevenshteinDistance(lhs: CharSequence, rhs: CharSequence): Int {
        val lhsLength = lhs.length
        val rhsLength = rhs.length

        var cost = Array(lhsLength + 1) { it }
        var newCost = Array(lhsLength + 1) { 0 }

        for (i in 1..rhsLength) {
            newCost[0] = i
            for (j in 1..lhsLength) {
                val match = if (lhs[j - 1] == rhs[i - 1]) 0 else 1
                val costReplace = cost[j - 1] + match
                val costInsert = cost[j] + 1
                val costDelete = newCost[j - 1] + 1
                newCost[j] = minOf(costInsert, costDelete, costReplace)
            }
            val swap = cost
            cost = newCost
            newCost = swap
        }
        return cost[lhsLength]
    }
}