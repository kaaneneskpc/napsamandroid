# NAPSAM Android — Uygulama Planı ve İlerleme

> Spesifikasyon: `docs/napsam-android-prompt.md` (v2). Bu dosya **ne yapıldığını** takip eder;
> kurallar ve kararlar spesifikasyondadır. Her faz sonunda bu dosya güncellenir ve onay alınır.
>
> Durum: ⬜ başlanmadı · 🟨 sürüyor · ✅ bitti ve onaylandı · ⛔ karar/engel bekliyor

| Faz | Kapsam | Durum |
|---|---|---|
| 0 | Eleştiri, kararlar, yerel git | ✅ |
| 1 | Web backend hazırlığı (napsamweb, yerel dal) | 🟨 onay bekliyor |
| 2 | Proje iskeleti | ⬜ |
| 3 | `:core:model` | ⬜ |
| 4 | `:core:designsystem` | ⬜ |
| 5 | `:core:domain` + parite testleri | ⬜ |
| 6 | `:core:data` | ⬜ |
| 7 | `:feature:home` | ⬜ |
| 8 | `:feature:suggestion` | ⬜ |
| 9 | `:feature:saved` + `:feature:settings` | ⬜ |
| 10 | `:app` | ⬜ |
| 11 | `:benchmark` | ⬜ |
| 12 | CI | ⬜ |
| 13 | Release hazırlığı | ⬜ |
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
| K1 | applicationId / isim | geliştirmede `com.kaaneneskpc.napsam`, Play öncesi marka araştırması | ✅ |
| K2 | targetSdk | 37 | ✅ |
| K3 | İçerik taşıma | JSON asset + ilk açılışta içe aktarma | ✅ |
| K4 | Açılış | hazır kart (0 dokunuş) | ✅ |
| K5 | Filtre arayüzü | v1'de yok | ✅ |
| K6 | "Tamam, yapıyorum" | mesaj + isteğe bağlı Kapat | ✅ |
| K7 | Bildirim/analytics/crash SDK | v1'de yok | ✅ |
| K8 | Swipe | düğmeler birincil, swipe kısayol | ✅ |
| K9 | detekt | v1'de yok | ✅ |
| K10 | Bağımlılık yönü | data → domain | ✅ |
| K11 | Deep link | v1'de yok | ✅ |
| K12 | DataStore | kotlinx.serialization tipli | ✅ |
| K13 | Explicit API | yalnızca model + domain | ✅ |
| K14 | Metin ayrıştırıcı | önce birebir parite | ✅ |
| K15 | KVKK veri sorumlusu + e-posta | Kaan Enes KAPICI · kaaneneskpc1@gmail.com | ✅ |
| K16 | Android repo | `kaaneneskpc/napsam-android`, private | ✅ |
| K17 | Web API v1 | config + suggest | ✅ |

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
- [ ] Wrapper Gradle 9.7.1 · AGP 9.4.0 · Kotlin 2.4.20 · KSP 2.3.12 · BOM 2026.09.00
- [ ] Daemon JVM toolchain 21 · Java/Kotlin hedefi 21
- [ ] **Uyum kanıtı:** Hilt + Room + KSP ile boş bir modül derlenir (olmazsa Kotlin 2.4.10)
- [ ] `build-logic` convention plugin'leri: android-application, android-library, feature,
      compose, hilt, room, jvm-library, spotless
