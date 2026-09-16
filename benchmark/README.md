# :benchmark

Macrobenchmark modülü (spesifikasyon B11). Testler cihazda ya da emülatörde çalışır, `:app`'i dışarıdan
sürer ve iki iş yapar:

1. **Baseline + Startup Profile üretimi** — `BaselineProfileGenerator`
2. **Ölçüm** — `StartupBenchmark` (soğuk açılış), `ScrollBenchmark` (kaydırma akıcılığı)

## Hedefler (B11)

| Ölçüm | Hedef | Metrik |
|---|---|---|
| Soğuk açılış | < 800 ms | `timeToInitialDisplayMs` |
| Açılış → ilk öneri | < 3 sn (yerel motorla < 1 sn) | `timeToFullDisplayMs` |
| Kaydırma takılması | < %1 | `frameOverrunMs` P99 |

`timeToFullDisplayMs`, ana ekranın `ReportDrawnWhen { state.suggestion != null }` bildirimiyle ilk
önerinin çizildiği anı gösterir.

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

Tek bir ölçüm:

```bash
./gradlew :benchmark:connectedBenchmarkReleaseAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.kaaneneskpc.napsam.benchmark.ScrollBenchmark
```

Sonuçlar `benchmark/build/outputs/connected_android_test_additional_output/` altındaki JSON dosyalarında.

## Notlar

- Ölçüm sürümü sürüm (release) derlemesidir; `:app` bu aşamada hata ayıklama anahtarıyla imzalanır
  (gerçek imzalama Faz 13).
- Emülatörde ölçmek gerekirse varsayılan 2 GB bellek yetmez: `-memory 4096 -gpu host` ile başlat.
- CI'da ölçümler koşmaz (emülatörde gürültülü). Profil üretimi `release.yml`'da yönetilen emülatörde yapılır:
  `CI=true` iken bu modül yalnızca `pixel6Api34` yönetilen cihazını, yerelde ise bağlı cihazı kullanır.
