package com.izmir.avmmap.domain.usecase

import com.izmir.avmmap.domain.model.ShoppingMall
import org.junit.Assert.*
import org.junit.Test

/**
 * MallMapper unit testleri.
 * Overpass API elementlerinin doğru dönüştürülmesini test eder.
 */
class MallMapperTest {

    @Test
    fun `mapElement with valid node should return ShoppingMall`() {
        val element = mapOf(
            "type" to "node",
            "id" to 123456789L,
            "lat" to 38.4237,
            "lon" to 27.1428,
            "tags" to mapOf(
                "name" to "İzmir Optimum AVM",
                "shop" to "mall",
                "addr:district" to "Bayraklı",
                "addr:street" to "Anadolu Caddesi",
                "addr:housenumber" to "12",
                "website" to "https://optimum.com.tr",
                "opening_hours" to "Mo-Su 10:00-22:00"
            )
        )

        val result = MallMapper.mapElement(element)

        assertNotNull(result)
        assertEquals("node_123456789", result!!.id)
        assertEquals("İzmir Optimum AVM", result.name)
        assertEquals(38.4237, result.lat, 0.0001)
        assertEquals(27.1428, result.lon, 0.0001)
        assertEquals("Bayraklı", result.district)
        assertEquals("Anadolu Caddesi 12", result.address)
        assertEquals("https://optimum.com.tr", result.website)
        assertEquals("Mo-Su 10:00-22:00", result.openingHours)
        assertEquals("node", result.osmType)
        assertEquals(123456789L, result.osmId)
    }

    @Test
    fun `mapElement with valid way should use center coordinates`() {
        val element = mapOf(
            "type" to "way",
            "id" to 987654321L,
            "center" to mapOf(
                "lat" to 38.4192,
                "lon" to 27.1287
            ),
            "tags" to mapOf(
                "name" to "Forum Bornova",
                "shop" to "mall"
            )
        )

        val result = MallMapper.mapElement(element)

        assertNotNull(result)
        assertEquals("way_987654321", result!!.id)
        assertEquals("Forum Bornova", result.name)
        assertEquals(38.4192, result.lat, 0.0001)
        assertEquals(27.1287, result.lon, 0.0001)
        assertEquals("way", result.osmType)
    }

    @Test
    fun `mapElement with valid relation should use center coordinates`() {
        val element = mapOf(
            "type" to "relation",
            "id" to 111222333L,
            "center" to mapOf(
                "lat" to 38.4350,
                "lon" to 27.1500
            ),
            "tags" to mapOf(
                "name" to "İzmir Park AVM",
                "shop" to "mall"
            )
        )

        val result = MallMapper.mapElement(element)

        assertNotNull(result)
        assertEquals("relation_111222333", result!!.id)
        assertEquals("İzmir Park AVM", result.name)
        assertEquals("relation", result.osmType)
    }

    @Test
    fun `mapElement with missing name should use brand as fallback`() {
        val element = mapOf(
            "type" to "node",
            "id" to 111L,
            "lat" to 38.42,
            "lon" to 27.14,
            "tags" to mapOf(
                "brand" to "Kipa",
                "shop" to "mall"
            )
        )

        val result = MallMapper.mapElement(element)

        assertNotNull(result)
        assertEquals("Kipa", result!!.name)
    }

    @Test
    fun `mapElement with no name or brand should use fallback name`() {
        val element = mapOf(
            "type" to "node",
            "id" to 222L,
            "lat" to 38.42,
            "lon" to 27.14,
            "tags" to mapOf(
                "shop" to "mall"
            )
        )

        val result = MallMapper.mapElement(element)

        assertNotNull(result)
        assertEquals("İsimsiz AVM kaydı", result!!.name)
    }

    @Test
    fun `mapElement with missing tags should return null`() {
        val element = mapOf(
            "type" to "node",
            "id" to 333L,
            "lat" to 38.42,
            "lon" to 27.14
        )

        val result = MallMapper.mapElement(element)
        assertNull(result)
    }

    @Test
    fun `mapElement with missing coordinates should return null`() {
        val element = mapOf(
            "type" to "node",
            "id" to 444L,
            "tags" to mapOf("name" to "Test AVM")
        )

        val result = MallMapper.mapElement(element)
        assertNull(result)
    }

    @Test
    fun `mapElement with name tr should use Turkish name`() {
        val element = mapOf(
            "type" to "node",
            "id" to 555L,
            "lat" to 38.42,
            "lon" to 27.14,
            "tags" to mapOf(
                "name:tr" to "Türkçe İsim AVM",
                "shop" to "mall"
            )
        )

        val result = MallMapper.mapElement(element)

        assertNotNull(result)
        assertEquals("Türkçe İsim AVM", result!!.name)
    }

    @Test
    fun `mapElements should filter out null elements`() {
        val elements = listOf(
            mapOf(
                "type" to "node",
                "id" to 1L,
                "lat" to 38.42,
                "lon" to 27.14,
                "tags" to mapOf("name" to "Geçerli AVM", "shop" to "mall")
            ),
            mapOf(
                "type" to "node",
                "id" to 2L
                // tags eksik - null dönmeli
            ),
            mapOf(
                "type" to "way",
                "id" to 3L,
                "center" to mapOf("lat" to 38.43, "lon" to 27.15),
                "tags" to mapOf("name" to "Başka AVM", "shop" to "mall")
            )
        )

        val result = MallMapper.mapElements(elements)

        assertEquals(2, result.size)
        assertEquals("Geçerli AVM", result[0].name)
        assertEquals("Başka AVM", result[1].name)
    }

    @Test
    fun `mapElement should extract district from suburb when addr district missing`() {
        val element = mapOf(
            "type" to "node",
            "id" to 666L,
            "lat" to 38.42,
            "lon" to 27.14,
            "tags" to mapOf(
                "name" to "Test AVM",
                "shop" to "mall",
                "suburb" to "Karşıyaka"
            )
        )

        val result = MallMapper.mapElement(element)

        assertNotNull(result)
        assertEquals("Karşıyaka", result!!.district)
    }

    @Test
    fun `toMarkerJson should produce valid JSON-like string`() {
        val mall = ShoppingMall(
            id = "node_1",
            name = "Test AVM",
            lat = 38.42,
            lon = 27.14,
            district = "Konak",
            website = "https://test.com"
        )

        val json = mall.toMarkerJson()

        assertTrue(json.contains("\"id\": \"node_1\""))
        assertTrue(json.contains("\"name\": \"Test AVM\""))
        assertTrue(json.contains("\"lat\": 38.42"))
        assertTrue(json.contains("\"lon\": 27.14"))
        assertTrue(json.contains("\"district\": \"Konak\""))
        assertTrue(json.contains("\"website\": \"https://test.com\""))
    }
}
