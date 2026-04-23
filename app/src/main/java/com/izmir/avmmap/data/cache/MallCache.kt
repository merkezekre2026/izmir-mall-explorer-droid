package com.izmir.avmmap.data.cache

import android.content.Context
import android.content.SharedPreferences
import com.izmir.avmmap.domain.model.ShoppingMall
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

/**
 * AVM verilerini basit SharedPreferences tabanlı olarak önbelleğe alır.
 * Room veya DataStore yerine hafif bir çözüm olarak tercih edilmiştir.
 * Ağ yoksa son başarılı veri bu önbellekten yüklenir.
 */
class MallCache(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("mall_cache", Context.MODE_PRIVATE)

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    companion object {
        private const val KEY_MALLS = "cached_malls"
        private const val KEY_TIMESTAMP = "cache_timestamp"
        private const val CACHE_VALIDITY_MS = 24 * 60 * 60 * 1000L // 24 saat
    }

    /**
     * Önbelleğe AVM listesi kaydeder.
     */
    suspend fun save(malls: List<ShoppingMall>) {
        val jsonString = json.encodeToString(malls)
        prefs.edit()
            .putString(KEY_MALLS, jsonString)
            .putLong(KEY_TIMESTAMP, System.currentTimeMillis())
            .apply()
    }

    /**
     * Önbellekten AVM listesini yükler.
     * @return Önbellekteki liste veya boş liste
     */
    suspend fun load(): List<ShoppingMall> {
        val jsonString = prefs.getString(KEY_MALLS, null) ?: return emptyList()
        return try {
            json.decodeFromString<List<ShoppingMall>>(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Önbelleğin geçerli olup olmadığını kontrol eder.
     */
    fun isValid(): Boolean {
        val timestamp = prefs.getLong(KEY_TIMESTAMP, 0L)
        return System.currentTimeMillis() - timestamp < CACHE_VALIDITY_MS
    }

    /**
     * Önbelleği temizler.
     */
    fun clear() {
        prefs.edit().clear().apply()
    }
}
