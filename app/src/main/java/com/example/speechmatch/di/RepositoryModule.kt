package com.example.speechmatch.di

import com.example.speechmatch.data.repository_impl.SpeechMatchRepositoryImpl
import com.example.speechmatch.domain.repository.SpeechMatchRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Domain katmanı ile Data katmanı arasındaki Repository (Veri Deposu) bağımlılıklarını bağlayan Hilt modülü. */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /** * SpeechMatchRepository soyut arayüzünü, somut implementasyonu (Impl) ile eşleştirerek
     * uygulama genelinde kullanılmak üzere tekil (Singleton) olarak sağlar.
     */
    @Binds
    @Singleton
    abstract fun bindSpeechMatchRepository(
        repositoryImpl: SpeechMatchRepositoryImpl
    ): SpeechMatchRepository
}