- [ ] Modüller: `:app`, `:core:{model,designsystem,domain,data}`, `:feature:{home,suggestion,saved,settings}`
      (`:benchmark` Faz 11'de)
- [ ] minSdk 26 · targetSdk (K2) · compileSdk 37 · R8 release'de açık
- [ ] Şablon Greeting/tema kodu kaldırılır
- [ ] `./gradlew assembleDebug spotlessCheck lintDebug testDebugUnitTest` yeşil

## Faz 3 — `:core:model`
- [ ] Enum'lar: Category (etiketleriyle), Place, Energy, WeatherNeed, Companion, StateStatus, SuggestionSource
- [ ] Suggestion, BudgetTier, WeeklyTheme, AppConfig, UserSuggestionState, SuggestionRequest bağlamı
- [ ] Rozet metinleri (A6.4) saf fonksiyon + testleri

## Faz 4 — `:core:designsystem`
- [ ] `napsam.css`'ten renk, tipografi, boşluk, yarıçap token'ları (açık/koyu)
- [ ] Manrope 400/600/700/800 gömülü
- [ ] Bileşenler: BudgetSelector, PrimaryCta, Chip, Badge, StepList, FallbackBlock,
      SuggestionCard iskeleti, SkeletonCard, CommitMessage, IconToggle
- [ ] Önizlemeler + Roborazzi: açık/koyu, %200 font

## Faz 5 — `:core:domain`
- [ ] BudgetCalculator (HALF_EVEN, amountText biçimi)
- [ ] ChipCatalog (saat → set)
- [ ] TextParser (C6, kaynak listelerden birebir)
- [ ] SuggestionEngine (C4; Random ve Clock enjekte)
- [ ] WeeklyThemeRule (C4.7)
- [ ] Repository arayüzleri
- [ ] Parite testleri (C8) + çeşitlilik istatistik testi

## Faz 6 — `:core:data`
- [ ] Room: içerik tablosu (+ sürüm), kullanıcı durum tablosu, şema export, DAO testleri
- [ ] İçerik içe aktarma (K3), sürüm değişiminde transaction'lı yenileme + test
- [ ] DataStore: tema, son bütçe, kurulum kimliği, config önbelleği
- [ ] Ktor: config (GET, retry), suggest (POST, 5 sn, retry yok), 429/5xx/zaman aşımı → Outcome
- [ ] Repository implementasyonları + Hilt modülleri + fake'li testler

## Faz 7 — `:feature:home`
- [ ] HomeUiState: bütçe, daraltma paneli (kapalı), mod, chip seçimi, metin, kart, meşgul/iskelet, kayıt sayısı
- [ ] Açılışta kart (K4), NAPSAM? akışı, oturumda son 30 hariç tutma
- [ ] ViewModel testleri (Turbine) + Compose UI testi (NAPSAM? → kart)
- [ ] Dar ekran/%200 font yerleşimi

## Faz 8 — `:feature:suggestion`
- [ ] Kart (A6.4 alanları ve biçimleri)
- [ ] Başka fikir = dismiss + yeni + kart başına kaydırma (regresyon testi)
- [ ] Kaydet / kayıttan çıkar · Tamam yapıyorum mesajı (K6)
- [ ] Swipe kısayolları (K8) ve düğme karşılıkları
- [ ] Yaz modu: yerel ayrıştırıcı; aiEnabled ise mobil API, 1 sn iskelet, 5 sn yerel düşüş

## Faz 9 — `:feature:saved` + `:feature:settings`
- [ ] Kaydedilenler: LazyColumn (key = slug), kaldır, boş durum metinleri
- [ ] Ayarlar: tema, verilerimi sil (tablo + DataStore + kurulum kimliği), aydınlatma metni,
      veri sorumlusu (K15), sürüm bilgisi

## Faz 10 — `:app`
- [ ] Application (Hilt), MainActivity (edge-to-edge), splash
- [ ] Navigation 3: Home, Saved, Settings; predictive back
- [ ] Network Security Config, strings TR/EN, uyarlanabilir yerleşim (≥ 600dp)
- [ ] Emülatörde (Pixel_9_Pro_API_37) tam akış + ekran görüntüleri, TalkBack, uçak modu

## Faz 11 — `:benchmark`
- [ ] Baseline Profile + Startup Profile üretimi
- [ ] Soğuk açılış ve kaydırma jank ölçümü (hedefler B11)

## Faz 12 — CI
- [ ] PR iş akışı: spotless, lint, unit test (parite dahil), assembleRelease
- [ ] Release iş akışı: bundle, baseline profile, (hesap hazırsa) internal track
- [ ] Gerçek sır yok; secret adları belgelenir

## Faz 13 — Release hazırlığı
- [ ] R8 keep kuralları, release derlemesi R8 sonrası emülatörde çalışıyor
- [ ] İmzalama yapılandırması (keystore repoda değil)
- [ ] Data Safety taslağı · KVKK aydınlatma metni (K15 bilgileriyle)
- [ ] Web gizlilik sayfasına Android bölümü (napsamweb)
- [ ] Marka araştırması sonucu → isim ve applicationId kesinleşir (K1)
- [ ] Mağaza metinleri ve ekran görüntüleri

## Faz 14 — GitHub (proje sahibi "GitHub'da repo aç" dediğinde)
- [ ] `gh api user --jq .login` = `kaaneneskpc` doğrulaması; değilse dur
- [ ] `gh repo create kaaneneskpc/napsam-android --private` (K16)
- [ ] Repo içi credential helper: liste sıfırlanır, yalnızca `gh auth git-credential`
- [ ] Uzak depo eklenir, `main` push
- [ ] Faz 12'de yerelde hazırlanan GitHub Actions iş akışlarının çalıştığı doğrulanır
- [ ] Private repoda secret scanning/push protection durumunun kontrolü (ücretsiz planda kısıtlı olabilir)
