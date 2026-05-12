package com.example.speechmatch.domain.usecase

import java.util.Calendar

/**
 * SM-2 aralıklı tekrar (Spaced Repetition) algoritmasının hesaplama sonucunu barındıran veri sınıfı.
 */
data class Sm2Result(
    /** Kelimenin öğrenilme kolaylık katsayısı (Minimum 1.3). */
    val easeFactor: Double,
    /** Kelimenin bir sonraki sorulmasına kadar geçecek gün sayısı. */
    val intervalDays: Int,
    /** Kelimenin arka arkaya kaç kez doğru (q >= 3) bilindiği. */
    val consecutiveCorrectAnswers: Int,
    /** Bir sonraki çalışma tarihinin milisaniye (Epoch) cinsinden değeri. */
    val nextReviewDate: Long
)

/**
 * Levenshtein algoritmasından elde edilen kalite skorunu (q) kullanarak,
 * kelimenin bir sonraki çalışma zamanlamasını (SM-2) otonom olarak hesaplayan UseCase.
 */
class UpdateSm2SpacedRepetitionUseCase {

    /**
     * SM-2 formülünü uygulayarak yeni tekrar aralığını (interval) ve kolaylık faktörünü (EF) belirler.
     * * @param qualityScore Telaffuz kalite skoru (0 ile 5 arası).
     * @param previousEaseFactor Kelimenin mevcut kolaylık faktörü.
     * @param previousInterval Kelimenin mevcut tekrar aralığı (gün).
     * @param consecutiveCorrect Mevcut ardışık doğru bilme serisi.
     * @return Yeni zamanlama verilerini içeren [Sm2Result] nesnesi.
     */
    operator fun invoke(
        qualityScore: Int,
        previousEaseFactor: Double,
        previousInterval: Int,
        consecutiveCorrect: Int
    ): Sm2Result {

        // 1. Yeni Kolaylık Faktörünün (Ease Factor) hesaplanması.
        var newEaseFactor = previousEaseFactor + (0.1 - (5 - qualityScore) * (0.08 + (5 - qualityScore) * 0.02))
        if (newEaseFactor < 1.3) {
            newEaseFactor = 1.3 // EF değeri 1.3'ün altına düşemez.
        }

        val newInterval: Int
        val newConsecutiveCorrect: Int

        // 2. Kalite skoruna göre (q < 3 başarısız, q >= 3 başarılı) Interval ve Seri güncellemeleri.
        if (qualityScore < 3) {
            // Başarısızlık durumu: Seri sıfırlanır, aralık 1 güne düşer.
            newInterval = 1
            newConsecutiveCorrect = 0
        } else {
            // Başarı durumu: Seri artar, aralık SM-2 kurallarına göre katlanarak büyür.
            newConsecutiveCorrect = consecutiveCorrect + 1
            newInterval = when (newConsecutiveCorrect) {
                1 -> 1
                2 -> 6
                else -> (previousInterval * newEaseFactor).toInt()
            }
        }

        // 3. Yeni aralığın (Interval) bugünün tarihine eklenerek bir sonraki çalışma tarihinin bulunması.
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, newInterval)

        return Sm2Result(
            easeFactor = newEaseFactor,
            intervalDays = newInterval,
            consecutiveCorrectAnswers = newConsecutiveCorrect,
            nextReviewDate = calendar.timeInMillis
        )
    }
}