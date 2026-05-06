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

@HiltViewModel
class SplashViewModel @Inject constructor(
    @ApplicationContext private val context: Context, // JSON okumak için Context eklendi
    private val repository: SpeechMatchRepository
) : ViewModel() {

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination = _startDestination.asStateFlow()

    init {
        initializeApp()
    }

    private fun initializeApp() {
        // Ağır I/O (Disk) işlemleri olduğu için Dispatchers.IO kullanıyoruz
        viewModelScope.launch(Dispatchers.IO) {

            // 1. ÖNYÜKLEME (BOOTSTRAPPING): Veritabanı boşsa önce JSON'u içeri bas!
            if (repository.getAllActiveWords().isEmpty()) {
                seedDatabaseFromJson()
            }

            // 2. YÖNLENDİRME (ROUTING): Veritabanı kesin olarak dolduktan sonra profile bak.
            val profile = repository.getUserProfile()
            if (profile == null || profile.currentLevel.isEmpty()) {
                _startDestination.value = "placement" // Sınava (Hoş Geldin ekranına) gönder
            } else {
                _startDestination.value = "recorder" // Antrenmana gönder
            }
        }
    }

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
                    cefrLevel = jsonObject.getString("cefrLevel"),
                    minimalPairId = jsonObject.getInt("minimalPairId"),
                    isArchived = false
                )
                repository.insertWord(entity)
            }
        } catch (e: Exception) {
            e.printStackTrace() // Ayrıştırma hatası olursa loga yaz
        }
    }
}