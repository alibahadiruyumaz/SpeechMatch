package com.example.speechmatch.presentation.placement

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
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
    val state by viewModel.state.collectAsStateWithLifecycle()
    val voiceState by viewModel.voiceState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) viewModel.startListening()
    }

    // Otonom Dinleme Ajanı (Sessizlik algılandığında tetiklenir)
    LaunchedEffect(voiceState.isSpeaking) {
        if (!voiceState.isSpeaking && voiceState.spokenText.isNotBlank()) {
            viewModel.onWordSpoken(voiceState.spokenText)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (state.isLoading) {
            CircularProgressIndicator()
            Text("Sınav Hazırlanıyor...", modifier = Modifier.padding(top = 16.dp))
        } else if (state.isTestFinished) {
            // SINAV BİTİŞ EKRANI
            Text("🎉 Sınav Tamamlandı!", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00C853))
            Text("Teşhis Edilen Seviye:", fontSize = 20.sp, modifier = Modifier.padding(top = 16.dp))
            Text(state.calculatedLevel, fontSize = 48.sp, fontWeight = FontWeight.ExtraBold, color = Color.Blue)

            Button(
                onClick = { onTestFinished(state.calculatedLevel) },
                modifier = Modifier.padding(top = 32.dp).fillMaxWidth().height(56.dp)
            ) {
                Text("Eğitime Başla", fontSize = 18.sp)
            }
        } else {
            // AKTİF SINAV EKRANI
            val currentWord = state.testWords.getOrNull(state.currentWordIndex)

            Text("Seviye Belirleme Sınavı", color = Color.Gray, fontSize = 18.sp)
            Text("Soru ${state.currentWordIndex + 1} / ${state.testWords.size}", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 32.dp))

            if (currentWord != null) {
                Text(currentWord.text, fontSize = 48.sp, fontWeight = FontWeight.ExtraBold)
                Text("/ ${currentWord.targetPhoneme} /", color = Color.Blue, fontSize = 24.sp, modifier = Modifier.padding(top = 8.dp))
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text("Söylediğin:", color = Color.Gray)
            Text(
                text = voiceState.spokenText.ifBlank { "..." },
                fontSize = 28.sp,
                color = if (voiceState.spokenText.isNotBlank()) Color.Black else Color.LightGray
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
                modifier = Modifier.fillMaxWidth().height(60.dp)
            ) {
                Text(if (voiceState.isSpeaking) "Dinleniyor... (Durdur)" else "Konuş", fontSize = 18.sp)
            }
        }
    }
}