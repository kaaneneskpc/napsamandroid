# Play Data Safety (Veri güvenliği) formu — taslak

Durum: 16.09.2026 kararıyla (sunucu yayında, AI kapalı) hazırlandı. Play Console → Uygulama içeriği → Veri
güvenliği. Uygulamanın davranışı değişirse (AI açılması, analiz/çökme aracı eklenmesi) form **yayından önce**
güncellenir.

## Uygulamanın gerçek veri davranışı (kaynak: kod)

| Veri | Nerede | Cihazdan çıkıyor mu |
|---|---|---|
| Kaydedilen / geçilen / "yapıyorum" denen öneriler | Room veritabanı, cihazda | Hayır. Bulut yedeği kapalı (`allowBackup="false"`, `data_extraction_rules.xml`); yalnızca kullanıcının başlattığı cihazdan cihaza doğrudan aktarımda yeni telefona taşınır |
| Tema tercihi, bütçe tablosu önbelleği | DataStore, cihazda | Hayır |
| "Yaz" alanına yazılan metin | Bellekte, yerel ayrıştırıcıda | Hayır (AI istemcisi uygulamada yok) |
| Ağ istekleri | `GET /api/v1/config/` (en fazla 12 saatte bir), `GET /api/v1/content/` (içerik sürümü değişince) | İsteklerde kimlik, çerez, kullanıcı verisi yok. IP adresi her bağlantıda olduğu gibi sunucuya (Vercel, Frankfurt) ulaşır; NAPSAM bunu saklamaz, barındırma sağlayıcısı yalnızca hizmeti sunmak ve hız sınırı için işler |
| Konum, reklam kimliği, cihaz kimliği, kişi/fotoğraf/dosya | Hiç erişilmez | — |

İzinler (birleşik sürüm manifesti): `INTERNET` ve androidx'in imza düzeyindeki
`DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`. Reklam, analiz, çökme raporlama ya da ödeme SDK'sı yok.

## Formdaki yanıtlar

### Veri toplama ve paylaşma

| Soru | Yanıt |
|---|---|
| Uygulamanız zorunlu kullanıcı veri türlerinden herhangi birini topluyor veya paylaşıyor mu? | **Hayır** |

Gerekçe: Google'ın tanımında "toplama", verinin uygulamadan cihaz dışına iletilmesidir. Uygulama hiçbir veri
türünü (kişisel bilgi, uygulama etkinliği, kimlik vb.) cihaz dışına göndermiyor; kayıtlar yalnızca cihazda
duruyor.

> **Karar noktası — IP adresi:** Ağ isteklerinde IP adresinin sunucuya ulaşması, uygulamanın bir veri türünü
> toplaması olarak değil, bağlantının kendisi olarak değerlendirildi; NAPSAM IP'yi kaydetmiyor, konum
> çıkarmıyor. Vercel'in güvenlik ve hız sınırı için yaptığı işleme ileride NAPSAM tarafından kayıt/analiz
> amacıyla kullanılırsa bu değerlendirme değişir ("Konum → yaklaşık konum" ya da "Cihaz veya diğer
> kimlikler" beyanı gerekebilir). Formu göndermeden önce Play'in güncel yardım metniyle bir kez daha
> karşılaştırılmalı.

"Hayır" yanıtıyla aşağıdaki bölümler form tarafından istenmez; yine de mağaza sayfasında doğru görünmesi için
bilgiler:

| Konu | Durum |
|---|---|
| Aktarım sırasında şifreleme | Tüm trafik HTTPS; şifresiz trafik Network Security Config ile kapalı |
| Veri silme talebi | Sunucuda kullanıcıya ait kayıt yok; yerel veriler sistem ayarlarından ya da uygulamayı kaldırarak silinir |
| Bağımsız güvenlik incelemesi | Yok |
| Aile politikası | Uygulama 13 yaş altını hedeflemiyor |

## Kullanılan kütüphaneler ve veri davranışları

B16: kullanılan her üçüncü taraf SDK hesaba katılır. Hiçbiri veri toplamıyor veya cihaz dışına göndermiyor:

| Kütüphane | Ne için | Veri |
|---|---|---|
| Jetpack Compose, Navigation 3, Lifecycle, Activity, Core, Splash Screen | Arayüz | Yok |
| Room, DataStore | Cihazda saklama | Yalnızca cihazda |
| Hilt (Dagger) | Bağımlılık enjeksiyonu | Yok |
| Ktor (Android motoru), kotlinx.serialization | Config ve içerik indirme | Kullanıcı verisi göndermez |
| kotlinx.coroutines, kotlinx.collections.immutable | Dil kütüphaneleri | Yok |
| ProfileInstaller | Baseline Profile yükleme | Yok |

Benchmark, test ve ekran görüntüsü kütüphaneleri sürüm paketine girmez.
