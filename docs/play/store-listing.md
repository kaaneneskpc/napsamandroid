# Play mağaza girişi ve uygulama içeriği — taslak

Dil: yalnızca Türkçe (tr-TR); uygulama tek dilli. Metinler web'in kendi cümlelerinden alındı (K19); Play meta
veri politikası gereği üstünlük iddiası ("en iyi", "1 numara"), emoji ve anahtar kelime tekrarı yok.

## Mağaza metinleri

**Uygulama adı** (en fazla 30 karakter):

```text
napsam: Ne yapsam ya?
```

**Kısa açıklama** (en fazla 80 karakter):

```text
Bütçene uyan tek bir somut fikir, tek dokunuşla. Üyelik yok, reklam yok.
```

**Tam açıklama** (en fazla 4000 karakter):

```text
Sonsuz listelere ve kararsızlığa son. 10 saniyede somut, uygulanabilir tek bir eylem.

"Ne yapsam ya?" dediğinde napsam'ı aç: bütçeni seç, NAPSAM?'a bas. Karşına tek bir kart çıkar: ne
yapacağın, 2-3 adımda nasıl yapacağın ve plan tutmazsa alternatifi. Beğenmezsen "Başka fikir", beğenirsen
kaydet ya da "Tamam, yapıyorum" de ve uygulamayı kapatıp harekete geç.

Tek odak
Seçenekler çoğaldıkça felç olursun. napsam tek ve net bir yön önerir.

Varsayılan: 0 TL
Zaman geçirmek parayla ölçülmez. Tüm öneriler önce bütçesiz kurgulanır; istersen Az, Orta, İyi ya da Bol
bütçe seçersin. Tutarlar net asgari ücrete göre hesaplanır.

Somut adımlar
Soyut tavsiye değil, hemen şimdi yapabileceğin sıralı adımlar.

İstersen daralt
Kelime seç ("kahve", "dışarı", "arkadaşımla") ya da ne istediğini birkaç kelimeyle yaz; öneri buna göre gelir.

Konum bağımsız
Her öneri Türkiye'nin her yerinde yapılabilir. Şehir, semt ya da işletme adı yok; konum izni istenmez.

Gizlilik
Üyelik, reklam ve takip yok. Kaydettiklerin yalnızca telefonunda durur. Öneriler telefonunda üretilir;
uygulama internetsiz de tam çalışır.
```

| Alan | Değer |
|---|---|
| Kategori | Yaşam Tarzı |
| E-posta (herkese açık) | kaaneneskpc1@gmail.com (K15) |
| Web sitesi | https://napsam.vercel.app |
| Gizlilik politikası | https://napsam.vercel.app/gizlilik/ (Android bölümü web dalı yayınlanınca görünür, `docs/RELEASE.md`) |

## Görseller

| Görsel | Durum |
|---|---|
| Uygulama ikonu 512×512 PNG | Hazır: `docs/play/assets/ikon-512.png` (web favicon'undan üretildi, uygulama ikonuyla aynı) |
| Öne çıkan görsel 1024×500 | Yapılacak — cihaz denemeleriyle birlikte en sonda |
| Telefon ekran görüntüleri (en az 2, önerilen 4-8) | Yapılacak — en sonda; açık ve koyu tema: ana ekran (kart), daraltma paneli, Kaydedilenler, Gizlilik |

> Play ekran görüntülerinde uzun kenar kısa kenarın en fazla 2 katı olabilir. Pixel 9 Pro emülatörü
> 1280×2856 (2,23:1) verdiği için doğrudan kullanılamaz: 1080×2160 (2:1) ya da 1080×1920 boyutlu bir
> emülatörde çekilecek.

## Uygulama içeriği formları

| Form | Yanıt |
|---|---|
| Gizlilik politikası | https://napsam.vercel.app/gizlilik/ |
| Reklamlar | Uygulamada reklam yok |
| Uygulama erişimi | Tüm işlevler giriş gerektirmeden kullanılabilir |
| İçerik derecelendirmesi (IARC) | Kategori: yardımcı araç / yaşam tarzı. Şiddet, cinsellik, küfür, alkol-tütün-madde, gerçek ya da taklit şans oyunu yok; kullanıcılar arası etkileşim ve içerik paylaşımı yok; konum paylaşımı yok; dijital satın alma yok. (500 önerinin metni alkol, tütün ve şans oyunu için tarandı: eşleşme yok; "tombala" parasız aile oyunu önerisi.) |
| Hedef kitle | 13 yaş ve üzeri (B16); çocuklara yönelik değil |
| Haber uygulaması | Hayır |
| Veri güvenliği | `docs/play/data-safety.md` |
| Devlet uygulaması | Hayır |
| Finansal özellikler | Yok (bütçe kademeleri yalnızca öneri filtresi; ödeme, kredi, yatırım yok) |
| Sağlık | Sağlık uygulaması değil |

## Ad çakışması araştırması (16.09.2026)

Paket adı `com.kaaneneskpc.napsam` kesinleşti; görünen ad "napsam" sonradan değişebilir. Web aramasında:

- **napsam.net** — "AI Destekli Kişiselleştirilmiş Öneriler | Film, Dizi, Yemek, Müzik" başlıklı Türkçe bir
  site. İşleteni sayfada belirtilmiyor, mağaza uygulaması görünmüyor. Aynı ad, aynı pazar ve benzer bir alan
  (öneri) olduğu için **karışıklık ve marka riski var**. Bu site sana ait değilse, ilk yüklemeden önce
  TÜRKPATENT marka araştırması (https://www.turkpatent.gov.tr) yapılması önerilir.
- Google Play'de "napsam" adlı uygulama bulunmadı; benzer adlar (NAPS, Naps, NAPSA) farklı alanlarda.
- İngilizce marka veri tabanlarında (Justia) "NAPSAM" kaydı görünmedi. TÜRKPATENT veri tabanı etkileşimli
  arama istediği için taranamadı.

Kaynaklar: [napsam.net](https://napsam.net/en), [Google Play arama sonuçları](https://play.google.com/store/apps/details?id=com.napsinfocenter.naps&hl=en),
[Justia Trademarks](https://trademarks.justia.com/).
