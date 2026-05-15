Harika bir dokunuş! Eski "1300 kelime" ibarelerini yeni ve devasa havuzumuz olan **4057 kelime** ile güncelledim. Ayrıca projeye ne kadar bilimsel ve pedagojik yaklaştığını jüriye tam anlamıyla yansıtmak için **"📊 Veritabanı ve Pedagojik Dağılım"** adında yepyeni bir bölüm ekleyerek o şık tabloyu da README'nin kalbine yerleştirdim.

İşte GitHub'a (ve sunumuna) doğrudan koyabileceğin o kusursuz, son ve en güncel README.md dosyan:

---

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

1. **Netflix Tarzı Çoklu Profil (Multi-Profile):** Uygulama, Netflix mimarisinde olduğu gibi birden fazla kullanıcının (Örn: Öğrenci, Hoca, Misafir) aynı cihazda birbirinden tamamen izole şekilde çalışmasına olanak tanır. Her kullanıcının kendi seviyesi, kelime arşivi ve SM-2 ilerlemesi ayrı tutulur.
2. **Levenshtein Distance Motoru:** Kullanıcının okuduğu kelime ile hedef kelime arasındaki farkı matris hesabı ile analiz eder. Bu veriyi kullanarak hocanın/kullanıcının anlayacağı **%100 üzerinden net başarı yüzdesi** üretir.
3. **Otonom SM-2 Hafıza Modeli:** Üretilen skoru işleyerek; kullanıcının doğru bilme serisine ve kolaylık faktörüne göre bir sonraki sorulma tarihini (Interval) dinamik olarak belirler.
4. **Mastery & Arşivleme Sistemi (180 Gün Kuralı):** Algoritma tarafından hesaplanan tekrar aralığı **180 günü** geçen kelimeler "Mastered" (Uzmanlaşıldı) kabul edilir ve kullanıcıya özel olarak arşivlenir.
5. **Otomatik Seviye Atlama (Promotion Engine):** Kullanıcı, mevcut CEFR seviyesindeki (örn. B1) **20 farklı kelimeyi** başarıyla arşivlediğinde, sistem kullanıcıyı otomatik olarak bir üst seviyeye (örn. B2) terfi ettirir.
6. **Dinamik Pedagojik Uyarılar:** Kelimenin fonetik yapısı analiz edilerek (Schwa sesi, TH/SH telaffuzu, sessiz harfler vb.) kullanıcıya gerçek zamanlı "Özel İpuçları" sunulur.
7. **Adaptif Tipografi:** Uzun kelimelerin (örn. "Nevertheless") kullanıcı arayüzünü bozmasını engellemek için kelime uzunluğuna bağlı dinamik font boyutlandırma algoritması kullanılır.
8. **Dinamik CEFR Müfredatı:** Python ve Datamuse API ile üretilmiş **4057 kelimelik devasa İngilizce fonetik veri seti** (A1-C2), uygulama ilk açıldığında asenkron olarak tohumlanır.
9. **Harf Harf Hata Analizi (Diffing):** `AnnotatedString` mimarisi kullanılarak eksik veya hatalı harfler ekranda anında kırmızı ile vurgulanarak görsel geri bildirim sağlanır.

## 📊 Veritabanı ve Pedagojik Dağılım

Uygulamanın kelime havuzu, Python veri madenciliği araçları ve Datamuse API kullanılarak İngilizce kelimelerin "milyonda bir kullanım sıklığına" (frekans) göre filtrelenmesiyle oluşturulmuştur. Bu dağılım, **Zipf Yasası'na** ve doğal dil öğrenme pedagojisine birebir uyumludur:

| CEFR Seviyesi | Kelime Sayısı | Seviye Açıklaması |
| --- | --- | --- |
| **A1** | 214 | Başlangıç (Beginner) - *Çok Yüksek Frekans* |
| **A2** | 248 | Temel (Elementary) |
| **B1** | 595 | Orta (Intermediate) |
| **B2** | 1000 | Orta Üstü (Upper-Intermediate) |
| **C1** | 1000 | İleri (Advanced) |
| **C2** | 1000 | Uzmanlık / Ana Dil (Proficiency) - *Düşük Frekans/Akademik* |
| **TOPLAM** | **4057** | **Aktif Kelime Havuzu** |

## 📈 Geliştirme Süreci (Sprint Log)

* ✅ **Sprint 1-3:** Room Database, DAO'lar, Dagger-Hilt ve SM-2/Levenshtein çekirdek algoritmalarının inşası.
* ✅ **Sprint 4-5:** Çevrimdışı SpeechRecognizer motoru, Headset güvenlik ajanı ve TTS entegrasyonu.
* ✅ **Sprint 6-7:** ViewModel katmanı, StateFlow entegrasyonu ve Jetpack Compose ile reaktif UI tasarımı.
* ✅ **Sprint 8-9:** **4057 kelimelik JSON veri havuzunun** entegrasyonu ve akıllı Seviye Belirleme Sınavı (Placement Engine).
* ✅ **Sprint 10-11:** Navigasyon haritası, Oturum Yönetimi, karne ekranı (Donut Chart) ve Zengin Metin (AnnotatedString) geri bildirim sistemi.
* ✅ **Sprint 12-13:** Ses motoru Singleton optimizasyonu, Dark/Light Mode uyumu ve "Derse Dön" kaza önleme mekaniği.
* ✅ **Sprint 14:** 180 Gün Mastery Kontrolü, Otomatik Terfi Sistemi ve Dinamik Font Ölçekleme.
* ✅ **Sprint 15 (Final / Finalize):** **Çoklu Profil Yönetimi (Multi-User Switcher)**, **Gelişmiş Profil Silme/Ekleme Mekanizması**, **Yüzdelik Analitik Skorlama** ve Android 12+ Splash Screen ile kurumsal kimlik inşası.

---

*Bu proje Ali Bahadır UYUMAZ tarafından geliştirilmiştir.*
