package com.example.speechmatch.presentation.placement

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WelcomeScreen(onStartTest: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🎙️ SpeechMatch'e Hoş Geldin!", fontSize = 28.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Yapay zeka destekli telaffuz eğitimine başlamadan önce, senin için en doğru kelimeleri seçmemiz gerekiyor.",
            textAlign = TextAlign.Center, color = Color.Gray, fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onStartTest, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text("Seviye Belirleme Sınavına Gir")
        }
    }
}

@Composable
fun LevelIntroScreen(level: String, onStartTraining: () -> Unit) {
    val description = when(level) {
        "A1" -> "Harika! İngilizce yolculuğuna temelden başlıyoruz. A1 seviyesinde en temel sesleri ve günlük kelimeleri öğreneceksin."
        "B1" -> "Güzel bir temeliniz var! B1 seviyesinde daha karmaşık kelime yapıları ve akıcı konuşma üzerine çalışacağız."
        "C1" -> "Mükemmel! C1 seviyesinde ileri düzey telaffuz hatalarını ve zorlayıcı akustik yapıları düzelteceğiz."
        else -> "Seviyen belirlendi! Senin için hazırladığımız özel müfredat ile eğitime başlayabilirsin."
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Seviyen: $level", fontSize = 40.sp, fontWeight = FontWeight.ExtraBold, color = Color.Blue)
        Spacer(modifier = Modifier.height(24.dp))
        Text(description, textAlign = TextAlign.Center, fontSize = 20.sp, lineHeight = 28.sp)
        Spacer(modifier = Modifier.height(40.dp))
        Button(onClick = onStartTraining, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text("Eğitimi Başlat")
        }
    }
}