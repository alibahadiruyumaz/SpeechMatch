package com.example.speechmatch.ui

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

// Bu küçücük etiket (@HiltAndroidApp), uygulamanın tüm yaşam döngüsünü
// Dagger-Hilt'in emrine verir. Tüm Singleton nesneler burada hayat bulur.
@HiltAndroidApp
class SpeechMatchApp : Application()