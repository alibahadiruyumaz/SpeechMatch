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

@Module
@InstallIn(SingletonComponent::class)
abstract class AudioModule {

    // Mikrofon Motoru Sözleşme Köprüsü
    @Binds
    @Singleton
    abstract fun bindVoiceToTextParser(
        parser: VoiceToTextParserImpl
    ): VoiceToTextParser

    // Yankı Engelleyici Ajan Sözleşme Köprüsü
    @Binds
    @Singleton
    abstract fun bindHeadsetStateObserver(
        observer: HeadsetStateObserverImpl
    ): HeadsetStateObserver
}