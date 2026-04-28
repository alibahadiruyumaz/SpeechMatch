package com.example.speechmatch.domain.repository

import com.example.speechmatch.data.local.entity.ReviewLogEntity
import com.example.speechmatch.data.local.entity.UserProfileEntity
import com.example.speechmatch.data.local.entity.VocabularyEntity

// Bu dosya sadece "Ne" yapılacağını söyler, "Nasıl" yapılacağını söylemez.
interface SpeechMatchRepository {
    // Vocabulary İşlemleri
    suspend fun insertWord(word: VocabularyEntity): Long
    suspend fun getAllActiveWords(): List<VocabularyEntity>
    suspend fun getWordWithMinimalPair(targetId: Int): List<VocabularyEntity>
    suspend fun archiveWord(id: Int)

    // SM-2 Log İşlemleri
    suspend fun upsertLog(log: ReviewLogEntity)
    suspend fun getDueLogs(currentTime: Long): List<ReviewLogEntity>
    suspend fun getLogForWord(vocabId: Int): ReviewLogEntity?

    // Kullanıcı Profil İşlemleri
    suspend fun upsertProfile(profile: UserProfileEntity)
    suspend fun getUserProfile(): UserProfileEntity?
}