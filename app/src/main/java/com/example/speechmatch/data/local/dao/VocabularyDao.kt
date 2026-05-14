package com.example.speechmatch.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.speechmatch.data.local.entity.VocabularyEntity

/** Kelime havuzunu (Vocabulary) ve çalışma listelerini ÇOKLU PROFİL mantığıyla yöneten DAO. */
@Dao
interface VocabularyDao {

    /** Yeni bir kelime ekler veya mevcutsa günceller. (Kelimeler tüm kullanıcılar için ortaktır) */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: VocabularyEntity): Long

    /** Belirtilen kullanıcı için arşivlenmemiş (interval < 180) tüm aktif kelimeleri getirir. */
    @Query("""
        SELECT v.* FROM Vocabulary_Table v 
        LEFT JOIN Review_Log r ON v.id = r.vocab_id AND r.user_id = :userId
        WHERE r.interval_days IS NULL OR r.interval_days < 180
    """)
    suspend fun getAllActiveWords(userId: Int): List<VocabularyEntity>

    /** Belirtilen kimliğe (ID) sahip kelimeyi getirir. */
    @Query("SELECT * FROM Vocabulary_Table WHERE id = :targetId LIMIT 1")
    suspend fun getWordWithMinimalPair(targetId: Int): List<VocabularyEntity>

    /** Öğrenimi başarıyla tamamlanmış kelimeyi ARTIK SADECE O KULLANICI İÇİN arşivler. */
    @Query("UPDATE Review_Log SET interval_days = 180 WHERE vocab_id = :vocabId AND user_id = :userId")
    suspend fun archiveWord(vocabId: Int, userId: Int)

    /** Belirli bir seviyedeki SADECE O KULLANICIYA AİT arşivlenmiş kelime sayısını döndürür. */
    @Query("""
        SELECT COUNT(*) FROM Review_Log r 
        INNER JOIN Vocabulary_Table v ON r.vocab_id = v.id 
        WHERE r.user_id = :userId AND v.cefrLevel = :level AND r.interval_days >= 180
    """)
    suspend fun getArchivedWordCountByLevel(level: String, userId: Int): Int

    /** * KULLANICIYA ÖZEL: Seviyesine uygun, daha önce hiç çalışmadığı veya
     * tekrar vakti gelmiş (ve 180 gün barajını geçmemiş) kelimeleri getirir.
     */
    @Query("""
        SELECT v.* FROM Vocabulary_Table v 
        INNER JOIN User_Profile u ON u.user_id = :userId
        LEFT JOIN Review_Log r ON v.id = r.vocab_id AND r.user_id = :userId
        WHERE v.cefrLevel <= u.currentLevel 
        AND (r.next_review_date IS NULL OR r.next_review_date <= :currentTimeMillis)
        AND (r.interval_days IS NULL OR r.interval_days < 180)
    """)
    suspend fun getWordsToReview(currentTimeMillis: Long, userId: Int): List<VocabularyEntity>
}