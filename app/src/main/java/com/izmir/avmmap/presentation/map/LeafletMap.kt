package com.izmir.avmmap.presentation.map

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.izmir.avmmap.domain.model.ShoppingMall

/**
 * WebView tabanlı Leaflet haritası composable'ı.
 * Haritayı assets/leaflet_map.html dosyasından yükler.
 * WebView-JS bridge üzerinden marker yönetimi yapar.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LeafletMap(
    malls: List<ShoppingMall>,
    selectedMall: ShoppingMall?,
    onMarkerClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val webAppInterface = remember { WebAppInterface(onMarkerClicked) }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowFileAccess = true
                settings.allowContentAccess = true
                settings.setSupportZoom(true)
                settings.builtInZoomControls = true
                settings.displayZoomControls = false

                webViewClient = WebViewClient()

                // JavaScript bridge'ini ekle
                addJavascriptInterface(webAppInterface, "Android")

                // Local HTML dosyasını yükle
                loadUrl("file:///android_asset/leaflet_map.html")

                webAppInterface.setWebView(this)
            }
        },
        update = { webView ->
            // Marker verilerini JSON formatında hazırla
            val markersArray = malls.joinToString(",") { it.toMarkerJson() }
            val markersJson = "[$markersArray]"

            // Harita yüklendiyse marker'ları gönder
            webView.evaluateJavascript(
                "if(typeof addMarkers === 'function') addMarkers($markersJson);",
                null
            )

            // Seçili AVM varsa odaklan
            selectedMall?.let { mall ->
                webView.evaluateJavascript(
                    "if(typeof focusOnLocation === 'function') focusOnLocation(${mall.lat}, ${mall.lon}, 16);",
                    null
                )
            }
        }
    )
}
