package com.izmir.avmmap.data.repository

import com.izmir.avmmap.data.cache.MallCache
import com.izmir.avmmap.data.network.OverpassApiService
import com.izmir.avmmap.data.network.OverpassApiException
import com.izmir.avmmap.domain.model.ShoppingMall
import com.izmir.avmmap.domain.repository.MallRepository

/**
 * MallRepository arayüzünün implementasyonu.
 * Overpass API'den veri çeker, hata durumlarında önbelleğe düşer.
 */
class MallRepositoryImpl(
    private val overpassApiService: OverpassApiService,
    private val cache: MallCache
) : MallRepository {

    /**
     * Overpass API'den AVM verilerini çeker.
     * Başarısız olursa önbellekten yüklemeyi dener.
     * @return Ham element listesi
     */
    override suspend fun fetchMalls(): Result<List<Map<String, Any?>>> {
        return try {
            val elements = overpassApiService.fetchIzmirMalls()
            Result.success(elements)
        } catch (e: OverpassApiException.RateLimited) {
            // Rate limit durumunda önbellekten yükle
            val cached = cache.load()
            if (cached.isNotEmpty()) {
                Result.success(emptyList()) // Boş dön, ViewModel cached kullanacak
            } else {
                Result.failure(e)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Önbellekten AVM verilerini yükler.
     */
    override suspend fun getCachedMalls(): List<ShoppingMall> {
        return cache.load()
    }

    /**
     * AVM verilerini önbelleğe kaydeder.
     */
    override suspend fun cacheMalls(malls: List<ShoppingMall>) {
        cache.save(malls)
    }
}
