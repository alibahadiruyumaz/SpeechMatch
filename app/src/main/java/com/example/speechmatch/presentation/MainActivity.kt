package com.example.speechmatch.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.speechmatch.presentation.recorder.RecorderScreen
import dagger.hilt.android.AndroidEntryPoint

// Hilt'in bu Activity'yi tanıması ve ViewModel'i içeri sızdırabilmesi için KESİNLİKLE GEREKLİ
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Android Studio'nun varsayılan temasını kullanıyoruz (Adı SpeechMatchTheme de olabilir)
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // İşte bizim asıl zeki arayüzümüz burada çağrılıyor!
                    RecorderScreen()
                }
            }
        }
    }
}