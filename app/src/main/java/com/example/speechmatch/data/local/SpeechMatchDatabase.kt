package com.example.speechmatch.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.speechmatch.data.local.dao.ReviewLogDao
import com.example.speechmatch.data.local.dao.UserProfileDao
import com.example.speechmatch.data.local.dao.VocabularyDao
import com.example.speechmatch.data.local.entity.ReviewLogEntity
import com.example.speechmatch.data.local.entity.UserProfileEntity
import com.example.speechmatch.data.local.entity.VocabularyEntity

// Sistemdeki tüm entity'leri (tabloları) burada deklare ediyoruz
@Database(
    entities = [VocabularyEntity::class, ReviewLogEntity::class, UserProfileEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SpeechMatchDatabase : RoomDatabase() {

    // İşçi sınıfların (DAO'ların) veritabanı ile konuşabilmesi için açılan kapılar
    abstract val vocabularyDao: VocabularyDao
    abstract val reviewLogDao: ReviewLogDao
    abstract val userProfileDao: UserProfileDao
}