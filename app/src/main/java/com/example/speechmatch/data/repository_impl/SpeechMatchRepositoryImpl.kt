package com.example.speechmatch.data.repository_impl

import com.example.speechmatch.data.local.dao.ReviewLogDao
import com.example.speechmatch.data.local.dao.UserProfileDao
import com.example.speechmatch.data.local.dao.VocabularyDao
import com.example.speechmatch.data.local.entity.ReviewLogEntity
import com.example.speechmatch.data.local.entity.UserProfileEntity
import com.example.speechmatch.data.local.entity.VocabularyEntity
import com.example.speechmatch.domain.repository.SpeechMatchRepository
import javax.inject.Inject

/**
 * Data Katmanı İşçi Sınıfı (Implementation).
 * Domain katmanındaki SpeechMatchRepository anayasasına kesin bir şekilde uyar.
 * Gelen soyut emirleri, Room DB DAO'ları aracılığıyla fiziksel disk işlemlerine dönüştürür.
 */
class SpeechMatchRepositoryImpl @Inject constructor(
    private val vocabularyDao: VocabularyDao,
    private val reviewLogDao: ReviewLogDao,
    private val userProfileDao: UserProfileDao
) : SpeechMatchRepository {

    // --- VOCABULARY İŞLEMLERİ ---
    override suspend fun insertWord(word: VocabularyEntity): Long {
        return vocabularyDao.insertWord(word)
    }

    override suspend fun getAllActiveWords(): List<VocabularyEntity> {
        return vocabularyDao.getAllActiveWords()
    }

    override suspend fun getWordWithMinimalPair(targetId: Int): List<VocabularyEntity> {
        return vocabularyDao.getWordWithMinimalPair(targetId)
    }

    override suspend fun archiveWord(id: Int) {
        vocabularyDao.archiveWord(id)
    }

    override suspend fun getWordsToReview(currentTimeMillis: Long): List<VocabularyEntity> {
        // İleride DAO içerisinde bugünden önce tekrar edilmesi gereken kelimeleri getiren SQL yazılacak
        return vocabularyDao.getWordsToReview(currentTimeMillis)
    }

    // --- SM-2 REVIEW LOG İŞLEMLERİ ---
    override suspend fun getReviewLogForWord(vocabId: Int): ReviewLogEntity? {
        // Arayüzdeki yeni ismi, DAO'daki eski isme (getLogForWord) bağlıyoruz
        return reviewLogDao.getLogForWord(vocabId)
    }

    override suspend fun insertOrUpdateReviewLog(reviewLog: ReviewLogEntity) {
        // Arayüzdeki yeni ismi, DAO'daki eski isme (upsertLog) bağlıyoruz
        reviewLogDao.upsertLog(reviewLog)
    }

    // --- USER PROFILE İŞLEMLERİ ---
    override suspend fun upsertProfile(profile: UserProfileEntity) {
        userProfileDao.upsertProfile(profile)
    }

    override suspend fun getUserProfile(): UserProfileEntity? {
        return userProfileDao.getUserProfile()
    }
}