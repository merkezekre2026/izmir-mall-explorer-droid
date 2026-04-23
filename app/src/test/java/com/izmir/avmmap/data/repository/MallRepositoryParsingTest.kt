package com.izmir.avmmap.data.repository

import com.izmir.avmmap.domain.model.ShoppingMall
import com.izmir.avmmap.domain.usecase.FilterMalls
import org.junit.Assert.*
import org.junit.Test

/**
 * Repository parsing ve filtreleme testleri.
 * Overpass API yanıtının doğru işlenmesini ve filtreleme mantığını test eder.
 */
class MallRepositoryParsingTest {

    @Test
    fun `filter by district should return matching malls`() {
        val malls = listOf(
            createMall("Optimum", district = "Bayraklı"),
            createMall("Forum", district = "Bornova"),
            createMall("Park", district = "Bayraklı"),
            createMall("Galleria", district = "Konak")
        )

        val result = FilterMalls.filter(malls, districtFilter = "Bayraklı")

        assertEquals(2, result.size)
        assertTrue(result.all { it.district == "Bayraklı" })
    }

    @Test
    fun `filter by website should return only malls with website`() {
        val malls = listOf(
            createMall("Optimum", website = "https://optimum.com"),
            createMall("Forum", website = null),
            createMall("Park", website = "https://park.com")
        )

        val result = FilterMalls.filter(malls, onlyWithWebsite = true)

        assertEquals(2, result.size)
        assertTrue(result.all { !it.website.isNullOrBlank() })
    }

    @Test
    fun `filter by hours should return only malls with opening hours`() {
        val malls = listOf(
            createMall("Optimum", hours = "Mo-Su 10:00-22:00"),
            createMall("Forum", hours = null),
            createMall("Park", hours = "Mo-Fr 09:00-21:00")
        )

        val result = FilterMalls.filter(malls, onlyWithHours = true)

        assertEquals(2, result.size)
        assertTrue(result.all { !it.openingHours.isNullOrBlank() })
    }

    @Test
    fun `filter by search query should match name`() {
        val malls = listOf(
            createMall("İzmir Optimum AVM"),
            createMall("Forum Bornova"),
            createMall("Optimum Outlet")
        )

        val result = FilterMalls.filter(malls, searchQuery = "Optimum")

        assertEquals(2, result.size)
        assertTrue(result.all { it.name.contains("Optimum", ignoreCase = true) })
    }

    @Test
    fun `filter by search query should match district`() {
        val malls = listOf(
            createMall("Optimum", district = "Bayraklı"),
            createMall("Forum", district = "Bornova"),
            createMall("Park", district = "Bayraklı")
        )

        val result = FilterMalls.filter(malls, searchQuery = "Bornova")

        assertEquals(1, result.size)
        assertEquals("Forum", result[0].name)
    }

    @Test
    fun `combined filters should work together`() {
        val malls = listOf(
            createMall("Optimum", district = "Bayraklı", website = "https://opt.com"),
            createMall("Forum", district = "Bayraklı", website = null),
            createMall("Park", district = "Bornova", website = "https://park.com"),
            createMall("Galleria", district = "Bayraklı", website = "https://gal.com")
        )

        val result = FilterMalls.filter(
            malls,
            districtFilter = "Bayraklı",
            onlyWithWebsite = true
        )

        assertEquals(2, result.size)
        assertTrue(result.all { it.district == "Bayraklı" && !it.website.isNullOrBlank() })
    }

    @Test
    fun `empty filter should return all malls`() {
        val malls = listOf(
            createMall("Optimum"),
            createMall("Forum"),
            createMall("Park")
        )

        val result = FilterMalls.filter(malls)

        assertEquals(3, result.size)
    }

    @Test
    fun `extractDistricts should return unique sorted districts`() {
        val malls = listOf(
            createMall("A", district = "Bayraklı"),
            createMall("B", district = "Konak"),
            createMall("C", district = "Bayraklı"),
            createMall("D", district = "Bornova"),
            createMall("E", district = "")
        )

        val districts = FilterMalls.extractDistricts(malls)

        assertEquals(3, districts.size)
        assertEquals("Bayraklı", districts[0])
        assertEquals("Bornova", districts[1])
        assertEquals("Konak", districts[2])
    }

    @Test
    fun `extractDistricts should exclude blank districts`() {
        val malls = listOf(
            createMall("A", district = ""),
            createMall("B", district = "  "),
            createMall("C", district = "Konak")
        )

        val districts = FilterMalls.extractDistricts(malls)

        assertEquals(1, districts.size)
        assertEquals("Konak", districts[0])
    }

    @Test
    fun `displayAddress should show district and address`() {
        val mall = ShoppingMall(
            id = "1",
            name = "Test",
            lat = 38.42,
            lon = 27.14,
            district = "Bayraklı",
            address = "Anadolu Cad. 12"
        )

        assertEquals("Bayraklı, Anadolu Cad. 12", mall.displayAddress)
    }

    @Test
    fun `displayAddress should show fallback when empty`() {
        val mall = ShoppingMall(
            id = "1",
            name = "Test",
            lat = 38.42,
            lon = 27.14
        )

        assertEquals("Adres bilgisi mevcut değil", mall.displayAddress)
    }

    @Test
    fun `filter should be case insensitive`() {
        val malls = listOf(
            createMall("Optimum AVM", district = "Bayraklı")
        )

        val result = FilterMalls.filter(malls, searchQuery = "optimum")

        assertEquals(1, result.size)
    }

    @Test
    fun `filter with no matches should return empty list`() {
        val malls = listOf(
            createMall("Optimum", district = "Bayraklı"),
            createMall("Forum", district = "Bornova")
        )

        val result = FilterMalls.filter(malls, districtFilter = "Konak")

        assertTrue(result.isEmpty())
    }

    /** Test için AVM oluşturucu yardımcı fonksiyon */
    private fun createMall(
        name: String,
        district: String = "",
        website: String? = null,
        hours: String? = null
    ): ShoppingMall {
        return ShoppingMall(
            id = "test_${name.hashCode()}",
            name = name,
            lat = 38.42,
            lon = 27.14,
            district = district,
            website = website,
            openingHours = hours,
            sourceType = "test"
        )
    }
}
