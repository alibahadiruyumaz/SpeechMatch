package com.example.speechmatch.data.repository_impl

import com.example.speechmatch.data.local.dao.ReviewLogDao
import com.example.speechmatch.data.local.dao.UserProfileDao
import com.example.speechmatch.data.local.dao.VocabularyDao
import com.example.speechmatch.data.local.entity.ReviewLogEntity
import com.example.speechmatch.data.local.entity.UserProfileEntity
import com.example.speechmatch.data.local.entity.VocabularyEntity
import com.example.speechmatch.domain.repository.SpeechMatchRepository
import javax.inject.Inject

/** * Domain katmanındaki soyut arayüzü uygulayan ve Room DAO'larını (Veri Erişim Nesneleri)
 * koordine eden veri deposu (Repository) implementasyonu.
 */
class SpeechMatchRepositoryImpl @Inject constructor(
    private val vocabularyDao: VocabularyDao,
    private val reviewLogDao: ReviewLogDao,
    private val userProfileDao: UserProfileDao
) : SpeechMatchRepository {

    // --- VOCABULARY İŞLEMLERİ ---

    /** Yeni bir kelimeyi veritabanına kaydeder. */
    override suspend fun insertWord(word: VocabularyEntity): Long {
        return vocabularyDao.insertWord(word)
    }

    /** Arşivlenmemiş (aktif) tüm kelimeleri getirir. */
    override suspend fun getAllActiveWords(): List<VocabularyEntity> {
        return vocabularyDao.getAllActiveWords()
    }

    /** Belirtilen kimliğe sahip kelimeyi getirir. */
    override suspend fun getWordWithMinimalPair(targetId: Int): List<VocabularyEntity> {
        return vocabularyDao.getWordWithMinimalPair(targetId)
    }

    /** SM-2 barajını geçen kelimeyi arşivler. */
    override suspend fun archiveWord(id: Int) {
        vocabularyDao.archiveWord(id)
    }

    /** Kullanıcının seviyesine uygun, henüz çalışılmamış veya tekrar vakti gelmiş kelimeleri getirir. */
    override suspend fun getWordsToReview(currentTimeMillis: Long): List<VocabularyEntity> {
        return vocabularyDao.getWordsToReview(currentTimeMillis)
    }

    // --- SM-2 REVIEW LOG İŞLEMLERİ ---

    /** Belirli bir kelimenin mevcut SM-2 ilerlemesini ve geçmişini getirir. */
    override suspend fun getReviewLogForWord(vocabId: Int): ReviewLogEntity? {
        return reviewLogDao.getLogForWord(vocabId)
    }

    /** Kelimeye ait SM-2 tekrar günlüğünü veritabanına ekler veya günceller (UPSERT). */
    override suspend fun insertOrUpdateReviewLog(reviewLog: ReviewLogEntity) {
        reviewLogDao.upsertLog(reviewLog)
    }

    // --- USER PROFILE İŞLEMLERİ ---

    /** Kullanıcı profilini ve teşhis verilerini ekler veya günceller (UPSERT). */
    override suspend fun upsertProfile(profile: UserProfileEntity) {
        userProfileDao.upsertProfile(profile)
    }

    /** Sistemdeki aktif kullanıcı profilini getirir. */
    override suspend fun getUserProfile(): UserProfileEntity? {
        return userProfileDao.getUserProfile()
    }
    override suspend fun getArchivedWordCountByLevel(level: String): Int {
        return vocabularyDao.getArchivedWordCountByLevel(level)
    }
}