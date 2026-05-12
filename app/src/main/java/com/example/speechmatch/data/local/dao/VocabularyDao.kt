package com.example.speechmatch.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.speechmatch.data.local.entity.VocabularyEntity

/** Kelime havuzunu (Vocabulary) ve çalışma listelerini yöneten DAO. */
@Dao
interface VocabularyDao {

    /** Yeni bir kelime ekler veya mevcutsa günceller. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: VocabularyEntity): Long

    /** Arşivlenmemiş (aktif) tüm kelimeleri getirir. */
    @Query("SELECT * FROM Vocabulary_Table WHERE is_archived = 0")
    suspend fun getAllActiveWords(): List<VocabularyEntity>

    /** Belirtilen kimliğe (ID) sahip kelimeyi getirir. */
    @Query("SELECT * FROM Vocabulary_Table WHERE id = :targetId LIMIT 1")
    suspend fun getWordWithMinimalPair(targetId: Int): List<VocabularyEntity>

    /** Öğrenimi başarıyla tamamlanmış (SM-2 barajını geçen) kelimeyi arşivler. */
    @Query("UPDATE Vocabulary_Table SET is_archived = 1 WHERE id = :id")
    suspend fun archiveWord(id: Int)

    /** Belirli bir seviyedeki (Örn: B1) arşivlenmiş kelime sayısını döndürür. */
    @Query("SELECT COUNT(*) FROM Vocabulary_Table WHERE cefrLevel = :level AND is_archived = 1")
    suspend fun getArchivedWordCountByLevel(level: String): Int

    /** * Kullanıcının CEFR seviyesine uygun; daha önce hiç çalışılmamış (yeni) veya
     * SM-2 algoritmasına göre tekrar vakti gelmiş aktif kelimeleri getirir.
     */
    @Query("""
        SELECT v.* FROM Vocabulary_Table v 
        INNER JOIN User_Profile u ON 1=1
        LEFT JOIN Review_Log r ON v.id = r.vocab_id 
        WHERE v.cefrLevel <= u.currentLevel 
        AND (r.next_review_date IS NULL OR r.next_review_date <= :currentTimeMillis)
        AND v.is_archived = 0
    """)
    suspend fun getWordsToReview(currentTimeMillis: Long): List<VocabularyEntity>
}