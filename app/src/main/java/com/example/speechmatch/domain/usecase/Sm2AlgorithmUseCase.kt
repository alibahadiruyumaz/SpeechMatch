package com.example.speechmatch.domain.usecase

import javax.inject.Inject
import kotlin.math.max

// SM-2 Algoritmasının sonuçlarını tutacak bir Veri Sınıfı (Data Class)
// Bu model sadece Domain katmanına aittir, Android SDK içermez.
data class Sm2Result(
    val easeFactor: Double,
    val intervalDays: Int,
    val consecutiveCorrectAnswers: Int,
    val nextReviewTimestamp: Long // SQLite'a yazılacak Unix Zaman Damgası
)

class Sm2AlgorithmUseCase @Inject constructor() {

    // Bir günün milisaniye cinsinden matematiksel karşılığı (24 saat * 60 dk * 60 sn * 1000 ms)
    private val ONE_DAY_IN_MILLIS = 86400000L

    operator fun invoke(
        qualityScore: Int,      // Levenshtein'den gelen akustik skor (0-5)
        previousEaseFactor: Double = 2.5, // Daha önce sorulmadıysa varsayılan 2.5
        previousInterval: Int = 0,        // Daha önce sorulmadıysa 0
        consecutiveCorrect: Int = 0       // Üst üste doğru bilme serisi
    ): Sm2Result {

        var newEaseFactor = previousEaseFactor
        var newInterval: Int
        var newConsecutiveCorrect = consecutiveCorrect

        // 1. Kullanıcı 3'ün altında skor aldıysa (Başarısız Telaffuz)
        if (qualityScore < 3) {
            newConsecutiveCorrect = 0 // Seriyi sıfırla (Ceza)
            newInterval = 1           // Hemen yarın tekrar sorulacak
        }
        // 2. Kullanıcı geçerli bir skor aldıysa (Başarılı Telaffuz)
        else {
            newConsecutiveCorrect += 1

            // SM-2 Kolaylık Faktörü (EF) formülü
            newEaseFactor = previousEaseFactor + (0.1 - (5 - qualityScore) * (0.08 + (5 - qualityScore) * 0.02))
            newEaseFactor = max(1.3, newEaseFactor) // EF asla 1.3'ün altına düşemez

            // Tekrar Aralığını (Interval) hesapla
            newInterval = when (newConsecutiveCorrect) {
                1 -> 1
                2 -> 6
                else -> (previousInterval * newEaseFactor).toInt()
            }
        }

        // 3. Hesaplanan gün sayısını (Interval), şu anki zamanın üzerine milisaniye olarak ekle
        val currentTimeMillis = System.currentTimeMillis()
        val nextReviewTimestamp = currentTimeMillis + (newInterval * ONE_DAY_IN_MILLIS)

        return Sm2Result(
            easeFactor = newEaseFactor,
            intervalDays = newInterval,
            consecutiveCorrectAnswers = newConsecutiveCorrect,
            nextReviewTimestamp = nextReviewTimestamp
        )
    }
}