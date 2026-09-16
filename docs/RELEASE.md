# Sürüm hazırlığı

Play'e ilk yükleme ve sonraki her sürüm için yapılacaklar. Mağaza metinleri ve form yanıtları:
`docs/play/store-listing.md`, `docs/play/data-safety.md`. CI: `docs/CI.md`.

## Bir kez yapılacaklar

### 1. Mobil API'nin yayınlanması (web reposu)

Karar (16.09.2026): sunucu yayınlanır, AI kapalı kalır. Uygulama yalnızca `config/` ve `content/` uçlarını
çağırır; bu uçlar yayınlanana kadar 404 döner ve uygulama gömülü veriyle tam çalışır.

1. `napsamweb` reposunda `feature/mobile-api-v1` dalını `main`'e birleştir. Dal; mobil API v1, haftalık tema
   rotasyonu, içerik güncelleme ucu ve gizlilik sayfasının Android / KVKK bölümlerini içerir.
2. `main`'e push → Vercel yalnızca `main`'i yayınlar (`vercel.json`). Deploy'u proje sahibi yapar.
3. Yayından sonra kontrol:
   - `https://napsam.vercel.app/api/v1/config/` → 200 ve `aiEnabled: false`
   - `https://napsam.vercel.app/api/v1/content/` → 200, `ETag` başlığı var
   - `https://napsam.vercel.app/gizlilik/` → "Android uygulaması" bölümü ve veri sorumlusu görünüyor
4. Sorun çıkarsa Vercel'de bir önceki deploy'a dönmek yeterli; uygulama sunucusuz çalışmaya devam eder.

> AI'ı açmak yalnızca Vercel'e Gemini anahtarı eklemek değildir: uygulamadaki AI istemcisi Faz 13'te
> kaldırıldı (kullanılmıyordu). Açılacaksa uygulama kodu, Data Safety beyanı (kullanıcı içeriği) ve iki
> gizlilik metni (web + uygulama) birlikte güncellenir.

### 2. Upload anahtarı

Play App Signing kullanılır: uygulamayı imzalayan asıl anahtarı Google saklar, bizde yalnızca **upload
anahtarı** olur. Kaybolursa Play Console'dan sıfırlanabilir, ama bu günler sürer; o yüzden iki ayrı yerde
yedeklenir (B10.1.5).

Anahtarı kendi terminalinde oluştur (parolaları sen belirlersin, hiçbir yere yapıştırma):

```bash
keytool -genkeypair -v -keystore ~/Keystores/napsam-upload.jks -storetype PKCS12 -alias napsam-upload -keyalg RSA -keysize 4096 -validity 10000
```

