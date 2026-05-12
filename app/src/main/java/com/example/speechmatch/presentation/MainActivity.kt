package com.example.speechmatch.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.speechmatch.presentation.placement.LevelIntroScreen
import com.example.speechmatch.presentation.placement.PlacementScreen
import com.example.speechmatch.presentation.placement.WelcomeScreen
import com.example.speechmatch.presentation.recorder.RecorderScreen
import com.example.speechmatch.presentation.splash.SplashViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Uygulamanın tek aktivite (Single Activity Architecture) giriş noktası.
 * * Android 12+ Splash Screen desteği, Hilt bağımlılık enjeksiyonu ve
 * Jetpack Compose navigasyon yapısını barındırır.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Modern Android Splash Screen API kurulumu
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            // Sistem temasına (Dark/Light) duyarlı küresel arka plan yapılandırması
            val isDark = isSystemInDarkTheme()
            val systemBgColor = if (isDark) Color(0xFF0B1120) else Color(0xFFF1F5F9)

            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = systemBgColor
                ) {
                    AppRouter()
                }
            }
        }
    }
}

/**
 * Uygulamanın tüm navigasyon (rota) yönetimini, ekran geçiş animasyonlarını
 * ve başlangıç hedefi (Start Destination) karar mekanizmasını yöneten ana bileşen.
 * * @param splashViewModel Uygulamanın hangi ekrandan (Sınav veya Antrenman)
 * başlayacağına karar veren [SplashViewModel].
 */
@Composable
fun AppRouter(
    splashViewModel: SplashViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val startDestination by splashViewModel.startDestination.collectAsState()
    val isDark = isSystemInDarkTheme()
    val systemBgColor = if (isDark) Color(0xFF0B1120) else Color(0xFFF1F5F9)
    val accentColor = if (isDark) Color(0xFFD0FF9A) else Color(0xFF2563EB)

    // Navigasyon geçişleri için standart süre (500ms)
    val animationDuration = 500

    if (startDestination == null) {
        // Durum 1: Başlangıç rotası hesaplanırken (Veritabanı kontrolü/Seeding) gösterilen yükleme alanı.
        Box(
            modifier = Modifier.fillMaxSize().background(systemBgColor),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = accentColor)
        }
    } else {
        // Durum 2: Navigasyon Grafiği (NavGraph) Tanımlaması
        NavHost(
            navController = navController,
            startDestination = startDestination!!,
            // Ekranlar arası kayma ve solma (Slide & Fade) animasyonlarının merkezi tanımı
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(animationDuration)
                ) + fadeIn(animationSpec = tween(animationDuration))
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(animationDuration)
                ) + fadeOut(animationSpec = tween(animationDuration))
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(animationDuration)
                ) + fadeIn(animationSpec = tween(animationDuration))
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(animationDuration)
                ) + fadeOut(animationSpec = tween(animationDuration))
            }
        ) {
            // ROTA: Seviye belirleme öncesi karşılama ekranı.
            composable("placement") {
                WelcomeScreen(onStartTest = { navController.navigate("actual_test") })
            }

            // ROTA: Aktif ses analizinin yapıldığı seviye tespit sınavı.
            composable("actual_test") {
                PlacementScreen(
                    onTestFinished = { calculatedLevel ->
                        navController.navigate("level_intro/$calculatedLevel") {
                            // Sınav ekranını geri yığınından temizler (Back-stack management)
                            popUpTo("actual_test") { inclusive = true }
                        }
                    }
                )
            }

            // ROTA: Tespit edilen seviyenin kullanıcıya açıklandığı ara ekran.
            composable("level_intro/{level}") { backStackEntry ->
                val calculatedLevel = backStackEntry.arguments?.getString("level") ?: "A1"

                LevelIntroScreen(level = calculatedLevel) {
                    navController.navigate("recorder") {
                        // Eğitim başladığında tüm sınav sürecini yığından atar.
                        popUpTo("placement") { inclusive = true }
                    }
                }
            }

            // ROTA: SM-2 algoritmasının ve günlük antrenmanların yapıldığı ana ekran.
            composable("recorder") {
                RecorderScreen()
            }
        }
    }
}