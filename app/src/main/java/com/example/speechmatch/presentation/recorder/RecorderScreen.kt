package com.example.speechmatch.presentation.recorder

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
fun RecorderScreen(
    viewModel: RecorderViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) viewModel.startListening()
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 1. HATA VEYA BİLGİLENDİRME YÖNETİMİ
        state.error?.let {
            Text(
                text = it,
                color = if (it.contains("bulunamadı")) Color(0xFFE65100) else Color.Red, // Kelime bittiyse turuncu, sistem hatasıysa kırmızı
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // 2. HEDEF KELİME VEYA YÜKLEME DURUMU
        val currentWord = state.activeWord
        if (currentWord != null) {
            Text(text = "Hedef Kelime", color = Color.Gray, fontSize = 16.sp)
            Text(text = currentWord.text, fontSize = 40.sp, fontWeight = FontWeight.ExtraBold)
            Text(text = "/ ${currentWord.targetPhoneme} /", color = Color.Blue, fontSize = 24.sp, modifier = Modifier.padding(top = 8.dp))
        } else {
            // SADECE HATA YOKSA YÜKLEME ÇEMBERİNİ GÖSTER (Sonsuz döngüyü çözen mantık)
            if (state.error == null) {
                CircularProgressIndicator(color = Color.Blue)
                Text(text = "Kelime yükleniyor...", color = Color.Gray, modifier = Modifier.padding(top = 16.dp))
            } else if (state.error!!.contains("bulunamadı")) {
                // Bugünlük görevler bittiğinde gösterilecek ekran
                Text(text = "🎉 Bugünlük hedeflerini tamamladın!", fontSize = 20.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray)
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // 3. KULLANICI METNİ
        Text(text = "Senin Söylediğin:", color = Color.Gray, fontSize = 16.sp)
        Text(
            text = state.spokenText.ifBlank { "..." },
            fontSize = 28.sp,
            fontWeight = FontWeight.Medium,
            color = if (state.spokenText.isNotBlank()) Color.Black else Color.LightGray,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 4. LEVENSHTEIN SONUCU
        state.evaluationResult?.let { result ->
            val resultColor = if (result.isPerfect) Color(0xFF00C853) else Color.Red
            Text(text = "Akustik Kalite Skoru: ${result.qualityScore} / 5", color = resultColor, fontSize = 22.sp, fontWeight = FontWeight.Bold)

            Button(
                onClick = { viewModel.proceedToNextWord() },
                modifier = Modifier.padding(top = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
            ) {
                Text("Sıradaki Kelimeye Geç", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // 5. MİKROFON BUTONU
        Button(
            onClick = {
                if (state.isSpeaking) {
                    viewModel.stopListening()
                    if (state.spokenText.isNotBlank()) {
                        viewModel.evaluateSpeech(state.spokenText)
                    }
                } else {
                    val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                    if (hasPermission) viewModel.startListening() else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            },
            modifier = Modifier.fillMaxWidth().height(60.dp),
            enabled = currentWord != null
        ) {
            Text(text = if (state.isSpeaking) "Dinlemeyi Durdur ve Analiz Et" else "Konuşmaya Başla", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}