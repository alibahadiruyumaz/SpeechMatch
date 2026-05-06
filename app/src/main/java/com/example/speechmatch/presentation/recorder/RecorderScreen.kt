package com.example.speechmatch.presentation.recorder

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle


@Composable
fun RecorderScreen(
    viewModel: RecorderViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(state.isSpeaking) {
        if (!state.isSpeaking && state.spokenText.isNotBlank() && state.evaluationResult == null) {
            viewModel.evaluateSpeech(state.spokenText)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) viewModel.startListening()
        }
    )

    // SPRINT 11: DETAYLI KARNE VE KIRMIZI VURGU
    if (state.isSessionFinished) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val totalScore = state.sessionResults.sumOf { it.score }
            val avgScore = if (state.sessionResults.isNotEmpty()) totalScore.toDouble() / state.sessionResults.size else 0.0

            Text("🎯 Antrenman Tamamlandı!", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6200EE), modifier = Modifier.padding(top = 16.dp))
            Text("Ortalama Başarı: %${(avgScore * 20).toInt()}", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF00C853), modifier = Modifier.padding(top = 8.dp, bottom = 24.dp))

            // Detaylı Kelime Listesi (Aşağı Kaydırılabilir)
            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) {
                items(state.sessionResults.size) { index ->
                    val result = state.sessionResults[index]

                    androidx.compose.material3.Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Hedef: ${result.targetWord}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text("${result.score} / 5", fontWeight = FontWeight.ExtraBold, color = if(result.score >= 4) Color(0xFF00C853) else Color.Red)
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Söylediğin:", fontSize = 14.sp, color = Color.Gray)

                            // HARF HARF FARK BULUCU (DIFFING) VE RENKLENDİRME
                            val annotatedString = androidx.compose.ui.text.buildAnnotatedString {
                                val target = result.targetWord.lowercase()
                                val spoken = result.spokenWord.lowercase()

                                for (i in result.spokenWord.indices) {
                                    val spokenChar = result.spokenWord[i]
                                    // Eğer harf hedef kelimenin aynı sırasında yoksa veya yanlışsa KIRMIZI boya
                                    if (i >= target.length || spoken[i] != target[i]) {
                                        withStyle(style = androidx.compose.ui.text.SpanStyle(color = Color.Red, fontWeight = FontWeight.Bold)) {
                                            append(spokenChar.toString())
                                        }
                                    } else {
                                        withStyle(style = androidx.compose.ui.text.SpanStyle(color = Color.DarkGray)) {
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

            Button(
                onClick = { (context as? Activity)?.finish() },
                modifier = Modifier.fillMaxWidth().height(56.dp).padding(top = 16.dp, bottom = 16.dp)
            ) {
                Text("Çıkış Yap", fontSize = 18.sp)
            }
        }
    } else {
        // --- NORMAL ANTRENMAN EKRANI ---
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 1. HATA YÖNETİMİ
            if (state.error != null && state.error != "BITTI") {
                Text(
                    text = state.error!!,
                    color = Color.Red,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // 2. HEDEF KELİME VEYA BİTİŞ
            val currentWord = state.activeWord
            if (currentWord != null) {
                Text(text = "Hedef Kelime", color = Color.Gray, fontSize = 16.sp)
                Text(text = currentWord.text, fontSize = 40.sp, fontWeight = FontWeight.ExtraBold)
                Text(text = "/ ${currentWord.targetPhoneme} /", color = Color.Blue, fontSize = 24.sp, modifier = Modifier.padding(top = 8.dp))
            } else {
                if (state.error == "BITTI") {
                    Text(text = "🎉 Tebrikler!", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00C853))
                    Text(text = "Bugünlük tüm kelimeleri bitirdiniz.", fontSize = 20.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray, modifier = Modifier.padding(top = 8.dp))

                    // Eğitimi erken bitirmese bile kelimeler tamamen bittiğinde karneye geçmesi için:
                    Button(
                        onClick = { viewModel.finishSession() },
                        modifier = Modifier.padding(top = 32.dp)
                    ) {
                        Text("Sonuçlarımı Gör")
                    }
                } else {
                    CircularProgressIndicator(color = Color.Blue)
                    Text(text = "Kelime yükleniyor...", color = Color.Gray, modifier = Modifier.padding(top = 16.dp))
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // 3. KULLANICI METNİ
            if (currentWord != null) {
                Text(text = "Senin Söylediğin:", color = Color.Gray, fontSize = 16.sp)
                Text(
                    text = state.spokenText.ifBlank { "..." },
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (state.spokenText.isNotBlank()) Color.Black else Color.LightGray,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 4. LEVENSHTEIN SONUCU VE BUTONLAR
                state.evaluationResult?.let { result ->
                    val resultColor = if (result.isPerfect) Color(0xFF00C853) else Color.Red
                    Text(text = "Akustik Kalite Skoru: ${result.qualityScore} / 5", color = resultColor, fontSize = 22.sp, fontWeight = FontWeight.Bold)

                    // SPRINT 10: SIRADAKİ VE DERSİ BİTİR BUTONLARI YAN YANA
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { viewModel.proceedToNextWord() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                            modifier = Modifier.weight(1f).padding(end = 8.dp).height(50.dp)
                        ) {
                            Text("Sıradaki", color = Color.White)
                        }

                        OutlinedButton(
                            onClick = { viewModel.finishSession() },
                            modifier = Modifier.weight(1f).padding(start = 8.dp).height(50.dp)
                        ) {
                            Text("Dersi Bitir", color = Color.Red, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // 5. MİKROFON BUTONU
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
                    modifier = Modifier.fillMaxWidth().height(60.dp)
                ) {
                    Text(text = if (state.isSpeaking) "Dinlemeyi Durdur ve Analiz Et" else "Konuşmaya Başla", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}