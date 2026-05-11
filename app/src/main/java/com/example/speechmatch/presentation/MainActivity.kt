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

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            // SPRINT 14: SİSTEM TEMASI ENTEGRASYONU (Açık/Koyu uyumu)
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

@Composable
fun AppRouter(
    splashViewModel: SplashViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val startDestination by splashViewModel.startDestination.collectAsState()
    val isDark = isSystemInDarkTheme()
    val systemBgColor = if (isDark) Color(0xFF0B1120) else Color(0xFFF1F5F9)
    val accentColor = if (isDark) Color(0xFFD0FF9A) else Color(0xFF2563EB)

    // Animasyon Süresi (Milisaniye)
    val animationDuration = 500

    if (startDestination == null) {
        // YÜKLENİYOR (SPLASH) EKRANI
        Box(
            modifier = Modifier.fillMaxSize().background(systemBgColor),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = accentColor)
        }
    } else {
        NavHost(
            navController = navController,
            startDestination = startDestination!!,
            // TÜM EKRANLAR İÇİN VARSAYILAN ÇIKIŞ VE GİRİŞ ANİMASYONLARI
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
            // ROTA: Karşılama Ekranı
            composable("placement") {
                WelcomeScreen(onStartTest = { navController.navigate("actual_test") })
            }

            // ROTA: Gerçek Sınav Ekranı
            composable("actual_test") {
                PlacementScreen(
                    onTestFinished = { calculatedLevel ->
                        navController.navigate("level_intro/$calculatedLevel") {
                            popUpTo("actual_test") { inclusive = true }
                        }
                    }
                )
            }

            // ROTA: Seviye Tanıtım Ekranı (B1 vs.)
            composable("level_intro/{level}") { backStackEntry ->
                val calculatedLevel = backStackEntry.arguments?.getString("level") ?: "A1"

                LevelIntroScreen(level = calculatedLevel) {
                    navController.navigate("recorder") {
                        // Eğitime başlandığında tüm sınav geçmişini sırtından at (Geri tuşu ile dönülmesin)
                        popUpTo("placement") { inclusive = true }
                    }
                }
            }

            // ROTA: Ana Antrenman Ekranı
            composable("recorder") {
                RecorderScreen()
            }
        }
    }
}