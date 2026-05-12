package com.example.speechmatch.ui

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Uygulamanın ana Application sınıfı ve Dagger-Hilt bağımlılık enjeksiyonu (DI) için başlangıç noktası.
 * * @HiltAndroidApp notasyonu, Hilt'in kod oluşturma sürecini tetikleyerek uygulamanın
 * tüm yaşam döngüsü boyunca kullanılacak bağımlılık konteynerini (SingletonComponent) başlatır.
 * Tüm 'Singleton' kapsamlı nesneler bu sınıfın yaşam döngüsüyle paralel olarak yönetilir.
 */
@HiltAndroidApp
class SpeechMatchApp : Application()