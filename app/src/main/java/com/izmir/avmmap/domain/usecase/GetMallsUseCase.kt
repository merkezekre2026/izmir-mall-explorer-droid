package com.izmir.avmmap.domain.usecase

import com.izmir.avmmap.domain.model.ShoppingMall
import com.izmir.avmmap.domain.repository.MallRepository

/**
 * AVM verilerini repository'den çeken ve işleyen use case.
 * Veri çekme, haritalama ve deduplication işlemlerini koordine eder.
 */
class GetMallsUseCase(
    private val repository: MallRepository
) {
    /**
     * İzmir'deki tüm AVM'leri çeker, dönüştürür ve temizler.
     * @return Filtrelenmiş ve deduplicate edilmiş AVM listesi
     */
    suspend operator fun invoke(): Result<List<ShoppingMall>> {
        return try {
            val result = repository.fetchMalls()
            result.map { malls ->
                val mapped = MallMapper.mapElements(malls)
                Deduplication.deduplicate(mapped)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
