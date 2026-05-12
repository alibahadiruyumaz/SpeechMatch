package com.example.speechmatch.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.speechmatch.data.local.entity.ReviewLogEntity

/** SM-2 öğrenme geçmişini ve tekrar zamanlamalarını yöneten DAO. */
@Dao
interface ReviewLogDao {

    /** Yeni performans kaydı ekler veya mevcutsa günceller (UPSERT). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLog(log: ReviewLogEntity)

    /** Aralıklı tekrar (SM-2) sistemine göre çalışma vakti gelmiş kayıtları getirir. */
    @Query("SELECT * FROM Review_Log WHERE next_review_date <= :currentTime")
    suspend fun getDueLogs(currentTime: Long): List<ReviewLogEntity>

    /** Belirli bir kelimenin mevcut SM-2 ilerlemesini ve geçmiş skorunu getirir. */
    @Query("SELECT * FROM Review_Log WHERE vocab_id = :vocabId")
    suspend fun getLogForWord(vocabId: Int): ReviewLogEntity?
}