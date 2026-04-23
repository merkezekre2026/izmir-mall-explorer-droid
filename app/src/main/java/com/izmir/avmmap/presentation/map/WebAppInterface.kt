package com.izmir.avmmap.presentation.map

import android.webkit.JavascriptInterface
import android.webkit.WebView

/**
 * WebView ile Kotlin arasındaki köprü.
 * JavaScript'ten gelen marker tıklama olaylarını ViewModel'e iletir.
 */
class WebAppInterface(
    private val onMarkerClicked: (String) -> Unit
) {
    private var webView: WebView? = null

    /** WebView referansını ayarlar */
    fun setWebView(webView: WebView) {
        this.webView = webView
    }

    /**
     * JavaScript'ten çağrılır: Marker tıklandığında.
     * @param mallId Tıklanan AVM'nin ID'si
     */
    @JavascriptInterface
    fun onMarkerClick(mallId: String) {
        onMarkerClicked(mallId)
    }

    /**
     * JavaScript'ten çağrılır: Harita hazır olduğunda.
     */
    @JavascriptInterface
    fun onMapReady() {
        // Harita hazır, veri gönderilebilir
    }

    /**
     * Kotlin'den JavaScript'e AVM marker'larını gönderir.
     * @param markersJson Marker verileri JSON formatında
     */
    fun sendMarkers(markersJson: String) {
        webView?.post {
            webView?.evaluateJavascript("addMarkers($markersJson)", null)
        }
    }

    /**
     * Kotlin'den JavaScript'e belirli bir AVM'ye odaklanma komutu gönderir.
     * @param lat Enlem
     * @param lon Boylam
     * @param zoom Yakınlaştırma seviyesi
     */
    fun focusOnMall(lat: Double, lon: Double, zoom: Int = 16) {
        webView?.post {
            webView?.evaluateJavascript("focusOnLocation($lat, $lon, $zoom)", null)
        }
    }

    /**
     * Tüm marker'ları temizler ve yeniden yükler.
     */
    fun clearAndReload(markersJson: String) {
        webView?.post {
            webView?.evaluateJavascript("clearAndReloadMarkers($markersJson)", null)
        }
    }
}
