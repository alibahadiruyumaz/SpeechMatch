package com.example.speechmatch.domain.repository

import com.example.speechmatch.data.local.entity.ReviewLogEntity
import com.example.speechmatch.data.local.entity.UserProfileEntity
import com.example.speechmatch.data.local.entity.VocabularyEntity

/** * Domain katmanı (Use Cases) ile veri kaynakları arasındaki iletişimi tanımlayan soyut veri deposu sözleşmesi.
 * Netflix tarzı "Çoklu Profil" (Multi-Profile) mimarisine uygun olarak güncellenmiştir.
 */
interface SpeechMatchRepository {

    // --- VOCABULARY İŞLEMLERİ ---

    /** Yeni bir kelimeyi ortak havuza ekler (Kelimeler tüm kullanıcılar için aynıdır). */
    suspend fun insertWord(word: VocabularyEntity): Long

    /** Belirtilen kullanıcı için arşivlenmemiş (aktif) tüm kelimeleri getirir. */
    suspend fun getAllActiveWords(userId: Int): List<VocabularyEntity>

    /** Belirtilen kimliğe (ID) sahip kelimeyi getirir. */
    suspend fun getWordWithMinimalPair(targetId: Int): List<VocabularyEntity>

    /** 180 gün barajını geçen kelimeyi SADECE O KULLANICI İÇİN arşivler. */
    suspend fun archiveWord(vocabId: Int, userId: Int)

    /** Aralıklı tekrar (SM-2) algoritmasına göre bugün çalışılması gereken kelimeleri KULLANICIYA ÖZEL getirir. */
    suspend fun getWordsToReview(currentTimeMillis: Long, userId: Int): List<VocabularyEntity>

    /** Kullanıcının belirli bir seviyede (Örn: B1) arşivlediği toplam kelime sayısını döndürür. */
    suspend fun getArchivedWordCountByLevel(level: String, userId: Int): Int


    // --- SM-2 REVIEW LOG İŞLEMLERİ ---

    /** Belirli bir kelimenin, belirli bir kullanıcıya ait mevcut SM-2 ilerleme durumunu getirir. */
    suspend fun getReviewLogForWord(vocabId: Int, userId: Int): ReviewLogEntity?

    /** Kelimenin kullanıcıya özel SM-2 performans günlüğünü ekler veya günceller (UPSERT). */
    suspend fun insertOrUpdateReviewLog(reviewLog: ReviewLogEntity)


    // --- USER PROFILE İŞLEMLERİ ---

    /** Yeni bir profil oluşturur veya mevcut profili günceller (UPSERT). */
    suspend fun upsertProfile(profile: UserProfileEntity)

    /** Sistemdeki belirli bir profile ait detayları (Seviye vb.) getirir. */
    suspend fun getUserProfile(userId: Int): UserProfileEntity?

    /** Netflix ekranı ("Kim İzliyor?") için sistemdeki tüm profilleri liste halinde getirir. */
    suspend fun getAllProfiles(): List<UserProfileEntity>

    suspend fun deleteProfile(profile: UserProfileEntity)
}