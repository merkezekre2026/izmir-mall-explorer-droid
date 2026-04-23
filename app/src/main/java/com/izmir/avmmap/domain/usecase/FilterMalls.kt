package com.izmir.avmmap.domain.usecase

import com.izmir.avmmap.domain.model.ShoppingMall

/**
 * AVM listesini çeşitli kriterlere göre filtreler.
 * İlçe, website varlığı ve çalışma saatine göre filtreleme destekler.
 */
object FilterMalls {

    /**
     * Verilen kriterlere göre AVM listesini filtreler.
     * @param malls Filtrelenecek liste
     * @param districtFilter İlçe filtresi (boş ise tümü gösterilir)
     * @param onlyWithWebsite Sadece website olanlar
     * @param onlyWithHours Sadece opening_hours olanlar
     * @param searchQuery Arama sorgusu (isimde aranır)
     */
    fun filter(
        malls: List<ShoppingMall>,
        districtFilter: String = "",
        onlyWithWebsite: Boolean = false,
        onlyWithHours: Boolean = false,
        searchQuery: String = ""
    ): List<ShoppingMall> {
        return malls.filter { mall ->
            // İlçe filtresi
            val districtMatch = districtFilter.isBlank() ||
                    mall.district.contains(districtFilter, ignoreCase = true)

            // Website filtresi
            val websiteMatch = !onlyWithWebsite || !mall.website.isNullOrBlank()

            // Saat filtresi
            val hoursMatch = !onlyWithHours || !mall.openingHours.isNullOrBlank()

            // Arama filtresi
            val searchMatch = searchQuery.isBlank() ||
                    mall.name.contains(searchQuery, ignoreCase = true) ||
                    mall.district.contains(searchQuery, ignoreCase = true)

            districtMatch && websiteMatch && hoursMatch && searchMatch
        }
    }

    /**
     * Verilen listedeki tüm benzersiz ilçeleri döndürür.
     * Filtre menüsü için kullanılır.
     */
    fun extractDistricts(malls: List<ShoppingMall>): List<String> {
        return malls
            .map { it.district }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
    }
}
