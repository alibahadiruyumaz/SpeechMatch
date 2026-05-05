package com.example.speechmatch.domain.repository

import com.example.speechmatch.data.local.entity.ReviewLogEntity
import com.example.speechmatch.data.local.entity.UserProfileEntity
import com.example.speechmatch.data.local.entity.VocabularyEntity

/**
 * Sistemin Anayasası (Clean Architecture - Domain Layer).
 * Domain katmanının (Use Cases) veritabanı ile konuşabilmesi için gereken katı sözleşmedir.
 * Burada "nasıl" yapılacağı değil, sadece "ne" yapılacağı tanımlanır.
 */
interface SpeechMatchRepository {

    // --- VOCABULARY (Kelime Havuzu) İŞLEMLERİ ---
    suspend fun insertWord(word: VocabularyEntity): Long
    suspend fun getAllActiveWords(): List<VocabularyEntity>
    suspend fun getWordWithMinimalPair(targetId: Int): List<VocabularyEntity>
    suspend fun archiveWord(id: Int)

    // Veritabanından SRS (Aralıklı Tekrar) algoritmasına göre bugün tekrar edilmesi gereken kelimeleri getirir.
    suspend fun getWordsToReview(currentTimeMillis: Long): List<VocabularyEntity>

    // --- SM-2 REVIEW LOG (Hafıza Eğrisi) İŞLEMLERİ ---
    suspend fun getReviewLogForWord(vocabId: Int): ReviewLogEntity?
    suspend fun insertOrUpdateReviewLog(reviewLog: ReviewLogEntity)

    // --- USER PROFILE (Kullanıcı Teşhis) İŞLEMLERİ ---
    suspend fun upsertProfile(profile: UserProfileEntity)
    suspend fun getUserProfile(): UserProfileEntity?
}