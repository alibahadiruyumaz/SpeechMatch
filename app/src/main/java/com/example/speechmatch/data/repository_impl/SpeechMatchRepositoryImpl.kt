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

    /** Yeni bir kelimeyi ortak havuza kaydeder. */
    override suspend fun insertWord(word: VocabularyEntity): Long {
        return vocabularyDao.insertWord(word)
    }

    /** Belirtilen kullanıcı için arşivlenmemiş (aktif) tüm kelimeleri getirir. */
    override suspend fun getAllActiveWords(userId: Int): List<VocabularyEntity> {
        return vocabularyDao.getAllActiveWords(userId)
    }

    /** Belirtilen kimliğe sahip kelimeyi getirir. */
    override suspend fun getWordWithMinimalPair(targetId: Int): List<VocabularyEntity> {
        return vocabularyDao.getWordWithMinimalPair(targetId)
    }

    /** 180 gün barajını geçen kelimeyi SADECE İLGİLİ KULLANICI İÇİN arşivler. */
    override suspend fun archiveWord(vocabId: Int, userId: Int) {
        vocabularyDao.archiveWord(vocabId, userId)
    }

    /** Kullanıcının seviyesine uygun, henüz çalışılmamış veya tekrar vakti gelmiş kelimeleri o kullanıcıya özel getirir. */
    override suspend fun getWordsToReview(currentTimeMillis: Long, userId: Int): List<VocabularyEntity> {
        return vocabularyDao.getWordsToReview(currentTimeMillis, userId)
    }

    /** Kullanıcının belirli bir seviyede arşivlediği (öğrendiği) toplam kelime sayısını döndürür. */
    override suspend fun getArchivedWordCountByLevel(level: String, userId: Int): Int {
        return vocabularyDao.getArchivedWordCountByLevel(level, userId)
    }


    // --- SM-2 REVIEW LOG İŞLEMLERİ ---

    /** Belirli bir kelimenin, ilgili kullanıcıya ait mevcut SM-2 ilerlemesini ve geçmişini getirir. */
    override suspend fun getReviewLogForWord(vocabId: Int, userId: Int): ReviewLogEntity? {
        return reviewLogDao.getLogForWord(vocabId, userId)
    }

    /** Kelimeye ve kullanıcıya ait SM-2 tekrar günlüğünü veritabanına ekler veya günceller (UPSERT). */
    override suspend fun insertOrUpdateReviewLog(reviewLog: ReviewLogEntity) {
        reviewLogDao.upsertLog(reviewLog)
    }


    // --- USER PROFILE İŞLEMLERİ ---

    /** Kullanıcı profilini ve teşhis verilerini ekler veya günceller (UPSERT). */
    override suspend fun upsertProfile(profile: UserProfileEntity) {
        userProfileDao.upsertProfile(profile)
    }

    /** Sistemdeki belirli bir ID'ye sahip kullanıcı profilini getirir. */
    override suspend fun getUserProfile(userId: Int): UserProfileEntity? {
        return userProfileDao.getUserProfile(userId)
    }

    /** Çoklu profil (Netflix) ekranı için sistemdeki tüm kullanıcıları getirir. */
    override suspend fun getAllProfiles(): List<UserProfileEntity> {
        return userProfileDao.getAllProfiles()
    }

    override suspend fun deleteProfile(profile: UserProfileEntity) {
        userProfileDao.deleteProfile(profile)
    }
}