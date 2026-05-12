package com.example.speechmatch.domain.usecase

import java.util.Locale
import kotlin.math.max

/**
 * Hedef kelime ile kullanıcının telaffuzu arasındaki fonetik benzerliği
 * Levenshtein Mesafe algoritması ile hesaplayarak SM-2 kalite skoru (0-5) üreten UseCase.
 */
class CalculateLevenshteinScoreUseCase {

    /**
     * İki metin arasındaki minimum düzenleme mesafesini (silme, ekleme, değiştirme) hesaplar.
     * * @param targetWord Okunması beklenen hedef kelime.
     * @param spokenText STT motorundan dönen kullanıcı telaffuzu.
     * @return SM-2 algoritması için 0 (Başarısız) ile 5 (Kusursuz) arası kalite skoru (q).
     */
    operator fun invoke(targetWord: String, spokenText: String): Int {
        val target = targetWord.trim().lowercase(Locale.ENGLISH)
        val spoken = spokenText.trim().lowercase(Locale.ENGLISH)

        if (target.isEmpty() || spoken.isEmpty()) return 0

        // Optimizasyon: Birebir eşleşme durumunda işlem yükünü önlemek için doğrudan tam puan döndürülür.
        if (target == spoken) return 5

        // Dinamik Programlama (DP) matrisinin oluşturulması.
        val dp = Array(target.length + 1) { IntArray(spoken.length + 1) }

        // Matrisin taban durumlarının (ilk satır ve sütun) ilklendirilmesi.
        for (i in 0..target.length) {
            dp[i][0] = i
        }
        for (j in 0..spoken.length) {
            dp[0][j] = j
        }

        // Levenshtein mesafesinin (Silme, Ekleme, Değiştirme maliyetleri) hesaplanması.
        for (i in 1..target.length) {
            for (j in 1..spoken.length) {
                val cost = if (target[i - 1] == spoken[j - 1]) 0 else 1

                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,       // Silme (Deletion)
                    dp[i][j - 1] + 1,       // Ekleme (Insertion)
                    dp[i - 1][j - 1] + cost // Değiştirme (Substitution)
                )
            }
        }

        val distance = dp[target.length][spoken.length]
        val maxLength = max(target.length, spoken.length)

        // Levenshtein mesafesinin %0 ile %100 arası benzerlik oranına dönüştürülmesi.
        val similarityRatio = 1.0 - (distance.toDouble() / maxLength.toDouble())

        // Benzerlik oranının SM-2 algoritması kalite skoruna (q) haritalanması.
        return when {
            similarityRatio >= 0.90 -> 5 // Kusursuz
            similarityRatio >= 0.80 -> 4 // Çok İyi
            similarityRatio >= 0.65 -> 3 // Geçer Not
            similarityRatio >= 0.50 -> 2 // Kötü
            similarityRatio >= 0.30 -> 1 // Çok Kötü
            else -> 0                    // Başarısız
        }
    }
}