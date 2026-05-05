package com.example.speechmatch.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.speechmatch.data.local.entity.VocabularyEntity

@Dao
interface VocabularyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: VocabularyEntity): Long

    @Query("SELECT * FROM Vocabulary_Table WHERE is_archived = 0")
    suspend fun getAllActiveWords(): List<VocabularyEntity>

    @Query("SELECT * FROM Vocabulary_Table WHERE id = :targetId LIMIT 1")
    suspend fun getWordWithMinimalPair(targetId: Int): List<VocabularyEntity>

    @Query("UPDATE Vocabulary_Table SET is_archived = 1 WHERE id = :id")
    suspend fun archiveWord(id: Int)

    // GÜNCELLENEN SQL SORGUSU: LEFT JOIN KULLANILDI
    // Hem daha önce hiç okunmamış (r.next_review_date IS NULL) yeni kelimeleri,
    // Hem de SM-2 algoritmasına göre tekrar vakti gelmiş kelimeleri getirir.
    @Query("""
        SELECT v.* FROM Vocabulary_Table v 
        LEFT JOIN Review_Log r ON v.id = r.vocab_id 
        WHERE (r.next_review_date IS NULL OR r.next_review_date <= :currentTimeMillis) 
        AND v.is_archived = 0
    """)
    suspend fun getWordsToReview(currentTimeMillis: Long): List<VocabularyEntity>
}