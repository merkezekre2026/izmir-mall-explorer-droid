package com.izmir.avmmap.data.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.json.*

/**
 * Overpass API servisi.
 * OpenStreetMap verilerini Overpass API üzerinden çeker.
 * İzmir il sınırları içindeki AVM verilerini sorgular.
 */
class OverpassApiService(
    private val httpClient: HttpClient
) {
    companion object {
        /** Overpass API endpoint'i - değiştirilebilir */
        var OVERPASS_URL = "https://overpass-api.de/api/interpreter"

        /** İzmir için arama sorgusu */
        private val IZMIR_OVERPASS_QUERY = """
            [out:json][timeout:60];
            area["name"="İzmir"]["admin_level"="4"]->.izmir;
            (
                node["shop"="mall"](area.izmir);
                way["shop"="mall"](area.izmir);
                relation["shop"="mall"](area.izmir);
                node["building"="retail"]["name"~"AVM|Alışveriş Merkezi|Mall|AVM",i](area.izmir);
                way["building"="retail"]["name"~"AVM|Alışveriş Merkezi|Mall|AVM",i](area.izmir);
                relation["building"="retail"]["name"~"AVM|Alışveriş Merkezi|Mall|AVM",i](area.izmir);
            );
            out body center;
        """.trimIndent()
    }

    /**
     * Overpass API'den AVM verilerini çeker.
     * @return Ham JSON element listesi
     * @throws OverpassApiException API hatası durumunda
     */
    suspend fun fetchIzmirMalls(): List<Map<String, Any?>> {
        val response = httpClient.post(OVERPASS_URL) {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody("data=$IZMIR_OVERPASS_QUERY")
        }

        val statusCode = response.status.value

        when {
            statusCode == 429 -> throw OverpassApiException.RateLimited
            statusCode == 504 -> throw OverpassApiException.Timeout
            statusCode != 200 -> throw OverpassApiException.HttpError(statusCode)
        }

        val responseBody: String = response.body()
        return parseOverpassResponse(responseBody)
    }

    /**
     * Overpass JSON yanıtını parse eder ve element listesini döndürür.
     */
    private fun parseOverpassResponse(json: String): List<Map<String, Any?>> {
        return try {
            val jsonObject = Json.parseToJsonElement(json).jsonObject
            val elements = jsonObject["elements"]?.jsonArray ?: return emptyList()

            elements.map { element ->
                parseElement(element.jsonObject)
            }
        } catch (e: Exception) {
            throw OverpassApiException.ParseError(e.message ?: "Bilinmeyen parse hatası")
        }
    }

    /**
     * Tek bir Overpass elementini Map'e dönüştürür.
     * Recursive olarak tüm alt alanları işler.
     */
    private fun parseElement(jsonObject: JsonObject): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        for ((key, value) in jsonObject) {
            result[key] = when (value) {
                is JsonObject -> parseElement(value)
                is JsonArray -> value.map { item ->
                    when (item) {
                        is JsonObject -> parseElement(item)
                        is JsonPrimitive -> parsePrimitive(item)
                        else -> null
                    }
                }
                is JsonPrimitive -> parsePrimitive(value)
                is JsonNull -> null
            }
        }

        return result
    }

    /** JSON primitive değerlerini uygun tiplere dönüştürür */
    private fun parsePrimitive(primitive: JsonPrimitive): Any? {
        if (primitive.isString) return primitive.content
        val boolean = primitive.booleanOrNull
        if (boolean != null) return boolean
        val long = primitive.longOrNull
        if (long != null) return long
        val double = primitive.doubleOrNull
        if (double != null) return double
        return primitive.content
    }
}

/**
 * Overpass API hata türleri.
 */
sealed class OverpassApiException(message: String) : Exception(message) {
    /** Rate limit aşıldı */
    data object RateLimited : OverpassApiException("API istek limiti aşıldı. Lütfen biraz bekleyin.")

    /** Zaman aşımı */
    data object Timeout : OverpassApiException("API zaman aşımı. Lütfen tekrar deneyin.")

    /** HTTP hatası */
    data class HttpError(val code: Int) : OverpassApiException("HTTP $code hatası oluştu.")

    /** Parse hatası */
    data class ParseError(val detail: String) : OverpassApiException("Veri ayrıştırma hatası: $detail")
}
