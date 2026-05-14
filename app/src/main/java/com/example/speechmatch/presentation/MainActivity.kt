package com.example.speechmatch.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.speechmatch.presentation.placement.LevelIntroScreen
import com.example.speechmatch.presentation.placement.PlacementScreen
import com.example.speechmatch.presentation.placement.WelcomeScreen
import com.example.speechmatch.presentation.profile.ProfileSelectionScreen
import com.example.speechmatch.presentation.recorder.RecorderScreen
import com.example.speechmatch.presentation.recorder.RecorderViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Uygulamanın tek aktivite (Single Activity Architecture) giriş noktası.
 * Tüm Çoklu Profil (Multi-User) navigasyon grafiğini barındırır.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
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
 * Uygulamanın tüm rota yönetimini ve ekran geçişlerini yöneten ana bileşen.
 * SplashViewModel kaldırılmış, başlangıç noktası ProfileSelection yapılmıştır.
 */
@Composable
fun AppRouter() {
    val navController = rememberNavController()
    val animationDuration = 500

    NavHost(
        navController = navController,
        startDestination = "profile_selection", // BAŞLANGIÇ NOKTASI ARTIK BURASI
        enterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(animationDuration)) + fadeIn(tween(animationDuration))
        },
        exitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(animationDuration)) + fadeOut(tween(animationDuration))
        },
        popEnterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(animationDuration)) + fadeIn(tween(animationDuration))
        },
        popExitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(animationDuration)) + fadeOut(tween(animationDuration))
        }
    ) {

        // ROTA 1: Netflix Tarzı Profil Seçim Ekranı
        composable("profile_selection") {
            ProfileSelectionScreen(
                onProfileSelected = { profile ->
                    // Eğer kullanıcının baselineScore'u 0 ise bu yeni bir hesaptır -> SINAVA GİDER
                    if (profile.baselineScore <= 0.0) {
                        navController.navigate("placement/${profile.userId}")
                    } else {
                        // Eğer puanı varsa zaten sınava girmiştir -> KENDİ KELİMELERİYLE DERSE GİDER
                        navController.navigate("recorder/${profile.userId}")
                    }
                }
            )
        }

        // ROTA 2: Seviye belirleme öncesi karşılama ekranı (Kullanıcı ID'si ile)
        composable(
            route = "placement/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 1
            WelcomeScreen(onStartTest = { navController.navigate("actual_test/$userId") })
        }

        // ROTA 3: Aktif ses analizinin yapıldığı seviye tespit sınavı (Kullanıcı ID'si ile)
        composable(
            route = "actual_test/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 1
            PlacementScreen(
                onTestFinished = { calculatedLevel ->
                    navController.navigate("level_intro/$calculatedLevel/$userId") {
                        popUpTo("actual_test/$userId") { inclusive = true }
                    }
                }
            )
        }

        // ROTA 4: Tespit edilen seviyenin açıklandığı ara ekran
        composable(
            route = "level_intro/{level}/{userId}",
            arguments = listOf(
                navArgument("level") { type = NavType.StringType },
                navArgument("userId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val calculatedLevel = backStackEntry.arguments?.getString("level") ?: "A1"
            val userId = backStackEntry.arguments?.getInt("userId") ?: 1

            LevelIntroScreen(level = calculatedLevel) {
                navController.navigate("recorder/$userId") {
                    // Sınav bittikten sonra geri tuşuna basarsa direk Profil Seçim ekranına düşsün
                    popUpTo("profile_selection") { inclusive = false }
                }
            }
        }

        // ROTA 5: Antrenman ve Algoritma Ekranı (Recorder)
        composable(
            route = "recorder/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 1

            val viewModel: RecorderViewModel = hiltViewModel()

            // Seçili olan Kullanıcı Kimliğini ViewModel'e şırınga ediyoruz!
            LaunchedEffect(userId) {
                viewModel.setUserId(userId)
            }

            RecorderScreen(viewModel = viewModel)
        }
    }
}