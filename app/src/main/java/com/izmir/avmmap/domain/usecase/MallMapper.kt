package com.izmir.avmmap.domain.usecase

import com.izmir.avmmap.domain.model.ShoppingMall

/**
 * Overpass API'den gelen ham JSON verisini domain modeline dönüştürür.
 * Farklı OSM element türlerini (node, way, relation) tek bir modele normalize eder.
 */
object MallMapper {

    /**
     * Overpass JSON yanıtındaki bir elementi ShoppingMall modeline dönüştürür.
     * @param element Overpass JSON elementi (Map olarak parse edilmiş)
     * @return Dönüştürülmüş ShoppingMall veya null (gerekli alanlar eksikse)
     */
    fun mapElement(element: Map<String, Any?>): ShoppingMall? {
        val tags = element["tags"] as? Map<String, Any?> ?: return null
        val id = element["id"] as? Number ?: return null
        val type = element["type"] as? String ?: "node"

        // İsim: name, name:tr veya fallback
        val name = extractName(tags)

        // Koordinatları al
        val lat: Double
        val lon: Double

        when (type) {
            "node" -> {
                lat = (element["lat"] as? Number)?.toDouble() ?: return null
                lon = (element["lon"] as? Number)?.toDouble() ?: return null
            }
            "way" -> {
                // Way'ler için center bilgisi Overpass tarafından sağlanır
                val center = element["center"] as? Map<String, Any?> ?: return null
                lat = (center["lat"] as? Number)?.toDouble() ?: return null
                lon = (center["lon"] as? Number)?.toDouble() ?: return null
            }
            "relation" -> {
                // Relation'lar için center bilgisi Overpass tarafından sağlanır
                val center = element["center"] as? Map<String, Any?> ?: return null
                lat = (center["lat"] as? Number)?.toDouble() ?: return null
                lon = (center["lon"] as? Number)?.toDouble() ?: return null
            }
            else -> return null
        }

        // İlçe bilgisini çıkar
        val district = extractDistrict(tags)

        // Adres bilgisini çıkar
        val address = extractAddress(tags)

        return ShoppingMall(
            id = "${type}_${id.toLong()}",
            name = name,
            lat = lat,
            lon = lon,
            district = district,
            address = address,
            website = tags["website"] as? String,
            openingHours = tags["opening_hours"] as? String,
            sourceType = "openstreetmap",
            osmType = type,
            osmId = id.toLong()
        )
    }

    /**
     * Birden fazla elementi toplu olarak dönüştürür ve null olanları filtreler.
     */
    fun mapElements(elements: List<Map<String, Any?>>): List<ShoppingMall> {
        return elements.mapNotNull { mapElement(it) }
    }

    /** İsim çıkarma mantığı: name > name:tr > fallback */
    private fun extractName(tags: Map<String, Any?>): String {
        val name = tags["name"] as? String
        if (!name.isNullOrBlank()) return name.trim()

        val nameTr = tags["name:tr"] as? String
        if (!nameTr.isNullOrBlank()) return nameTr.trim()

        // Brand bilgisini fallback olarak kullan
        val brand = tags["brand"] as? String
        if (!brand.isNullOrBlank()) return brand.trim()

        return "İsimsiz AVM kaydı"
    }

    /** İlçe bilgisini addr:district veya addr:city'den çıkar */
    private fun extractDistrict(tags: Map<String, Any?>): String {
        val district = tags["addr:district"] as? String
        if (!district.isNullOrBlank()) return district.trim()

        val suburb = tags["suburb"] as? String
        if (!suburb.isNullOrBlank()) return suburb.trim()

        val cityDistrict = tags["addr:city"] as? String
        if (!cityDistrict.isNullOrBlank()) return cityDistrict.trim()

        return ""
    }

    /** Adres bilgisini birleştirir */
    private fun extractAddress(tags: Map<String, Any?>): String {
        val parts = mutableListOf<String>()

        val street = tags["addr:street"] as? String
        if (!street.isNullOrBlank()) parts.add(street.trim())

        val housenumber = tags["addr:housenumber"] as? String
        if (!housenumber.isNullOrBlank()) parts.add(housenumber.trim())

        val postcode = tags["addr:postcode"] as? String
        if (!postcode.isNullOrBlank()) parts.add(postcode.trim())

        return parts.joinToString(" ")
    }
}
