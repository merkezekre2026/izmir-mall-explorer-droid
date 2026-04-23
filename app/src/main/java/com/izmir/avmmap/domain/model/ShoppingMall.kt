package com.izmir.avmmap.domain.model

import kotlinx.serialization.Serializable

/**
 * Uygulama içindeki temel AVM veri modeli.
 * Overpass API'den gelen ham veri bu modele dönüştürülür.
 */
@Serializable
data class ShoppingMall(
    val id: String,
    val name: String,
    val lat: Double,
    val lon: Double,
    val district: String = "",
    val address: String = "",
    val website: String? = null,
    val openingHours: String? = null,
    val sourceType: String = "overpass",
    val osmType: String = "node",
    val osmId: Long = 0L
) {
    /** Kullanıcıya gösterilecek kısa adres bilgisi */
    val displayAddress: String
        get() = buildString {
            if (district.isNotBlank()) append(district)
            if (address.isNotBlank()) {
                if (isNotEmpty()) append(", ")
                append(address)
            }
            if (isEmpty()) append("Adres bilgisi mevcut değil")
        }

    /** Harita marker'ı için JSON formatında veri */
    fun toMarkerJson(): String {
        return """
            {
                "id": "$id",
                "name": ${name.toJsonString()},
                "lat": $lat,
                "lon": $lon,
                "district": ${district.toJsonString()},
                "address": ${address.toJsonString()},
                "website": ${website?.toJsonString() ?: "null"},
                "openingHours": ${openingHours?.toJsonString() ?: "null"}
            }
        """.trimIndent()
    }

    private fun String.toJsonString(): String {
        return "\"${this.replace("\\", "\\\\").replace("\"", "\\\"")}\""
    }
}
