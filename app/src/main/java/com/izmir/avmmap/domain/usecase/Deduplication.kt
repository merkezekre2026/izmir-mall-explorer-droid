package com.izmir.avmmap.domain.usecase

import com.izmir.avmmap.domain.model.ShoppingMall
import kotlin.math.abs

/**
 * AVM listesindeki tekrarlayan kayıtları temizler.
 * Aynı veya çok yakın koordinat + benzer isim eşleşmelerini deduplicate eder.
 */
object Deduplication {

    /** Koordinat eşik değeri: ~50 metre */
    private const val COORDINATE_THRESHOLD = 0.0005

    /** İsim benzerlik eşiği (0-1 arası, 1 = tam eşleşme) */
    private const val NAME_SIMILARITY_THRESHOLD = 0.85

    /**
     * Verilen AVM listesinden tekrarlayan kayıtları kaldırır.
     * İlk görülen kayıt korunur, sonraki benzer kayıtlar atılır.
     */
    fun deduplicate(malls: List<ShoppingMall>): List<ShoppingMall> {
        val result = mutableListOf<ShoppingMall>()

        for (mall in malls) {
            val isDuplicate = result.any { existing ->
                areSimilar(existing, mall)
            }
            if (!isDuplicate) {
                result.add(mall)
            }
        }

        return result
    }

    /**
     * İki AVM kaydının benzer olup olmadığını kontrol eder.
     * Hem koordinat hem isim benzerliği kontrol edilir.
     */
    fun areSimilar(a: ShoppingMall, b: ShoppingMall): Boolean {
        val coordinateSimilar = isCoordinateSimilar(a.lat, a.lon, b.lat, b.lon)
        val nameSimilar = isNameSimilar(a.name, b.name)
        return coordinateSimilar && nameSimilar
    }

    /** Koordinatların birbirine yakın olup olmadığını kontrol eder */
    private fun isCoordinateSimilar(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Boolean {
        return abs(lat1 - lat2) < COORDINATE_THRESHOLD &&
                abs(lon1 - lon2) < COORDINATE_THRESHOLD
    }

    /** İki ismin benzerliğini hesaplar (Jaro-Winkler benzeri basitleştirilmiş) */
    private fun isNameSimilar(name1: String, name2: String): Boolean {
        val normalized1 = normalizeName(name1)
        val normalized2 = normalizeName(name2)

        if (normalized1 == normalized2) return true
        if (normalized1.contains(normalized2) || normalized2.contains(normalized1)) return true

        val similarity = calculateSimilarity(normalized1, normalized2)
        return similarity >= NAME_SIMILARITY_THRESHOLD
    }

    /** İsim normalizasyonu: küçük harf, gereksiz karakter temizliği */
    private fun normalizeName(name: String): String {
        return name.lowercase()
            .replace(Regex("[^a-z0-9çğıöşüâîûê]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    /** Basit Levenshtein benzerlik oranı */
    private fun calculateSimilarity(s1: String, s2: String): Double {
        if (s1.isEmpty() && s2.isEmpty()) return 1.0
        if (s1.isEmpty() || s2.isEmpty()) return 0.0

        val maxLen = maxOf(s1.length, s2.length)
        val distance = levenshteinDistance(s1, s2)
        return 1.0 - (distance.toDouble() / maxLen)
    }

    /** Levenshtein mesafe algoritması */
    private fun levenshteinDistance(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }

        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j

        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,
                    dp[i][j - 1] + 1,
                    dp[i - 1][j - 1] + cost
                )
            }
        }

        return dp[s1.length][s2.length]
    }
}
