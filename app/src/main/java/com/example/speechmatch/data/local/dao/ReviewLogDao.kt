package com.example.speechmatch.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.speechmatch.data.local.entity.ReviewLogEntity

/** SM-2 öğrenme geçmişini kullanıcı bazlı (Multi-Profile) yöneten DAO. */
@Dao
interface ReviewLogDao {

    /** Yeni performans kaydı ekler veya mevcutsa günceller (UPSERT). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLog(log: ReviewLogEntity)

    /** Aralıklı tekrar sistemine göre SADECE O KULLANICININ çalışma vakti gelmiş kayıtlarını getirir. */
    @Query("SELECT * FROM Review_Log WHERE user_id = :userId AND next_review_date <= :currentTime")
    suspend fun getDueLogs(currentTime: Long, userId: Int): List<ReviewLogEntity>

    /** Belirli bir kelimenin, SADECE O KULLANICIYA AİT SM-2 ilerlemesini getirir. */
    @Query("SELECT * FROM Review_Log WHERE vocab_id = :vocabId AND user_id = :userId")
    suspend fun getLogForWord(vocabId: Int, userId: Int): ReviewLogEntity?
}