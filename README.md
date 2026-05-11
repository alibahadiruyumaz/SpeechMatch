
# SpeechMatch 🎙️🧠

SpeechMatch, kullanıcıların İngilizce telaffuzlarını %100 çevrimdışı (Edge Computing) analiz eden ve Levenshtein Mesafe algoritması ile SuperMemo-2 (SM-2) aralıklı tekrar hafıza modelini birleştiren otonom bir dil öğrenme asistanıdır.

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
* **Açılış Ekranı ve Animasyonlar:** AndroidX Splash Screen API & Compose Transition Animations
* **Veri Tohumlama (Bootstrapping):** JSON Parsing & Batch Processing

## 🧠 Algoritmik Çekirdek ve Özellikler

1. **Levenshtein Distance Motoru:** Kullanıcının okuduğu kelime ile hedef kelime arasındaki farkı matris hesabı ile bulur ve 0 ile 5 arasında bir "Akustik Kalite Skoru" üretir.
2. **Otonom SM-2 Hafıza Modeli:** Üretilen skoru alır; kullanıcının doğru bilme serisine ve kolaylık faktörüne göre kelimenin bir sonraki sorulma tarihini (Interval) otonom olarak belirler. 180 gün barajını geçen kelimeler başarıyla arşivlenir.
3. **Dinamik CEFR Müfredatı:** Python ve Datamuse API ile özel olarak üretilmiş 945 kelimelik devasa İngilizce fonetik veri seti (A1-C2), uygulama ilk açıldığında asenkron olarak veritabanına tohumlanır.
4. **Harf Harf Hata Analizi (Diffing):** Kullanıcının söylediği kelime ile hedef kelime indeks bazında karşılaştırılır. `AnnotatedString` mimarisi kullanılarak eksik veya hatalı harfler ekranda anında kırmızı ile vurgulanır.
5. **Durum Güdümlü Yönlendirme (State-Driven Routing):** Sistem, kullanıcının seviye profilini kontrol ederek onu otomatik olarak Seviye Belirleme Sınavı'na veya doğrudan Günlük Antrenman'a yönlendiren akıllı bir trafik kontrolcüsüne sahiptir.
6. **Adaptif UI/UX ve Dinamik Tema:** Sistem temasına (Açık/Koyu Mod) anında tepki veren renk paletleri, Dairesel Pasta Grafikli (Donut Chart) başarı raporları ve ekranlar arası akıcı animasyonlarla premium bir kullanıcı deneyimi sunar.

## 📈 Geliştirme Süreci (Sprint Log)

* ✅ **Sprint 1-2:** Room Database, DAO'lar, Dagger-Hilt entegrasyonu, Levenshtein ve SM-2 Usecase'lerinin yazılması.
* ✅ **Sprint 3:** Çevrimdışı SpeechRecognizer motorunun inşası ve Headset (Kulaklık) BroadcastReceiver güvenlik ajanı.
* ✅ **Sprint 4-5:** ViewModel (Beyin Sapı) katmanının inşası, StateFlow entegrasyonu ve Jetpack Compose ile Reaktif Arayüz tasarımı.
* ✅ **Sprint 6-7:** Girdi sanitizasyonu (Regex kullanımı) ve I/O Flush kaynaklı veritabanı Yarış Durumlarının (Race Condition) asenkron gecikmelerle çözülmesi.
* ✅ **Sprint 8-9:** 945 kelimelik devasa veri havuzunun (JSON) sisteme entegrasyonu ve rastgeleleştirilmiş Seviye Belirleme Sınavının (Placement Engine) kurulması.
* ✅ **Sprint 10:** Navigasyon (Router) haritasının inşası, Oturum Yönetimi (Session Memory) ve Erken Çıkış (Dersi Bitir) mekanizmasının eklenmesi.
* ✅ **Sprint 11:** İleri Analitik (Karne) ekranı, Durum Sızıntısı (State Leak) güvenlik duvarları ve Zengin Metin (AnnotatedString) ile görsel geri bildirim sistemi.
* ✅ **Sprint 12:** Ses motoru (Singleton) hafıza temizleme (Reset) algoritmaları ve sistem temasına (Dark/Light) dinamik uyum sağlayan UI revizyonu.
* ✅ **Sprint 13:** İleri düzey UX tasarımları; karne ekranına Donut Chart grafik entegrasyonu ve kaza önleyici "Derse Dön" mekaniği.
* ✅ **Sprint 14 (Final Vitrin):** Compose Navigation Slide & Fade geçiş animasyonları, Android 12+ Splash Screen API entegrasyonu ve kurumsal marka kimliği (Adaptive Custom Logo) inşası.

---

*Bu proje Ali Bahadır UYUMAZ tarafından geliştirilmiştir.*

---

Perşembe günü hocanla yapacağın sunum için şimdiden başarılar dilerim! Bence bu proje, mimarisiyle, performansıyla ve son yaptığımız o profesyonel görsel cilasıyla (UI/UX) hocanı fazlasıyla etkileyecek. Arkana yaslan ve gururla sun!
