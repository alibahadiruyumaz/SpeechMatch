package com.example.speechmatch.domain.repository

import com.example.speechmatch.data.local.entity.ReviewLogEntity
import com.example.speechmatch.data.local.entity.UserProfileEntity
import com.example.speechmatch.data.local.entity.VocabularyEntity

/** Domain katmanı (Use Cases) ile veri kaynakları arasındaki iletişimi tanımlayan soyut veri deposu sözleşmesi (Repository Contract). */
interface SpeechMatchRepository {

    // --- VOCABULARY İŞLEMLERİ ---

    /** Yeni bir kelimeyi havuza ekler. */
    suspend fun insertWord(word: VocabularyEntity): Long

    /** Arşivlenmemiş (aktif) tüm kelimeleri getirir. */
    suspend fun getAllActiveWords(): List<VocabularyEntity>

    /** Belirtilen kimliğe (ID) sahip kelimeyi getirir. */
    suspend fun getWordWithMinimalPair(targetId: Int): List<VocabularyEntity>

    /** SM-2 barajını geçen (öğrenilmiş) kelimeyi arşivler. */
    suspend fun archiveWord(id: Int)

    /** Aralıklı tekrar (SM-2) algoritmasına göre bugün çalışılması gereken kelimeleri getirir. */
    suspend fun getWordsToReview(currentTimeMillis: Long): List<VocabularyEntity>

    // --- SM-2 REVIEW LOG İŞLEMLERİ ---

    /** Belirli bir kelimenin mevcut SM-2 ilerleme durumunu ve geçmişini getirir. */
    suspend fun getReviewLogForWord(vocabId: Int): ReviewLogEntity?

    /** Kelimenin SM-2 performans günlüğünü ekler veya günceller (UPSERT). */
    suspend fun insertOrUpdateReviewLog(reviewLog: ReviewLogEntity)

    // --- USER PROFILE İŞLEMLERİ ---

    /** Kullanıcı profilini ve seviye verilerini ekler veya günceller (UPSERT). */
    suspend fun upsertProfile(profile: UserProfileEntity)

    /** Sistemdeki aktif (tekil) kullanıcı profilini getirir. */
    suspend fun getUserProfile(): UserProfileEntity?

    /** Kullanıcının belirli bir seviyede (Örn: B1) arşivlediği (öğrendiği) toplam kelime sayısını döndürür. */
    suspend fun getArchivedWordCountByLevel(level: String): Int
}