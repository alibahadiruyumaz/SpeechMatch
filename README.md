# SpeechMatch 🎙️🧠

SpeechMatch, kullanıcıların İngilizce telaffuzlarını %100 çevrimdışı (Edge Computing) analiz eden ve Levenshtein Mesafe algoritması ile SuperMemo-2 (SM-2) aralıklı tekrar hafıza modelini birleştiren otonom bir dil öğrenme asistanıdır.

## 🚀 Proje Vizyonu ve Mimari
Bu proje, bulut sunucularına (AWS, Google Cloud) olan bağımlılığı tamamen ortadan kaldırarak cihazın kendi ARM işlemcisini kullanmayı hedefler. Gecikmesiz (Zero-Latency) ses tanıma ve maksimum veri gizliliği temel alınmıştır.

Projede **Clean Architecture (Temiz Mimari)** ve **MVVM** tasarım desenleri uygulanmaktadır.

## 🛠️ Kullanılan Teknolojiler (Tech Stack)
* **Dil:** Kotlin
* **Bağımlılık Enjeksiyonu (DI):** Dagger-Hilt
* **Yerel Veritabanı:** Room Database (SQLite)
* **Asenkron İşlemler:** Kotlin Coroutines & StateFlow
* **Ses İşleme Motoru:** Android SpeechRecognizer (EXTRA_PREFER_OFFLINE)
* **Donanım Gözlemcisi:** BroadcastReceiver (Yankı Koruma Sistemi)

## 🧠 Algoritmik Çekirdek
1.  **Levenshtein Distance Motoru:** Kullanıcının okuduğu kelime ile hedef kelime arasındaki farkı matris hesabı ile bulur ve 0 ile 5 arasında bir "Akustik Kalite Skoru" (q) üretir.
2.  **Otonom SM-2 Hafıza Modeli:** Üretilen q skorunu alır; kullanıcının doğru bilme serisine (n) ve kolaylık faktörüne (EF) göre kelimenin bir sonraki sorulma tarihini (Interval) otonom olarak belirler.

## 📈 Geliştirme Süreci (Sprint Log)
* ✅ **Sprint 1:** Room Database, DAO'lar ve Entity (Tablo) mimarilerinin kurulumu.
* ✅ **Sprint 2:** Dagger-Hilt entegrasyonu, Levenshtein ve SM-2 Usecase'lerinin yazılması, Repository katmanı.
* ✅ **Sprint 3:** Çevrimdışı SpeechRecognizer motorunun inşası, AudioModule köprüsü ve Headset (Kulaklık) BroadcastReceiver güvenlik ajanı.
* ⏳ **Sprint 4:** ViewModel (Beyin Sapı) katmanının inşası ve Coroutines yönetimi. (Planlanıyor)
* ⏳ **Sprint 5:** Jetpack Compose ile Kullanıcı Arayüzü (UI) tasarımı. (Planlanıyor)

---
*Bu proje Ali Bahadır UYUMAZ tarafından geliştirilmektedir.*
