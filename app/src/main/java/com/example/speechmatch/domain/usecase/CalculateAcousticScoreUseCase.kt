package com.example.speechmatch.domain.usecase

import javax.inject.Inject

/**
 * Kullanıcının telaffuzu ile hedef kelime arasındaki fonetik benzerliği
 * Levenshtein Mesafe (Dynamic Programming) algoritması ile hesaplayan UseCase.
 */
class CalculateAcousticScoreUseCase @Inject constructor() {

    /**
     * İki metin arasındaki farkı analiz eder ve SM-2 algoritması için 0-5 arası bir kalite skoru (q) üretir.
     * * @param targetWord Okunması beklenen hedef kelime.
     * @param userPronunciation Ses tanıma (STT) motorundan dönen kullanıcı telaffuzu.
     * @return 0 (Tamamen Başarısız) ile 5 (Kusursuz) arasında akustik doğruluk skoru.
     */
    operator fun invoke(targetWord: String, userPronunciation: String): Int {
        val target = targetWord.lowercase()
        val user = userPronunciation.lowercase()

        val dp = Array(target.length + 1) { IntArray(user.length + 1) }

        // DP Matrisinin taban durumlarını (ilk satır ve sütun) ilklendirme
        for (i in 0..target.length) {
            dp[i][0] = i
        }
        for (j in 0..user.length) {
            dp[0][j] = j
        }

        // Levenshtein Mesafesi hesaplama (Silme, Ekleme, Değiştirme maliyetleri matrisi)
        for (i in 1..target.length) {
            for (j in 1..user.length) {
                val cost = if (target[i - 1] == user[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,      // Silme (Deletion)
                    dp[i][j - 1] + 1,      // Ekleme (Insertion)
                    dp[i - 1][j - 1] + cost // Değiştirme (Substitution)
                )
            }
        }

        val distance = dp[target.length][user.length]
        val maxLen = maxOf(target.length, user.length)

        // Sıfıra bölme (Divide by Zero) güvenlik kontrolü
        if (maxLen == 0) return 5

        // Levenshtein mesafesini doğruluk yüzdesine dönüştürme
        val accuracyPercentage = ((maxLen - distance).toDouble() / maxLen) * 100

        // Doğruluk yüzdesini SM-2 algoritmasının kalite skoruna (q) haritalama
        return when {
            accuracyPercentage >= 90 -> 5 // Kusursuz
            accuracyPercentage >= 75 -> 4 // Çok İyi
            accuracyPercentage >= 60 -> 3 // Geçer Not (Zorlanarak doğru)
            accuracyPercentage >= 40 -> 2 // Kötü (Hatalı telaffuz)
            accuracyPercentage >= 20 -> 1 // Çok Kötü (Anlaşılmaz)
            else -> 0                     // Başarısız (Farklı kelime algılandı)
        }
    }
}