package com.example.speechmatch.presentation.placement

import com.example.speechmatch.data.local.entity.VocabularyEntity

/**
 * Seviye belirleme sınavı (Placement Test) ekranının anlık durumunu temsil eden veri paketi.
 * UI bileşenleri bu state nesnesini dinleyerek sınav sürecindeki değişimleri kullanıcıya yansıtır.
 */
data class PlacementUiState(

    /** Sınavda o an aktif olan kelimenin liste indeksi. */
    val currentWordIndex: Int = 0,

    /** Sınav için havuzdan seçilen ve kullanıcıya sorulacak kelimelerin listesi. */
    val testWords: List<VocabularyEntity> = emptyList(),

    /** Tüm sınav soruları tamamlandığında true değerini alan bitiş bayrağı. */
    val isTestFinished: Boolean = false,

    /** * Sınav performansı ve akustik skorlara göre algoritma tarafından
     * teşhis edilen CEFR dil seviyesi (Örn: "A1", "B1", "C1").
     */
    val calculatedLevel: String = "A1",

    /** Veritabanı sorguları veya başlangıç hazırlıkları sürerken true olan yüklenme durumu. */
    val isLoading: Boolean = true,

    /** İşlem sırasında oluşabilecek (G/Ç hatası vb.) kullanıcı dostu hata mesajı. */
    val errorMessage: String? = null
)