# :benchmark

Macrobenchmark modülü (spesifikasyon B11). Testler cihazda ya da emülatörde çalışır, `:app`'i dışarıdan
sürer ve iki iş yapar:

1. **Baseline + Startup Profile üretimi** — `BaselineProfileGenerator` (açılış, ana ekranda kaydırma, Kaydedilenler;
   Gizlilik gezilmez: alt bilgideki bağlantı UiAutomator'a ayrı düğüm olarak görünmüyor)
2. **Ölçüm** — `StartupBenchmark` (soğuk açılış), `ScrollBenchmark` (kaydırma akıcılığı)

## Hedefler (B11)

| Ölçüm | Hedef | Metrik |
|---|---|---|
| Soğuk açılış | < 800 ms | `timeToInitialDisplayMs` |
| Açılış → ilk öneri | < 3 sn (yerel motorla < 1 sn) | `timeToFullDisplayMs` |
| Kaydırma takılması | < %1 | `frameOverrunMs` P99 |

`timeToFullDisplayMs`, ana ekranın `ReportDrawnWhen { state.suggestion != null }` bildirimiyle ilk
önerinin çizildiği anı gösterir.

## Son ölçüm (16.09.2026, emülatör)

Pixel_9_Pro_API_37 emülatörü (4 GB, donanım GPU), `benchmarkRelease` (R8), 10 tur. Emülatör sonuçları gerçek
cihaz yerine geçmez; göreli karşılaştırma içindir.

| Ölçüm | Profilsiz | Baseline Profile ile | Hedef |
|---|---|---|---|
| Soğuk açılış, ilk kare (medyan) | 209 ms | 158 ms (−%24) | < 800 ms |
| Açılış → ilk öneri (medyan) | 293 ms | 209 ms (−%29) | < 1 sn |

Kaydırma (`ScrollBenchmark`, profille): kare CPU süresi P50 23,9 ms / P90 34,8 ms / P99 67,1 ms; bütçe aşımı
P50 9,1 ms / P99 68,4 ms. Aynı emülatörde `dumpsys gfxinfo` ile karşılaştırma: napsam takılan kare %6,7
(P50 24 ms), sistem Ayarlar uygulaması %10,8 (P50 22 ms) — emülatörün kendi tavanı. %1 hedefi gerçek cihazda
ölçülmeli.

Üretilen profil: `app/src/release/generated/baselineProfiles/` (24.396 kural, 1.810'u uygulama sınıfları;
Startup Profile ile aynı içerik).

## Çalıştırma

Bağlı bir cihaz ya da emülatör gerekir (ölçüm için gerçek cihaz tercih edilir; emülatör sonuçları
yalnızca göreli karşılaştırma içindir).

Profil üretimi — sonuç `app/src/release/generated/baselineProfiles/` altına yazılır:

```bash
./gradlew :app:generateReleaseBaselineProfile
```

Ölçümler:

```bash
./gradlew :benchmark:connectedBenchmarkReleaseAndroidTest
```

Emülatörde ölçüm Macrobenchmark tarafından hata sayılır; bilerek emülatörde ölçülecekse:

```bash
./gradlew :benchmark:connectedBenchmarkReleaseAndroidTest -Pandroid.testInstrumentationRunnerArguments.androidx.benchmark.suppressErrors=EMULATOR
```

Tek bir ölçüm:

```bash
./gradlew :benchmark:connectedBenchmarkReleaseAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.kaaneneskpc.napsam.benchmark.ScrollBenchmark
```

Sonuçlar `benchmark/build/outputs/connected_android_test_additional_output/` altındaki JSON dosyalarında.

## Notlar

- **AGP 9 ile eklenti uyumsuzluğu:** Baseline Profile eklentisi 1.5.0, profil toplama türevini
  (`nonMinifiedRelease`) `initWith(release)` ile kurup yalnızca eski `isMinifyEnabled`'ı kapatıyor. AGP 9'un
  `optimization { enable = true }` ayarı kopyalanıp açık kaldığı için kurallar karışık sınıf adlarıyla
  (`La;`, `Lcb0;`) toplanıyor ve Gradle "hiç kural üretilmedi" diyordu. `app/build.gradle.kts`'teki
  `androidComponents.finalizeDsl` bu türevde R8'i kapatıyor; eklenti bunu kendisi yaptığında kaldırılabilir.
- Ölçüm sürümü sürüm (release) derlemesidir; `:app` bu aşamada hata ayıklama anahtarıyla imzalanır
  (gerçek imzalama Faz 13).
- Emülatörde ölçmek gerekirse varsayılan 2 GB bellek yetmez: `-memory 4096 -gpu host` ile başlat.
- CI'da ölçümler koşmaz (emülatörde gürültülü). Profil üretimi `release.yml`'da yönetilen emülatörde yapılır:
  `CI=true` iken bu modül yalnızca `pixel6Api34` yönetilen cihazını, yerelde ise bağlı cihazı kullanır.
