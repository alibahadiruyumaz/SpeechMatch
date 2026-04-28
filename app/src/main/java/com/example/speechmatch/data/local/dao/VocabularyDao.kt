package com.example.speechmatch.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.speechmatch.data.local.entity.VocabularyEntity

@Dao
interface VocabularyDao {

    // 1. Yeni Kelime Ekleme (Aynı kelime gelirse üzerine yazar)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: VocabularyEntity): Long

    // 2. Tüm Aktif Kelimeleri Getirme (Arşivlenmiş olanlar hariç)
    @Query("SELECT * FROM Vocabulary_Table WHERE is_archived = 0")
    suspend fun getAllActiveWords(): List<VocabularyEntity>

    // 3. AKUSTİK ÇATIŞMA SORGUSU (Sistemin en önemli fonksiyonu)
    // Kullanıcı bir kelimeyi hatalı okuduğunda, o kelimenin minimal_pair_id'sini (fonetik tuzağını)
    // kullanarak her iki kelimeyi tek bir asenkron hamlede RAM'e çekeriz.
    @Query("SELECT * FROM Vocabulary_Table WHERE id = :targetId OR id = (SELECT minimal_pair_id FROM Vocabulary_Table WHERE id = :targetId)")
    suspend fun getWordWithMinimalPair(targetId: Int): List<VocabularyEntity>

    // 4. ESNEK SİLME (Soft-Delete)
    // Bir kelimeyi fiziksel olarak (DELETE FROM) silmek, o kelimeye bağlı logları ve
    // minimal_pair ilişkilerini çökerteceği için NullPointerException'a sebep olur.
    // Bu yüzden silmek yerine kelimenin "is_archived" bayrağını 1 (true) yaparız.
    @Query("UPDATE Vocabulary_Table SET is_archived = 1 WHERE id = :id")
    suspend fun archiveWord(id: Int)

}