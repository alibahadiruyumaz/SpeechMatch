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

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideSpeechMatchDatabase(app: Application): SpeechMatchDatabase {
        return Room.databaseBuilder(
            app,
            SpeechMatchDatabase::class.java,
            "speechmatch_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideVocabularyDao(db: SpeechMatchDatabase): VocabularyDao = db.vocabularyDao

    @Provides
    @Singleton
    fun provideReviewLogDao(db: SpeechMatchDatabase): ReviewLogDao = db.reviewLogDao

    @Provides
    @Singleton
    fun provideUserProfileDao(db: SpeechMatchDatabase): UserProfileDao = db.userProfileDao
}