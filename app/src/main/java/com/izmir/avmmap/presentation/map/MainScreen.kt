package com.izmir.avmmap.presentation.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.izmir.avmmap.R
import com.izmir.avmmap.domain.model.ShoppingMall
import com.izmir.avmmap.presentation.detail.MallDetailSheet

/**
 * Ana ekran: Arama + Harita + AVM Listesi hibrit düzeni.
 * Üstte arama kutusu, ortada harita, altta collapsible liste.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MapViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showFilters by remember { mutableStateOf(false) }
    var showList by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Üst bar: Arama + Filtre butonu
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = viewModel::updateSearchQuery,
                onFilterClick = { showFilters = !showFilters },
                mallCount = uiState.mallCount,
                isUsingCache = uiState.isUsingCache
            )

            // Filtre paneli
            AnimatedVisibility(
                visible = showFilters,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                FilterPanel(
                    districts = uiState.availableDistricts,
                    selectedDistrict = uiState.districtFilter,
                    onDistrictSelected = viewModel::updateDistrictFilter,
                    onlyWithWebsite = uiState.onlyWithWebsite,
                    onlyWithHours = uiState.onlyWithHours,
                    onToggleWebsite = viewModel::toggleWebsiteFilter,
                    onToggleHours = viewModel::toggleHoursFilter,
                    onClearFilters = viewModel::clearFilters
                )
            }

            // Harita
            Box(modifier = Modifier.weight(1f)) {
                LeafletMap(
                    malls = uiState.filteredMalls,
                    selectedMall = uiState.selectedMall,
                    onMarkerClicked = viewModel::onMarkerClicked,
                    modifier = Modifier.fillMaxSize()
                )

                // Yükleme göstergesi
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // Hata mesajı
                uiState.errorMessage?.let { error ->
                    Snackbar(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp),
                        action = {
                            TextButton(onClick = { viewModel.loadMalls() }) {
                                Text(stringResource(R.string.retry))
                            }
                        }
                    ) {
                        Text(error)
                    }
                }

                // Liste toggle butonu
                FloatingActionButton(
                    onClick = { showList = !showList },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        imageVector = if (showList) Icons.Default.Map else Icons.Default.List,
                        contentDescription = if (showList) stringResource(R.string.show_map) else stringResource(R.string.show_list)
                    )
                }
            }

            // Alt liste paneli
            AnimatedVisibility(
                visible = showList,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                MallListSheet(
                    malls = uiState.filteredMalls,
                    onMallClick = { mall ->
                        viewModel.selectMall(mall)
                        showList = false
                    },
                    modifier = Modifier.heightIn(max = 300.dp)
                )
            }
        }
    }

    // Detay bottom sheet
    uiState.selectedMall?.let { mall ->
        MallDetailSheet(
            mall = mall,
            onDismiss = { viewModel.selectMall(null) },
            sheetState = sheetState
        )
    }
}

/**
 * Arama çubuğu ve üst bilgi çubuğu.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    mallCount: Int,
    isUsingCache: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
    ) {
        // Başlık
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            if (isUsingCache) {
                Icon(
                    imageVector = Icons.Default.CloudOff,
                    contentDescription = stringResource(R.string.offline_mode),
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Arama kutusu
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            placeholder = {
                Text(stringResource(R.string.search_placeholder))
            },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null)
            },
            trailingIcon = {
                Row {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.clear))
                        }
                    }
                    IconButton(onClick = onFilterClick) {
                        Icon(Icons.Default.FilterList, contentDescription = stringResource(R.string.filters))
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        // Bilgi çubuğu
        Text(
            text = stringResource(R.string.mall_count, mallCount),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
    }
}

/**
 * Filtre paneli: İlçe, website, saat filtreleri.
 */
@Composable
fun FilterPanel(
    districts: List<String>,
    selectedDistrict: String,
    onDistrictSelected: (String) -> Unit,
    onlyWithWebsite: Boolean,
    onlyWithHours: Boolean,
    onToggleWebsite: () -> Unit,
    onToggleHours: () -> Unit,
    onClearFilters: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(12.dp)
    ) {
        // İlçe filtresi
        Text(
            text = stringResource(R.string.district_filter),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))

        // İlçe seçim chips'leri
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = selectedDistrict.isEmpty(),
                onClick = { onDistrictSelected("") },
                label = { Text(stringResource(R.string.all_districts)) }
            )
        }

        // İlçe listesi (yatay scroll)
        androidx.compose.foundation.lazy.LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            items(districts) { district ->
                FilterChip(
                    selected = selectedDistrict == district,
                    onClick = {
                        onDistrictSelected(if (selectedDistrict == district) "" else district)
                    },
                    label = { Text(district, maxLines = 1) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Toggle filtreler
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FilterChip(
                selected = onlyWithWebsite,
                onClick = onToggleWebsite,
                label = { Text(stringResource(R.string.with_website)) },
                leadingIcon = if (onlyWithWebsite) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                } else null
            )
            FilterChip(
                selected = onlyWithHours,
                onClick = onToggleHours,
                label = { Text(stringResource(R.string.with_hours)) },
                leadingIcon = if (onlyWithHours) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                } else null
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Filtreleri temizle
        TextButton(onClick = onClearFilters) {
            Icon(Icons.Default.ClearAll, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(stringResource(R.string.clear_filters))
        }
    }
}

/**
 * AVM listesi: Alt panelde gösterilen kaydırılabilir liste.
 */
@Composable
fun MallListSheet(
    malls: List<ShoppingMall>,
    onMallClick: (ShoppingMall) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
    ) {
        // Liste başlığı
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Storefront,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.mall_list_title, malls.size),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        HorizontalDivider()

        // AVM listesi
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(malls, key = { it.id }) { mall ->
                MallListItem(
                    mall = mall,
                    onClick = { onMallClick(mall) }
                )
            }
        }
    }
}

/**
 * Tek bir AVM listesi öğesi.
 */
@Composable
fun MallListItem(
    mall: ShoppingMall,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // AVM ikonu
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))

        // AVM bilgileri
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = mall.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (mall.displayAddress.isNotBlank()) {
                Text(
                    text = mall.displayAddress,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Durum ikonları
        Row {
            if (!mall.website.isNullOrBlank()) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = stringResource(R.string.has_website),
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(16.dp)
                )
            }
            if (!mall.openingHours.isNullOrBlank()) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = stringResource(R.string.has_hours),
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
