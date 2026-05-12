package com.example.speechmatch.presentation.recorder

/**
 * Antrenman ekranının (RecorderScreen) yaşam döngüsü boyunca bürünebileceği durumları
 * temsil eden mühürlü (sealed) sınıf yapısı.
 * * Bu yapı, UI'ın tutarsız durumlara (invalid states) girmesini engeller ve
 * reaktif akışı kontrol altında tutar.
 */
sealed class RecorderState {

    /** Uygulamanın etkileşime hazır olduğu, mikrofonun kapalı olduğu bekleme durumu. */
    object Idle : RecorderState()

    /** Ses tanıma motorunun aktif olduğu ve kullanıcının konuşmasının beklendiği durum. */
    object Listening : RecorderState()

    /** * Ses verisinin STT motorundan geldiği, Levenshtein ve SM-2 algoritmalarının
     * asenkron olarak işletildiği analiz süreci.
     */
    object Analyzing : RecorderState()

    /** * Analiz sürecinin başarıyla tamamlandığını ve sonucun hazır olduğunu belirten durum.
     * @param qualityScore Levenshtein algoritması tarafından üretilen 0-5 arası başarı puanı.
     * @param isPerfect Skorun SM-2 barajını (genellikle 4 ve üzeri) geçip geçmediği.
     */
    data class Success(val qualityScore: Int, val isPerfect: Boolean) : RecorderState()

    /** * İşlem sırasında oluşan (mikrofon izni, STT hatası vb.) kritik hataları temsil eden durum.
     * @param message Kullanıcıya gösterilecek hata açıklaması.
     */
    data class Error(val message: String) : RecorderState()
}