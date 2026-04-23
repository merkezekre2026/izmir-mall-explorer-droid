package com.izmir.avmmap.domain.usecase

import com.izmir.avmmap.domain.model.ShoppingMall
import org.junit.Assert.*
import org.junit.Test

/**
 * Deduplication unit testleri.
 * Tekrarlayan AVM kayıtlarının doğru temizlenmesini test eder.
 */
class DeduplicationTest {

    @Test
    fun `deduplicate should remove exact duplicates`() {
        val malls = listOf(
            createMall("AVM 1", 38.4237, 27.1428),
            createMall("AVM 1", 38.4237, 27.1428),
            createMall("AVM 2", 38.4192, 27.1287)
        )

        val result = Deduplication.deduplicate(malls)

        assertEquals(2, result.size)
        assertEquals("AVM 1", result[0].name)
        assertEquals("AVM 2", result[1].name)
    }

    @Test
    fun `deduplicate should keep different malls`() {
        val malls = listOf(
            createMall("Optimum AVM", 38.4237, 27.1428),
            createMall("Forum Bornova", 38.4192, 27.1287),
            createMall("İzmir Park", 38.4350, 27.1500)
        )

        val result = Deduplication.deduplicate(malls)

        assertEquals(3, result.size)
    }

    @Test
    fun `deduplicate should handle empty list`() {
        val result = Deduplication.deduplicate(emptyList())
        assertTrue(result.isEmpty())
    }

    @Test
    fun `deduplicate should handle single item`() {
        val malls = listOf(createMall("Tek AVM", 38.42, 27.14))
        val result = Deduplication.deduplicate(malls)
        assertEquals(1, result.size)
    }

    @Test
    fun `areSimilar should return true for same coordinates and similar names`() {
        val mall1 = createMall("İzmir Optimum AVM", 38.4237, 27.1428)
        val mall2 = createMall("İzmir Optimum AVM", 38.4238, 27.1429)

        assertTrue(Deduplication.areSimilar(mall1, mall2))
    }

    @Test
    fun `areSimilar should return false for different coordinates`() {
        val mall1 = createMall("Optimum AVM", 38.4237, 27.1428)
        val mall2 = createMall("Optimum AVM", 38.5000, 27.2000)

        assertFalse(Deduplication.areSimilar(mall1, mall2))
    }

    @Test
    fun `areSimilar should return false for different names at same location`() {
        val mall1 = createMall("Optimum AVM", 38.4237, 27.1428)
        val mall2 = createMall("Forum Bornova", 38.4237, 27.1428)

        assertFalse(Deduplication.areSimilar(mall1, mall2))
    }

    @Test
    fun `areSimilar should detect partial name match`() {
        val mall1 = createMall("İzmir Optimum Alışveriş Merkezi", 38.4237, 27.1428)
        val mall2 = createMall("İzmir Optimum", 38.4237, 27.1428)

        assertTrue(Deduplication.areSimilar(mall1, mall2))
    }

    @Test
    fun `deduplicate should keep first occurrence`() {
        val malls = listOf(
            createMall("Birinci", 38.4237, 27.1428),
            createMall("Birinci", 38.4237, 27.1428),
            createMall("Birinci", 38.4237, 27.1428)
        )

        val result = Deduplication.deduplicate(malls)

        assertEquals(1, result.size)
        assertEquals("Birinci", result[0].name)
    }

    @Test
    fun `deduplicate should handle large list efficiently`() {
        val malls = (1..1000).map { i ->
            createMall("AVM $i", 38.42 + (i * 0.001), 27.14 + (i * 0.001))
        }

        val result = Deduplication.deduplicate(malls)

        // Tümü farklı koordinatlarda, hepsi korunmalı
        assertEquals(1000, result.size)
    }

    @Test
    fun `deduplicate should handle case insensitive names`() {
        val malls = listOf(
            createMall("OPTIMUM AVM", 38.4237, 27.1428),
            createMall("optimum avm", 38.4237, 27.1428)
        )

        val result = Deduplication.deduplicate(malls)

        assertEquals(1, result.size)
    }

    /** Test için AVM oluşturucu yardımcı fonksiyon */
    private fun createMall(name: String, lat: Double, lon: Double): ShoppingMall {
        return ShoppingMall(
            id = "test_${name.hashCode()}",
            name = name,
            lat = lat,
            lon = lon,
            district = "Test İlçe",
            sourceType = "test"
        )
    }
}
