package com.example.speechmatch.presentation.recorder

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Kullanıcının günlük telaffuz antrenmanlarını yaptığı ve performans raporlarını görüntülediği ana çalışma ekranı.
 * * Bu ekran; gerçek zamanlı ses analizi, donanımsal kulaklık takibi, SM-2 algoritma geri bildirimleri
 * ve dairesel başarı grafikleri içeren kapsamlı bir analiz raporu sunar.
 * * @param viewModel Antrenman sürecini, ses motorunu ve SM-2 ilerleme kayıtlarını yöneten [RecorderViewModel].
 */
@Composable
fun RecorderScreen(
    viewModel: RecorderViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // --- Dinamik Tema ve Modern Renk Paleti Yapılandırması ---
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

    /** * Otomatik Değerlendirme Döngüsü:
     * Kullanıcı konuşmayı bitirdiğinde ve metin hazır olduğunda akustik analizi tetikler.
     */
    LaunchedEffect(state.isSpeaking) {
        if (!state.isSpeaking && state.spokenText.isNotBlank() && state.evaluationResult == null) {
            viewModel.evaluateSpeech(state.spokenText)
        }
    }

    /** Mikrofon erişim izni akışını yöneten fırlatıcı. */
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) viewModel.startListening()
        }
    )

    if (state.isSessionFinished) {
        // --- Durum 1: Oturum Sonu Analiz Raporu (Premium Report UI) ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(bgColor)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val totalScore = state.sessionResults.sumOf { it.score }
            val maxPossibleScore = state.sessionResults.size * 5
            val progressPercentage = if (maxPossibleScore > 0) totalScore.toFloat() / maxPossibleScore else 0f
            val displayPercentage = (progressPercentage * 100).toInt()

            Text("🎯 Analiz Raporu", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = primaryTextColor, modifier = Modifier.padding(top = 16.dp))

            /** Başarı Oranı Donut Grafiği: Toplam oturum performansını görselleştirir. */
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(vertical = 24.dp).size(140.dp)
            ) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    color = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0),
                    strokeWidth = 12.dp,
                    strokeCap = StrokeCap.Round
                )
                CircularProgressIndicator(
                    progress = { progressPercentage },
                    modifier = Modifier.fillMaxSize(),
                    color = successColor,
                    strokeWidth = 12.dp,
                    strokeCap = StrokeCap.Round
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("%$displayPercentage", fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = primaryTextColor)
                    Text("Başarı", fontSize = 14.sp, color = secondaryTextColor)
                }
            }

            /** Oturumdaki kelimelerin detaylı hata analizini (Metin işaretleme ile) listeleyen bölüm. */
            LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                items(state.sessionResults.size) { index ->
                    val result = state.sessionResults[index]

                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation),
                        colors = CardDefaults.cardColors(containerColor = cardBgColor)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Hedef: ${result.targetWord}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = primaryTextColor)
                                Text("${result.score} / 5", fontWeight = FontWeight.ExtraBold, color = if(result.score >= 4) successColor else errorColor)
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Söylediğin:", fontSize = 14.sp, color = secondaryTextColor)

                            /** Karşılaştırmalı Metin İşaretleme: Yanlış söylenen karakterleri vurgular. */
                            val annotatedString = buildAnnotatedString {
                                val target = result.targetWord.lowercase()
                                val spoken = result.spokenWord.lowercase()

                                for (i in result.spokenWord.indices) {
                                    val spokenChar = result.spokenWord[i]
                                    if (i >= target.length || spoken[i] != target[i]) {
                                        withStyle(style = SpanStyle(color = errorColor, fontWeight = FontWeight.Bold)) {
                                            append(spokenChar.toString())
                                        }
                                    } else {
                                        withStyle(style = SpanStyle(color = primaryTextColor)) {
                                            append(spokenChar.toString())
                                        }
                                    }
                                }
                            }
                            Text(text = annotatedString, fontSize = 18.sp)
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { viewModel.resumeSession() },
                    modifier = Modifier.weight(1f).padding(end = 8.dp).height(60.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Derse Dön", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { (context as? Activity)?.finish() },
                    modifier = Modifier.weight(1f).padding(start = 8.dp).height(60.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if(isDark) cardBgColor else primaryTextColor)
                ) {
                    Text("Çıkış Yap", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if(isDark) accentColor else Color.White)
                }
            }
        }
    } else {
        // --- Durum 2: Aktif Antrenman Ekranı (Interactive Training UI) ---
        Column(
            modifier = Modifier.fillMaxSize().background(bgColor).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (state.error != null && state.error != "BITTI") {
                Text(text = state.error!!, color = errorColor, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(bottom = 16.dp))
            }

            /** Yankı (Echo) Uyarısı: Kulaklık takılı değilse kullanıcıyı bilgilendirir. */
            val currentWord = state.activeWord
            if (!state.isHeadsetConnected && currentWord != null) {
                Row(
                    modifier = Modifier.padding(bottom = 24.dp).background(cardBgColor.copy(alpha = 0.8f), RoundedCornerShape(12.dp)).padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = "Bilgi", tint = secondaryTextColor, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Yankıyı önlemek için kulaklık takmanız önerilir.", color = secondaryTextColor, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }

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
                        Text(text = "HEDEF KELİME", color = secondaryTextColor, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text(text = currentWord.text, fontSize = 42.sp, fontWeight = FontWeight.ExtraBold, color = accentColor)
                            IconButton(onClick = { viewModel.playActiveWord() }, modifier = Modifier.padding(start = 12.dp)) {
                                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Dinle", tint = accentColor, modifier = Modifier.size(36.dp))
                            }
                        }
                        Text(text = "/ ${currentWord.targetPhoneme} /", color = primaryTextColor, fontSize = 22.sp, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            } else {
                if (state.error == "BITTI") {
                    Text(text = "🎉 Tebrikler!", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = successColor)
                    Text(text = "Bugünlük tüm kelimeleri bitirdiniz.", fontSize = 20.sp, color = secondaryTextColor, modifier = Modifier.padding(top = 8.dp))
                    Button(onClick = { viewModel.finishSession() }, modifier = Modifier.padding(top = 32.dp), colors = ButtonDefaults.buttonColors(containerColor = accentColor)) {
                        Text("Sonuçlarımı Gör", color = accentTextColor, fontWeight = FontWeight.Bold)
                    }
                } else {
                    CircularProgressIndicator(color = accentColor)
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            if (currentWord != null) {
                Text(text = "SENİN SÖYLEDİĞİN:", color = secondaryTextColor, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                Text(
                    text = state.spokenText.ifBlank { "..." },
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (state.spokenText.isNotBlank()) primaryTextColor else secondaryTextColor.copy(alpha = 0.5f),
                    modifier = Modifier.padding(top = 12.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                /** Akustik Değerlendirme Sonucu: SM-2 kalite skorunu kullanıcıya sunar. */
                state.evaluationResult?.let { result ->
                    val resultColor = if (result.isPerfect) successColor else errorColor
                    Text(text = "Akustik Kalite Skoru: ${result.qualityScore} / 5", color = resultColor, fontSize = 22.sp, fontWeight = FontWeight.Bold)

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { viewModel.proceedToNextWord() },
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                            modifier = Modifier.weight(1f).padding(end = 8.dp).height(56.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Sıradaki", color = accentTextColor, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                        }

                        OutlinedButton(
                            onClick = { viewModel.finishSession() },
                            modifier = Modifier.weight(1f).padding(start = 8.dp).height(56.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Dersi Bitir", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            if (currentWord != null) {
                Button(
                    onClick = {
                        if (state.isSpeaking) {
                            viewModel.stopListening()
                        } else {
                            val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                            if (hasPermission) viewModel.startListening() else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(72.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (state.isSpeaking) errorColor else cardBgColor)
                ) {
                    Text(
                        text = if (state.isSpeaking) "Duraklat ve Analiz Et" else "Konuşmaya Başla",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (state.isSpeaking) Color.White else accentColor
                    )
                }
            }
        }
    }
}