package com.izmir.avmmap.presentation.map

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.izmir.avmmap.IzmirAVMApp
import com.izmir.avmmap.data.network.OverpassApiException
import com.izmir.avmmap.domain.model.ShoppingMall
import com.izmir.avmmap.domain.usecase.FilterMalls
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Ana ekran ViewModel'i.
 * AVM verilerini yönetir, filtreleme ve arama işlemlerini koordine eder.
 */
class MapViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as IzmirAVMApp).container

    /** UI durumunu temsil eden data class */
    data class UiState(
        val allMalls: List<ShoppingMall> = emptyList(),
        val filteredMalls: List<ShoppingMall> = emptyList(),
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val searchQuery: String = "",
        val districtFilter: String = "",
        val onlyWithWebsite: Boolean = false,
        val onlyWithHours: Boolean = false,
        val selectedMall: ShoppingMall? = null,
        val availableDistricts: List<String> = emptyList(),
        val isUsingCache: Boolean = false,
        val mallCount: Int = 0
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadMalls()
    }

    /**
     * AVM verilerini yükler.
     * Önce cache'i kontrol eder, sonra API'den çeker.
     */
    fun loadMalls() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // Önce cache'den yükle
            val cachedMalls = container.mallRepository.getCachedMalls()
            if (cachedMalls.isNotEmpty()) {
                _uiState.update {
                    it.copy(
                        allMalls = cachedMalls,
                        isUsingCache = true,
                        mallCount = cachedMalls.size
                    )
                }
                applyFilters()
            }

            // API'den çek
            val result = container.getMallsUseCase()
            result.onSuccess { malls ->
                if (malls.isNotEmpty()) {
                    // Mapper ile dönüştürülmüş verileri cache'le
                    container.mallRepository.cacheMalls(malls)
                    _uiState.update {
                        it.copy(
                            allMalls = malls,
                            isLoading = false,
                            isUsingCache = false,
                            errorMessage = null,
                            mallCount = malls.size,
                            availableDistricts = FilterMalls.extractDistricts(malls)
                        )
                    }
                    applyFilters()
                } else if (cachedMalls.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "İzmir'de AVM bulunamadı."
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(isLoading = false)
                    }
                    applyFilters()
                }
            }.onFailure { error ->
                val errorMsg = when (error) {
                    is OverpassApiException.RateLimited -> "Çok fazla istek gönderildi. Lütfen biraz bekleyin."
                    is OverpassApiException.Timeout -> "Bağlantı zaman aşımı. Lütfen tekrar deneyin."
                    is OverpassApiException.HttpError -> "Sunucu hatası: ${error.code}"
                    is OverpassApiException.ParseError -> "Veri işlenirken hata oluştu."
                    else -> {
                        if (cachedMalls.isNotEmpty()) {
                            "İnternet bağlantısı yok. Önbellek kullanılıyor."
                        } else {
                            "Veriler yüklenirken hata oluştu: ${error.localizedMessage}"
                        }
                    }
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = if (cachedMalls.isEmpty()) errorMsg else null
                    )
                }
            }
        }
    }

    /**
     * Arama sorgusunu günceller ve filtreleri uygular.
     */
    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    /**
     * İlçe filtresini günceller.
     */
    fun updateDistrictFilter(district: String) {
        _uiState.update { it.copy(districtFilter = district) }
        applyFilters()
    }

    /**
     * Website filtresini toggler.
     */
    fun toggleWebsiteFilter() {
        _uiState.update { it.copy(onlyWithWebsite = !it.onlyWithWebsite) }
        applyFilters()
    }

    /**
     * Opening hours filtresini toggler.
     */
    fun toggleHoursFilter() {
        _uiState.update { it.copy(onlyWithHours = !it.onlyWithHours) }
        applyFilters()
    }

    /**
     * Seçili AVM'yi günceller (marker tıklaması veya liste tıklaması).
     */
    fun selectMall(mall: ShoppingMall?) {
        _uiState.update { it.copy(selectedMall = mall) }
    }

    /**
     * JS tarafından gelen marker tıklamasını işler.
     */
    fun onMarkerClicked(mallId: String) {
        val mall = _uiState.value.filteredMalls.find { it.id == mallId }
            ?: _uiState.value.allMalls.find { it.id == mallId }
        selectMall(mall)
    }

    /**
     * Mevcut filtreleri uygular.
     */
    private fun applyFilters() {
        val state = _uiState.value
        val filtered = FilterMalls.filter(
            malls = state.allMalls,
            districtFilter = state.districtFilter,
            onlyWithWebsite = state.onlyWithWebsite,
            onlyWithHours = state.onlyWithHours,
            searchQuery = state.searchQuery
        )
        _uiState.update {
            it.copy(
                filteredMalls = filtered,
                availableDistricts = FilterMalls.extractDistricts(state.allMalls)
            )
        }
    }

    /**
     * Tüm filtreleri sıfırlar.
     */
    fun clearFilters() {
        _uiState.update {
            it.copy(
                searchQuery = "",
                districtFilter = "",
                onlyWithWebsite = false,
                onlyWithHours = false
            )
        }
        applyFilters()
    }
}
