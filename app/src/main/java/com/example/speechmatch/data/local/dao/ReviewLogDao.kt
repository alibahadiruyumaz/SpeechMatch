package com.example.speechmatch.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.speechmatch.data.local.entity.ReviewLogEntity

@Dao
interface ReviewLogDao {

    // 1. Yeni Log Ekleme veya Güncelleme (UPSERT)
    // Eğer o kelime için daha önce bir log varsa (vocab_id aynıysa), üzerine yazar (REPLACE).
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLog(log: ReviewLogEntity)

    // 2. Vakti Gelmiş (Due) Kelimeleri Getirme
    // Zamanı şu anki zamandan (currentTime) küçük veya eşit olan tüm logları çeker.
    @Query("SELECT * FROM Review_Log WHERE next_review_date <= :currentTime")
    suspend fun getDueLogs(currentTime: Long): List<ReviewLogEntity>

    // 3. Spesifik Bir Kelimenin Logunu Çekme
    // Kullanıcı bir kelimeyi okuduğunda, o kelimenin geçmiş skorunu (q) getirmek için kullanılır.
    @Query("SELECT * FROM Review_Log WHERE vocab_id = :vocabId")
    suspend fun getLogForWord(vocabId: Int): ReviewLogEntity?
}