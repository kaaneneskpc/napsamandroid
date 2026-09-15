# NAPSAM — Android Uygulama Yapım Promptu · v2 (canlı web sistemiyle entegre)

> **Kullanım:** Bu dosyanın tamamını Claude Code / Android Studio ajanına tek seferde ver.
> Üç bölümden oluşur:
> **BÖLÜM A — ÜRÜN** (ne yapılacak) · **BÖLÜM B — ANDROID MÜHENDİSLİK** (nasıl yapılacak) ·
> **BÖLÜM C — CANLI WEB SİSTEMİ VE ENTEGRASYON** (üretimde çalışan gerçeklik)
>
> **Öncelik kuralı:** Bölüm C, üretimde çalışan ve ölçülerek doğrulanmış sistemi anlatır.
> A veya B'deki bir varsayım C ile çelişirse **C geçerlidir**; çelişkiyi bana bildir.
> Bölüm B'deki kurallar **pazarlığa kapalıdır**. Bir kuralı ihlal etmen gerekiyorsa önce bana sor.
>
> Sürüm ve politika bilgileri **14.09.2026** tarihinde Maven depolarının metadata kayıtlarından
> ve resmi dokümanlardan doğrulanmıştır. Uygulama planı ve ilerleme takibi: `docs/PLAN.md`.

---

## v2'DE DEĞİŞENLER (v1: 12.09.2026 → v2: 14.09.2026)

| Konu | v1 varsayımı | v2 gerçeği (kaynak) |
|---|---|---|
| Backend | Supabase Edge Function + Claude API | **Django 5.2 (Vercel) + Supabase yalnızca Postgres + Gemini (şu an kapalı)** — C1, C2 |
| İstemci sözleşmesi | `POST /functions/v1/suggest`, `count: 3` | Web: `POST /api/suggest/` tek öneri döner. Android için **mobil API v1** gerekir (CSRF engeli) — C3 |
| Supabase anon key | İstemcide tutulur | **İstemci Supabase'e hiç erişemez ve erişmemeli** (RLS açık, anon/authenticated yetkileri kaldırılmış) — C2 |
| Seed içerik | 60 örnek yazılacak | **500 öneri hazır ve denetimli** (`napsamweb/core/fixtures/suggestions.json`), tek doğruluk kaynağı — C5 |
| Öneri motoru | Tarif yok | **Web motoru birebir port edilir** (sabitler, filtre sırası, gevşetme, örnekleme) + parite testleri — C4, C8 |
| Bütçe | "Remote config'ten gelir" | Oranlar ve 28.075 TL asgari ücretle hesaplanan tutarlar sabitlendi — A6.2 |
| Chip seti | 20–25 chip | Günün saatine göre 8–10 chip, 4 zaman dilimi — C7 |
| Haftalık tema | Genel tanım | 4 tema anahtarı; **web'de rotasyon hatası var** — A6.6 |
| Ana ekran | Açılış → 1 dokunuş → kart | Web açılışta **hazır kartla** gelir (0 dokunuş); filtre paneli kapalı başlar — A6, C9 |
| Hız sınırı | Belirtilmemiş | Vercel WAF: `/api/*` için **IP başına 60 sn'de 100 istek**, aşan 429 alır — C2 |
| Sürümler | Kotlin 2.3.21, AGP 9.2, BOM 2026.08.00 | Kotlin 2.4.20, AGP 9.4.0, BOM 2026.09.00, Gradle ≥9.6 — B1 |
| İsim | Belirtilmemiş | **"napsam" adıyla napsam.net'te benzer bir Türk öneri uygulaması var** (marka riski) — C10 |
| Açık kararlar | — | Faz 0'da onaylanacak kararlar listesi — C11 |

---
---

# BÖLÜM A — ÜRÜN

## A1. ROL VE GÖREV

Sen kıdemli bir Android mühendisisin. Türkiye için, kasıtlı olarak sade bir "ne yapsam"
uygulamasının **Android istemcisini** yazacaksın. Ürünün **web sürümü üretimde çalışıyor**
(https://napsam.vercel.app). Android, web'in ürün kararlarını, içeriğini ve öneri motorunu
devralır; kendi başına yeni bir ürün icat etmez.

Bu projede en büyük risk özellik eksikliği değil, **özellik ve mimari fazlalığı**.
Bir özellik veya soyutlama eklemeyi önerdiğinde, önce neyi çıkardığını söyle.

## A2. ÜRÜN ÖZETİ

**Tek cümle:** "Ne yapsam ya" diye takılan insana, tek dokunuşla, bütçesine uyan, şu an ve
bulunduğu yerde yapabileceği tek bir somut fikir veren uygulama.

**Başarı tanımı:** Kullanıcı uygulamayı açar, 10 saniye içinde fikri alır, uygulamayı
**kapatır** ve o şeyi yapar.

**Ne DEĞİL:** Etkinlik/bilet uygulaması değil · sosyal ağ değil · yapılacaklar listesi değil ·
takvim değil · rehber değil · kelime jeneratörü değil.

**Gerçek rakip:** Başka bir uygulama değil; kullanıcının telefonu eline alıp Instagram'a
girmesi. Bu refleksi kırmanın tek yolu daha hızlı olmak.

## A3. SADELİK ANAYASASI — pazarlık yok

```
1.  AÇILIŞ → FİKİR arası maksimum 1 dokunuş. (Web: açılışta hazır kart, 0 dokunuş — C11-K4)
2.  Onboarding YOK. Hoş geldin ekranı yok, tanıtım slaytı yok, anket yok.
3.  İlk açılışta üyelik YOK. İzin isteme YOK. Uygulama anonim çalışır.
4.  Ekranda aynı anda en fazla 1 birincil eylem bulunur.
5.  Toplam ekran sayısı 4'ü geçmez.
6.  Filtreler OPSİYONEL. Hiçbiri doldurulmadan uygulama tam çalışır ve kullanıcı bunu
    NET anlar: filtre alanı kapalı başlar ("İstersen daralt — kelime seç ya da yaz").
7.  Bir öneri tek ekrana sığar. Detay sayfası yok, "devamını oku" yok.
8.  Metin kısa: başlık ≤ 6 kelime, kanca tek cümle, adım 2–3, "olmazsa" alternatifi zorunlu.
9.  Kısa oturum İYİDİR. Kullanıcıyı uygulamada tutmaya çalışma, dışarı çıkarmaya çalış.
    Bu üründe "engagement" başarı değil, hatadır.
10. Bildirim günde en fazla 1. Kapatması tek dokunuş. (v1'de olup olmayacağı: C11-K7)
11. Kullanıcıya teknik/pazarlama jargonu gösterilmez. Örnek: "algoritma" kelimesi web
    metninden bilinçli olarak çıkarıldı; Android metinlerine de girmez.
```

**v1'de AÇIKÇA YAPILMAYACAKLAR:**
profil fotoğrafı · arkadaş ekleme · yorum · beğeni · takip · sohbet · streak zorlaması ·
rozet · seviye · puan · günlük giriş ödülü · haber akışı · arama geçmişi ekranı ·
istatistik paneli · tema mağazası · reklam · üyelik/giriş

## A4. KONUM BAĞIMSIZLIK KURALI

**Uygulama şehir, ilçe veya bölge ayrımı YAPMAZ.** Bir öneri Türkiye'nin her yerinde
yapılabilir olmalı: evde, dışarıda, parkta, sokakta, iş çıkışında — fark etmez.

**Her öneri üç kaptan birine girer** (`place` alanı):
```
ev        Evde, hiçbir yere gitmeden yapılabilir.            (havuzda 264)
yakin     Yürüme mesafesinde — park, sokak, market, kafe,     (havuzda 116)
          kütüphane, komşu, durak. Her yerleşim yerinde var.
mekanli   Belirli TİPTE mekan gerektirir (sinema, müze, havuz). (havuzda 120)
          Öneri mekanın TÜRÜNÜ söyler (`venue_type`), ismini söylemez.
```

| ❌ Yanlış | ✅ Doğru |
|---|---|
| "Kadıköy sahilinde yürü" | "En yakın su kenarına yürü; yoksa en uzun caddeyi baştan sona yürü" |
| "Moda'da kahvaltı yap" | "Hiç girmediğin bir esnaf lokantasında kahvaltı et" |
| "Ankara Kalesi'ne çık" | "Bulunduğun yerin en yüksek noktasını bul, oraya çık" |
| "Vapurla karşıya geç" | "Toplu taşımaya bin, son durağa git, yürüyerek dön" |

- Önerinin özü **eylem**dir, **yer** değil.
- Sadece büyük şehirde işe yarayan öneri havuza **girmez**.
- Her önerinin `fallback` alanı **zorunlu**.
- **Veri modelinde, istekte ve yanıtta şehir/ilçe/koordinat alanı YOKTUR ve eklenmeyecektir.**
- Bu kural web'de otomatik doğrulayıcıyla zorlanır (C5.3). Android içerik üretmez; içeriği
  bu doğrulayıcıdan geçmiş havuzdan alır.

## A5. ARAŞTIRMA DAYANAĞI

| Bulgu | Kaynak | Ürüne etkisi |
|---|---|---|
| Günlük boş zaman: 4s 34dk | TÜİK Zaman Kullanım Araştırması | Öneriler 15dk–3sa aralığında |
| Boş zamanın %39,5'i TV/video, %29'u sosyal yaşam, %15,6'sı dinlenme | TÜİK | Ekran dışı öneri öncelikli |
| Gençler maliyet kısıtıyla ağırlıklı kafe + AVM'de sosyalleşiyor; tiyatro/konser erişimi %2-4 | bianet derlemesi, 2026 | **Ücretsiz varsayılan mod** |
| Yeme-içme işletmesi 62 bin (2010) → 159 bin (2025) | TEPAV | Kafe her yerde ama pahalı |
| 2026 net asgari ücret 28.075 TL | ÇSGB | Bütçe kademesi referansı; **hardcode etme** |

> Not: Bu tablodaki rakamlar v1 dokümanından aktarılmıştır; v2'de yeniden doğrulanmadı.

**Rakipler:** *Ne Yapsam* (karar çarkı), *Bugün Ne?* (kategori bazlı günlük öneri),
*Ne Yesem?* (ikili eleme). Global: Bored API tabanlılar, *Stop Being Bored*,
*Bored No More* (swipe + favori). **Ayrıca:** napsam.net — aynı isimle "AI destekli
kişiselleştirilmiş öneriler (film, dizi, yemek, müzik)" sunan Türkçe bir ürün (C10).

**Boşluk = bizim farkımız:** hiçbiri TL bütçesini gerçekten dikkate almıyor · hiçbiri
"nasıl yapılır" demiyor · hiçbiri Türkiye'nin gündelik dokusunu tanımıyor.

**Dürüst risk:** Bu kategorinin problemi retention; yenilik hissi 5-7 günde ölüyor. v1'de
retention'ı ayakta tutacak tek şey **öneri kalitesi**.

## A6. ÇEKİRDEK AKIŞ (web'de üretimde doğrulanmış hâli)

```
┌──────────────────────────────────────────┐
│  napsam.                  Kaydedilenler ② ◐│  ← üst bar: kayıt sayısı rozeti, tema
│                                            │
│  ● KARAR VERME MOTORU · SIFIR TEREDDÜT     │
│        “Ne yapsam ya?”                      │
│  Sonsuz listelere ve kararsızlığa son.     │
│  10 saniyede somut, uygulanabilir tek      │
│  bir eylem.                                │
│ ┌────────────────────────────────────────┐ │
│ │ BÜTÇE DURUMU                            │ │
│ │ [₺ Bedava][₺ Az][₺ Orta][₺ İyi][₺ Bol] │ │  ← 5 düğme, altında tutar metni
│ │ ┌────────────────────────────────────┐ │ │
│ │ │          ⚡ NAPSAM?                 │ │ │  ← TEK BİRİNCİL EYLEM
│ │ └────────────────────────────────────┘ │ │
│ │ TEK DOKUNUŞ · TEK KESİN KARAR · ...     │ │
│ │ ⌄ İstersen daralt — kelime seç ya da yaz│ │  ← KAPALI başlar
│ └────────────────────────────────────────┘ │
│  CANLI ÖNERİ / HEMEN ŞİMDİ BAŞLA           │
│ ┌────────────────────────────────────────┐ │
│ │ KATEGORİ         [₺0 Bedava][25-50 dk]  │ │
│ │                  [Evde][Tek başına]     │ │
│ │ Başlık (≤6 kelime)                      │ │
│ │ Kanca (tek cümle)                       │ │
│ │ NASIL YAPILIR (3 ADIM)  ① ② ③           │ │
│ │ GEREKENLER (varsa)                      │ │
│ │ ⇄ OLMAZSA ALTERNATİFİ …                 │ │
│ │ [✓ TAMAM, YAPIYORUM] [↻ Başka fikir] [♡]│ │
│ └────────────────────────────────────────┘ │
└──────────────────────────────────────────┘
```

### A6.1 Üç giriş modu

**A) NAPSAM? (ana yol, `mode=quick`)** — filtre yok, soru yok. Saat arka planda biliniyor.
Web'de ana ekran açılır açılmaz bir öneriyle gelir; NAPSAM? yeni öneri ister.

