package com.example.speechmatch.data.repository_impl

import com.example.speechmatch.data.local.dao.ReviewLogDao
import com.example.speechmatch.data.local.dao.UserProfileDao
import com.example.speechmatch.data.local.dao.VocabularyDao
import com.example.speechmatch.data.local.entity.ReviewLogEntity
import com.example.speechmatch.data.local.entity.UserProfileEntity
import com.example.speechmatch.data.local.entity.VocabularyEntity
import com.example.speechmatch.domain.repository.SpeechMatchRepository
import javax.inject.Inject

// @Inject constructor sayesinde Hilt, arka planda DAO'ları otomatik olarak buraya besler.
class SpeechMatchRepositoryImpl @Inject constructor(
    private val vocabularyDao: VocabularyDao,
    private val reviewLogDao: ReviewLogDao,
    private val userProfileDao: UserProfileDao
) : SpeechMatchRepository {

    override suspend fun insertWord(word: VocabularyEntity): Long = vocabularyDao.insertWord(word)
    override suspend fun getAllActiveWords(): List<VocabularyEntity> = vocabularyDao.getAllActiveWords()
    override suspend fun getWordWithMinimalPair(targetId: Int): List<VocabularyEntity> = vocabularyDao.getWordWithMinimalPair(targetId)
    override suspend fun archiveWord(id: Int) = vocabularyDao.archiveWord(id)

    override suspend fun upsertLog(log: ReviewLogEntity) = reviewLogDao.upsertLog(log)
    override suspend fun getDueLogs(currentTime: Long): List<ReviewLogEntity> = reviewLogDao.getDueLogs(currentTime)
    override suspend fun getLogForWord(vocabId: Int): ReviewLogEntity? = reviewLogDao.getLogForWord(vocabId)

    override suspend fun upsertProfile(profile: UserProfileEntity) = userProfileDao.upsertProfile(profile)
    override suspend fun getUserProfile(): UserProfileEntity? = userProfileDao.getUserProfile()
}