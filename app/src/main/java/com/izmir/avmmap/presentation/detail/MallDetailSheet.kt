package com.izmir.avmmap.presentation.detail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.izmir.avmmap.R
import com.izmir.avmmap.domain.model.ShoppingMall

/**
 * AVM detay bottom sheet'i.
 * Marker tıklandığında açılan detay kartı.
 * AVM adı, adres, koordinat, website, çalışma saatleri bilgilerini gösterir.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MallDetailSheet(
    mall: ShoppingMall,
    onDismiss: () -> Unit,
    sheetState: SheetState
) {
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Başlık
            Text(
                text = mall.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Adres bilgisi
            DetailRow(
                icon = Icons.Default.LocationOn,
                label = stringResource(R.string.address),
                value = mall.displayAddress
            )

            // İlçe
            if (mall.district.isNotBlank()) {
                DetailRow(
                    icon = Icons.Default.Map,
                    label = stringResource(R.string.district),
                    value = mall.district
                )
            }

            // Koordinatlar
            DetailRow(
                icon = Icons.Default.MyLocation,
                label = stringResource(R.string.coordinates),
                value = String.format("%.6f, %.6f", mall.lat, mall.lon)
            )

            // Website
            mall.website?.let { website ->
                DetailRow(
                    icon = Icons.Default.Language,
                    label = stringResource(R.string.website),
                    value = website,
                    isClickable = true,
                    onClick = {
                        try {
                            val url = if (website.startsWith("http")) website else "https://$website"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        } catch (_: Exception) { }
                    }
                )
            }

            // Çalışma saatleri
            mall.openingHours?.let { hours ->
                DetailRow(
                    icon = Icons.Default.Schedule,
                    label = stringResource(R.string.opening_hours),
                    value = hours
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Kaynak bilgisi
            Text(
                text = stringResource(R.string.data_source),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Haritada göster butonu
            OutlinedButton(
                onClick = {
                    val uri = Uri.parse("geo:${mall.lat},${mall.lon}?q=${mall.lat},${mall.lon}(${mall.name})")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    try {
                        context.startActivity(intent)
                    } catch (_: Exception) { }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Directions, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.open_in_maps))
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Tek bir detay satırı.
 */
@Composable
fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    isClickable: Boolean = false,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isClickable) Modifier.clickable { onClick() }
                else Modifier
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isClickable) FontWeight.Medium else FontWeight.Normal,
                color = if (isClickable) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