**B) Kelime seç (`mode=chips`)** — günün saatine göre değişen **8–10 chip** (tam liste C7).
Seçilen kelime **sert filtredir**: yalnızca o etikete sahip öneriler havuza girer. Hiçbiri
tutmazsa kısıt gevşer (C4.5), kullanıcı boş ekran görmez. İstekte en fazla 8 kelime.

**C) Yaz (`mode=text`)** — "3 saatim var param yok", "canım deniz gördü". Metin, AI olmadan
çalışan kural tabanlı ayrıştırıcıyla bağlama çevrilir (C6). AI katmanı yalnızca bu modda ve
yalnızca sunucuda açıksa denenir (C2.4). En fazla 280 karakter. Klavyede "Gönder" tetikler.

**Daraltma paneli davranışı (web'den):** kapalı başlar · açıkken "Kelime Seç / Yaz" sekmeleri ·
özet rozeti: 1 kelime → kelimenin kendisi, 2+ → "N kelime", yazı modu doluysa → "yazıldı" ·
"Temizle" düğmesi yalnızca seçim varken görünür · yazı alanı ipucu: `Örn: “3 saatim var param yok”`.

### A6.2 Bütçe — tek dokunuş, varsayılan "Bedava"

Tutarlar **koda gömülmez**; kademeler **net asgari ücrete oranla** saklanır, tutar çalışma
anında hesaplanır: `tutar = round_half_even(oran × ücret / 10) × 10`.
Aşağıdaki değerler, web'in bugün kullandığı yapılandırmadır (`seed.py`, ücret 28.075 TL):

| key | Etiket | İpucu | ratio_min | ratio_max | Hesaplanan | Kartta görünen |
|---|---|---|---|---|---|---|
| `bedava` | Bedava | — | 0 | 0 | 0–0 | `0 TL` |
| `az` | Az | çay-simit | 0 | 0.005343 | 0–150 | `~1-150 TL` |
| `orta` | Orta | bir kahve, bir tatlı | 0.005343 | 0.017809 | 150–500 | `~150-500 TL` |
| `iyi` | İyi | dışarıda yemek | 0.017809 | 0.053428 | 500–1.500 | `~500-1.500 TL` |
| `bol` | Bol | etkinlik, aktivite | 0.053428 | yok | 1.500– | `1.500 TL+` |

- **Tavan sert kuraldır:** `cost_max ≤ amount_max`; bütçe hiçbir koşulda gevşetilmez.
- **Kademe bir tercihtir:** ücretli kademede o bandı gerçekten kullanan (`cost_max > amount_min`)
  öneriler varsa yalnızca onlar gösterilir (C4.3). Ölçülen sorun: bu kural yokken "Bol"
  seçene %82 bedava öneri dönüyordu.
- Uygulamada bu tablonun **yerel varsayılanı** bulunur; güncel değerler mobil API'den (C3)
  gelir. Config gelmezse uygulama yerel varsayılanla tam çalışır.
- Havuz dağılımı: Bedava 250 · Az 100 · Orta 75 · İyi 45 · Bol 30.

### A6.3 Opsiyonel filtreler

Motor ve API dört filtreyi destekler: **süre** (dakika, üst sınır) · **kiminle**
(`solo|couple|friends|family|kids`) · **enerji** (`low|medium|high`) · **ev/dışarı** (`ev|disari`).
**Web'de bu filtrelerin arayüzü YOKTUR.** Android v1'de arayüz eklenip eklenmeyeceği: C11-K5.

### A6.4 Öneri kartı — tek ekran

Alanlar ve biçimler web'le **aynı** olmalı (`napsamweb/core/models.py` + `serializers.py`).
Rozetin **yapısı** `:core:model`'de (`Suggestion.badges()`: CostBadge, DurationBadge, PlaceBadge,
CompanionBadge), **metni** arayüz kaynaklarında (strings.xml, TR + EN) üretilir; yapı kuralları
500 öneride `contract/parity/badges.json` ile test edilir (Faz 3).

| Öğe | Kural |
|---|---|
| Kategori etiketi | `categoryLabel` (A7.1 etiketleri), kategoriye özgü görsel bant |
| Maliyet rozeti | `cost_max == 0` → `₺0 Bedava` · `cost_note` varsa `~₺{cost_max} {cost_note}` · yoksa `~₺{cost_max}` |
| Süre rozeti | min = max → `{max} dk` · değilse `{min}-{max} dk` |
| Yer rozeti | `ev` → `Evde` · `yakin` → `Dışarı / Yakın` · `mekanli` → `venue_type` ilk harfi büyük (Türkçe locale ile: "İnternet kafe") |
| Kiminle rozeti | liste boş veya ≥4 değer → `Farketmez` · değilse ilk 2 etiketi ` / ` ile birleştir (Tek başına · Çift · Arkadaşlarla · Ailece · Çocuklu) |
| Başlık / kanca | ≤6 kelime / tek cümle |
| Adımlar | başlık metni `Nasıl yapılır (N adım)`, 2–3 numaralı adım |
| Gerekenler | `required_items` boş değilse `Gerekenler` başlığıyla en fazla 3 öğe |
| Olmazsa | `Olmazsa alternatifi` başlığı + `fallback` — her kartta zorunlu |
| Eylemler | `Tamam, yapıyorum` (birincil) · `Başka fikir` · kalp (Kaydet / Kayıttan çıkar) |

**Davranışlar:**
- **Başka fikir** = mevcut öneriyi 30 gün boyunca geç (dismiss) + yeni öneri iste.
  Yeni kart geldiğinde **kartın başı mutlaka görünür olmalı** (web'de bu chat'te düzeltilen
  hata: kullanıcı altta kalıp yeni öneriyi göremiyordu). Android'de kaydırma konumu sıfırlanır.
- **Tamam, yapıyorum** = öneriyi "yapıldı" işaretle ve şu mesajı göster:
  **"Harika seçim. Şimdi ekranı kapat."** / **"Bu uygulamada iyi bir oturum, kısa bitendir."**
  Mesajın kapatma (X) düğmesi yalnızca mesajı gizler; uygulama kapatılmaz (C11-K6, web gibi).
- Bekleme 1 saniyeyi geçerse spinner değil **iskelet kart**. Kart geçiş animasyonu ~260 ms.
- Bir oturumda gösterilen son **30 öneri** tekrar gösterilmez.
- Kaydırma (swipe) hareketi **yoktur**; tüm eylemler görünür düğmelerdir (C11-K8, web gibi).

### A6.5 Kaydedilenler
Başlık `Kaydedilenler` · alt metin `Sağa kaydırdıkların. Klasör yok, etiket yok — sadece liste.` ·
her satırda başlık, kanca, maliyet/süre/yer rozeti ve kaldır (X) düğmesi · boş durumda
`Henüz bir şey kaydetmedin.` / `Bir öneriyi beğendiğinde kalp ikonuna bas, burada birikir.` ·
üst barda kayıt sayısı rozeti. **Android v1'de kayıtlar yalnızca cihazdadır** (web ile senkron yok).

### A6.6 Haftalık tema (arka planda, UI yok)
Tema anahtarları: `yuru` (Bu hafta yürü) · `elini_kullan` (Bu hafta elini kullan) ·
`ekransiz` (Bu hafta ekransız bir akşam) · `duzen` (Bu hafta bir şeyi düzene sok).
Önerinin `theme_tags` alanı temayı içeriyorsa puana **+1.0** eklenir. Bu ağırlık bilinçli olarak
seçim penceresinden (1.5) küçüktür; büyük olduğunda tema sert filtre gibi davranıp seçimleri
6 öneriye sıkıştırmıştı.

> **Web'deki hata Faz 1'de düzeltildi (yerel dal, canlıda değil):** Web aktif temayı "başlangıç
> tarihi bugünden önce olan en yeni tema" diye seçiyordu ve temalar yalnızca seed çalışırken
> tarihleniyordu; seed haftasındaki tema kalıcı olarak aktif kalıyordu. Artık ISO hafta
> numarasına dayalı deterministik rotasyon var; Android aynı kuralı uygular (C4.7).

### A6.7 Ekran metinleri (web'de canlı, birebir)
- Üst etiket: `Karar verme motoru · Sıfır tereddüt`
- Başlık: `“Ne yapsam ya?”`
- Alt metin: `Sonsuz listelere ve kararsızlığa son.` + `10 saniyede somut, uygulanabilir tek bir eylem.`
- Düğme altı: `Tek dokunuş · Tek kesin karar · Sonsuz kaydırma yok`
- Kart üstü: `Canlı Öneri / Hemen şimdi başla`
- Alt bilgi: `Konum bağımsız, reklamsız, hemen harekete geçiren Türkiye boş zaman rehberi.`
- Üç tanıtım kutusu, kartın altında alt alta (C11-K19):
  `01 / Kararsızlık yok` · `Tekil odak` · `Seçenekler çoğaldıkça felç olursun. NAPSAM tek ve net bir yön önerir.`
  `02 / Sıfır maliyet` · `Varsayılan: 0 TL` · `Zaman geçirmek parayla ölçülmez. Tüm öneriler önce bütçesiz kurgulanır.`
  `03 / Anında hareket` · `Somut adımlar` · `Soyut tavsiye değil, hemen şimdi yapabileceğin sıralı adımlar.`
- Alt bilginin sonunda `Gizlilik` bağlantısı → Gizlilik ekranı.

**Ana ekran sırası (web ve Stitch tasarımı, C11-K19):** üst bar → üst etiket, başlık, alt metin →
bütçe paneli (bütçe, NAPSAM?, düğme altı notu, "İstersen daralt") → `Canlı Öneri` kartı →
tanıtım kutuları → alt bilgi.

## A7. TÜRKİYE İÇERİK MODELİ

### A7.1 Kategoriler (anahtar → etiket, havuzda her birinde 31–42 öneri)
```
ev_ici → Ev içi          mutfak → Mutfak           disari_yakin → Dışarı / yakın
yeme_icme → Yeme-içme    hareket → Hareket         kultur → Kültür
sosyal → Sosyal          ogrenme → Öğrenme         uretme → Üretme
doga → Doğa              kendine_bakim → Kendine bakım  iyilik → İyilik
oyun → Oyun              ufak_isler → Ufak işler
```

