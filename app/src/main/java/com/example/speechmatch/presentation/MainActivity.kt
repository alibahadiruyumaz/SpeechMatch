package com.example.speechmatch.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.speechmatch.presentation.placement.LevelIntroScreen
import com.example.speechmatch.presentation.placement.PlacementScreen
import com.example.speechmatch.presentation.placement.PlacementViewModel
import com.example.speechmatch.presentation.placement.WelcomeScreen
import com.example.speechmatch.presentation.recorder.RecorderScreen
import com.example.speechmatch.presentation.splash.SplashViewModel
import dagger.hilt.android.AndroidEntryPoint

// Hilt'in bu Activity'yi tanıması ve ViewModel'i içeri sızdırabilmesi için KESİNLİKLE GEREKLİ
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Android Studio'nun varsayılan temasını kullanıyoruz
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Eski kör RecorderScreen() çağrısı silindi. Artık yönlendirmeyi Router yapıyor.
                    AppRouter()
                }
            }
        }
    }
}


@Composable
fun AppRouter(
    splashViewModel: SplashViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val startDestination by splashViewModel.startDestination.collectAsState()

    if (startDestination == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        NavHost(
            navController = navController,
            startDestination = startDestination!!
        ) {
            // ROTA: Karşılama
            composable("placement") {
                WelcomeScreen(onStartTest = { navController.navigate("actual_test") })
            }

            // ROTA: Gerçek Sınav
            composable("actual_test") {
                PlacementScreen(
                    // SPRINT 10: Sınavdan dönen 'calculatedLevel' parametresini kurye gibi alıyoruz
                    onTestFinished = { calculatedLevel ->
                        // Rotanın sonuna seviyeyi ekliyoruz (Örn: level_intro/B1)
                        navController.navigate("level_intro/$calculatedLevel") {
                            popUpTo("actual_test") { inclusive = true }
                        }
                    }
                )
            }

            // ROTA: Seviye Tanıtımı (DİKKAT: Artık dışarıdan {level} parametresi bekliyor!)
            composable("level_intro/{level}") { backStackEntry ->
                // Kuryenin getirdiği paketi (B1) açıyoruz, boşsa A1 varsayıyoruz
                val calculatedLevel = backStackEntry.arguments?.getString("level") ?: "A1"

                // Artık burada sıfır bir ViewModel yaratmaya gerek yok, veriyi kuryeden aldık!
                LevelIntroScreen(level = calculatedLevel) {
                    navController.navigate("recorder") {
                        popUpTo("level_intro/{level}") { inclusive = true }
                    }
                }
            }

            // ROTA: Antrenman
            composable("recorder") {
                RecorderScreen()
            }
        }
    }
}