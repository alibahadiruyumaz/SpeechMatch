package com.example.speechmatch.di

import com.example.speechmatch.data.repository_impl.SpeechMatchRepositoryImpl
import com.example.speechmatch.domain.repository.SpeechMatchRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSpeechMatchRepository(
        repositoryImpl: SpeechMatchRepositoryImpl
    ): SpeechMatchRepository
}