### A7.2 Kültürel bağlam
İçerikte `cultural_tags` alanı var (`bayram` 26 · `pazar_gunu` 13 · `ramazan` 8 · `ay_sonu` 7)
ancak **web motoru bu alanı şu an kullanmıyor**. Android v1'de de kullanılmaz (parite).
`seasonality` ay numarası listesidir (58 öneride dolu); boş liste = tüm yıl.

**Ekonomik kural:** Kullanıcı bütçe seçmediyse varsayılan `bedava`dır ve ücretli öneri hiç gösterilmez.

### A7.3 Dil ve ton
- "Sen" dili, samimi ama laubali değil. Kısa cümle.
- Koçvari dil **YASAK**: "Hayatını değiştir!" ❌ · İngilizce devşirme **YASAK**: "mindful", "self-care" ❌
- ✅ *"Dolaptaki üç şeyle bir şey pişir. Tarife bakma."*
- Yasaklı ifade, içerik ve yer listeleri web doğrulayıcısında tanımlıdır (C5.3).
- Öneri içerikleri **Türkçedir ve çevrilmez**; `values-en` yalnızca arayüz metinleri içindir.

## A8. ÖNERİ MOTORU MİMARİSİ

```
Katman 1 — YEREL SEED MOTORU        ana yol, internetsiz
  500 öneri uygulamayla birlikte gelir (C5.4).
  Seçim algoritması web'deki core/engine.py'nin birebir portudur (C4).
  Sıfır maliyet, sıfır halüsinasyon, sıfır ağ beklemesi.

Katman 2 — AI (sunucuda, opsiyonel)  yalnızca "Yaz" modu
  Django → Google Gemini (gemini-3.5-flash-lite). ŞU AN KAPALI (GEMINI_API_KEY yok).
  İstemci AI sağlayıcısını hiç görmez; yalnızca mobil API'yi çağırır (C3).
  Kota sunucuda: günlük global 200 · IP başına 15 · kimlik başına 5.
  Hata, zaman aşımı, 429 veya AI kapalıysa → sessizce Katman 1.

Katman 3 — Mekan çözümleme           v1'DE YOK (web'de de yok). Konum izni istenmez.
```

## A9. İSTEMCİ SÖZLEŞMESİ
v1'deki `POST /functions/v1/suggest` sözleşmesi **geçersizdir**. Mevcut web API'si ve
Android için tasarlanan mobil API v1: **C3**.

---
---

# BÖLÜM B — ANDROID MÜHENDİSLİK KURALLARI

## B1. TOOLCHAIN VE SÜRÜMLER (doğrulama: 14.09.2026)

```
Kotlin                2.4.20        (07.09.2026) · Compose derleyici eklentisi aynı sürüm
KSP                   2.3.12        (Kotlin'den bağımsız sürümleniyor; Faz 2'de Kotlin 2.4.20
                                     ile Room + Hilt + serialization derlenerek kanıtlandı)
Compose BOM           2026.09.00    (09.09.2026 · Compose 1.12.x, Material3 1.4.x)
AGP                   9.4.0         (Eylül 2026 · en az Gradle 9.6.0 · en yüksek API 37 ·
                                     yerleşik Kotlin: kotlin-android eklentisi kullanılmaz)
Gradle                9.7.1         (≥ 9.6.0 zorunlu)
JDK                   21            (makinede OpenJDK 21.0.12; Gradle daemon toolchain da 21)
compileSdk            37
targetSdk             37            (Play şartı ≥ 36 · C11-K2 onaylı)
minSdk                26            (Android 8.0)
```

**Kütüphaneler (Maven metadata, 14.09.2026 itibarıyla en yeni stabil):**
```
Hilt (com.google.dagger)            2.60.1   · AGP 9 gerektirir
androidx.hilt                       1.4.0    (hilt-lifecycle-viewmodel-compose)
Room + androidx.room Gradle eklentisi 2.8.5
DataStore                           1.2.1
Navigation 3                        1.1.7    (1.2.0-rc01 var; stabil kullanılır)
Lifecycle (runtime-compose, viewmodel-navigation3)  2.11.0
activity-compose                    1.13.0
core-ktx                            1.19.0
core-splashscreen                   1.2.0
profileinstaller                    1.4.1
benchmark-macro / baselineprofile   1.5.0
uiautomator                         2.4.0
desugar_jdk_libs                    KULLANILMAZ (minSdk 26'da java.time yerleşik)
Ktor client                         3.5.2
kotlinx.serialization-json          1.11.0
kotlinx.coroutines                  1.11.0
kotlinx.collections.immutable       0.5.2
Turbine 1.2.1 · Roborazzi 1.74.0 · Robolectric 4.17 · JUnit 4.13.2
Timber 5.0.1 · LeakCanary 2.14
Spotless 8.10.2 + ktlint 1.8.0 · compose-rules 0.6.6
detekt 1.23.8  (Kotlin 2.4 ile uyumu şüpheli, 2.0 yalnızca alpha — karar: C11-K9)
```

**Neden bu değerler:**
- **Play hedef API şartı** (resmi sayfa, 14.09.2026): 31 Ağustos 2026'dan beri yeni
  uygulamalar ve güncellemeler **API 36 veya üstünü** hedeflemek zorunda; uzatma talebi
  1 Kasım 2026'ya kadar. Android 17 (API 37) stabil yayında.
- **API 37 hedeflemenin bu uygulamaya etkisi:** 600dp ve üstü ekranlarda yön ve en-boy
  kısıtlamaları yok sayılır ve bundan çıkılamaz (uygulama uyarlanabilir olmalı) · Certificate
  Transparency varsayılan açık · TLS'de ECH varsayılan açık · yansıma ile `static final`
  değiştirilemez. Widget, SMS, Bluetooth, arka plan ses ya da yerel ağ kullanılmadığı için
  diğer değişiklikler etkilemez.
