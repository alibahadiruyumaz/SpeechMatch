package com.example.speechmatch.domain.usecase

import java.util.Locale
import kotlin.math.max

/**
 * Uç bilişim algoritmik kalbi: Hedef kelime ile söylenen kelimeyi hizalar.
 * 0 (Tamamen Yanlış) ile 5 (Kusursuz) arası bir kalite skoru döner (SM-2 formatı).
 */
class CalculateLevenshteinScoreUseCase {

    operator fun invoke(targetWord: String, spokenText: String): Int {
        val target = targetWord.trim().lowercase(Locale.ENGLISH)
        val spoken = spokenText.trim().lowercase(Locale.ENGLISH)

        if (target.isEmpty() || spoken.isEmpty()) return 0

        // Eğer cihaz birebir aynısını anladıysa, matrisi yormadan direkt 5 puan dön (Optimizasyon)
        if (target == spoken) return 5

        // Dinamik Programlama Matrisi (Boyut: [targetLength + 1][spokenLength + 1])
        val dp = Array(target.length + 1) { IntArray(spoken.length + 1) }

        // Matrisin ilk satır ve sütununu baz operasyon (silme/ekleme) maliyetleriyle doldur
        for (i in 0..target.length) {
            dp[i][0] = i
        }
        for (j in 0..spoken.length) {
            dp[0][j] = j
        }

        // Matrisi doldurma (Kesişim maliyetlerini hesapla)
        for (i in 1..target.length) {
            for (j in 1..spoken.length) {
                val cost = if (target[i - 1] == spoken[j - 1]) 0 else 1 // Harfler aynıysa maliyet 0, farklıysa 1 (Değiştirme)

                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,       // Silme (Deletion)
                    dp[i][j - 1] + 1,       // Ekleme (Insertion)
                    dp[i - 1][j - 1] + cost // Değiştirme (Substitution)
                )
            }
        }

        // Matrisin sağ alt köşesi: Toplam Levenshtein Mesafesi
        val distance = dp[target.length][spoken.length]

        // Maksimum olası mesafe, iki kelimeden en uzun olanıdır
        val maxLength = max(target.length, spoken.length)

        // Benzerlik oranını % olarak hesapla (0.0 ile 1.0 arası)
        val similarityRatio = 1.0 - (distance.toDouble() / maxLength.toDouble())

        // SM-2 algoritmasının kabul ettiği 0-5 kalite skoruna (q) çevir
        return when {
            similarityRatio >= 0.90 -> 5 // Kusursuz
            similarityRatio >= 0.80 -> 4 // Çok iyi (Ufak pürüz)
            similarityRatio >= 0.65 -> 3 // Sınırda geçer
            similarityRatio >= 0.50 -> 2 // Kötü (Zar zor anlaşıldı)
            similarityRatio >= 0.30 -> 1 // Çok kötü
            else -> 0                    // Tamamen alakasız
        }
    }
}