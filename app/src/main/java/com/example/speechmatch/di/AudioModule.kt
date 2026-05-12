package com.example.speechmatch.di

import com.example.speechmatch.data.recognizer.HeadsetStateObserverImpl
import com.example.speechmatch.data.recognizer.VoiceToTextParserImpl
import com.example.speechmatch.domain.repository.HeadsetStateObserver
import com.example.speechmatch.domain.repository.VoiceToTextParser
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Ses tanıma ve donanım izleme arayüzlerinin bağımlılık enjeksiyonunu (DI) sağlayan Hilt modülü. */
@Module
@InstallIn(SingletonComponent::class)
abstract class AudioModule {

    /** Çevrimdışı ses tanıma motorunun (SpeechRecognizer) uygulama genelinde tekil (Singleton) örneğini bağlar. */
    @Binds
    @Singleton
    abstract fun bindVoiceToTextParser(
        parser: VoiceToTextParserImpl
    ): VoiceToTextParser

    /** Donanımsal kulaklık durumunu takip eden gözlemcinin uygulama genelinde tekil (Singleton) örneğini bağlar. */
    @Binds
    @Singleton
    abstract fun bindHeadsetStateObserver(
        observer: HeadsetStateObserverImpl
    ): HeadsetStateObserver
}