# İzmir AVM Haritası (IzmirAVMMap)

İzmir'deki tüm alışveriş merkezlerini interaktif harita üzerinde gösteren modern bir Android uygulaması.

## 📱 Özellikler

- **İnteraktif Harita**: Leaflet tabanlı, marker clustering destekli harita
- **Gerçek Zamanlı Veri**: OpenStreetMap + Overpass API üzerinden canlı AVM verisi
- **Arama**: AVM adına göre hızlı arama
- **Filtreleme**: İlçe, website varlığı, çalışma saatine göre filtreleme
- **Detay Kartı**: AVM bilgileri, adres, website, çalışma saatleri
- **Çevrimdışı Destek**: Son başarılı veri önbellekte saklanır
- **Koyu Tema**: Sistem ayarlarına göre otomatik tema değişimi
- **Türkçe Arayüz**: Tamamen Türkçe kullanıcı deneyimi

## 🏗️ Mimari

```
Clean-ish MVVM mimarisi:
├── domain/          → İş mantığı, modeller, use case'ler
├── data/            → API servisleri, önbellek, repository
├── presentation/    → Compose ekranları, ViewModel'ler
└── webmap/          → Leaflet HTML/JS assetleri
```

## 🛠️ Teknolojiler

| Bileşen | Teknoloji |
|---------|-----------|
| Dil | Kotlin |
| UI | Jetpack Compose + Material3 |
| Harita | WebView + Leaflet.js |
| Veri | Overpass API (OpenStreetMap) |
| Networking | Ktor Client (OkHttp engine) |
| Serialization | Kotlinx Serialization |
| State | ViewModel + StateFlow |
| Önbellek | SharedPreferences |
| Test | JUnit 4 |

## 🚀 Kurulum

### Ön Koşullar
- Android Studio Ladybug (2024.2.1) veya üzeri
- JDK 17
- Android SDK 35
- İnternet bağlantısı (ilk çalışma için)

### Adımlar

1. **Projeyi klonlayın:**
   ```bash
   git clone <repo-url>
   cd IzmirAVMMap
   ```

2. **Android Studio'da açın:**
   - Android Studio > Open > Proje klasörünü seçin
   - Gradle sync'in tamamlanmasını bekleyin

3. **Çalıştırın:**
   - Bir cihat veya emülatör seçin
   - ▶️ butonuna tıklayın

### Emülatör Kurulumu
- AVD Manager > Create Virtual Device
- Pixel 7 veya üzeri önerilir
- Google Play Services gerekmez (harita internet üzerinden yüklenir)

## 📋 Neden Overpass API?

| Kriter | Overpass API | Nominatim | Manuel Veri |
|--------|-------------|-----------|-------------|
| Toplu sorgu | ✅ Tek sorgu ile tüm AVM'ler | ❌ Yalnızca geocoding | ✅ |
| Gerçek zamanlı | ✅ OSM canlı verisi | ✅ | ❌ Güncellenmez |
| Ücretsiz | ✅ | ✅ | ✅ |
| Rate limit | ⚠️ Var ama makul | ⚠️ Var | Yok |
| Veri kalitesi | ✅ Topluluk doğrulaması | ✅ | ⚠️ Bakım gerekir |

Overpass API, OpenStreetMap verilerini sorgulamak için en uygun araçtır. Tek bir sorgu ile İzmir sınırları içindeki tüm AVM'leri çekebiliriz.

## ⚡ Rate Limit ve Cache Stratejisi

### Rate Limit
- Overpass API: ~100 istek/saat (anonim)
- Uygulama: İlk açılışta tek istek, ardından cache
- 429 hatası alındığında otomatik olarak cache'e düşülür

### Cache Stratejisi
- **Süre**: 24 saat geçerli
- **Depolama**: SharedPreferences (hafif, Room gerektirmez)
- **Davranış**:
  1. Uygulama açılır → Cache'den yükle (hızlı gösterim)
  2. Arka planda API'den çek → Güncelle
  3. Ağ yoksa → Sadece cache
  4. API hatası → Cache varsa sessizce kullan

## 🗺️ Overpass Sorgu Mantığı

```
1. İzmir area'sını bul: area["name"="İzmir"]["admin_level"="4"]
2. İçinde ara:
   - shop=mall (node, way, relation)
   - building=retail + name~AVM|Alışveriş Merkezi|Mall
3. Sonuçları normalize et
4. Tekrarlayan kayıtları temizle
```

## 🧪 Testler

```bash
# Tüm testleri çalıştır
./gradlew test

# Tek test sınıfı
./gradlew test --tests "com.izmir.avmmap.domain.usecase.MallMapperTest"
./gradlew test --tests "com.izmir.avmmap.domain.usecase.DeduplicationTest"
./gradlew test --tests "com.izmir.avmmap.data.repository.MallRepositoryParsingTest"
```

### Test Kapsamı
- **MallMapperTest**: Overpass element → ShoppingMall dönüşümü (12 test)
- **DeduplicationTest**: Tekrar kayıtları temizleme (10 test)
- **MallRepositoryParsingTest**: Filtreleme ve parsing (14 test)

## ⚠️ Bilinen Kısıtlar

1. **İlk yükleme gecikmesi**: Overpass API 10-30 saniye sürebilir
2. **Rate limit**: Yoğun kullanımda geçici engellenebilir
3. **Way/Relation koordinatları**: Overpass `center` alanı kullanılır, tam sınır polygonu değil
4. **İlçe bilgisi**: OSM tag'lerinden türetilir, her AVM'de bulunmayabilir
5. **Adres eksikliği**: Bazı OSM kayıtlarında addr bilgisi olmayabilir
6. **Offline harita**: Tile'lar internet gerektirir, tam offline destek yoktur
7. **Konum izni**: Opsiyonel, verilmezse sadece harita gösterilir

## 🔮 Gelecek Özellikler

- [ ] **Yol tarifi**: Google Maps / OSRM entegrasyonu ile yön tarifi
- [ ] **Favoriler**: Kullanıcının sık ziyaret ettiği AVM'leri kaydetme
- [ ] **Açık/Kapalı tahmini**: Çalışma saatine göre gerçek zamanlı durum
- [ ] **Harici browser**: Website linklerini varsayılan tarayıcıda açma
- [ ] **Harita stilleri**:不同 harita katmanları (uydu, terrain)
- [ ] **Bildirimler**: Yakındaki AVM'ler için bildirim
- [ ] **Paylaşım**: AVM bilgilerini paylaşma
- [ ] **Değerlendirme**: Kullanıcı yorumları ve puanlama
- [ ] **Etkinlikler**: AVM etkinlikleri ve kampanyalar
- [ ] **Widget**: Ana ekran widget'ı ile yakın AVM'ler

## 📄 Lisans

Bu proje OpenStreetMap verilerini kullanır. OSM verileri [Open Database License](https://opendatacommons.org/licenses/odbl/) altında lisanslanmıştır.

```
© OpenStreetMap contributors
```

## � Katkıda Bulunma

1. Fork yapın
2. Feature branch oluşturun (`git checkout -b feature/yeni-ozellik`)
3. Değişikliklerinizi commit edin
4. Push yapın ve Pull Request açın

---

**İzmir AVM Haritası** — İzmir'deki alışveriş merkezlerini keşfedin! 🛍️
