# CI (GitHub Actions)

Repo henüz GitHub'da değil (Faz 14); aşağıdaki iş akışları repo açıldığında kendiliğinden çalışmaya başlar.
Hiçbiri gizli anahtar kullanmaz.

## İş akışları

| Dosya | Tetikleyici | Ne yapar |
|---|---|---|
| `workflows/pr.yml` | her PR, `main`'e push | `./gradlew check verifyRoborazziDebug` (biçim, lint, birim + `contract/` parite + ekran görüntüsü testleri), ardından `:app:assembleRelease` (lintVital + R8). Hata olursa raporları ve ekran görüntüsü farklarını artifact olarak yükler. |
| `workflows/release.yml` | `v*` etiketi, elle | Yönetilen emülatörde (`pixel6Api34`, KVM) Baseline + Startup Profile üretir, profille `:app:bundleRelease` derler; AAB, `mapping.txt` ve üretilen profili artifact olarak yükler. |
| `workflows/screenshots.yml` | elle | Golden ekran görüntülerini Linux'ta yeniden kaydeder ve artifact olarak yükler (repoya yazmaz). |
| `dependabot.yml` | haftalık | Action sürümlerini günceller. Gradle bağımlılıkları bilerek dışarıda (sürüm kataloğu elle doğrulanıyor, B1). |

Ortak kurulum `actions/gradle-kurulum` içinde: JDK 21 (Temurin), Gradle önbelleği, CI bellek ayarı.

Yerelde aynı denetim:

```bash
./gradlew check verifyRoborazziDebug
```

```bash
./gradlew :app:assembleRelease
```

## Güvenlik

- Her iş akışında `permissions: contents: read`; `actions/checkout` kimlik bilgisini diske bırakmaz
  (`persist-credentials: false`).
- Action'lar etiketle değil commit SHA'sıyla sabitlenir (etiket sonradan taşınamaz); sürüm yorumda yazar.
- `pull_request` kullanılır, `pull_request_target` kullanılmaz: fork'tan gelen PR sırlara erişemez.
- Derleme geliştirici makinesine bağlı değildir: depolar yalnızca `settings.gradle.kts`'te (google,
  mavenCentral, Gradle eklenti portalı), global init script gerekmez; JDK sürümü
  `gradle/gradle-daemon-jvm.properties`'ten gelir.
- Gradle önbelleği için `cache-provider: basic` (MIT). `gradle/actions`'ın varsayılan "enhanced" önbelleği kapalı
  kaynaktır ve private repolarda yalnızca önizleme olarak sunulur; repo private (K16).

## Sırlar

Şu an hiçbiri tanımlı değil ve iş akışları hiçbirini okumuyor. İleride gerekecek adlar:

| Ad | Ne için | Ne zaman |
|---|---|---|
| `NAPSAM_UPLOAD_KEYSTORE_BASE64` | Upload anahtar deposu (base64) | Faz 13, imzalama |
| `NAPSAM_UPLOAD_KEYSTORE_PASSWORD` | Anahtar deposu parolası | Faz 13 |
| `NAPSAM_UPLOAD_KEY_ALIAS` | Anahtar adı | Faz 13 |
| `NAPSAM_UPLOAD_KEY_PASSWORD` | Anahtar parolası | Faz 13 |
| `PLAY_SERVICE_ACCOUNT_JSON` | Play Console internal track yüklemesi | Play hesabı hazır olunca |

Değerler repoya hiçbir biçimde girmez; yalnızca GitHub'da, onaylayıcı gerektiren bir `release` ortamının
sırları olarak tutulması önerilir.

## İlk çalıştırmada bakılacaklar

1. **Ekran görüntüleri macOS'ta kaydedildi.** Linux'ta yazı tipi yumuşatması farklıysa `pr.yml` ekran görüntüsü
   karşılaştırmasında düşer. Fark yalnızca kenar yumuşatmasıysa `screenshots.yml`'ı çalıştır, artifact'teki
   PNG'leri `*/src/test/screenshots/` altına koyup commit'le; bundan sonra golden'lar Linux'ta kaydedilir.
2. **Runner belleği.** Private repo runner'ı 2 çekirdek / 7 GB varsayıldı; Gradle 3 GB ve Kotlin derleyicisi
   aynı süreçte çalışır. Daha büyük runner kullanılırsa ayar gevşetilebilir.
3. **Sürüm akışı uzun sürer.** İlk çalıştırmada emülatör sistem görüntüsü indirilir. Üretilen
   `baseline-prof.txt` / `startup-prof.txt` artifact'ten alınıp `app/src/release/generated/baselineProfiles/`
   altına commit'lenmelidir; yerel ve PR derlemeleri de o zaman profili içerir.
4. **Sürüm AAB'si Play'e yüklenemez.** Faz 13'e kadar hata ayıklama anahtarıyla imzalıdır.
5. **Ölçümler CI'da koşmaz.** `StartupBenchmark` / `ScrollBenchmark` emülatörde gürültülü sonuç verir; gerçek
   cihazda elle çalıştırılır (`benchmark/README.md`).