- `compileSdk 37` Compose 1.12 ve Navigation 3 1.2 hattının zorunlu kıldığı seviye.
- `minSdk 26`: Android 8 altı pratikte yok; 24'e inmek WorkManager, bildirim kanalı ve
  adaptive icon tarafında gereksiz dallanma yaratır. (Şablon 24 ile geldi → 26'ya çekilir.)

**Mevcut Android Studio şablonu (`/StudioProjects/napsam`, 14.09.2026):** AGP 9.3.2 ·
Kotlin 2.2.10 · BOM 2026.02.01 · Gradle 9.5.0 · minSdk 24 · targetSdk 37 · Java 11 ·
Gradle daemon toolchain 25 · `applicationId = com.kaaneneskpc.napsam` · AGP 9 DSL
(`compileSdk { version = release(37) }`, `optimization { enable }`, `src/main/keepRules/`) ·
foojay resolver · configuration cache açık · `FAIL_ON_PROJECT_REPOS` açık. Git deposu değil.
**Faz 2'de (14.09.2026) B1–B2'ye göre dönüştürüldü**; AGP 9 DSL'i korundu. AGP 9 kendi başına
Kotlin Gradle eklentisini 2.2.10'da tutar; kök build'de `kotlin-jvm` 2.4.20 ve `ksp` 2.3.12
bildirilerek yükseltilir (`./gradlew buildEnvironment` ile doğrulanır).

**Sürüm yönetimi:**
- Tüm bağımlılıklar `gradle/libs.versions.toml` üzerinden. Modül dosyalarında **çıplak sürüm yasak**.
- Ortak yapılandırma `build-logic/` içinde **convention plugin** olarak.
- `dependencyResolutionManagement { repositoriesMode.set(FAIL_ON_PROJECT_REPOS) }`

## B2. MODÜL YAPISI

> **Aşırı modülerleşme yasak.** 4 ekranlık bir uygulamayı 15 modüle bölmek derleme süresini
> iyileştirmez, sadece gezinmeyi zorlaştırır.

```
:app                      Application, MainActivity, DI graph kökü, navigasyon host
:core:model               Saf Kotlin data class + enum. Android bağımlılığı YOK.
:core:designsystem        Tema, renk, tipografi, boşluk, temel bileşenler
:core:data                Repository impl, Room, DataStore, Ktor, mapper, içerik içe aktarma
:core:domain              Öneri motoru (C4), metin ayrıştırıcı (C6), chip seti (C7),
                          bütçe hesabı, repository arayüzleri
:feature:home             Ana ekran
:core:ui                  Öneriye özgü ortak arayüz: kart, rozet ve kategori metinleri (home + saved)
:feature:saved            Kaydedilenler
:feature:privacy          Gizlilik (web gizlilik sayfası + veri sorumlusu, C11-K18)
:build-logic              Convention plugin'ler
:benchmark                Macrobenchmark + Baseline Profile üretimi
```

**Bağımlılık yönü kuralları:**
```
:app         → :feature:*, :core:*
:feature:*   → :core:model, :core:designsystem, :core:domain
:core:data   → :core:domain (arayüzleri uygular), :core:model
:core:domain → :core:model
:core:model  → hiçbir şey
```
> v1'de `:core:domain → :core:data` yazıyordu ve aynı anda "repository arayüzleri domain'de"
> deniyordu; bu döngüsel bir çelişkiydi. v2'de arayüzler domain'de, uygulamaları data'da.
> Faz 0 onayına tabidir (C11-K10).

- `:feature:*` modülleri **birbirine bağımlı olamaz**; ihtiyaç `:app` navigasyonuyla çözülür.
- `:core:data` implementasyonu `:feature:*` tarafından **doğrudan görülmez**.
- Öneri motoru `:core:domain`'de **saf Kotlin**dir (Android tipi yok); JVM'de test edilir.

## B3. MİMARİ KURALLARI

**Katmanlı mimari + Unidirectional Data Flow (UDF).**

```
UI (Composable)  →  ViewModel  →  UseCase (opsiyonel)  →  Repository  →  DataSource
     ▲                                                                       │
     └───────────────────── immutable UiState ◄─────────────────────────────┘
```

1. Composable'lar **stateless** yazılır. State hoisting zorunlu.
2. ViewModel dışa **tek bir `StateFlow<UiState>`** açar.
3. `UiState` **immutable** `data class`; liste alanları `ImmutableList` veya `@Immutable`.
4. Tek seferlik olaylar `Channel` → `Flow` ile veya `consumed` bayrağıyla. `LiveData` **yasak**.
5. ViewModel'e Composable referansı, Context veya framework tipi geçilmez.
6. UseCase katmanı **zorunlu değil**; tek satır delegasyon yapan UseCase yazma.
7. Repository her zaman arayüz; implementasyon `:core:data`'da.
8. Hata yönetimi: repository `Result<T>` veya sealed `Outcome<T>` döner. **Exception istemci
   katmanına sızmaz.**
9. Tek Activity. Fragment **kullanılmaz**. XML layout **kullanılmaz**.

## B4. COMPOSE KURALLARI

1. **Edge-to-edge zorunlu.** `enableEdgeToEdge()`; inset'ler `WindowInsets` ile, çift padding yok.
2. **Predictive back zorunlu.** `BackHandler` / `PredictiveBackHandler`; opt-out **yasak**.
3. Flow toplama daima `collectAsStateWithLifecycle()`. Düz `collectAsState()` **yasak**.
4. Composable gövdesinde yan etki **yasak**; `LaunchedEffect`, `DisposableEffect`, `SideEffect`.
5. `remember` anahtarları eksiksiz; `remember { }` içinde ağ/disk işi yasak.
6. `Modifier` her public composable'da **ilk opsiyonel parametre**, varsayılanı `Modifier`.
7. Liste render'ında `LazyColumn` + **stabil `key`** (öneri için `slug`).
8. Compose derleyici metrikleri açık:
   ```kotlin
   composeCompiler {
       reportsDestination = layout.buildDirectory.dir("compose_compiler")
       metricsDestination = layout.buildDirectory.dir("compose_compiler")
   }
   ```
9. `derivedStateOf` sadece gerçek türetilmiş hesaplamada.
10. Animasyon 200–300 ms; yalnızca kart geçişi ve iskelet. Fazlası md.9'a aykırı.
11. Tüm boyut/renk/tipografi `:core:designsystem` token'larından. Çıplak `Color(0xFF...)`,
    `16.dp` **yasak** (designsystem tanımları hariç). Token kaynağı: C9.4.

## B5. NAVİGASYON

**Navigation 3 (`androidx.navigation3` 1.1.7).**
```
:app içinde tek NavDisplay.
Rotalar @Serializable data class / data object olarak :app içinde.
Rotalar: Home, Saved, Privacy (öneri kartı ana ekranın parçasıdır — C11-K4)
Ayarlar ekranı YOK; tema düğmesi üst barda (web gibi — C11-K18)
```
- Feature modülleri **kendi rotalarını tanımlamaz**; `:app` bağlar.
- Navigasyon kararı **state'ten** türer.
- Deep link / paylaşım: web'de öneri başına bir sayfa YOK → v1 kararı C11-K11.

## B6. EŞZAMANLILIK

1. `viewModelScope` / `lifecycleScope` dışında scope yok. **`GlobalScope` yasak.**
   (Uygulama ömürlü iş gerekiyorsa DI ile `@ApplicationScope`.)
2. Ana thread'de `runBlocking` **yasak**.
3. Dispatcher'lar DI ile (`@IoDispatcher`, `@DefaultDispatcher`); doğrudan `Dispatchers.IO` yok.
4. Repository fonksiyonları **main-safe**.
5. `flowOn` sadece upstream'de.
6. `stateIn(scope, SharingStarted.WhileSubscribed(5_000), initial)` standart kalıp.
7. Uzun döngülerde `ensureActive()` / `yield()`.

## B7. VERİ KATMANI — OFFLINE-FIRST

**Ana akış internetsiz tam çalışır.** Ağ yalnızca iki şey için kullanılır: config yenileme ve
(sunucuda açıksa) "Yaz" modunda AI.

```
İçerik (Room, KSP)
  - Kaynak: napsamweb/core/fixtures/suggestions.json (500 öneri, C5).
  - Faz 1'deki dışa aktarma betiğiyle Android'e taşınır; Supabase'de içerik değişince mobil API'den
    indirilir (C3.4b). İçerik sürümü = içeriğin kanonik JSON'unun SHA-256'sı.
  - Uygulamaya taşıma biçimi (hazır .db asset'i mi, JSON asset + ilk açılışta içe aktarma mı):
    C11-K3. İçerik tablosu sürüm değişince tek transaction'da yenilenir.
  - Kullanıcı verisi (kaydedilen, geçilen + dismissed_until, yapılan, son görülenler) içerik
    tablosundan AYRI tutulur ve içerik güncellemesinde silinmez.
  - Şema versiyonlanır, migration yazılır. Kullanıcı verisinde destructive migration YASAK.
  - exportSchema = true, schema dosyaları repoda tutulur.
  - Tüm DAO sorguları suspend veya Flow döner.

Tercihler (DataStore)
  - Tema (sistem/açık/koyu), kurulum kimliği (C3.2), config önbelleği. Bütçe web gibi hatırlanmaz,
    her açılışta "Bedava" (web yalnızca temayı saklıyor).
  - Proto mu, kotlinx.serialization ile tipli DataStore mu: C11-K12.
  - SharedPreferences YASAK.

Remote config
  - Kaynak: Django mobil API `GET /api/v1/config/` (C3.3). Firebase Remote Config KULLANILMAZ.
    Sunucu API'si canlıda olmayabilir (Faz 13 kararı); yerel varsayılanlar `contract/content/meta.json`'dan gelir.
  - Her zaman yerel varsayılanı vardır (A6.2 tablosu, tema kuralı, aiEnabled=false).
  - En fazla günde bir kez veya uygulama ön plana geldiğinde ve önbellek 12 saatten eskiyse çekilir.
```

**Senkronizasyon:** Kaydedilenler v1'de yalnızca lokaldir. Üyelik yok. Web'deki
`/api/state/` Android'den **çağrılmaz**.

## B8. AĞ KATMANI

```
Ktor Client 3.5.2 + kotlinx.serialization
  - Taban adres: https://napsam.vercel.app/api/v1/   (Supabase SDK YOK)
  - JSON: ignoreUnknownKeys = true, explicitNulls = false
  - Timeout: connect 10s, request 20s (AI çağrısı istemci tarafında 5s — aşağıya bak)
  - Çerez YOK, CSRF YOK. Kimlik yalnızca X-Napsam-Install başlığı (C3.2).
  - Retry: yalnızca GET config'te, ağ hatası/zaman aşımı/5xx için ilk istek + en fazla 2 yeniden
    deneme (1 sn, 2 sn). POST'ta retry YOK.
  - 429 (Vercel WAF veya sunucu kotası): retry yok, sessizce yerel motora düş,
    config için bir sonraki denemeyi en az 10 dakika ertele.
  - Tüm DTO'lar ayrı; domain modele mapper ile çevrilir. DTO UI'a sızmaz.
  - Loglama YALNIZCA debug build'de. Release'de gövde logu YASAK.
```

**Zaman aşımı ve hata UX'i:** Yerel motor zaten anında yanıt verdiği için ağ beklemesi
yalnızca "Yaz" modunda AI açıkken olur. 1 saniyeyi geçerse **iskelet kart**; 5 saniyede yanıt
yoksa yerel motordan öneri verilir ve kullanıcıya hata gösterilmez.

## B9. BAĞIMLILIK ENJEKSİYONU

**Hilt 2.60.1 (KSP).** Projede **tek DI çerçevesi**.
```
@HiltAndroidApp     Application
@AndroidEntryPoint  MainActivity (tek Activity)
@HiltViewModel      Tüm ViewModel'ler
Modüller :core:data içinde, @Binds ile arayüz→impl
Qualifier'lar: @IoDispatcher, @DefaultDispatcher, @ApplicationScope
```
- Constructor enjeksiyonu; `@Inject lateinit var` yerine.
- `@Singleton` yalnızca gerçekten tekil olması gerekenlerde.
- Motorun rastgelelik kaynağı (`Random`) ve saat (`Clock`) enjekte edilir → deterministik test.

## B10. GÜVENLİK

> **Ölçülü ol.** Bu uygulama finansal veri, sağlık verisi veya kimlik taşımıyor.

### B10.1 ZORUNLU
1. **İstemcide hiçbir API anahtarı yoktur ve gerekmez.** Gemini anahtarı yalnızca Vercel
   ortam değişkenindedir (şu an tanımsız). Supabase'e istemciden erişim tasarım gereği
   kapalıdır. APK herkesin açabildiği bir ZIP'tir: `BuildConfig`, `gradle.properties`,
   `strings.xml`, native kod — hiçbiri sır saklama yeri değildir.
2. **Kota ve hız sınırı sunucudadır** (Vercel WAF + Django kotaları). İstemci sayacı UX detayıdır.
3. **Network Security Config**: `cleartextTrafficPermitted="false"`. Debug adresleri release'e sızmaz.
4. **R8 açık** (AGP 9 DSL: `optimization { enable = true }`, kaynak küçültme açık); keep kuralları
   `src/main/keepRules/` altında serialization ve Room için doğru yazılır.
5. **Release imzalama** ayrı keystore ile; keystore **asla** repoya girmez, en az iki yerde
   yedeklenir. Play App Signing kullanılır.
6. **Release build'de log yok.** Timber yalnızca debug tree.
7. **Kullanıcı girdisi loglanmaz**, hiçbir yere ham hâliyle gönderilmez ("Yaz" metni yalnızca
   AI açıkken mobil API'ye gider).
8. **Repo hijyeni:** web reposunda tüm git geçmişi taranmış, sır bulunmamıştır; GitHub
   secret scanning ve push protection açıktır. Android reposu da aynı ayarlarla açılır.

### B10.2 GEREKİRSE
9. Hassas yerel veri yok. İleride token saklanırsa: Android Keystore + AES/GCM.
10. **Certificate pinning önerilmez** (Vercel sertifikaları döner; yanlış pin tüm kurulumları kırar).
11. **Play Integrity API** v1'de gerekli değil.

### B10.3 İLKE
İstemci tarafı güvenlik bir yavaşlatıcıdır, duvar değildir. Gerçek zorlama sunucuda olur.

## B11. PERFORMANS

```
Soğuk açılış bütçesi        < 800 ms  (orta segment cihaz, release build)
Açılış → ilk öneri          < 3 sn    (yerel motorla hedef < 1 sn)
Jank oranı                  < %1      (Macrobenchmark FrameTimingMetric)
AAB indirme boyutu          < 15 MB   (Manrope yazı tipi dahil)
```

1. **Baseline Profile + Startup Profile** — `:benchmark` modülünden, her release öncesi.
2. `Application.onCreate` içinde ağ, disk veya SDK init **yasak**. İçerik içe aktarma ilk
   öneri isteğinde, arka planda ve tek seferlik yapılır.
3. Splash: `androidx.core:core-splashscreen`. Yapay bekleme **yasak**.
4. İçerik uygulamayla gelir; ilk açılışta ağ beklenmez.
5. Compose derleyici raporları her sürümde incelenir.
6. `LeakCanary` yalnızca debug variant'ta.
7. 16 KB sayfa boyutu uyumluluğu doğrulanır (native kütüphane varsa).

## B12. ERİŞİLEBİLİRLİK

1. Tüm dokunma hedefleri **min 48dp**.
2. Anlamlı her ikonda `contentDescription`; dekoratiflerde `null`.
3. Metin **sp**; uygulama **%200 font ölçeğinde** kırılmadan çalışır — test kriteri.
4. Kontrast WCAG AA (4.5:1). **Tasarımdan gelen istisnalar** (renkler Stitch tasarımıyla birebir
   kalır, proje sahibinin kararı): açık temada turuncu küçük etiketler, turuncu zemin üzerinde
   beyaz (NAPSAM?, adım numarası, sayı rozeti), açık temada bütçe tutarı. Bunlar
   `ColorContrastTest`'te belgelenir; diğer tüm metin çiftleri AA testiyle korunur.
5. Bilgi **yalnızca renkle** iletilmez (bedava/ücretli: ikon + metin).
6. TalkBack ile tam akış: aç → NAPSAM? → kart → yapıyorum.
7. Kart tek anlamlı düğüm olarak okunur (`Modifier.semantics`).
8. Açık/koyu tema zorunlu; varsayılan sistem, üst bardaki düğmeyle elle seçilebilir (web'deki gibi).

## B13. TEST STRATEJİSİ

> Kapsam hedefi bir sayı değil, risktir. Öneri seçim mantığı, bütçe filtresi ve metin
> ayrıştırıcı %100'e yakın test edilir; UI süslemesi edilmez.

```
Birim (JVM)
  - Motor PARİTE testleri: web'den dışa aktarılan altın vektörlerle (C8). Zorunlu.
  - ViewModel: Turbine + kotlinx-coroutines-test (StandardTestDispatcher)
  - Repository: fake DataSource ile
  - Bütçe hesabı ve bant: sınır değer testleri zorunlu
  - Room DAO: in-memory DB

Compose UI
  - Compose test v2 API'leri (v1 kullanma); JVM'de Robolectric ile
  - Kritik akış: ana ekran → NAPSAM? → kart görünür → "yapıyorum" → mesaj görünür
  - "Başka fikir" sonrası kartın başı görünür (web'de yaşanan hatanın regresyon testi)

Screenshot
  - Roborazzi: öneri kartı açık/koyu tema, en uzun Türkçe metinli öneri, %200 font ölçeği

Macrobenchmark
  - Soğuk açılış, kaydırma jank'i; CI'da regresyon eşiği

Yasak
  - Espresso + XML tabanlı test · Thread.sleep ile bekleme
```

## B14. KOD KALİTESİ VE STİL

```
Spotless + ktlint          format zorunlu, CI'da fail eder
Android Lint               abortOnError = true (release)
detekt                     C11-K9 kararına göre
Kotlin explicit API mode   C11-K13 kararına göre
```
- `!!` operatörü **yasak** (test kodu hariç).
- Boş `catch` ve genel `catch (e: Exception)` yutma **yasak**.
- Tüm arayüz metni `strings.xml`'de; hardcoded string **yasak**. `values/` Türkçe varsayılan +
  `values-en/`. Öneri içeriği veri olduğu için bu kuralın dışındadır.
- Çoğul ifadeler `plurals` ile ("N adım", "N kelime").
- **Türkçe locale tuzağı:** `lowercase()`/`uppercase()` çağrılarında daima `Locale` belirt.
  Karşılaştırma ve etiket eşleştirmesinde web'le parite için `Locale.ROOT` (Python `str.lower()`
  ile aynı davranış). Metin ayrıştırıcıda normalizasyon kararı: C11-K14.
- Para biçimi `NumberFormat.getCurrencyInstance(Locale.forLanguageTag("tr-TR"))`; kart
  rozetleri ise web'le aynı metin kalıbını kullanır (A6.4).
- Tarih/saat `java.time` (minSdk 26'da yerleşik; core library desugaring gerekmez).
  `Calendar`/`SimpleDateFormat` yasak.
- Yuvarlama: bütçe tutarlarında `RoundingMode.HALF_EVEN` (Python `Decimal.quantize` ile parite).

## B15. ANALİTİK VE GİZLİLİK

Web sürümünde analytics, reklam ve takip çerezi **yoktur** ve gizlilik sayfası bunu açıkça
taahhüt eder ("Reklam kimliği, çapraz site takip çerezi" toplanmıyor). Android bu taahhüdü bozmaz.

```
Varsayılan (öneri): analytics SDK YOK · crash SDK YOK · Play Console Android Vitals yeterli.
Bir SDK eklenirse (C11-K7):
  İzlenecek olaylar: app_open · suggestion_shown(mode, place, budgetTier) ·
    suggestion_dismissed · suggestion_saved · suggestion_done · refresh_count · session_duration
  İzlenmeyecekler: serbest metin (ham) · konum · cihaz kimliği ile eşleşen profil
  Açık rıza sonrası başlar; rıza yoksa SDK init edilmez; Data Safety'de beyan edilir.
```

## B16. GOOGLE PLAY UYUMLULUĞU

```
□ targetSdk ≥ 36 (C11-K2)
□ Android App Bundle (AAB) ile yayın
□ Play App Signing etkin
□ Data Safety formu eksiksiz; kullanılan HER üçüncü taraf SDK beyan edilir
□ Gizlilik politikası URL'i: web'deki https://napsam.vercel.app/gizlilik/ yalnızca web'i
  anlatıyor (çerez vb.) → Android bölümü eklenmeli (Faz 13)
□ Hesap silme: üyelik yok; "verilerimi sil" yerel veriyi siler
□ Yaş derecelendirmesi: 13+
□ Reklam yok → AD_ID izni yok
□ Ödeme yok → Billing yok
□ Geliştirici hesabı doğrulaması tamamlanmış
□ Paket adı (applicationId) ilk yüklemeden sonra DEĞİŞTİRİLEMEZ → isim/marka kararı
  ilk internal track yüklemesinden ÖNCE verilir (C10, C11-K1)
```

## B17. KVKK (Türkiye)

- **Aydınlatma metni** uygulama içinden erişilebilir olmalı: alt bilgideki `Gizlilik` bağlantısı →
  Gizlilik ekranı (web gizlilik sayfasının Android'e uyarlanmış metni).
- **Açık rıza** yalnızca analytics ve bildirim için istenir (bunlar v1'de yoksa rıza ekranı da yok);
  çekirdek işlev rızaya bağlanamaz.
- **Silme hakkı:** ayrı düğme yok (web gibi). Kayıtlar ve tercihler yalnızca cihazdadır; Gizlilik
  metni, uygulamanın verilerini sistem ayarlarından temizlemenin veya uygulamayı kaldırmanın
  hepsini (kullanıcı tablosu, DataStore, kurulum kimliği) sildiğini açıkça yazar.
- Konum **istenmez, saklanmaz, gönderilmez**.
- **Veri sorumlusu bilgileri ve iletişim kanalı Gizlilik ekranında yer alır:**
  **Kaan Enes KAPICI · kaaneneskpc1@gmail.com** (C11-K15). Web gizlilik sayfasında şu an iletişim
  bilgisi yoktur; Faz 13'te web'e de aynı bilgi eklenir.
- Yurt dışı aktarım aydınlatma metninde belirtilir: Vercel (sunucu bölgesi fra1, Frankfurt);
  AI açıksa Google (Gemini); Supabase veritabanı bölgesi Faz 13'te doğrulanıp yazılır.

## B18. CI/CD

```
GitHub Actions — her PR'da:
  1. ./gradlew spotlessCheck
  2. ./gradlew lintRelease
  3. ./gradlew testDebugUnitTest   (motor parite testleri dahil)
  4. ./gradlew assembleRelease (imzasız)
  5. Compose derleyici metrik farkı raporu

main'e merge'de ek olarak:
  6. Baseline Profile üretimi
  7. Macrobenchmark regresyon kontrolü
  8. bundleRelease → Play Console internal track (isim kararı ve geliştirici hesabı sonrası)

Sırlar GitHub Secrets'ta. Keystore base64 secret olarak, CI'da decode edilir.
```

**Depo ve erişim kuralları:**
- Kod **yalnızca kişisel GitHub hesabına** (`kaaneneskpc`) gider. Bu bilgisayar bir şirket
  makinesidir; anahtar zinciri (`osxkeychain`), `~/.git-credentials` ve genel git ayarındaki
  kimlikler şirkete aittir ve **asla kullanılmaz**.
- Push ve repo oluşturma yalnızca `gh` CLI ile; her işlemden önce
  `gh api user --jq .login` sonucunun `kaaneneskpc` olduğu doğrulanır, değilse durulur.
- Repo içi git ayarında `credential.https://github.com.helper` listesi sıfırlanıp yalnızca
  `gh auth git-credential` bırakılır (web reposundaki gibi). `gh auth setup-git` ÇALIŞTIRILMAZ.
- Repo adı ve görünürlüğü: C11-K16.

## B19. KESİN YASAKLAR — bu listeyi ihlal eden kod reddedilir

**Mimari**
- `GlobalScope` · ana thread'de `runBlocking` · `LiveData` · XML layout veya Fragment
- ViewModel'i alt composable'lara parametre olarak geçirmek
- Composable gövdesinde yan etki · `collectAsState()` · enjeksiyonsuz `Dispatchers.IO`
- DTO'yu UI katmanına sızdırmak · `Result`/`Outcome` yerine exception fırlatmak

**Entegrasyon**
- İstemciden Supabase'e bağlanmak (SDK, REST, anon key — hepsi)
- İstemciden AI sağlayıcısına (Gemini/Claude) doğrudan istek atmak
- Web'in çerez + CSRF tabanlı `/api/suggest/` ve `/api/state/` uçlarını Android'den çağırmak
- Öneri motorunun davranışını web'den farklılaştırmak (parite testi olmadan)
- İçeriği Android reposunda elle düzenlemek (tek kaynak web fixture'ı)

**Güvenlik**
- Herhangi bir API anahtarını APK'ya koymak
- Release build'de gövde logu veya kullanıcı girdisi logu · cleartext HTTP
- Keystore'u repoya commit etmek · istemci kontrolünü güvenlik önlemi saymak
- Şirket kimlikleriyle (anahtar zinciri, kayıtlı git kimlikleri) push etmek

**Ürün**
- Onboarding, hoş geldin, tanıtım veya zorunlu giriş ekranı
- Ana ekrana ikinci bir birincil eylem
- Öneri kartını detay sayfasına bölmek
- Veri modeline şehir/ilçe/bölge/koordinat alanı eklemek
- Bütçe tutarlarını koda gömmek (yerel varsayılan oran tablosu hariç)
- Öneri metnine gerçek mekan, semt veya işletme ismi koymak
- Streak, rozet, seviye, puan, günlük ödül · kullanıcıyı uygulamada tutmaya yönelik mekanik
- İlk açılışta izin istemek (konum, bildirim)

**Genel**
- `!!` operatörü · hardcoded string, renk veya boyut
- Locale belirtmeden `lowercase()`/`uppercase()`
- Sürüm katalogu dışında bağımlılık · gerekçesiz yeni kütüphane

## B20. DEFINITION OF DONE

```
□ Kod B19'daki yasakların hiçbirini ihlal etmiyor
□ Spotless + lint temiz (detekt: C11-K9)
□ ViewModel ve mantık için birim testi yazılmış; motor değiştiyse parite testleri geçiyor
□ Kritik akış için Compose UI testi var
□ Açık ve koyu temada kontrol edilmiş
□ %200 font ölçeğinde kırılmıyor
□ TalkBack ile gezinilebiliyor
□ Türkçe ve İngilizce arayüz metinleri tam
□ Uçak modunda anlamlı davranıyor (çökmüyor, boş ekran göstermiyor)
□ Compose derleyici raporunda yeni skip edilemeyen composable yok
□ Release build'de derleniyor ve R8 sonrası çalışıyor
□ Emülatörde (Pixel_9_Pro_API_37) çalıştırılıp ekran görüntüsüyle doğrulanmış
```

## B21. ÇALIŞMA DÜZENİ VE FAZLAR

Sırayla ilerle, **her fazın sonunda onayımı al**. Faz atlama. Ayrıntılı görev listesi ve
ilerleme durumu: **`docs/PLAN.md`** (her faz bitince güncellenir).

```
Faz 0   Eleştiri + açık kararlar (C11) + git/GitHub kurulumu
Faz 1   Web backend hazırlığı (napsamweb): mobil API v1, tema rotasyonu, içerik ve
        parite dışa aktarımı — web'i bozmadan, testli, main'e push = canlı deploy
Faz 2   Proje iskeleti: build-logic, sürüm kataloğu, modüller, Spotless, lint
Faz 3   :core:model
Faz 4   :core:designsystem (Stitch son tasarımı + Manrope)
Faz 5   :core:domain — motor, metin ayrıştırıcı, chip, bütçe + parite testleri
Faz 6   :core:data — Room, içerik içe aktarma, DataStore, Ktor, repository'ler
Faz 7   :feature:home
Faz 8   (Faz 7'ye katıldı: kart ana ekranın parçası, ayrı modül yok — :core:ui + :feature:home)
Faz 9   :feature:saved + :feature:privacy
Faz 10  :app — Navigation 3, Hilt, splash, edge-to-edge, predictive back
Faz 11  :benchmark — Macrobenchmark + Baseline Profile
Faz 12  CI — GitHub Actions
Faz 13  Release hazırlığı — R8, imzalama, Data Safety, KVKK metni, mağaza, isim/marka
```

Her fazda **çalışan kod** üret; pseudocode verme. Kod yazmadan önce o fazda hangi Bölüm B
kurallarının geçerli olduğunu bir satırda belirt. Faz sonunda derle, testleri çalıştır ve
kanıt göster (test çıktısı, ekran görüntüsü).

## B22. BENİM DOLDURACAKLARIM

```
☑ applicationId: geliştirmede com.kaaneneskpc.napsam; Play öncesi marka araştırması (K1)
☑ GitHub repo: kaaneneskpc/napsam-android, private (K16)
☑ KVKK veri sorumlusu: Kaan Enes KAPICI · kaaneneskpc1@gmail.com (K15)
☑ Analytics / crash: v1'de yok (K7)
☑ targetSdk: 37 (K2)
☑ v1 kapsam kararları: C11 tablosu (14.09.2026 onaylı)
□ Google Play geliştirici hesabı durumu (Faz 13'e kadar gerekmez)
```
> v1'deki "Supabase proje URL'i ve anon key" maddesi kaldırıldı: istemci Supabase kullanmaz.
> Reklam ve Premium/Billing v1'de yok.

---
---

# BÖLÜM C — CANLI WEB SİSTEMİ VE ENTEGRASYON

## C1. Sistem haritası (14.09.2026)

```
Canlı site      https://napsam.vercel.app
Web reposu      https://github.com/kaaneneskpc/napsam  (public, varsayılan dal main, lisans dosyası yok)
Yerel web       /Users/kkapici/StudioProjects/napsamweb
Yerel Android   /Users/kkapici/StudioProjects/napsam
Barındırma      Vercel (proje "napsam", bölge fra1). Git bağlı: YALNIZCA main dalı otomatik
                production deploy olur; diğer dallar deploy üretmez
                (vercel.json → git.deploymentEnabled {"**": false, "main": true}).
Uygulama        Python 3.13 · Django 5.2 · WhiteNoise · psycopg 3
Veritabanı      Supabase Postgres, transaction pooler (6543). Seed işlemleri session pooler (5432).
Yapay zeka      Google Gemini (gemini-3.5-flash-lite), Django içinden. ŞU AN KAPALI.
Sağlık          GET /healthz/ → {"ok":true,"suggestions":500,"aiEnabled":false,"wage":"28075"}
Testler         Web: 145 test (DATABASE_URL= python manage.py test core), hiçbiri ağa çıkmaz
                (mobil API, tema rotasyonu ve dışa aktarma testleri dahil)
```

## C2. Backend gerçekleri

### C2.1 Mevcut web uçları (Android'den ÇAĞRILMAZ)
| Uç | Yöntem | Not |
|---|---|---|
| `/` | GET | Ana ekran; açılışta motor bir öneri seçer ve sayfaya gömer |
| `/kaydedilenler/` · `/gizlilik/` | GET | |
| `/api/suggest/` | POST | CSRF zorunlu; tek öneri döner |
| `/api/state/` | POST | CSRF zorunlu; `save|unsave|dismiss|done`; veritabanına yazan TEK uç |
| `/healthz/` | GET | |
| `/yonetim/` | — | Django admin; hiç admin hesabı yok |

**Neden Android bu uçları kullanamaz:** Django CSRF koruması HTTPS isteklerinde `Referer`
başlığını da doğrular; native istemci bunu göndermez ve 403 alır. Çerez tabanlı kimliği
(`napsam_anon`) mobil istemciye taşımak da CSRF riskini geri getirir. Çözüm: C3.

### C2.2 Kimlik ve gizlilik (web)
- `napsam_anon` çerezi: rastgele UUID, HttpOnly, Secure, SameSite=Lax, 1 yıl.
- Sadece gezinen ziyaretçi için veritabanına **tek satır yazılmaz** (testle kilitli).
- Kayıt yalnızca kaydet/geç/yapıyorum eylemlerinde oluşur.
- IP açık saklanmaz; AI kotası için tuzlanmış kısa özeti tutulur.

### C2.3 Veri erişimi
Supabase `public` şemasındaki 18 tablonun hepsinde RLS açık ve policy yok; `anon` ve
`authenticated` rollerinin tablo yetkileri kaldırılmış. Django tablo sahibi rolüyle bağlanır.
Supabase yalnızca Postgres olarak kullanılır; BaaS olarak değil.

### C2.4 AI katmanı
- Yalnızca `mode=text` ve `GEMINI_API_KEY` tanımlıysa denenir; başarısızlıkta seed motoruna düşer.
- Kotalar (en dar olan geçerli): `global` 200/gün · `ip:<özet>` 15/gün · `anon:<uuid>` 5/gün.
  Global ve IP tavanı `RemoteConfig` üzerinden değiştirilebilir.
- Aynı bağlam için yanıt 7 gün önbellekte tutulur. Çıktı, içerik doğrulayıcısından geçer.
- En güçlü maliyet koruması: AI Studio'da faturalandırma kapalıysa kota bitince 429 döner.

### C2.5 Hız sınırı (Vercel Firewall)
- Kural "API hiz siniri": yol öneki `/api/` · IP başına · 60 sn'de 100 istek · aşan `429`
  (`x-vercel-mitigated: deny`). Sınır, 130 istekli testle doğrulandı (100 geçti, 30 engellendi).
- `/api/v1/` de bu kuralın kapsamındadır. Türkiye'de mobil operatörler çok sayıda kullanıcıyı
  aynı IP'den (CGNAT) çıkarabildiği için Android istemcisi API'yi **seyrek** çağırır.
- Bu kural repoda değil Vercel ayarlarında durur. Hobby planında proje başına 1 hız sınırı kuralı.

### C2.6 Web'de bilinçli olarak yapılmayan sertleştirmeler
CSP başlığı yok · HSTS 1 saat · ana sayfada `|safe` ile basılan kayıt listesi. Proje sahibi
bunları şimdilik istemedi; Android işi bunlara dokunmaz.

## C3. Mobil API v1 (Faz 1'de web'e eklendi)

> Durum (14.09.2026): kod web reposunda yerel `feature/mobile-api-v1` dalında, testli, **push
> edilmedi ve canlıda değil**. Proje sahibinin kararı: web projesi tamamlandı, odak mobil.
> Android ağ katmanı yerel Django sunucusuyla (`DATABASE_URL= python manage.py runserver`)
> test edilir. Sunucunun yayınlanıp yayınlanmayacağı Faz 13'te kararlaştırılır; uygulama
> sunucu olmadan da tam çalışır.

### C3.1 İlkeler
- Önek `/api/v1/`. Web uçları olduğu gibi kalır; web davranışı değişmez.
- **Çerez yok, CSRF yok** (`csrf_exempt` güvenlidir çünkü kimlik doğrulaması çereze dayanmaz).
- İstek ve yanıtta şehir/ilçe/konum alanı yoktur.
- Her uç web'le aynı motoru, doğrulayıcıyı ve kotaları kullanır; kod kopyalanmaz.
- Web test paketi + yeni uç testleri geçmeden main'e push edilmez.

### C3.2 Kimlik
- İstemci ilk açılışta rastgele bir UUID v4 üretir, DataStore'da saklar: `X-Napsam-Install`.
- Yalnızca AI kotasının kimlik kapsamı için kullanılır (web'deki `anon:<uuid>` karşılığı).
- Cihaz kimliği, reklam kimliği veya donanım bilgisi KULLANILMAZ. Uygulama verileri sistem
  ayarlarından temizlenince yeni UUID üretilir ("verilerimi sil" düğmesi yok, C11-K18).

### C3.3 `GET /api/v1/config/`
```json
{
  "schema": 1,
  "wage": "28075",
  "currency": "TRY",
  "budgetTiers": [
    {"key": "bedava", "label": "Bedava", "hint": "", "order": 0,
     "ratioMin": "0", "ratioMax": "0", "amountMin": 0, "amountMax": 0, "amountText": "0 TL"},
    {"key": "bol", "label": "Bol", "hint": "etkinlik, aktivite", "order": 4,
     "ratioMin": "0.053428", "ratioMax": null, "amountMin": 1500, "amountMax": null, "amountText": "1.500 TL+"}
  ],
  "weeklyTheme": "ekransiz",
  "themeRotation": ["yuru", "elini_kullan", "ekransiz", "duzen"],
  "aiEnabled": false,
  "contentVersion": "sha256:e86fe1162b400…",
  "suggestionCount": 500,
  "dismissCooldownDays": 30,
  "androidMinVersionCode": 1
}
```
- Ücret ve oranlar **metin** (ondalık hassasiyeti korunur; istemci BigDecimal ile okur).
- `androidMinVersionCode` sunucudaki `RemoteConfig` anahtarı `android_min_version_code`'dan gelir (varsayılan 1).
- `Cache-Control: public, max-age=3600`. Yanıtta çerez yoktur.
- İstemci config'i önbellekler; `contentVersion` cihazdakinden farklıysa içerik C3.4b ucundan
  indirilir (C11-K3).

### C3.4 `POST /api/v1/suggest/` — Android bunu yalnızca "Yaz" modu + AI açıkken çağırır
İstek (web gövdesiyle aynı alanlar):
```json
{
  "mode": "text",
  "budget": "bedava",
  "keywords": [],
  "text": "3 saatim var param yok",
  "filters": {"duration": 180, "companions": "solo", "energy": "low", "place": "ev"},
  "excluded": ["ev-cekmece-bosalt"]
}
```
Başlık: `X-Napsam-Install: <uuid>`. Sınırlar: `keywords` ≤ 8, `text` ≤ 280, `excluded` ≤ 40.

Yanıt (web'deki `suggestion_to_dict` + meta):
```json
{
  "suggestion": {
    "slug": "ev-cekmece-bosalt",
    "title": "Bir çekmeceyi tamamen boşalt",
    "hook": "Zihnindeki dağınıklığı en somut yerden hafiflet.",
    "steps": ["…", "…", "…"],
    "fallback": "…",
    "category": "ev_ici",
    "categoryLabel": "Ev içi",
    "badges": {"cost": "₺0 Bedava", "duration": "20-40 dk", "place": "Evde", "companion": "Tek başına / Çift"},
    "requiredItems": [],
    "place": "ev",
    "venueType": "",
    "isFree": true,
    "source": "seed | ai"
  },
  "meta": {"mode": "text", "budget": "bedava", "keywords": [], "isNight": false, "aiQuotaLeft": 4}
}
```
- `source: "ai"` olan öneri yerel havuzda yoktur; kart bilgisi yanıttan çizilir (rozet metinleri
  sunucudan gelir). `source: "seed"` ise yalnızca slug kullanılır, kart yerel içerikten çizilir.
  AI önerisinin kaydedilmesi (tam içeriğin cihazda saklanması) Faz 8'de kart ve kaydetme akışıyla
  birlikte yapılır; Faz 6'da kullanıcı tablosu yalnızca yerel havuzdaki önerileri tutar.
- Hatalar gövdesi `{"error": "…"}`: `400` başlık eksik/geçersiz UUID v4 veya bozuk JSON ·
  `405` yanlış yöntem · `429` **yalnızca Vercel WAF**'tan gelir · `5xx`.
  Hepsinde istemci sessizce yerel motora düşer.
- **AI kotası dolduğunda 429 DÖNMEZ:** sunucu seed motoruna düşer, `200` ile `source: "seed"` ve
  `meta.aiQuotaLeft: 0` döner. İstemci `aiQuotaLeft == 0` görürse o gün için AI denemesini bırakır.
- Uç her modu kabul eder (sunucu tarafı seçim), ama offline-first kuralı gereği Android
  `quick` ve `chips` modlarını yerelde çalıştırır.

### C3.4b `GET /api/v1/content/` — Supabase içeriğiyle güncelleme (15.09.2026)
```json
{"schema": 1, "contentVersion": "sha256:…", "count": 500, "suggestions": [ {fixture biçiminde 22 alan}, … ]}
```
- Yalnızca etkin seed öneriler (AI üretimleri ve pasifler girmez), slug sırasıyla.
- `contentVersion` = içeriğin kanonik JSON'unun (`sort_keys`, `ensure_ascii=False`, ayırıcısız)
  SHA-256'sı; config'teki ve dışa aktarılan meta'daki sürümle aynı hesap. Supabase'de öneri
  değişince sürüm değişir (önbellek `post_save`/`post_delete` sinyaliyle düşer).
- `ETag` = sürüm, `If-None-Match` eşleşirse `304`. `Cache-Control: public, max-age=3600`. Çerez yok.
- İstemci: config yenilenince sunucu sürümü cihazdakinden farklıysa indirir, doğrular (şema, sayı,
  yinelenen slug, bilinmeyen anahtar) ve tek transaction'da yazar. İndirilen içerik sonraki açılışta
  gömülü kopyayla ezilmez; gömülü kopya yalnızca uygulama yeni içerikle güncellenince yeniden kurulur.
- Uç sunucu yayınlanana kadar (Faz 13) çalışmaz; uygulama gömülü kopyayla tam çalışır.

### C3.5 v1'de olmayanlar
Kayıt senkronu (`/state`; hafıza web gibi cihazda — C11-K3) · paylaşım sayfası. Hepsi
C11 kararlarına bağlı olarak sonraki sürümlerde.

## C4. Öneri motoru spesifikasyonu (web `core/engine.py`, birebir)

### C4.1 Bağlam
```
budgetKey: String = "bedava"        keywords: List<String>
durationMax: Int?                   companions: String?   (solo|couple|friends|family|kids)
energy: String?  (low|medium|high)  placePref: String?    (ev|disari)
weather: {condition, tempC}?        now: LocalDateTime     (cihaz saati)
excluded: Set<slug>
isNight  = hour >= 22 || hour < 6
isSolo   = companions == null || "" || "solo"
```

### C4.2 Sabitler
```
NIGHT_START_HOUR 22 · NIGHT_END_HOUR 6 · VENUE_OPEN_FROM 8 · VENUE_OPEN_UNTIL 23
W_KEYWORD 3.0 · W_THEME 1.0 · W_ENERGY 2.0 · W_COMPANION 2.0 · W_PLACE_PREF 2.5
W_FREE_BONUS 0.5 · W_FREE_WHEN_PAID -2.0 · W_RECENT_CATEGORY_PENALTY -2.5
W_ALREADY_SHOWN_PENALTY -1.5 · W_JITTER 1.2 · NEAR_SCORE 1.5
TOP_N_FRACTION 0.30 · TOP_N_MIN 15 · DISMISS_COOLDOWN_DAYS 30
Oturumda hariç tutulan son görülen: 30 (istek sınırı 40)
```
> Web kodunda `W_IN_BAND 3.5` ve `W_BELOW_BAND 1.0` tanımlı ama **kullanılmıyor**; port edilmez.

### C4.3 Sert filtreler (sıra önemli)
1. Aktif seed öneriler (AI üretimleri genel havuza girmez).
2. **Bütçe tavanı:** kademe bilinmiyorsa tavan 0 · `amountMax == null` ise tavan yok ·
   değilse `cost_max <= tavan`.
3. **Gece** (`isNight`): tek başınaysa `night_unsafe_solo` olanlar düşer ·
   saat 8–23 aralığı DIŞINDAYSA `place == mekanli` düşer (yani 23:00–05:59).
4. **Hava** (yalnızca biliniyorsa; v1'de gönderilmez): `rain|snow|storm|sleet` → `dry` ve
   `warm` gerektirenler düşer · `tempC < 8` → `warm` düşer.
5. **Süre:** `duration_min <= durationMax`.
6. **Enerji:** `low` → `high` düşer · `high` → `low` düşer.
7. **Ev/dışarı:** `ev` → yalnızca `place == ev` · `disari` → `place != ev`.
8. **Hariç tutulanlar:** `excluded` slug'ları + `dismissed_until > now` olan kayıtlar.
9. Öğe bazında: **kiminle** (`companions` seçildiyse öneri listesinde olmalı) ·
   **mevsim** (liste boş değilse bu ay listede olmalı) ·
   **kelimeler** (seçildiyse, küçük harfe çevrilmiş etiketlerle en az bir kesişim).
10. **Bütçe bandı:** kademe `bedava` değilse `bantta = cost_max > amountMin`;
    `bantta` boş değilse yalnızca onu döndür, boşsa tüm havuzu döndür.

### C4.4 Puan
```
+ W_KEYWORD × (ctx.keywords içinde öneri etiketlerinde geçenlerin sayısı)
+ W_THEME          aktif tema theme_tags içindeyse
+ W_ENERGY         ctx.energy == öneri.energy
+ W_COMPANION      ctx.companions öneri listesinde
+ W_PLACE_PREF     (ev ve place==ev) veya (disari ve place!=ev)
+ W_FREE_BONUS     kademe bedava ve cost_max == 0
+ W_FREE_WHEN_PAID kademe ücretli ve cost_max == 0
+ W_RECENT_CATEGORY_PENALTY  kategori, son 5 kullanıcı kaydının (updated_at'e göre) kategorilerindeyse
+ W_ALREADY_SHOWN_PENALTY    öneri o son 5 kayıttaysa
+ uniform(0, W_JITTER)       enjekte edilen Random ile
```

### C4.5 Seçim ve gevşetme
```
döngü:
  havuz = sertFiltre(ctx)
  havuz boş değilse:
     sırala (puan azalan)
     yakın = en iyi puana NEAR_SCORE (1.5) veya daha yakın olanlar (sıralı)
     kapak = max(15, ceil(havuz.size × 0.30))
     döndür random(yakın.take(kapak))
  ctx = gevşet(ctx) ; null ise döngüden çık
son çare: aktif, bedava (cost_max == 0) rastgele bir seed öneri — boş ekran yok

gevşet sırası: excluded → keywords → durationMax → companions → energy → placePref → weather
BÜTÇE ASLA GEVŞETİLMEZ.
```

### C4.6 Kullanıcı durumları (yerel)
- `dismiss`: durum geçildi, `dismissed_until = now + 30 gün`.
- `save` / `unsave` (web'de unsave durumu `shown`a çeker) · `done`: `done_at = now`.
- "Son 5 kayıt" cezası bu kayıtların `updated_at` sırasından hesaplanır.

### C4.7 Haftalık tema kuralı (web `config.theme_for_date`, Faz 1'de sabitlendi)
```
etkin = isActive olan temalar;  boşsa → null
pazartesi = günün ISO haftasının pazartesisi
buHafta = etkin içinde pazartesi ≤ startsOn ≤ gün olanlar
buHafta boş değilse → (startsOn, key) en büyük olan
değilse → sıralı = etkin, sıralama anahtarı (THEME_ROTATION'daki indeks, key);
          rotasyonda olmayanlar (len(THEME_ROTATION), key) ile sona
          → sıralı[isoHaftaNumarası mod sıralı.size]
THEME_ROTATION = [yuru, elini_kullan, ekransiz, duzen]
```
- Android'de sunucu temaları yoktur: config gelmediyse `THEME_ROTATION[isoHafta mod 4]`,
  geldiyse `weeklyTheme`. ISO hafta: `IsoFields.WEEK_OF_WEEK_BASED_YEAR`.
- Doğrulama: `contract/parity/theme.json` (8 senaryo × 10 tarih, yıl dönümü dahil).

## C5. İçerik sözleşmesi

### C5.1 Tek doğruluk kaynağı
`napsamweb/core/fixtures/suggestions.json` — 500 öneri. Android ve ileride iOS içeriği bu
dosyadan alır. Android reposunda içerik elle düzenlenmez.

### C5.2 Alanlar (22 alan, hepsi her öneride)
```
slug              benzersiz, [a-z0-9-]
title             ≤ 6 kelime, ≤ 60 karakter, emir kipi
hook              tek cümle, ≤ 160 karakter
steps             2–3 dize
fallback          zorunlu
category          A7.1 anahtarı
cost_min/cost_max TL tamsayı, cost_max ≥ cost_min
cost_note         ≤ 80 karakter, boş olabilir
duration_min/max  dakika, max ≥ min
companions        solo|couple|friends|family|kids listesi
energy            low(337)|medium(125)|high(38)
place             ev|yakin|mekanli
venue_type        yalnızca mekanli için dolu, işletme ismi yok
weather_need      any(422)|dry(75)|warm(3)
seasonality       ay numaraları 1–12, boş = tüm yıl
cultural_tags     (şu an motor kullanmıyor)
required_items    ≤ 3
theme_tags        yuru|elini_kullan|ekransiz|duzen
tags              kelime modu etiketleri (C5.5)
night_unsafe_solo gece tek başına önerilmesin (13 öneri)
```

### C5.3 Doğrulayıcı (web `core/validators.py`) — içerik bu kurallardan geçmeden yayınlanmaz
- **Konum:** 81 il adı + semt/yer terimleri (kadıköy, moda, boğaz, vapur, iskele, metrobüs,
  marmaray, teleferik…). Karşılaştırma Türkçe harfler ASCII'ye katlanarak yapılır; tek
  kelimelik terimler tam kelime, çok kelimeliler alt dize olarak aranır.
- **Güvenlik:** bahis, kumar, alkol türleri, sigara, nargile.
- **Ton:** "hayatını değiştir", "yolculuğa çık", "potansiyelini keşfet", "mindful", "self-care",
  "detoks", "motivasyon", "enerjini yükselt".
- **Biçim:** başlık ≤ 6 kelime · 2–3 dolu adım · fallback dolu · hook tek cümle ·
  mekanli ↔ venue_type tutarlılığı · required_items ≤ 3 · seasonality yalnızca 1–12 tamsayı ·
  `companions` "friends" içeriyorsa `tags` "arkadaşımla" içermeli · maliyet ve süre aralıkları tutarlı.
- **Yakın tekrar:** başlık benzerliği ≥ 0.72 veya ilk adımı birebir aynı öneri reddedilir.
- Komut: `python manage.py seed --check`.

### C5.4 Android'e taşıma hattı (Faz 1 + Faz 6)
```
1. napsamweb:  DATABASE_URL= .venv/bin/python manage.py export_mobile --out ../napsam/contract
     - Geçici bellek içi SQLite kurar, seed'i yükler; geliştirme DB'sine ve Supabase'e dokunmaz
       (SQLite dışı yapılandırmada çalışmayı reddeder)
     - İçerik doğrulayıcısı (seed --check ile aynı) hata verirse HİÇBİR ŞEY yazılmaz
     → contract/README.md                 "otomatik üretilir, elle düzenleme"
     → contract/content/suggestions.json  fixture'ın BİREBİR kopyası
     → contract/content/meta.json         contentVersion (sha256), count, wage, budgetTiers,
                                          defaultBudget, themeRotation, dismissCooldownDays,
                                          sessionExcluded, categories, companions, chipWindows
     → contract/parity/*.json             (C8)
   Çıktı deterministiktir (iki çalıştırma bayt bayt aynı; export_date gibi değişken alan yok).
2. napsam: contract/ Android reposunda commit edilir; modüller buradan okur
   (content → uygulama asset'leri, parity → :core:domain test kaynakları). Kopya çoğaltılmaz.
3. Uygulama: gömülü içerik sürümü değiştiyse (uygulama güncellemesi) veya sunucu yeni sürüm
   bildirdiyse (C3.4b) içerik tablosu tek transaction'da yenilenir; kullanıcı tablosu korunur,
   artık var olmayan slug'lara ait kayıtlar yetim bırakılmaz
```

### C5.5 Etiket evreni (18) ve havuzdaki sıklığı
```
ucuz 298 · evde 260 · kısa sürsün 186 · dışarı 182 · yeni bir şey 170 · sakin 145 ·
arkadaşımla 145 · ellerimi kullanayım 126 · tek başıma 119 · öğrenmek 84 · yemek 77 ·
hareket 61 · gece 45 · yürümek 41 · fotoğraf 23 · kalabalık olmasın 17 · kahve 13 · müzik 13
```
> `müzik` etiketi (13 öneri) hiçbir chip setinde yok ve "Yaz" modu sözlüğünde de yok; bu
> önerilere kelimeyle ulaşılamıyor, yalnızca genel havuzdan çıkabiliyorlar.

## C6. Metin ayrıştırıcı spesifikasyonu (web `core/textmode.py`)

Kelime listeleri ve sözlük **birebir port edilir** (kaynak dosyadan kopyalanır, elle yeniden yazılmaz).
```
normalize: trim + küçük harf + çoklu boşluk → tek boşluk
bütçe:     BEDAVA ifadeleri → "bedava" · BOL ifadeleri → "bol" ·
           regex (\d+)\s*(tl|lira|₺) → sıralı kademelerde amountMax null veya tutar ≤ amountMax olan ilk kademe
süre:      "yarım saat|yarim saat" → 30 · "bütün gün|butun gun|tüm gün" → 480 ·
           (\d+)\s*saat → ×60 · (\d+)\s*(dakika|dk)
kiminle:   sözlük sırası couple → friends → family → kids → solo, alt dize eşleşmesi, ilk bulunan
enerji:    düşük ifadeler önce, sonra yüksek
ev/dışarı: ev ifadeleri önce, sonra dışarı
kelimeler: sözlük sırasıyla alt dize eşleşmesi, etiketler tekrarsız
birleştirme (web build_context):
  - kelimeler: seçilenler + metinden çıkanlar, sıra korunur, tekrarsız
  - bütçe: metinde bütçe ifadesi VARSA seçili kademeyi EZER
  - süre/kiminle/enerji/yer: yalnızca kullanıcı seçmediyse metinden gelir
```
> **Bilinen zayıflık (parite verisiyle doğrulandı):** alt dize eşleşmesi yanlış pozitif üretir —
> `"açık havada bir şey"` → `keywords: ["yemek"]` ("aç"), `"susadım"` → `["dışarı"]` ("su").
> K14 kararı: Android önce **birebir parite** ile port eder; iyileştirme ileride web ile birlikte
> ayrı bir iş olarak yapılır ve parite verisi yeniden üretilir.

## C7. Chip setleri (web `core/chips.py`)
```
06:00–10:59  yürümek · kahve · dışarı · yeni bir şey · sakin · ellerimi kullanayım ·
             öğrenmek · kısa sürsün · ucuz · yemek
11:00–16:59  dışarı · yürümek · arkadaşımla · hareket · öğrenmek · fotoğraf · yemek ·
             kalabalık olmasın · yeni bir şey · ucuz
17:00–21:59  evde · sakin · arkadaşımla · yemek · ellerimi kullanayım · kahve ·
             yeni bir şey · kısa sürsün · dışarı · tek başıma
22:00–05:59  evde · sakin · gece · tek başıma · kısa sürsün · ellerimi kullanayım · öğrenmek · ucuz
```
Chip metni = içerikteki etiket metni (birebir aynı olmak zorunda).

## C8. Parite testleri

İki (ileride üç) motor implementasyonu zamanla birbirinden sapar. Bunu önlemek için web
tarafında bir dışa aktarma betiği (Faz 1) Python motorunun **deterministik** çıktılarını
JSON olarak üretir; Android `:core:domain` testleri aynı dosyaları okur:

```
budget.json      5 ücret değeri × 5 kademe → ratio, amountMin/Max, amountText
badges.json      500 öneri → categoryLabel, isFree, cost/duration/place/companion rozet metni
chips.json       0–23 her saat → chip seti
textmode.json    164 cümle → ayrıştırma sonucu (sözlüklerin tüm ifadeleri + bilinen tuzaklar)
candidates.json  73 bağlam (sabit tarih-saat, jitter yok) → sert filtre sonrası slug kümesi
                 (bütçe × saat, gece sınırları, süre, kiminle, enerji, yer, hava, kelime,
                 büyük harfli kelime, mevsim, birleşik, hariç tutma; 3'ü bilerek boş)
relax.json       6 bağlam → düşürülen alanlar sırasıyla + ilk dolu havuz + lastResort
sample.json      havuz boyutları → kapak değeri
theme.json       8 senaryo × 10 tarih → beklenen tema (C4.7)
engine.json      motor sabitleri + DISMISS_COOLDOWN_DAYS, RECENT_STATES, SESSION_EXCLUDED,
                 MAX_KEYWORDS, MAX_EXCLUDED, MAX_TEXT_LEN
```
Rastgelelik içeren son seçim parite testine girmez; olasılık dağılımı Android'de ayrıca
istatistiksel testle doğrulanır (web'deki "VarietyTests" mantığı).

## C9. Web'de bu süreçte alınmış ürün/UX kararları (Android'e aynen geçer)

1. **Filtreler isteğe bağlı hissettirmeli:** daraltma paneli kapalı başlar; kullanıcı hiçbir
   şey seçmeden NAPSAM?'a basabileceğini net anlamalı.
2. **Mobil yerleşim taşmamalı:** metinler ve kartlar iç içe geçmez. Android'de bütçe tek satırda
   5 sütun, kart eylemleri tek satırdır (Stitch mobil tasarımı); %200 yazı ölçeğinde de taşmaz.
3. **"Başka fikir" sonrası kartın başı görünür olmalı.**
4. **"algoritma" kelimesi kullanıcı metinlerinden çıkarıldı.**
5. **Gizlilik sayfasında olmayan iletişim kanalına atıf yapılmaz.**
6. **İçerik büyütme dersleri:**
   - Sabit örnekleme kapağı (15) havuz büyüdükçe çeşitliliği boğdu → havuzla orantılı kapak.
   - "friends" içeren 68 öneride "arkadaşımla" etiketi eksikti → o chip seçildiğinde havuzun
     %70'i oyuna sıkışıyordu → etiket eklendi, doğrulayıcı kuralı yazıldı.
   - Seçilen kelime ve bütçe bandı başta yumuşak puandı ve ölçüldüğünde kırıktı ("kahve"
     seçene %25 isabet) → sert filtre yapıldı.
7. **Görsel dil ve içerik = web ve son tasarım, birebir** (proje sahibinin talimatı, 14.09.2026).
   İçerik ve sıra web'deki gibidir; görünüm Stitch son mobil tasarımıdır (C9.4). Web'de olmayan
   hiçbir özellik, ekran, düğme veya renk ayarı eklenmez; yeni bir şey istenirse proje sahibi söyler.
   Yapı ve mimari (modüller, UDF, Compose kuralları) Android'in doğru olduğu gibidir.

### C9.4 Tasarım token kaynağı
**Stitch son tasarımı**, repoda `docs/design/`: `DESIGN.md` (tasarım sistemi), `mobile-light.html` +
`mobile-light.png`, `mobile-dark.html` + `mobile-dark.png`. Renk, köşe yarıçapı, boşluk ve yazı
boyutları bu HTML'lerdeki Tailwind değerlerinden birebir alınır. Yazı tipi **Manrope**
(DESIGN.md; OFL lisanslı, Android'e **gömülü**, offline-first) — Stitch'in örnek HTML'i Plus Jakarta
Sans yükler, bu tasarım sistemiyle çelişen bir dışa aktarma ayrıntısıdır. Metinler web'den (A6.7).

## C10. Güvenlik, hukuk ve operasyon notları

- **Sızıntı taraması (13.09.2026):** web geçmişi temiz; istemciye giden dosyalarda anahtar yok;
  `.env`, `.git`, kaynak dosyalar dışarıdan 404; bağımlılıklarda bilinen açık yok.
- **Marka/isim riski:** "napsam" adı telif değil marka hukuku (6769 SMK) kapsamındadır.
  napsam.net (alan adı 29.09.2025) aynı isimle Türkçe bir AI öneri ürünü sunuyor.
  **Play'e ilk yüklemeden ÖNCE** TÜRKPATENT'te "NAPSAM" ve "NE YAPSAM" için 9, 42 ve 41.
  sınıflarda araştırma yapılır; gerekirse isim ve applicationId değişir.
- **Lisans:** web reposunda lisans dosyası yok (tüm haklar saklı). Öneri metinleri yapay zekâ
  yardımıyla üretildiği için telif korumasının kapsamı belirsiz.
- **LinkedIn paylaşımı** planlanıyor; postta "Android ve iOS yakında" ifadesi var.
- **iOS** da planlanıyor: C3 sözleşmesi, C4 spesifikasyonu ve C8 parite dosyaları platformdan
  bağımsız tutulur.

## C11. Kararlar — 14.09.2026'da proje sahibi tarafından onaylandı

| # | Karar | Onaylanan | Gerekçe |
|---|---|---|---|
| K1 | Uygulama adı ve applicationId | Geliştirmede `com.kaaneneskpc.napsam`; Play yüklemesinden önce marka araştırması | Paket adı ilk yüklemeden sonra değişmez |
| K2 | targetSdk | 37 | Stabil; bir sonraki Play şartına hazır; bu uygulamayı etkileyen değişiklik az |
| K3 | İçeriğin uygulamaya taşınması | Supabase içeriğinin gömülü JSON kopyası + ilk açılışta içe aktarma; Supabase'de içerik değişince `GET /api/v1/content/`'ten indirme (15.09.2026 revize). Hafıza (kayıtlar) web gibi cihazda | Veri seti web ile aynı ve güncel kalır, internetsiz çalışır; hesap olmadığı için web hafızası cihazlar arası taşınmıyor |
| K4 | Açılış davranışı | Web gibi açılışta hazır kart (0 dokunuş), öneri kartı ana ekranın parçası | Hız vaadi; web'de doğrulanmış |
| K5 | 4 opsiyonel filtrenin arayüzü | v1'de yok | Web'de yok; Sadelik md.6; motor destekliyor, sonra açılabilir |
| K6 | "Tamam, yapıyorum" sonrası | Web'deki mesaj ve mesajı gizleyen X; uygulamayı kapatan düğme **yok** (14.09.2026 revize) | Web birebirliği; kendiliğinden kapanma Android'de hata gibi algılanır |
| K7 | Bildirim, analytics, crash SDK | v1'de hiçbiri yok | İlk açılışta izin yasağı; web gizlilik taahhüdü; Data Safety sade kalır |
| K8 | Swipe | **Yok**; yalnızca görünür düğmeler (14.09.2026 revize) | Web'de yok; sadelik |
| K9 | detekt | v1'de yok; Spotless+ktlint+compose-rules+Lint; detekt 2.0 stabil olunca | detekt 1.23.8 Kotlin 2.4 ile uyumsuz olabilir |
| K10 | Modül bağımlılık yönü | data → domain (arayüzler domain'de) | v1 metni döngüseldi |
| K11 | Deep link / paylaşım | v1'de yok | Web'de öneri başına sayfa yok |
| K12 | DataStore türü | kotlinx.serialization ile tipli DataStore | Proto kadar tip güvenli, protobuf eklentisi yükü yok |
| K13 | Explicit API mode | Kapalı (yalnızca `:core:model` ve `:core:domain`'de açık) | Uygulama içi modüllerde gürültü |
| K14 | Metin ayrıştırıcı eşleşmesi | Önce birebir parite; iyileştirme web ile birlikte ayrı faz | Motorlar ayrışmasın |
| K15 | KVKK veri sorumlusu ve iletişim | Kaan Enes KAPICI · kaaneneskpc1@gmail.com | Proje sahibi tarafından verildi |
| K16 | Android repo | `kaaneneskpc/napsam-android`, private — **ERTELENDİ:** tüm işler bitince, proje sahibi açıkça isteyince (Faz 14). O zamana kadar yalnızca yerel git, uzak depo yok. | Yayına kadar gizli; web reposundan ayrı |
| K17 | Web API işi | Faz 1'de `/api/v1/config` + `/api/v1/suggest` | Android'in ağ ihtiyacı bunlarla sınırlı |
| K18 | Ekranlar | Ana ekran, Kaydedilenler, Gizlilik (`:feature:privacy`). Ayarlar ekranı ve "verilerimi sil" düğmesi yok; tema üst barda (14.09.2026) | Web birebirliği |
| K19 | Ana ekran içeriği | Web sırası: başlık → bütçe paneli → kart → üç tanıtım kutusu → alt bilgi (Gizlilik bağlantısı); görünüm Stitch son tasarımı (14.09.2026) | Proje sahibinin talimatı ve tasarımı |
