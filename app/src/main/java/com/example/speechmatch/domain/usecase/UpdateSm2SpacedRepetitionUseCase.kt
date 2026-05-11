package com.example.speechmatch.domain.usecase

import java.util.Calendar

/**
 * Öz-Beyana Dayalı SRS'in reddi.
 * Levenshtein mesafe skorunu (q) alarak SM-2 algoritmasını otonom olarak işletir.
 */
data class Sm2Result(
    val easeFactor: Double, // Float'tan Double'a terfi ettirildi
    val intervalDays: Int,
    val consecutiveCorrectAnswers: Int,
    val nextReviewDate: Long
)

class UpdateSm2SpacedRepetitionUseCase {

    operator fun invoke(
        qualityScore: Int,
        previousEaseFactor: Double, // Float'tan Double'a terfi ettirildi
        previousInterval: Int,
        consecutiveCorrect: Int
    ): Sm2Result {


        var newEaseFactor = previousEaseFactor + (0.1 - (5 - qualityScore) * (0.08 + (5 - qualityScore) * 0.02))
        if (newEaseFactor < 1.3) {
            newEaseFactor = 1.3
        }

        val newInterval: Int
        val newConsecutiveCorrect: Int

        if (qualityScore < 3) {
            newInterval = 1
            newConsecutiveCorrect = 0
        } else {
            newConsecutiveCorrect = consecutiveCorrect + 1
            newInterval = when (newConsecutiveCorrect) {
                1 -> 1
                2 -> 6
                else -> (previousInterval * newEaseFactor).toInt()
            }
        }

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