- Dosya **repo dışında** durur (`*.jks`, `*.keystore` zaten `.gitignore`'da).
- Yedek: parola yöneticisi (dosya + parolalar) ve şifreli bir harici disk.
- Yerel sürüm derlemesi için repo kökünde `keystore.properties` oluştur (git'e girmez):

```properties
storeFile=/Users/<kullanıcı>/Keystores/napsam-upload.jks
storePassword=...
keyAlias=napsam-upload
keyPassword=...
```

Gradle bu dosyayı, yoksa `NAPSAM_UPLOAD_STORE_FILE`, `NAPSAM_UPLOAD_STORE_PASSWORD`, `NAPSAM_UPLOAD_KEY_ALIAS`,
`NAPSAM_UPLOAD_KEY_PASSWORD` ortam değişkenlerini okur. Hiçbiri yoksa sürüm hata ayıklama anahtarıyla
imzalanır; eksik bilgi verilirse derleme eksik alanların adıyla durur (değerler yazdırılmaz).

İmzayı doğrulamak için:

```bash
./gradlew :app:bundleRelease
```

```bash
keytool -printcert -jarfile app/build/outputs/bundle/release/app-release.aab
```

Çıktıdaki sahip (`Owner`) upload anahtarının adı olmalı, `CN=Android Debug` olmamalı.

### 3. Play Console

- Geliştirici hesabı kimlik doğrulaması tamamlanmış olmalı (B16).
- Uygulama oluştur: varsayılan dil Türkçe, uygulama (oyun değil), ücretsiz.
- Paket adı: **`com.kaaneneskpc.napsam`** (16.09.2026'da kesinleşti; ilk yüklemeden sonra değişmez).
- Play App Signing: Google'ın ürettiği uygulama imzalama anahtarını kabul et; ilk AAB'yi upload anahtarıyla
  imzalı yükle.
- "Uygulama içeriği" formları ve mağaza girişi: `docs/play/`.

### 4. GitHub sırları (Faz 14'te repo açıldıktan sonra)

`release` adında, onaylayıcı gerektiren bir ortam oluştur ve sırları oraya ekle (`docs/CI.md`). Anahtar
deposunu base64'e çevirip panoya al, GitHub arayüzüne kendin yapıştır:

```bash
base64 -i ~/Keystores/napsam-upload.jks | pbcopy
```

## Her sürümde

1. **Sürüm numarası:** `app/build.gradle.kts` → `versionCode` bir artar, `versionName` güncellenir
   (ilk mağaza sürümü için `1.0.0` önerilir).
2. **Gömülü içerik:** web'de içerik değiştiyse `export_mobile` ile `contract/` ve uygulama varlıkları
   yenilenir; parite testleri geçmeli. (Yayındaki uygulama değişen içeriği sunucudan da indirir; gömülü kopya
   ilk açılış ve çevrimdışı kurulum içindir.)
3. **Baseline Profile:** `release.yml` artifact'indeki ya da yerelde üretilen profil
   `app/src/release/generated/baselineProfiles/` altına commit'lenir (`benchmark/README.md`).
4. **Denetim:**

```bash
./gradlew check verifyRoborazziDebug
```

5. **Cihazda parite testi:** JVM testleri Android'in regex motorunu (ICU) göremez; 16.09'da "Yaz" modu
   `(?U)` bayrağı yüzünden cihazda çöküyordu. Emülatör açıkken:

```bash
./gradlew :core:data:connectedDebugAndroidTest
```

6. **R8 sonrası cihaz denemesi** (emülatör de olur), imzalı sürüm derlemesiyle:
   - açılış, öneri kartı, NAPSAM?, daraltma paneli (kelime ve yaz, yazıp gönder), kaydet, Kaydedilenler,
     Gizlilik, tema, "Tamam, yapıyorum";
   - süreç ölümü: uygulama Kaydedilenler ekranındayken arka plana al, `adb shell am kill com.kaaneneskpc.napsam`,
     launcher'dan geri dön → ekran ve geri yığını korunmalı, tek geri tuşu Ana ekrana dönmeli. Uygulamayı
     `am start -n` ile açıp launcher'dan dönmek görevde ikinci bir örnek açar ve geri tuşunu "yutulmuş" gösterir;
     denemede yalnızca launcher intent'i (`adb shell monkey -p com.kaaneneskpc.napsam -c
     android.intent.category.LAUNCHER 1`) kullan;
   - uçak modunda soğuk açılış.
7. **Paket:** `vX.Y.Z` etiketi → `release.yml` (imzalı AAB artifact) ya da yerelde `:app:bundleRelease`.
8. **Play:** AAB'yi önce internal testing'e yükle, sürüm notunu Türkçe yaz, Play'in ön yükleme raporunu
   (pre-launch report) kontrol et, sonra aşamalı yayın.

## R8 notları

Uygulamaya özel keep kuralı yok ve şu an gerekmiyor (16.09.2026'da kontrol edildi):

- kotlinx.serialization, Room, Hilt ve DataStore kendi R8 kurallarını taşıyor; serialization kuralları
  `@Serializable` nesnelerin `INSTANCE.serializer()` ve companion serializer'larını koruyor.
- Navigation 3, rota sınıfının adını çalışma anında (`getClass().getName()`) kaydedip `Class.forName` ile
  geri okuyor; ad aynı APK içinde tutarlı olduğu için R8'in yeniden adlandırması sorun değil.
- Ktor motoru açıkça veriliyor (`Android.create()`); ServiceLoader keşfi yok.
- Sürüm manifestinde yalnızca `INTERNET` izni var; reklam kimliği (`AD_ID`) izni yok.

Bir kütüphane eklenir ya da güncellenirse 6. maddedeki cihaz denemesi özellikle süreç ölümüyle birlikte
tekrarlanır.
