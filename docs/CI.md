# CI (GitHub Actions)

Repo: https://github.com/kaaneneskpc/napsamandroid (private). Aşağıdaki iş akışları orada çalışır.
PR denetimi gizli anahtar kullanmaz; sürüm akışı imza sırlarını yalnızca tanımlıysa kullanır.

## İş akışları

| Dosya | Tetikleyici | Ne yapar |
|---|---|---|
| `workflows/pr.yml` | her PR, `main`'e push | `./gradlew check verifyRoborazziDebug` (biçim, lint, birim + `contract/` parite + ekran görüntüsü testleri), ardından `:app:assembleRelease` (lintVital + R8). Hata olursa raporları ve ekran görüntüsü farklarını artifact olarak yükler. |
| `workflows/release.yml` | `v*` etiketi, elle | Yönetilen emülatörde (`pixel6Api34`, KVM) Baseline + Startup Profile üretir, profille `:app:bundleRelease` derler ve upload anahtarıyla imzalar; AAB, `mapping.txt` ve üretilen profili artifact olarak yükler. İmza sırları yoksa uyarı verir, paketi hata ayıklama anahtarıyla imzalar ve artifact adına `hata-ayiklama-imzali` yazar. |
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

Şu an hiçbiri tanımlı değil. `release.yml` imza sırlarını tanımlıysa kullanır (anahtar deposu yalnızca runner'ın
geçici klasörüne yazılır ve iş sonunda silinir); PR denetimi hiçbir sır okumaz. Upload anahtarının
oluşturulması ve sırlara eklenmesi: `docs/RELEASE.md`.

| Ad | Ne için | Ne zaman |
|---|---|---|
| `NAPSAM_UPLOAD_KEYSTORE_BASE64` | Upload anahtar deposu (base64) | İlk sürüm paketinden önce |
| `NAPSAM_UPLOAD_KEYSTORE_PASSWORD` | Anahtar deposu parolası | İlk sürüm paketinden önce |
| `NAPSAM_UPLOAD_KEY_ALIAS` | Anahtar adı | İlk sürüm paketinden önce |
| `NAPSAM_UPLOAD_KEY_PASSWORD` | Anahtar parolası | İlk sürüm paketinden önce |
| `PLAY_SERVICE_ACCOUNT_JSON` | Play Console internal track yüklemesi | Play hesabı hazır olunca |

Değerler repoya hiçbir biçimde girmez; yalnızca GitHub'da, onaylayıcı gerektiren bir `release` ortamının
sırları olarak tutulması önerilir.

## İlk çalıştırma (17.09.2026)

Repo açıldıktan sonraki ilk `main` push'unda PR denetimi **10 dk 49 sn**'de yeşil geçti (önbellek boşken).

1. **Ekran görüntüleri Linux'ta da tuttu.** macOS'ta kaydedilen 16 golden, ubuntu runner'da farksız doğrulandı;
   yeniden kaydetmek gerekmedi. İleride yalnızca kenar yumuşatması farkıyla düşerse `screenshots.yml`'ı çalıştır,
   artifact'teki PNG'leri `*/src/test/screenshots/` altına koyup commit'le.
2. **Runner belleği yetti.** 3 GB Gradle + aynı süreçte Kotlin derleyicisi ayarıyla sorun çıkmadı.
3. **Sürüm akışı uzun sürer.** İlk çalıştırmada emülatör sistem görüntüsü indirilir. Üretilen
   `baseline-prof.txt` / `startup-prof.txt` artifact'ten alınıp `app/src/release/generated/baselineProfiles/`
   altına commit'lenmelidir; yerel ve PR derlemeleri de o zaman profili içerir.
4. **İmza sırları eklenmeden sürüm AAB'si Play'e yüklenemez;** artifact adı `hata-ayiklama-imzali` ile biter.
5. **Ölçümler CI'da koşmaz.** `StartupBenchmark` / `ScrollBenchmark` emülatörde gürültülü sonuç verir; gerçek
   cihazda elle çalıştırılır (`benchmark/README.md`).
