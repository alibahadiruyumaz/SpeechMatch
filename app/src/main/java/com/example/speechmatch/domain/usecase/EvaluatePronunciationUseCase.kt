package com.example.speechmatch.domain.usecase

import com.example.speechmatch.data.local.entity.ReviewLogEntity
import com.example.speechmatch.data.local.entity.VocabularyEntity
import com.example.speechmatch.domain.repository.SpeechMatchRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class EvaluationResult(
    val qualityScore: Int,
    val isPerfect: Boolean
)

class EvaluatePronunciationUseCase @Inject constructor(
    private val repository: SpeechMatchRepository
) {
    private val calculateLevenshtein = CalculateLevenshteinScoreUseCase()
    private val updateSm2 = UpdateSm2SpacedRepetitionUseCase()

    suspend operator fun invoke(
        targetWord: VocabularyEntity,
        spokenText: String
    ): EvaluationResult = withContext(Dispatchers.IO) {

        val qScore = calculateLevenshtein(targetWord.text, spokenText)

        val currentLog = repository.getReviewLogForWord(targetWord.id) ?: ReviewLogEntity(
            logId = 0,
            vocabId = targetWord.id,
            lastAccuracyScore = 0,
            easeFactor = 2.5,
            intervalDays = 0,
            nextReviewDate = System.currentTimeMillis()
        )

        val syntheticConsecutive = if (currentLog.intervalDays > 0) 2 else 0

        val sm2Result = updateSm2(
            qualityScore = qScore,
            previousEaseFactor = currentLog.easeFactor,
            previousInterval = currentLog.intervalDays,
            consecutiveCorrect = syntheticConsecutive
        )

        val newLog = currentLog.copy(
            lastAccuracyScore = qScore,
            easeFactor = sm2Result.easeFactor,
            intervalDays = sm2Result.intervalDays,
            nextReviewDate = sm2Result.nextReviewDate
        )
        repository.insertOrUpdateReviewLog(newLog)

        return@withContext EvaluationResult(
            qualityScore = qScore,
            isPerfect = qScore >= 4
        )
    }
}