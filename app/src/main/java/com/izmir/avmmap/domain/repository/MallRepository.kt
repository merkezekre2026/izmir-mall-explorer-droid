package com.izmir.avmmap.domain.repository

import com.izmir.avmmap.domain.model.ShoppingMall

/**
 * AVM verilerine erişim için repository arayüzü.
 * Data katmanında implemente edilir.
 */
interface MallRepository {
    /**
     * Overpass API'den AVM verilerini çeker.
     * @return Ham element listesi (Map<String, Any?> formatında)
     */
    suspend fun fetchMalls(): Result<List<Map<String, Any?>>>

    /**
     * Önbellekten AVM verilerini yükler.
     * @return Önbellekteki AVM listesi veya boş liste
     */
    suspend fun getCachedMalls(): List<ShoppingMall>

    /**
     * AVM verilerini önbelleğe kaydeder.
     */
    suspend fun cacheMalls(malls: List<ShoppingMall>)
}
