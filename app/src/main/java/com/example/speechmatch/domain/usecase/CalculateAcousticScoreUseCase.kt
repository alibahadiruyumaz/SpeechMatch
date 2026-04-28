package com.example.speechmatch.domain.usecase

import javax.inject.Inject

// @Inject constructor() ifadesi, bu beynin (usecase) Hilt tarafından
// ViewModel'lara otomatik olarak enjekte edilmesini sağlar.
class CalculateAcousticScoreUseCase @Inject constructor() {

    // operator fun invoke: Bu sınıfı sanki bir fonksiyonmuş gibi doğrudan
    // çağırmamıza olanak tanır (Örn: calculateScore("think", "sink") )
    operator fun invoke(targetWord: String, userPronunciation: String): Int {
        val target = targetWord.lowercase()
        val user = userPronunciation.lowercase()

        val dp = Array(target.length + 1) { IntArray(user.length + 1) }

        // Matrisin ilk satır ve sütunlarını doldurma (Taban durumları)
        for (i in 0..target.length) {
            dp[i][0] = i
        }
        for (j in 0..user.length) {
            dp[0][j] = j
        }

        // Levenshtein Mesafesini Dinamik Programlama ile hesaplama
        for (i in 1..target.length) {
            for (j in 1..user.length) {
                val cost = if (target[i - 1] == user[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,      // Silme
                    dp[i][j - 1] + 1,      // Ekleme
                    dp[i - 1][j - 1] + cost // Değiştirme
                )
            }
        }

        val distance = dp[target.length][user.length]
        val maxLen = maxOf(target.length, user.length)

        // Sıfıra bölme (Divide by Zero) hatasını önleme
        if (maxLen == 0) return 5

        // Doğruluk yüzdesini hesapla
        val accuracyPercentage = ((maxLen - distance).toDouble() / maxLen) * 100

        // %80 üstü 5 veya 4 alır. SM-2'de q < 3 olursa sistem kelimeyi başa sarar (I=1 olur).
        return when {
            accuracyPercentage >= 90 -> 5 // Kusursuz
            accuracyPercentage >= 75 -> 4 // Çok İyi
            accuracyPercentage >= 60 -> 3 // Geçer Not (Zorlandı ama başardı)
            accuracyPercentage >= 40 -> 2 // Kötü (Hata var)
            accuracyPercentage >= 20 -> 1 // Çok Kötü (Anlaşılmaz)
            else -> 0                     // Felaket (Farklı kelime okundu)
        }
    }
}