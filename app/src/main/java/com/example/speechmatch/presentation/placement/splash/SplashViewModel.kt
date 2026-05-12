package com.example.speechmatch.presentation.splash

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.speechmatch.data.local.entity.VocabularyEntity
import com.example.speechmatch.domain.repository.SpeechMatchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import javax.inject.Inject

/**
 * Uygulamanın başlatma (startup) sürecini, veri tohumlamayı (seeding)
 * ve kullanıcı durumuna göre yönlendirme (routing) mantığını yöneten ViewModel.
 */
@HiltViewModel
class SplashViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: SpeechMatchRepository
) : ViewModel() {

    /** Uygulamanın hangi ekrandan başlayacağını (Placement veya Recorder) belirleyen reaktif durum. */
    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination = _startDestination.asStateFlow()

    init {
        initializeApp()
    }

    /** Uygulama ilk açıldığında gerekli veri hazırlıklarını ve profil kontrollerini asenkron olarak yürütür. */
    private fun initializeApp() {
        viewModelScope.launch(Dispatchers.IO) {

            // 1. Veritabanı boşsa Assets içerisindeki JSON dosyasından kelime havuzunu (vocabulary) oluşturur.
            if (repository.getAllActiveWords().isEmpty()) {
                seedDatabaseFromJson()
            }

            // 2. Kullanıcı profilini kontrol ederek; yeni kullanıcıyı Seviye Tespit Sınavına,
            // kayıtlı kullanıcıyı ana çalışma ekranına yönlendirir.
            val profile = repository.getUserProfile()
            if (profile == null || profile.currentLevel.isEmpty()) {
                _startDestination.value = "placement"
            } else {
                _startDestination.value = "recorder"
            }
        }
    }

    /** * 'vocabulary_seed.json' dosyasını okuyarak veritabanı ön yükleme (bootstrapping) işlemini gerçekleştirir.
     * Bu işlem sadece uygulamanın ilk kurulumundaki ilk açılışta bir kez çalışır.
     */
    private suspend fun seedDatabaseFromJson() {
        try {
            val inputStream = context.assets.open("vocabulary_seed.json")
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonString)

            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val entity = VocabularyEntity(
                    id = 0,
                    text = jsonObject.getString("text"),
                    targetPhoneme = jsonObject.getString("targetPhoneme"),
                    easyRead = jsonObject.getString("easyRead"),
                    cefrLevel = jsonObject.getString("cefrLevel"),
                    minimalPairId = if (jsonObject.has("minimalPairId")) jsonObject.getInt("minimalPairId") else null,
                    isArchived = false
                )
                repository.insertWord(entity)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}