package com.izmir.avmmap.presentation.map

import android.annotation.SuppressLint
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.izmir.avmmap.domain.model.ShoppingMall

private const val TAG = "LeafletMap"

/**
 * WebView tabanlı Leaflet haritası composable'ı.
 * Haritayı assets/leaflet_map.html dosyasından yükler.
 * WebView-JS bridge üzerinden marker yönetimi yapar.
 *
 * Düzeltmeler:
 * - Yerel Leaflet dosyaları kullanılır (CDN bağımlılığı kaldırıldı)
 * - WebChromeClient eklendi (JS hatalarını yakalar)
 * - Mixed content mode ayarlandı
 * - WebView yükleme durumu takip edilerek race condition düzeltildi
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

    // WebView'in yüklenip yüklenmediğini takip et
    var isWebViewReady by remember { mutableStateOf(false) }

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

                // Mixed content modu: local HTML'den remote resource yüklemeye izin ver
                settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

                // Cache modu: offline çalışmayı destekle
                settings.cacheMode = android.webkit.WebSettings.LOAD_DEFAULT

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        Log.d(TAG, "WebView page finished loading: $url")
                        isWebViewReady = true
                    }

                    override fun onReceivedError(
                        view: WebView?,
                        errorCode: Int,
                        description: String?,
                        failingUrl: String?
                    ) {
                        super.onReceivedError(view, errorCode, description, failingUrl)
                        Log.e(TAG, "WebView error: $errorCode - $description at $failingUrl")
                    }
                }

                webChromeClient = object : WebChromeClient() {
                    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                        consoleMessage?.let {
                            Log.d(
                                TAG,
                                "JS Console [${it.messageLevel()}]: ${it.message()} " +
                                    "(line ${it.lineNumber()} of ${it.sourceId()})"
                            )
                        }
                        return true
                    }

                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        super.onProgressChanged(view, newProgress)
                        if (newProgress >= 100) {
                            Log.d(TAG, "WebView progress: $newProgress%")
                        }
                    }
                }

                // JavaScript bridge'ini ekle
                addJavascriptInterface(webAppInterface, "Android")

                // Local HTML dosyasını yükle
                loadUrl("file:///android_asset/leaflet_map.html")

                webAppInterface.setWebView(this)
            }
        },
        update = { webView ->
            if (!isWebViewReady) return@AndroidView

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
