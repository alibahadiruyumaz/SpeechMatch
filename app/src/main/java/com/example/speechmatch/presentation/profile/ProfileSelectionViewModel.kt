package com.example.speechmatch.presentation.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.speechmatch.data.local.entity.UserProfileEntity
import com.example.speechmatch.data.local.entity.VocabularyEntity
import com.example.speechmatch.domain.repository.SpeechMatchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import javax.inject.Inject

/** Profil seçim ekranının UI State'ini temsil eden veri sınıfı. */
data class ProfileSelectionUiState(
    val profiles: List<UserProfileEntity> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

/** * Netflix tarzı "Kim İzliyor/Çalışıyor?" ekranının iş mantığını yöneten ViewModel.
 * Uygulamanın ilk açılışındaki kelime yükleme (Seeding) işlemini de artık burası yapar!
 */
@HiltViewModel
class ProfileSelectionViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: SpeechMatchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileSelectionUiState())
    val uiState: StateFlow<ProfileSelectionUiState> = _uiState.asStateFlow()

    init {
        // Uygulama açılır açılmaz hem kelimeleri yükle hem profilleri çek
        seedDatabaseFromJson()
        loadProfiles()
    }

    /** Veritabanı boşsa JSON üzerinden ön yükleme (Seed) yapar. */
    private fun seedDatabaseFromJson() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (repository.getAllActiveWords(1).isNotEmpty()) {
                    return@launch
                }

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
                _uiState.update { it.copy(errorMessage = "JSON Kelime Yükleme Hatası: ${e.message}") }
            }
        }
    }

    /** Veritabanından kayıtlı tüm kullanıcı profillerini çeker. */
    fun loadProfiles() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val profiles = repository.getAllProfiles()

                if (profiles.isEmpty()) {
                    val defaultProfile = UserProfileEntity(
                        profileName = "Misafir",
                        currentLevel = "A1",
                        baselineScore = 0.0,
                        chronicErrorPhonemes = ""
                    )
                    repository.upsertProfile(defaultProfile)

                    val updatedProfiles = repository.getAllProfiles()
                    _uiState.update { it.copy(profiles = updatedProfiles, isLoading = false) }
                } else {
                    _uiState.update { it.copy(profiles = profiles, isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Profiller yüklenemedi: ${e.message}")
                }
            }
        }
    }

    /** Yeni bir profil oluşturur ve veritabanına kaydeder. */
    fun createNewProfile(name: String) {
        if (name.isBlank()) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val newProfile = UserProfileEntity(
                    profileName = name.trim(),
                    currentLevel = "A1",
                    baselineScore = 0.0,
                    chronicErrorPhonemes = ""
                )
                repository.upsertProfile(newProfile)
                loadProfiles()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Profil oluşturulamadı: ${e.message}")
                }
            }
        }
    }

    /** * SEÇİLEN PROFİLİ SİLER
     * Basılı tutma menüsünden tetiklenir.
     */
    fun deleteProfile(profile: UserProfileEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.deleteProfile(profile)
                loadProfiles() // Sildikten sonra listeyi tekrar yükler
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Profil silinemedi: ${e.message}")
                }
            }
        }
    }
}