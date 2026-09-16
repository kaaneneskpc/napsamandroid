# NAPSAM Android — Uygulama Planı ve İlerleme

> Spesifikasyon: `docs/napsam-android-prompt.md` (v2). Bu dosya **ne yapıldığını** takip eder;
> kurallar ve kararlar spesifikasyondadır. Her faz sonunda bu dosya güncellenir ve onay alınır.
>
> Durum: ⬜ başlanmadı · 🟨 sürüyor · ✅ bitti ve onaylandı · ⛔ karar/engel bekliyor

| Faz | Kapsam | Durum |
|---|---|---|
| 0 | Eleştiri, kararlar, yerel git | ✅ |
| 1 | Web backend hazırlığı (napsamweb, yerel dal) | ✅ |
| 2 | Proje iskeleti | ✅ |
| 3 | `:core:model` | ✅ |
| 4 | `:core:designsystem` | ✅ |
| 5 | `:core:domain` + parite testleri | ✅ |
| 6 | `:core:data` | ✅ |
| 7 | `:feature:home` + `:core:ui` (Faz 8 dahil) | ✅ |
| 8 | ~~`:feature:suggestion`~~ → Faz 7'ye katıldı | — |
| 9 | `:feature:saved` + `:feature:privacy` | ✅ |
| 10 | `:app` | ✅ |
| 11 | `:benchmark` | ✅ (cihazda ölçüm en sonda) |
| 12 | CI | ✅ (GitHub'da ilk çalıştırma Faz 14) |
| 13 | Release hazırlığı | 🟨 onay bekliyor (görseller ve cihaz denemesi en sonda) |
| 14 | GitHub (yalnızca proje sahibi isteyince) | ⛔ ertelendi |

---

## Faz 0 — Eleştiri ve kararlar

### 0.1 v1 dokümanında kesin hatalı olanlar (v2'ye işlendi, onay gerekmez)
1. **Backend yanlış tarif edilmiş.** Supabase Edge Function + Claude yok; Django + Gemini var.
2. **İstemci sözleşmesi çalışmaz.** `count: 3`, `timeOfDay`, `weeklyTheme` istemciden gitmez; web
   tek öneri döner. Web uçları CSRF yüzünden native istemciye kapalı → mobil API v1 şart.
3. **Supabase anon key istemcide işe yaramaz** (RLS + kaldırılmış yetkiler) ve olmamalı.
4. **"60 örnek öneri yaz" adımı gereksiz**: 500 denetimli öneri var; ikinci bir içerik kaynağı
   açmak iki havuzun ayrışması demek.
5. **Modül bağımlılık kuralı döngüseldi** (domain → data ve arayüzler domain'de).
6. **Sürümler eskimiş**: Kotlin 2.3.21 → 2.4.20, AGP 9.2 → 9.4.0 (Gradle ≥ 9.6), BOM → 2026.09.00.
7. **KVKK maddesi var olmayan bir iletişim kanalı varsayıyordu**; web'de bu cümle kaldırıldı.

### 0.2 Aşırı sadeleştirmenin ürünü işlevsiz bırakabileceği yerler
- **İçerik güncellemesi yalnızca uygulama güncellemesiyle** (K3) olursa, retention'ın tek kozu
  olan içerik tazeliği mağaza onay döngüsüne bağlanır. v1 için kabul edilebilir; v1.1'de
  `/api/v1/content` düşünülmeli.
- **Kayıtlar web ile senkron değil.** Web'de kaydeden kullanıcı telefonda göremez. Üyelik
  yokken bunun temiz çözümü yok; bilinçli olarak kabul ediliyor.
- **Filtre arayüzü yok (K5)**: "30 dakikam var" demenin tek yolu "Yaz" modu. Yaz modunun
  alt dize eşleşmesi zayıf (C6), yani bu yol kırılgan. K14 kararı bu yüzden önemli.

### 0.3 Bu ölçekte gereksiz veya maliyetli bulduğum kurallar
- **Hazır Room .db asset'i** (K3): Room şema kimlik karmasıyla birebir uyumlu .db üretmek ayrı
  bir araç zinciri ister; 500 satırlık JSON'u ilk açılışta içe aktarmak ölçülebilir bir gecikme
  yaratmaz ve sürüm yenilemeyi basitleştirir.
- **Proto DataStore** (K12): protobuf Gradle eklentisi ve üretilmiş kod, 4 alanlık tercih için ağır.
- **Explicit API mode tüm core modüllerde** (K13): yayınlanmayan modüllerde gürültü.
- **detekt** (K9): 1.23.8 Kotlin 2.4 ile uyumlu olmayabilir; alpha araca CI bağlamak kırılganlık.
- **Macrobenchmark + Baseline Profile + Play upload'ın CI'da her main merge'de çalışması**:
  emülatör gerektirir, ücretsiz runner'da yavaş. Faz 11–12'de yalnızca release dalında çalıştırılması önerilir.
- **Bildirim (A3 md.10)**: POST_NOTIFICATIONS izni md.3 ile çelişir; v1'den çıkarılması önerilir (K7).
- **Hava + mekan çözümleme**: konum izni gerektirir, web'de yok; v1'den çıkarıldı.

### 0.4 Riskler ve doğrulanmamış varsayımlar
- **Kotlin 2.4.20 + KSP 2.3.12 + Hilt 2.60.1 + Room 2.8.5** birlikte derlenebilirliği kanıtlanmadı
  → Faz 2'nin ilk işi. Olmazsa Kotlin 2.4.10'a inilir.
- **İki motor implementasyonu** zamanla ayrışır → parite testleri (C8) CI'da zorunlu.
- **Web'de haftalık tema dönmüyor** → Faz 1'de düzeltilmezse Android'le parite anlamsız.
- **CGNAT + WAF** → çok kullanıcılı bir mobil ağda config çağrıları 429 alabilir; istemci seyrek çağırır.
- **İsim/marka** → Play'e ilk yüklemeden önce karar; applicationId sonradan değişmez.
- **Gizlilik politikası URL'i** web'i anlatıyor; Android bölümü olmadan Play incelemesinde sorun çıkabilir.

### 0.5 Kararlar (spesifikasyon C11) — onayını bekleyenler
| # | Karar | Onaylanan (14.09.2026) | Onay |
|---|---|---|---|
| K1 | applicationId / isim | `com.kaaneneskpc.napsam` kesin (16.09); görünen ad için marka araştırması raporlandı | ✅ |
| K2 | targetSdk | 37 | ✅ |
| K3 | İçerik taşıma | Supabase içeriğinin gömülü kopyası + içe aktarma; Supabase'de değişince mobil API'den indirme; hafıza cihazda (15.09 revize) | ✅ |
| K4 | Açılış | hazır kart (0 dokunuş) | ✅ |
| K5 | Filtre arayüzü | v1'de yok | ✅ |
| K6 | "Tamam, yapıyorum" | web mesajı + mesajı gizleyen X; uygulamayı kapatan düğme yok (revize) | ✅ |
| K7 | Bildirim/analytics/crash SDK | v1'de yok | ✅ |
| K8 | Swipe | yok, yalnızca düğmeler (revize) | ✅ |
| K9 | detekt | v1'de yok | ✅ |
| K10 | Bağımlılık yönü | data → domain | ✅ |
| K11 | Deep link | v1'de yok | ✅ |
| K12 | DataStore | kotlinx.serialization tipli | ✅ |
| K13 | Explicit API | yalnızca model + domain | ✅ |
| K14 | Metin ayrıştırıcı | önce birebir parite | ✅ |
| K15 | KVKK veri sorumlusu + e-posta | Kaan Enes KAPICI · kaaneneskpc1@gmail.com | ✅ |
| K16 | Android repo | `kaaneneskpc/napsam-android`, private | ✅ |
| K17 | Web API v1 | config + suggest | ✅ |
| K18 | Ekranlar | Ana ekran, Kaydedilenler, Gizlilik; ayarlar ekranı ve sil düğmesi yok, tema üst barda | ✅ |
| K19 | Ana ekran | web içeriği ve sırası (tanıtım kutuları + alt bilgi dahil), görünüm Stitch son tasarımı | ✅ |
| K20 | Mobil API ve AI (16.09) | sunucu yayınlanır (config + içerik), AI kapalı; uygulamada AI istemcisi ve kurulum kimliği yok | ✅ |

> **Web birebirliği (14.09.2026, proje sahibinin talimatı):** Android içerik olarak web'in, görünüm
> olarak Stitch son tasarımının (`docs/design/`) aynısıdır; web'de olmayan özellik eklenmez.
> Bu yüzden K6 ve K8 revize edildi, K18–K19 eklendi, `:feature:settings` → `:feature:privacy`.

### 0.6 Git (yalnızca yerel)
- [x] `git init` (uzak depo YOK), `.gitignore`'a keystore, `.kotlin/`, `.idea/`, `build/` eklenmesi
- [x] İlk commit: şablon + docs

> **GitHub işleri ertelendi** (14.09.2026, proje sahibinin talimatı): repo açma, push, secret
> scanning ve CI'ın GitHub'a bağlanması tüm işler bittikten sonra, proje sahibi açıkça
> "GitHub'da repo aç" dediğinde yapılır → **Faz 14**. O zamana kadar hiçbir uzak depo eklenmez.

---

## Faz 1 — Web backend hazırlığı (`/StudioProjects/napsamweb`)
**Amaç:** Android'in ihtiyaç duyduğu sunucu parçaları, web'i bozmadan.

> **Deploy kararı (14.09.2026):** Web projesi tamamlandı sayılıyor; odak mobil uygulama.
> Faz 1 değişiklikleri web reposunda **yerel bir dalda** kalır, push/deploy edilmez, canlı site
> değişmez. Uygulama offline-first olduğu için sunucu API'si canlıda olmadan da tam çalışır
> (AI kapalı, bütçe tablosunun yerel yedeği var). Android ağ katmanı yerel Django sunucusuyla
> test edilir. Sunucu API'sinin yayınlanıp yayınlanmayacağı **Faz 13**'te kararlaştırılır.
- [x] Web reposunda yerel `feature/mobile-api-v1` dalı; başlangıçta 116 test yeşil (SQLite)
- [x] `GET /api/v1/config/` (C3.3) + 8 test: şekil, oranların ve ücretin metin olması, asgari
      ücret değişince tutarların kayması, çerez bırakmaması, önbellek başlığı, konum alanı yok
- [x] `POST /api/v1/suggest/` (C3.4) + 9 test: CSRF'siz çalışması (web ucu aynı durumda 403),
      UUID v4 başlık zorunluluğu, çerez/profil yazmaması, yanıt şeklinin web ile aynı olması,
      serbest metin, AI kotasının kurulum kimliğiyle sayılması, AI hatasında seed'e düşme
- [x] Öneri akışı web ve mobilde ortak `suggest_response` fonksiyonunda (kod kopyası yok)
- [x] Haftalık tema rotasyonu (C4.7) + 8 test (seed düzeninde takılı kalma regresyonu dahil)
- [x] `export_mobile` komutu + 4 test (çıktı içeriği, determinizm, bozuk içerikte durma,
      SQLite dışında çalışmayı reddetme)
- [x] `seed --check` temiz · Django `check` temiz · **145 test yeşil** (116 → 145)
- [x] README: mobil API, dışa aktarma, tema rotasyonu, test sayısı
- [x] Dışa aktarma `napsam/contract/`'a çalıştırıldı: 500 öneri + meta + 9 parite dosyası;
      iki çalıştırma bayt bayt aynı
- [x] Web değişiklikleri yerel dalda commit'lendi
- [~] ~~main'e push → Vercel deploy~~ **yapılmadı** (proje sahibi kararı, 14.09.2026) → Faz 13

## Faz 2 — Proje iskeleti
- [x] Wrapper Gradle 9.7.1 (gömülü Kotlin 2.4.0) · AGP 9.4.0 · Kotlin 2.4.20 · KSP 2.3.12 · BOM 2026.09.00
- [x] Daemon JVM toolchain 21 (`updateDaemonJvm`) · Java hedefi 21 (built-in Kotlin bunu izler)
- [x] Kök sınıf yolunda AGP'nin getirdiği KGP 2.2.10 → **2.4.20**, KSP 2.2.10-2.0.2 → **2.3.12**
      yükseltildiği `buildEnvironment` ile doğrulandı
- [x] **Uyum kanıtı:** geçici deneme dosyasıyla Room (`ProbeDatabase_Impl`, şema JSON), Hilt
      (`Hilt_MainActivity`, aggregation) ve kotlinx.serialization derlendi; deneme dosyaları silindi.
      Kotlin 2.4.10'a inmek gerekmedi.
- [x] `build-logic` convention plugin'leri: `napsam.android.application`, `napsam.android.library`,
      `napsam.android.compose`, `napsam.android.feature`, `napsam.android.room`, `napsam.hilt`,
      `napsam.jvm.library`. Spotless kök build'de (ktlint 1.8.0 `android_studio` stili + compose-rules 0.6.6)
- [x] Modüller: `:app`, `:core:{model,domain,data,designsystem}`, `:feature:{home,suggestion,saved,settings}`
      (`:benchmark` Faz 11'de). `:core:model` ve `:core:domain` saf Kotlin + explicit API (K13)
- [x] SDK/JVM seviyeleri sürüm kataloğunda: minSdk 26 · targetSdk 37 · compileSdk 37 · jvmTarget 21 ·
      release'de R8 (`optimization { enable = true }`) · lint `abortOnError`
- [x] Şablon temizliği: Greeting, şablon teması, Espresso'lu örnek testler, kullanılmayan renkler,
      gereksiz `-v26` klasör eki ve tekrarlı activity etiketi kaldırıldı. `NapsamApplication`
      (`@HiltAndroidApp`) ve `@AndroidEntryPoint MainActivity` eklendi; ekran içeriği Faz 7–10'da
- [x] `./gradlew assembleDebug testDebugUnitTest :core:model:test :core:domain:test spotlessCheck lintDebug`
      **yeşil**: 7 Android modülünde lint "No issues found", format temiz, debug APK üretildi.
      Test görevleri çalışıyor ancak henüz test kaynağı yok (NO-SOURCE); ilk testler Faz 3'te
- [x] Sadeleştirme: minSdk 26'da `java.time` yerleşik olduğu için core library desugaring eklenmedi

## Faz 3 — `:core:model`
- [x] Enum'lar (web anahtarlarıyla, `fromKey`): Category, Place, Energy, WeatherNeed, CompanionType,
      StateStatus, SuggestionSource, SuggestionMode, PlacePreference.
      `Companion` yerine `CompanionType`: Kotlin'in `companion object` adıyla çakışıyor, companion'ı
      olan sınıflarda `Companion.SOLO` yanlış sembole çözülürdü
- [x] Modeller: Suggestion (22 alan), BudgetTier (oranlar BigDecimal), WeeklyTheme, AppConfig,
      UserSuggestionState (`isCoolingDown`), SuggestionContext + Weather. Şehir/ilçe/koordinat alanı yok
- [x] Rozetler: model metin değil **yapı** üretir (CostBadge, DurationBadge, PlaceBadge, CompanionBadge);
      "Bedava", "dk", "Evde" gibi kelimeler B14 gereği Faz 8'de strings.xml'e gider (TR + EN).
      Kategori etiketleri enum'da tutulmaz (veri veya arayüz kaynağı)
- [x] Testler (16, hepsi yeşil):
      - `SuggestionBadgesParityTest`: 500 önerinin tamamında rozetler `contract/parity/badges.json` ile aynı;
        mekan türlerinin küçük harfli olduğu kilitlendi (Türkçe ilk harf büyütme = web capitalize)
      - `SuggestionBadgesTest`: sınır değerler (boş not, tek sayı süre, 0/3/4 kiminle, bekleme süresi)
      - `ContentKeysTest`: içerikteki tüm değerler enum'a dönüşüyor, anahtarlar meta dosyasıyla ve
        web istek değerleriyle aynı
- [x] Convention: saf Kotlin modüllerinin testleri `contract/` klasörünü sistem özelliğiyle okur ve
      klasörü görev girdisi olarak bildirir (içerik değişince testler yeniden çalışır)
- [x] `:core:model:test spotlessCheck assembleDebug` yeşil

## Faz 4 — `:core:designsystem`
> **Kaynak değişti (14.09.2026):** proje sahibi son Stitch tasarımını verdi; token'lar `napsam.css`
> yerine `docs/design/` altındaki mobil açık/koyu tasarımdan birebir alındı. Web'de olmayan eklemeler
> (SkeletonCard, uygulamayı kapatan düğme, swipe, ayarlar ekranı, renk düzeltmeleri) kaldırıldı.
- [x] Token'lar tasarımla birebir: renk (Tailwind zinc/orange, açık/koyu, koyu temada turuncu ışıma),
      köşe yarıçapı 4/6/12/16dp, boşluk, yazı boyutları (sp)
- [x] Manrope 400–800 gömülü (değişken font, OFL lisansı `assets/licenses`). Stitch HTML'i Plus Jakarta
      Sans yüklüyor; DESIGN.md ve web Manrope dediği için Manrope kaldı
- [x] İkonlar tasarımdaki çizgi ikonlardan (ImageVector): bolt, check, refresh, heart, swap, clock, pin,
      person, chevron, sun, moon, close
- [x] Bileşenler (web sırasına göre): NapsamBackground, NapsamTopBar (Kaydedilenler + sayı, tema),
      OverlinePill, NapsamConsole, BudgetSelector, NapsamPrimaryButton (NAPSAM?), CtaNote, NapsamDivider,
      RefineToggle, NapsamChip, StageHeader, NapsamCard, CardBand, NapsamBadge, SectionLabel, StepList,
      NeedsRow, FallbackBlock, CardActions (Tamam yapıyorum · Başka fikir · kalp), CommitMessage,
      SkeletonBlock, ValuePropCard, NapsamFooter (Gizlilik bağlantısı)
- [x] %200 yazı ölçeği: bütçe sütunları kelime bölünmeden azalır (5 → 3), kart eylemleri iki satıra
      geçer, adım numarası dairesi yazıyla büyür, üst bar sığmazsa alt satıra geçer
- [x] `ColorContrastTest` (4): tüm metin çiftleri AA; tasarımdan gelen istisnalar (açık temada turuncu
      küçük etiket, turuncu üzerinde beyaz, açık temada bütçe tutarı) ayrı testte belgelendi (B12.4)
- [x] Roborazzi (5 referans, `src/test/screenshots`): ana ekran açık/koyu/%200 + tasarımda görünmeyen
      web durumları açık/koyu. Örnek içerik test kaynağındaki katalogda (web metinleri)
- [x] Robolectric SDK 36 (SDK 37'de `InputManager.getInstance()` yok), durdurulmuş Compose saati
      (sonsuz animasyonlar), çalışma zamanı jar'ı Gradle'dan + çevrimdışı mod (indirme kopuyordu)
- [x] `:feature:settings` → `:feature:privacy`
- [x] `verifyRoborazziDebug spotlessCheck lintDebug assembleDebug testDebugUnitTest :core:model:test`
      yeşil; 7 Android modülünde lint "No issues found"

## Faz 5 — `:core:domain`
> Başlamadan önce Faz 4 tasarımı son Stitch dışa aktarımıyla yeniden karşılaştırıldı (14.09.2026):
> en yeni zip yalnızca kullanılan iki ekran + DESIGN.md içeriyor, tüm görünür metinler katalogla aynı,
> eksik yok. Stitch MCP bu oturumda tanımlı değildi (anahtar gerekir, MCP oturum başında yüklenir).
- [x] `EngineConstants`: web motor sabitleri + DISMISS_COOLDOWN_DAYS, RECENT_STATES, SESSION_EXCLUDED, MAX_*
- [x] `BudgetCalculator`: oran × ücret → 10 TL'ye HALF_EVEN yuvarlama, "~1-150 TL" / "1.500 TL+" metni,
      bilinmeyen kademe tavanı 0, bant alt sınırı
- [x] `ChipCatalog`: saat → set (4 pencere)
- [x] `TextParser`: C6 listeleri web kaynağından birebir, Unicode boşluk/rakam ve dilden bağımsız küçük harf
      (Python ile aynı), `applyTo` = web `build_context` birleştirmesi (metin bütçeyi ezer, seçimleri ezmez)
- [x] `WeeklyThemeRule`: C4.7 + config teması yoksa yerel rotasyon
- [x] `SuggestionEngine`: sert filtreler web sırasıyla, puan, yakınlık eşiği + havuzla ölçeklenen kapak,
      gevşetme (bütçe hariç), son çare bedava öneri. `Random` ve `ZoneId` enjekte; yerel saat bağlamdan
      gelir, bekleme süresi karşılaştırması için zaman dilimiyle ana çevrilir. Yalnızca SEED öneriler havuzda
- [x] Repository arayüzleri: içerik, kullanıcı durumu, config, uzak öneri (AI), tercihler (yalnızca tema;
      web bütçeyi hatırlamıyor) + `Outcome`/`DataError`
- [x] `contract/` okuyucusu `:core:model` testFixtures'a taşındı; model ve domain testleri ortak kullanıyor
- [x] Parite testleri (C8): bütçe (5 ücret × 5 kademe + gömülü meta), chips (24 saat), metin (164 cümle),
      sert filtre (73 bağlam), gevşetme (6 bağlam), kapak, tema (80), motor sabitleri — hepsi web ile aynı
- [x] Davranış testleri (web `VarietyTests` eşikleri, sabit tohum): 150 seçimde havuzun %40'ından fazlası,
      tema payı < %85, kelime isabeti 25/25, seçim her zaman havuzdan, bekleme süresi sınırı, son çare,
      bütçe hiç gevşemiyor, AI öneriler havuz dışı, puan ağırlıkları, son 5 kayıt, gevşetme sırası
- [x] 31 domain + 16 model + 9 designsystem testi yeşil

## Faz 6 — `:core:data`
- [x] Room (v1, şema `core/data/schemas/` altında): `suggestion`, `content_info` (sürüm), `user_state`
      (içerikten ayrı, yabancı anahtar yok). Liste alanları JSON dizisi. Destructive migration yok
- [x] İçerik asset olarak paketlenir: yeni `napsam.android.content` convention'ı `contract/content`'ten
      yalnızca `suggestions.json` + `meta.json`'u variant API ile asset yapar (parite dosyaları APK'ya girmez)
- [x] İçe aktarma (K3): meta sürümü DB'dekinden farklıysa tek transaction'da yenileme; kullanıcı kayıtları
      korunur, artık olmayan önerilerin kayıtları silinir; bozuk içerik (bilinmeyen anahtar, sayı tutmuyor,
      yinelenen slug) hiç yazılmaz. Kontrol süreç başına bir kez, ilk öneri isteğinde (B11.2)
- [x] Kullanıcı durumu geçişleri web `api_state` + `engine.mark_*` ile birebir (unsave `updated_at`'e dokunmaz,
      kaydetmek beklemeyi kaldırır, içerikte olmayan slug için kayıt açılmaz); Kaydedilenler `-saved_at` sırasıyla
- [x] DataStore (K12, kotlinx.serialization): tema, kurulum kimliği (UUID v4), config önbelleği ve 429
      ertelemesi. Bozuk dosya varsayılana döner. Web gibi bütçe saklanmaz
- [x] Ktor (Android motoru, çerez/CSRF yok, connect 10 sn / request 20 sn): config GET — önbellek 12 saat,
      ağ/zaman aşımı/5xx için 2 yeniden deneme (1 sn, 2 sn), 429'da deneme yok + 10 dk erteleme, geçersiz
      yanıt önbelleğe yazılmaz. Suggest POST — web gövdesi ve sınırları, 5 sn istemci zaman aşımı, yeniden deneme yok
- [x] AI yanıtı domain'de `RemoteSuggestion` (Seed = yerel slug, Generated = sunucudan kart alanları + rozet
      metinleri) + `aiQuotaLeft`; arayüz değişikliği `RemoteSuggestionRepository`'de
- [x] Hilt modülleri (dispatcher'lar, uygulama scope'u, Room, DataStore, HttpClient, Clock/Random/motor,
      @Binds). Robolectric kurulumu ayrı `napsam.android.robolectric` convention'ına taşındı (ekran görüntüsü
      testleri de onu kullanıyor)
- [x] Testler (26): içe aktarma 5 (birebir içerik, tekrar okumama, sürüm yenileme + yetim temizliği, bozuk
      içerik, istisna sızmaması), kullanıcı durumu 5, config 7 (MockEngine + gerçek DataStore + sanal zaman),
      AI isteği 6 (gövde, kurulum kimliği, seed/ai eşleme, hata tipleri, 5 sn zaman aşımı), ayarlar 3
- [ ] Ertelenen: AI önerisinin cihazda kaydedilmesi → Faz 8 (kart ve kaydetme akışıyla)

### Faz 6 eki — Supabase veri seti ile güncelleme (15.09.2026, proje sahibinin kararı)
> Soru: "Mobilde veri seti ve hafıza web'deki gibi Supabase'den mi gelsin?" Supabase salt okunur
> incelendi: 500 etkin seed öneri uygulamadaki kopyayla birebir (slug/başlık/maliyet/süre imzası
> eşleşti), bütçe kademeleri ve temalar aynı, tüm tablolarda RLS açık ve anon/authenticated yetkisiz
> (doğrudan istemci erişimi kapalı). Web hafızası tarayıcı çerezine bağlı anonim profil (50 profil,
> 337 kayıt), hesap yok. Karar: **veri seti = gömülü kopya + sunucudan güncelleme**, **hafıza cihazda**.
- [x] Web (`feature/mobile-api-v1`, yerel): `GET /api/v1/content/` (C3.4b), içerik sürümü artık veritabanı
      içeriğinin kanonik SHA-256'sı (config, içerik ucu ve dışa aktarma aynı hesap), öneri değişince önbellek düşer
- [x] Android: `content_info.embedded_version` (şema v1, yayınlanmamış olduğu için migration gerekmedi);
      indirilen içerik gömülü kopyayla ezilmez, uygulama güncellemesi gömülü kopyayı yeniden kurar;
      config yenilemesi sürüm farklıysa içeriği indirir, doğrular, tek transaction'da yazar; 429 erteleme ortak;
      havuz önbelleği içerik sürümüne bağlı

## Faz 7 — `:feature:home`
> **Yapı kararı (15.09.2026):** kart ana ekranın parçası (K4, web'de de öyle) ve kendi ekranı yok; ayrı
> `:feature:suggestion` modülü kaldırıldı, Faz 8 bu faza katıldı. Ana ekran ile Kaydedilenler'in paylaştığı
> öneri arayüzü (kart, rozet ve kategori metinleri) yeni `:core:ui` modülünde.
- [x] Web sırası (K19): başlık → karar paneli (bütçe, NAPSAM?, not, "İstersen daralt") → Canlı Öneri kartı
      → "Tamam, yapıyorum" mesajı → üç tanıtım kutusu → alt bilgi + Gizlilik bağlantısı
- [x] Açılışta hazır kart (K4); NAPSAM? seçili bütçe ve mod ile yeni öneri ister ve kart bölümüne kaydırır;
      bütçe seçmek tek başına öneri istemez (web gibi); istek sürerken yenisi başlamaz
- [x] Daraltma paneli kapalı başlar; "Kelime Seç / Yaz" sekmeleri, saate göre chip seti, "yazıldı" /
      kelime / "N kelime" özeti, yalnızca seçim varken "Temizle"; Yaz sekmesi yazı alanını odaklar,
      klavyedeki "Gönder" öneri ister (kaydırmaz)
- [x] Bağlam web isteğiyle aynı: kelime modunda ilk 8 kelime, yaz modunda yalnızca metin (`TextParser.applyTo`,
      metindeki bütçe ifadesi seçili kademeyi ezer), oturumda gösterilen son 30 hariç, kullanıcı kayıtları,
      haftalık tema `WeeklyThemeRule.activeThemeKey`
- [x] Kart (A6.4): kategori bandı + 4 rozet, başlık, kanca, "Nasıl yapılır (N adım)", gerekenler (varsa),
      olmazsa alternatifi, eylemler; ~260 ms giriş animasyonu (web `cardIn`)
- [x] Başka fikir = geç (30 gün) + yeni öneri + kart başına kaydırma · Kaydet / kayıttan çıkar (kalp dolu)
      · Tamam, yapıyorum = yapıldı + mesaj, X yalnızca mesajı gizler (K6), yeni öneri mesajı kaldırır
- [x] Web hiçbir yerde `mark_shown` çağırmıyor: "gösterildi" kaydı ve `shown_count` kaldırıldı
- [x] Rozet ve kategori metinleri `strings.xml`'de (web gibi yalnızca Türkçe), 500 öneride `badges.json`
      ile birebir; büyük harf dönüşümü cihaz dilinden bağımsız Türkçe ("CANLI ÖNERİ")
- [x] Tasarım sistemi eki: `RefineTabs`, `NapsamTextField` (DESIGN.md "Inputs"); `compose-stability.conf`
      ile `:core:model` tipleri kararlı; `contract/` test ayarı `napsam.contract.tests` convention'ı
- [x] Geçici kabuk: `MainActivity` ana ekranı açar (edge-to-edge içerik boşluğuyla); üst bar, navigasyon,
      Gizlilik ve tema düğmesi Faz 9–10
- [x] Testler: ViewModel 10 (açılış, bant, kelime filtresi ve özet, yaz modu, modların ayrılığı, son 30,
      başka fikir, kaydet, tamam mesajı, config değişimi), arayüz 3 (NAPSAM? → yeni kart, daraltma paneli,
      tamam mesajı), ekran görüntüsü 6 (açık, koyu, %200, kelime seçimi, yaz modu koyu, en uzun öneri +
      kayıtlı + mesaj), rozet paritesi 1 (500 öneri)
- [ ] Ertelenen: "Yaz" modunda AI yolu (mobil API, 1 sn iskelet, 5 sn yerel düşüş) → Faz 13, sunucu
      yayını ve AI açma kararıyla birlikte. Bugün sunucuda AI kapalı; web de metni yerel kurallarla işliyor

## Faz 8 — `:feature:suggestion`
> Faz 7'ye katıldı (kart ve eylemleri orada). AI önerisi kartı, kaydedilmesi ve "Yaz" modunda AI yolu
> Faz 13'e ertelendi (sunucu yayını + AI açma kararı).

## Faz 9 — `:feature:saved` + `:feature:privacy`
- [x] Kaydedilenler (web `saved.html`): "← Ana ekran", başlık ve alt metin, düz liste (LazyColumn, key = slug,
      en son kaydedilen önce), her satırda başlık, kanca, maliyet/süre/yer rozetleri ve "Listeden çıkar",
      boş durumda web metinleri (liste gelmeden boş durum gösterilmez)
- [x] Web'de "Sağa kaydırdıkların" yazan alt metin "Kalp ikonuna bastıkların" oldu: uygulamada kaydırma yok
      (K8) ve kaydetme kalple yapılıyor. Metin web'den sapan tek yer, proje sahibine bildirildi
- [x] Gizlilik (K18): web gizlilik sayfasının uygulamaya uyarlanmış metni (tarayıcı çerezi yerine cihazda
      saklama), "İnternet ne zaman kullanılıyor" bölümü, silme yolu sistem ayarları/uygulamayı kaldırma (B17)
      ve veri sorumlusu **Kaan Enes KAPICI · kaaneneskpc1@gmail.com** (K15)
- [x] Tasarım sistemi eki: `NapsamBackLink` (web `.navlink`)
- [x] Testler: ViewModel 2 (liste ve kaldırma, liste gelmeden boş durum yok), arayüz 3 (liste + kaldırma,
      boş durum, gizlilikte veri sorumlusu/silme/geri), ekran görüntüsü 5 (kaydedilenler açık/koyu/boş,
      gizlilik açık/koyu)

## Faz 10 — `:app`
- [x] Application (Hilt), MainActivity (edge-to-edge), sistem splash'i (yapay bekleme yok)
- [x] Navigation 3: Home, Saved, Privacy; `enableOnBackInvokedCallback` ile öngörülü geri
- [x] Hilt grafiği ilk kez uçtan uca kullanılır (Faz 6 bağlamaları); Network Security Config (cleartext kapalı)
- [x] Üst bar (logo, Kaydedilenler + sayı, tema düğmesi); seçilen tema DataStore'da
- [x] Türkçe uygulama metinleri (K19: tek dil), geniş ekranda web `--stage` (760dp) genişliğinde ortalanmış içerik
- [x] Emülatörde (Pixel_9_Pro_API_37) tam akış + ekran görüntüleri, TalkBack açıkken kullanım, uçak modu, %200 yazı, yatay
- Notlar:
  - `configRepository.refresh()` `HomeViewModel`'den `NapsamAppViewModel`'e taşındı (uygulama başına bir kez).
  - Üst bar web'deki gibi sabittir (`.header { position: fixed }`); web'de içerik barın altından kayar, burada
    bar akışın üstünde durur — metin ve sıra aynı.
  - TalkBack açıkken gezinme denendi; adb ile enjekte edilen dokunuşlar "dokunarak keşfet" yerine doğrudan
    etkinleştirdiği için sesli çıktı doğrulanamadı, etiketler erişilebilirlik ağacından okundu.

## Faz 11 — `:benchmark`
- [x] `:benchmark` modülü (`com.android.test` + `androidx.baselineprofile`), `:app`'e `baselineProfile`
      bağımlılığı ve `profileinstaller`; `benchmarkRelease` / `nonMinifiedRelease` türevleri derleniyor
      (R8 açıkken sorunsuz, APK 1,9 MB)
- [x] Baseline Profile + Startup Profile üretimi: `BaselineProfileGenerator` (açılış → kaydırma →
      Kaydedilenler → Gizlilik), `./gradlew :app:generateReleaseBaselineProfile`
- [x] Soğuk açılış ve kaydırma ölçümleri: `StartupBenchmark` (profilsiz ve profille), `ScrollBenchmark`
      (`FrameTimingMetric`); "açılış → ilk öneri" için ana ekran `ReportDrawnWhen` ile bildiriyor
- [ ] Cihazda çalıştırma ve sonuçların hedeflerle karşılaştırılması (B11) — cihaz denemeleri en sona toplandı
- Kaydırma takılması için yapılan düzeltmeler (proje sahibinin bildirimi üzerine): zemin iki kez
  çiziliyordu, artık yalnızca uygulama kabuğunda; ışıma fırçası önbelleğe alındı (`drawWithCache`);
  nabız ve ok animasyonları beste yerine çizim aşamasında okunuyor (`graphicsLayer`).

## Faz 12 — CI
- [x] PR iş akışı (`.github/workflows/pr.yml`): `check verifyRoborazziDebug` (spotless, lint, birim + parite +
      ekran görüntüsü testleri) ve `:app:assembleRelease`; hata olursa raporlar artifact
- [x] Release iş akışı (`release.yml`): yönetilen emülatörde (`pixel6Api34`, KVM) Baseline + Startup Profile,
      `bundleRelease`, AAB + mapping + profil artifact. Internal track: Play hesabı hazır olmadığı için yok
- [x] Gerçek sır yok; ileride gerekecek secret adları `docs/CI.md`'de
- [x] Ortak kurulum `.github/actions/gradle-kurulum` (JDK 21, `basic` Gradle önbelleği, 7 GB runner için
      bellek ayarı); action'lar commit SHA'sıyla sabit, Dependabot haftalık günceller
- [x] Golden'lar macOS'ta kaydedildiği için Linux'ta yeniden kaydetme akışı (`screenshots.yml`, elle)
- [ ] GitHub'da ilk çalıştırma (Faz 14): Linux ekran görüntüsü farkı, runner belleği, emülatör süresi
- Notlar: `:benchmark` CI'da (`CI=true`) yalnızca yönetilen cihazı, yerelde bağlı cihazı kullanır.
  actionlint makinede yok; YAML sözdizimi Ruby ile, Gradle komutları yerelde CI bellek ayarıyla doğrulandı.

## Faz 13 — Release hazırlığı
- [x] Sunucu kararı (16.09, K20): mobil API yayınlanır, **AI kapalı**. Uygulama yalnızca `config/` ve `content/`
      çağırır; kullanılmayan AI istemcisi (`RemoteSuggestionRepository`, `suggest` DTO'ları) ve kurulum kimliği
      kaldırıldı — metin cihazdan çıkmaz, kimlik üretilmez. Web dalının `main`'e alınması ve deploy'u proje
      sahibinde (`docs/RELEASE.md`)
- [x] Paket adı `com.kaaneneskpc.napsam` kesinleşti (K1). Görünen ad araştırması: napsam.net adlı Türkçe bir
      öneri sitesi var (ad çakışması riski); TÜRKPATENT araştırması önerildi (`docs/play/store-listing.md`)
- [x] R8: uygulamaya keep kuralı gerekmiyor — kütüphane kuralları ve Navigation 3'ün çalışma anı sınıf adı
      serileştirmesi incelendi (`docs/RELEASE.md` R8 notları). Cihazda R8 sonrası deneme en sonda
- [x] İmzalama: `keystore.properties` (git dışı) ya da ortam değişkenleri; yoksa hata ayıklama anahtarı, yarım
      yapılandırmada derleme durur. Geçici test anahtarıyla uçtan uca doğrulandı. `release.yml` imza sırlarını
      kullanıyor, yoksa uyarı + `hata-ayiklama-imzali` artifact adı
- [x] KVKK aydınlatma metni (md. 10): Gizlilik ekranına sunucu/yurt dışı aktarım (Vercel, Frankfurt), hukuki
      sebep, md. 11 hakları eklendi; kurulum kimliği ifadesi kaldırıldı. Hukukçu kontrolü önerilir
- [x] Web gizlilik sayfası (napsamweb `feature/mobile-api-v1`, yerel commit `7b18171`): Android bölümü, Vercel +
      Supabase bölgesi (ikisi de Frankfurt, `eu-central-1` doğrulandı), md. 11 hakları, veri sorumlusu; 151 web
      testi yeşil
- [x] Bulut yedeği kapalı (`allowBackup="false"` + `data_extraction_rules.xml`); cihazdan cihaza aktarım açık
- [x] Uygulama ikonu: şablondaki Android robotu yerine web favicon'u (turuncu zemin, beyaz şimşek), temalı
      ikon katmanı; splash ikonu turuncu daire içinde. Play ikonu 512×512 (`docs/play/assets/ikon-512.png`)
- [x] Data Safety taslağı (`docs/play/data-safety.md`): "veri toplanmıyor/paylaşılmıyor", IP adresi karar
      notuyla; kütüphanelerin veri davranışı
- [x] Mağaza metinleri ve uygulama içeriği formları (`docs/play/store-listing.md`); IARC için 500 öneri alkol,
      tütün ve şans oyunu açısından tarandı
- [ ] Ekran görüntüleri (≤ 2:1 en-boy), öne çıkan görsel 1024×500, R8'li sürümün cihazda denenmesi — en sonda
- [ ] Play Console hesabı, upload anahtarı oluşturma ve sırlar — proje sahibi (`docs/RELEASE.md`)

## Faz 14 — GitHub (proje sahibi "GitHub'da repo aç" dediğinde)
- [ ] `gh api user --jq .login` = `kaaneneskpc` doğrulaması; değilse dur
- [ ] `gh repo create kaaneneskpc/napsam-android --private` (K16)
- [ ] Repo içi credential helper: liste sıfırlanır, yalnızca `gh auth git-credential`
- [ ] Uzak depo eklenir, `main` push
- [ ] Faz 12'de yerelde hazırlanan GitHub Actions iş akışlarının çalıştığı doğrulanır
- [ ] Private repoda secret scanning/push protection durumunun kontrolü (ücretsiz planda kısıtlı olabilir)
