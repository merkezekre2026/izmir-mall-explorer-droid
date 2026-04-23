/**
 * Leaflet Bridge - Android WebView ile iletişim köprüsü.
 * Kotlin'den gelen marker verilerini haritada gösterir.
 * Marker tıklamalarını Android tarafına iletir.
 */

// İzmir merkez koordinatları
var IZMIR_CENTER = [38.4237, 27.1428];
var DEFAULT_ZOOM = 12;

// Harita ve katman referansları
var map = null;
var markersLayer = null;
var selectedMarker = null;
var allMarkers = {};
var userLocationMarker = null;

/**
 * Haritayı başlatır.
 */
function initMap() {
    map = L.map('map', {
        center: IZMIR_CENTER,
        zoom: DEFAULT_ZOOM,
        zoomControl: true,
        attributionControl: true
    });

    // OSM tile layer
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
        maxZoom: 19
    }).addTo(map);

    // Marker cluster grubu
    markersLayer = L.markerClusterGroup({
        maxClusterRadius: 50,
        spiderfyOnMaxZoom: true,
        showCoverageOnHover: false,
        zoomToBoundsOnClick: true,
        disableClusteringAtZoom: 16
    });
    map.addLayer(markersLayer);

    // Android bridge hazır bildirimi
    if (typeof Android !== 'undefined') {
        Android.onMapReady();
    }
}

/**
 * Marker'ları haritaya ekler.
 * @param {Array} markers - AVM marker dizisi
 */
function addMarkers(markers) {
    if (!map || !markersLayer) {
        // Harita henüz hazır değilse, biraz bekle
        setTimeout(function() { addMarkers(markers); }, 500);
        return;
    }

    // Mevcut marker'ları temizle
    markersLayer.clearLayers();
    allMarkers = {};

    markers.forEach(function(mall) {
        var icon = L.divIcon({
            className: 'mall-marker',
            iconSize: [12, 12],
            iconAnchor: [6, 6]
        });

        var marker = L.marker([mall.lat, mall.lon], { icon: icon });

        // Popup içeriği
        var popupContent = '<div class="mall-popup">';
        popupContent += '<h3>' + escapeHtml(mall.name) + '</h3>';
        if (mall.district) {
            popupContent += '<p>📍 ' + escapeHtml(mall.district) + '</p>';
        }
        if (mall.address) {
            popupContent += '<p>🏠 ' + escapeHtml(mall.address) + '</p>';
        }
        if (mall.openingHours) {
            popupContent += '<p>🕐 ' + escapeHtml(mall.openingHours) + '</p>';
        }
        if (mall.website) {
            popupContent += '<a class="popup-link" href="' + escapeHtml(mall.website) + '" target="_blank">🌐 Website</a>';
        }
        popupContent += '</div>';

        marker.bindPopup(popupContent, { maxWidth: 250 });

        // Tıklama olayı
        marker.on('click', function() {
            onMarkerClick(mall.id);
        });

        markersLayer.addLayer(marker);
        allMarkers[mall.id] = marker;
    });

    // Tüm marker'ları ekrana sığdır
    if (markers.length > 0) {
        fitAllMarkers();
    }
}

/**
 * Tüm marker'ları ekrana sığdırır.
 */
function fitAllMarkers() {
    if (markersLayer && markersLayer.getLayers().length > 0) {
        var bounds = markersLayer.getBounds();
        if (bounds.isValid()) {
            map.fitBounds(bounds, { padding: [50, 50], maxZoom: 14 });
        }
    }
}

/**
 * Belirli bir konuma odaklanır.
 * @param {number} lat - Enlem
 * @param {number} lon - Boylam
 * @param {number} zoom - Yakınlaştırma seviyesi
 */
function focusOnLocation(lat, lon, zoom) {
    if (!map) return;

    map.setView([lat, lon], zoom || 16);

    // Seçili marker'ı vurgula
    if (selectedMarker) {
        var prevIcon = L.divIcon({
            className: 'mall-marker',
            iconSize: [12, 12],
            iconAnchor: [6, 6]
        });
        selectedMarker.setIcon(prevIcon);
    }

    // Yeni marker'ı seç
    var markerId = null;
    for (var id in allMarkers) {
        var m = allMarkers[id];
        var latlng = m.getLatLng();
        if (Math.abs(latlng.lat - lat) < 0.0001 && Math.abs(latlng.lng - lon) < 0.0001) {
            markerId = id;
            break;
        }
    }

    if (markerId && allMarkers[markerId]) {
        selectedMarker = allMarkers[markerId];
        var selectedIcon = L.divIcon({
            className: 'mall-marker selected',
            iconSize: [16, 16],
            iconAnchor: [8, 8]
        });
        selectedMarker.setIcon(selectedIcon);
        selectedMarker.openPopup();
    }
}

/**
 * Tüm marker'ları temizler ve yenilerini ekler.
 * @param {Array} markers - Yeni marker dizisi
 */
function clearAndReloadMarkers(markers) {
    if (markersLayer) {
        markersLayer.clearLayers();
    }
    allMarkers = {};
    selectedMarker = null;
    addMarkers(markers);
}

/**
 * Kullanıcı konumunu haritada gösterir.
 * @param {number} lat - Enlem
 * @param {number} lon - Boylam
 */
function showUserLocation(lat, lon) {
    if (!map) return;

    if (userLocationMarker) {
        map.removeLayer(userLocationMarker);
    }

    var icon = L.divIcon({
        className: 'mall-marker user-location',
        iconSize: [14, 14],
        iconAnchor: [7, 7]
    });

    userLocationMarker = L.marker([lat, lon], { icon: icon })
        .addTo(map)
        .bindPopup('📍 Konumunuz');
}

/**
 * Marker tıklama olayını Android'e iletir.
 * @param {string} mallId - Tıklanan AVM ID'si
 */
function onMarkerClick(mallId) {
    if (typeof Android !== 'undefined') {
        Android.onMarkerClick(mallId);
    }
}

/**
 * HTML karakterlerini escape eder.
 * @param {string} text - Escape edilecek metin
 * @returns {string} Escape edilmiş metin
 */
function escapeHtml(text) {
    if (!text) return '';
    var div = document.createElement('div');
    div.appendChild(document.createTextNode(text));
    return div.innerHTML;
}

// Haritayı başlat
document.addEventListener('DOMContentLoaded', initMap);

// DOM hazır değilse hemen başlat
if (document.readyState === 'complete' || document.readyState === 'interactive') {
    initMap();
}
