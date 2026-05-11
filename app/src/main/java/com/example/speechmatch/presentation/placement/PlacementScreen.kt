package com.example.speechmatch.presentation.placement

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun PlacementScreen(
    viewModel: PlacementViewModel = hiltViewModel(),
    onTestFinished: (String) -> Unit // Sınav bitince ana ekrana yönlendirecek tetikleyici
) {
    // SENİN ORİJİNAL STATE YAPIN
    val state by viewModel.state.collectAsStateWithLifecycle()
    val voiceState by viewModel.voiceState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // --- SPRINT 14: DİNAMİK SİSTEM TEMASI ---
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) Color(0xFF0B1120) else Color(0xFFF1F5F9)
    val cardBgColor = if (isDark) Color(0xFF1E293B) else Color(0xFFFFFFFF)
    val primaryTextColor = if (isDark) Color.White else Color(0xFF0F172A)
    val secondaryTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val accentColor = if (isDark) Color(0xFFD0FF9A) else Color(0xFF2563EB)
    val accentTextColor = if (isDark) Color(0xFF0F172A) else Color.White
    val successColor = Color(0xFF10B981)
    val errorColor = Color(0xFFEF4444)
    val cardElevation = if (isDark) 8.dp else 2.dp

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) viewModel.startListening()
    }

    // Otonom Dinleme Ajanı (Senin orijinal mantığın: Sessizlik algılandığında tetiklenir)
    LaunchedEffect(voiceState.isSpeaking) {
        if (!voiceState.isSpeaking && voiceState.spokenText.isNotBlank()) {
            viewModel.onWordSpoken(voiceState.spokenText)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(bgColor).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (state.isLoading) {
            // YÜKLENİYOR EKRANI
            CircularProgressIndicator(color = accentColor)
            Text("Sınav Hazırlanıyor...", color = secondaryTextColor, modifier = Modifier.padding(top = 16.dp))

        } else if (state.isTestFinished) {
            // SINAV BİTİŞ EKRANI
            Text("🎉 Sınav Tamamlandı!", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = successColor)
            Text("Teşhis Edilen Seviye:", fontSize = 20.sp, color = secondaryTextColor, modifier = Modifier.padding(top = 16.dp))

            Text(
                text = state.calculatedLevel,
                fontSize = 64.sp,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            Button(
                onClick = { onTestFinished(state.calculatedLevel) },
                modifier = Modifier.padding(top = 32.dp).fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text("Eğitime Başla", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = accentTextColor)
            }

        } else {
            // AKTİF SINAV EKRANI
            val currentWord = state.testWords.getOrNull(state.currentWordIndex)

            Text("Seviye Belirleme Sınavı", color = secondaryTextColor, fontSize = 18.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Text(
                text = "Soru ${state.currentWordIndex + 1} / ${state.testWords.size}",
                color = primaryTextColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 32.dp, top = 8.dp)
            )

            if (currentWord != null) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = cardElevation),
                    colors = CardDefaults.cardColors(containerColor = cardBgColor)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "OKUNACAK KELİME", color = secondaryTextColor, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        Text(text = currentWord.text, fontSize = 42.sp, fontWeight = FontWeight.ExtraBold, color = accentColor, modifier = Modifier.padding(top = 16.dp))
                        Text(text = "/ ${currentWord.targetPhoneme} /", color = primaryTextColor, fontSize = 22.sp, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text("SENİN SÖYLEDİĞİN:", color = secondaryTextColor, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
            Text(
                text = voiceState.spokenText.ifBlank { "..." },
                fontSize = 32.sp,
                fontWeight = FontWeight.Medium,
                color = if (voiceState.spokenText.isNotBlank()) primaryTextColor else secondaryTextColor.copy(alpha = 0.5f),
                modifier = Modifier.padding(top = 12.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = {
                    if (voiceState.isSpeaking) {
                        viewModel.stopListening()
                    } else {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                            viewModel.startListening()
                        } else {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(72.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (voiceState.isSpeaking) errorColor else cardBgColor)
            ) {
                Text(
                    text = if (voiceState.isSpeaking) "Dinleniyor... (Durdur)" else "Konuşmaya Başla",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (voiceState.isSpeaking) Color.White else accentColor
                )
            }
        }
    }
}