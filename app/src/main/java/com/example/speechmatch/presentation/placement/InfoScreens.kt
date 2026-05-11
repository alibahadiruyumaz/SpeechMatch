package com.example.speechmatch.presentation.placement

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
    // SİSTEM TEMASI (Açık / Koyu)
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) Color(0xFF0B1120) else Color(0xFFF1F5F9)
    val primaryTextColor = if (isDark) Color.White else Color(0xFF0F172A)
    val secondaryTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val accentColor = if (isDark) Color(0xFFD0FF9A) else Color(0xFF2563EB)
    val accentTextColor = if (isDark) Color(0xFF0F172A) else Color.White

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🎙️",
            fontSize = 64.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "SpeechMatch'e\nHoş Geldin!",
            fontSize = 36.sp,
            fontWeight = FontWeight.ExtraBold,
            color = primaryTextColor,
            textAlign = TextAlign.Center,
            lineHeight = 42.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Yapay zeka destekli telaffuz eğitimine başlamadan önce, senin için en doğru kelimeleri seçmemiz gerekiyor.",
            fontSize = 18.sp,
            color = secondaryTextColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onStartTest,
            modifier = Modifier.fillMaxWidth().height(64.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
        ) {
            Text(
                text = "Seviye Belirleme Sınavına Gir",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = accentTextColor
            )
        }
    }
}

@Composable
fun LevelIntroScreen(level: String, onStartTraining: () -> Unit) {
    // SİSTEM TEMASI (Açık / Koyu)
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) Color(0xFF0B1120) else Color(0xFFF1F5F9)
    val primaryTextColor = if (isDark) Color.White else Color(0xFF0F172A)
    val secondaryTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val accentColor = if (isDark) Color(0xFFD0FF9A) else Color(0xFF2563EB)
    val accentTextColor = if (isDark) Color(0xFF0F172A) else Color.White

    // SENİN HARİKA DİNAMİK AÇIKLAMA MANTIĞIN KORUNDU
    val description = when(level) {
        "A1" -> "Harika! İngilizce yolculuğuna temelden başlıyoruz. A1 seviyesinde en temel sesleri ve günlük kelimeleri öğreneceksin."
        "B1" -> "Güzel bir temeliniz var! B1 seviyesinde daha karmaşık kelime yapıları ve akıcı konuşma üzerine çalışacağız."
        "C1" -> "Mükemmel! C1 seviyesinde ileri düzey telaffuz hatalarını ve zorlayıcı akustik yapıları düzelteceğiz."
        else -> "Seviyen belirlendi! Senin için hazırladığımız özel müfredat ile eğitime başlayabilirsin."
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Teşhis Edilen Seviyen:",
            fontSize = 20.sp,
            color = secondaryTextColor,
            fontWeight = FontWeight.Medium
        )

        Text(
            text = level,
            fontSize = 96.sp,
            fontWeight = FontWeight.ExtraBold,
            color = accentColor,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = description,
            textAlign = TextAlign.Center,
            fontSize = 20.sp,
            color = primaryTextColor,
            lineHeight = 28.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(56.dp))

        Button(
            onClick = onStartTraining,
            modifier = Modifier.fillMaxWidth().height(64.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
        ) {
            Text(
                text = "Eğitimi Başlat",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = accentTextColor
            )
        }
    }
}