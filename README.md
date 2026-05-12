

# SpeechMatch 🎙️🧠

SpeechMatch, kullanıcıların İngilizce telaffuzlarını %100 çevrimdışı (Edge Computing) analiz eden, Levenshtein Mesafe algoritması ile SuperMemo-2 (SM-2) aralıklı tekrar hafıza modelini birleştiren otonom ve akıllı bir dil öğrenme asistanıdır.

## 🚀 Proje Vizyonu ve Mimari

Bu proje, bulut sunucularına (AWS, Google Cloud) olan bağımlılığı tamamen ortadan kaldırarak cihazın kendi ARM işlemcisini kullanmayı hedefler. Gecikmesiz (Zero-Latency) ses tanıma ve maksimum veri gizliliği temel alınmıştır.

Projede **Clean Architecture (Temiz Mimari)** ve **MVVM** tasarım desenleri sıkı bir şekilde uygulanmış olup, uygulamanın veri akışı bütünüyle **Reaktif (Single Source of Truth)** olarak tasarlanmıştır.

## 🛠️ Kullanılan Teknolojiler (Tech Stack)

* **Dil:** Kotlin
* **Kullanıcı Arayüzü (UI):** Jetpack Compose & Compose Navigation
* **Bağımlılık Enjeksiyonu (DI):** Dagger-Hilt
* **Yerel Veritabanı:** Room Database (SQLite)
* **Asenkron İşlemler:** Kotlin Coroutines & StateFlow
* **Ses İşleme Motoru:** Android SpeechRecognizer (EXTRA_PREFER_OFFLINE)
* **Metinden Sese (TTS):** Android TextToSpeech API
* **Açılış Ekranı ve Animasyonlar:** AndroidX Splash Screen API & Compose Transition Animations

## 🧠 Algoritmik Çekirdek ve Gelişmiş Özellikler

1. **Levenshtein Distance Motoru:** Kullanıcının okuduğu kelime ile hedef kelime arasındaki farkı matris hesabı ile analiz eder ve 0 ile 5 arasında bir "Akustik Kalite Skoru" üretir.
2. **Otonom SM-2 Hafıza Modeli:** Üretilen skoru işleyerek; kullanıcının doğru bilme serisine ve kolaylık faktörüne göre bir sonraki sorulma tarihini (Interval) dinamik olarak belirler.
3. **Mastery & Arşivleme Sistemi (180 Gün Kuralı):** Algoritma tarafından hesaplanan tekrar aralığı **180 günü** geçen kelimeler "Mastered" (Uzmanlaşıldı) kabul edilir ve otomatik olarak arşivlenerek aktif havuzdan çıkarılır.
4. **Otomatik Seviye Atlama (Promotion Engine):** Kullanıcı, mevcut CEFR seviyesindeki (örn. B1) **20 farklı kelimeyi** başarıyla arşivlediğinde, sistem kullanıcıyı otomatik olarak bir üst seviyeye (örn. B2) terfi ettirir.
5. **Dinamik Pedagojik Uyarılar:** Kelimenin fonetik yapısı analiz edilerek (Schwa sesi, TH/SH telaffuzu, sessiz harfler vb.) kullanıcıya gerçek zamanlı "Özel İpuçları" sunulur.
6. **Adaptif Tipografi:** Uzun kelimelerin (örn. "Nevertheless") kullanıcı arayüzünü bozmasını engellemek için kelime uzunluğuna bağlı dinamik font boyutlandırma algoritması kullanılır.
7. **Dinamik CEFR Müfredatı:** Python ve Datamuse API ile üretilmiş 945 kelimelik devasa İngilizce fonetik veri seti (A1-C2), uygulama ilk açıldığında asenkron olarak tohumlanır.
8. **Harf Harf Hata Analizi (Diffing):** `AnnotatedString` mimarisi kullanılarak eksik veya hatalı harfler ekranda anında kırmızı ile vurgulanarak görsel geri bildirim sağlanır.

## 📈 Geliştirme Süreci (Sprint Log)

* ✅ **Sprint 1-3:** Room Database, DAO'lar, Dagger-Hilt ve SM-2/Levenshtein çekirdek algoritmalarının inşası.
* ✅ **Sprint 4-5:** Çevrimdışı SpeechRecognizer motoru, Headset güvenlik ajanı ve TTS entegrasyonu.
* ✅ **Sprint 6-7:** ViewModel katmanı, StateFlow entegrasyonu ve Jetpack Compose ile reaktif UI tasarımı.
* ✅ **Sprint 8-9:** 945 kelimelik JSON veri havuzunun entegrasyonu ve akıllı Seviye Belirleme Sınavı (Placement Engine).
* ✅ **Sprint 10-11:** Navigasyon haritası, Oturum Yönetimi, karne ekranı (Donut Chart) ve Zengin Metin (AnnotatedString) geri bildirim sistemi.
* ✅ **Sprint 12-13:** Ses motoru Singleton optimizasyonu, Dark/Light Mode uyumu ve "Derse Dön" kaza önleme mekaniği.
* ✅ **Sprint 14 (Final Vitrin):** **180 Gün Mastery Kontrolü**, **Otomatik Terfi Sistemi**, **Dinamik Font Ölçekleme** ve Android 12+ Splash Screen entegrasyonu ile kurumsal kimlik inşası.

---

*Bu proje Ali Bahadır UYUMAZ tarafından geliştirilmiştir.*

---
