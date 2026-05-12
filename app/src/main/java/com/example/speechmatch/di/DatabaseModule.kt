package com.example.speechmatch.di

import android.app.Application
import androidx.room.Room
import com.example.speechmatch.data.local.SpeechMatchDatabase
import com.example.speechmatch.data.local.dao.ReviewLogDao
import com.example.speechmatch.data.local.dao.UserProfileDao
import com.example.speechmatch.data.local.dao.VocabularyDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Room veritabanı ve DAO nesnelerinin bağımlılık enjeksiyonunu (DI) sağlayan Hilt modülü. */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /** Uygulama genelinde kullanılacak tekil (Singleton) Room veritabanı örneğini oluşturur. */
    @Provides
    @Singleton
    fun provideSpeechMatchDatabase(app: Application): SpeechMatchDatabase {
        return Room.databaseBuilder(
            app,
            SpeechMatchDatabase::class.java,
            "speechmatch_db"
        ).build()
    }

    /** Kelime havuzu işlemlerinden sorumlu DAO nesnesini sağlar. */
    @Provides
    @Singleton
    fun provideVocabularyDao(db: SpeechMatchDatabase): VocabularyDao = db.vocabularyDao

    /** SM-2 algoritması performans kayıtlarından sorumlu DAO nesnesini sağlar. */
    @Provides
    @Singleton
    fun provideReviewLogDao(db: SpeechMatchDatabase): ReviewLogDao = db.reviewLogDao

    /** Kullanıcı profili ve teşhis verilerinden sorumlu DAO nesnesini sağlar. */
    @Provides
    @Singleton
    fun provideUserProfileDao(db: SpeechMatchDatabase): UserProfileDao = db.userProfileDao
}