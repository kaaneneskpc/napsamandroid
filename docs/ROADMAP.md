1) Kuralları ajana ver:
   AGENTS.md docs/napsam-android-prompt.md ve README.md dosyalarını oku. Proje
   köküne, kod ajanlarının okuyacağı en fazla 40 satırlık bir AGENTS.md yaz. İçinde şunlar olsun:
   ürün kuralları, modül kuralları, yasaklar, kullanılacak komutlar ve "bitti" tanımı. Spec'te
   olmayan bir kural ekleme. Dosyayı yazmadan önce metni bana göster.

2) İki ajan kur .claude/agents/ klasörüne iki ajan dosyası oluştur:

android-dev: Kotlin ve Compose kodu yazar. Araçları: Read, Edit, Write, Grep, Glob, Bash. Önce
AGENTS.md'yi okur, planı gösterip onay bekler. Ekran görüntüsü (golden) kaydetmez, test silmez,
commit ve push yapmaz.

ui-reviewer: Sadece kontrol eder, kod değiştirmez. Araçları: Read, Grep, Glob, Bash. ./gradlew check
verifyRoborazziDebug çalıştırır, hataları ve ekran görüntüsü farklarını listeler, sonucu YEŞİL ya da
KIRMIZI diye raporlar.

3) Kuralı otomatiğe bağla .claude/settings.json dosyası oluştur:

- Her dosya düzenlemesinden (Edit veya Write) sonra ./gradlew spotlessApply -q çalışan bir
  PostToolUse hook'u ekle.
- git commit ve git push komutlarını yasakla.

4) Özellik 1: Kartı paylaş
   Kartın ana düğmesi değişmiyor; kalbin yanına küçük bir paylaş ikonu geliyor.
   android-dev ajanını kullan.

CONTEXT: :core:designsystem, :core:ui ve :feature:home modülleri.
Spec A6.4, B12, B19, B20 ve C11-K21.
GOAL: Öneri kartında kalbin yanında bir "Paylaş" ikon düğmesi olsun.
Basınca Android paylaşım penceresi açılsın.
Paylaşılan metin: başlık, numaralı adımlar ve "Olmazsa: ..." satırı.
CONSTRAINT: Ana düğme tek kalsın ("Tamam, yapıyorum"). Yeni kütüphane ve izin yok.
İkon NapsamIcons'a eklensin. Metinler strings.xml'de, Türkçe ve İngilizce.
Ekran görüntüsü kaydetme, commit atma.
VERIFY: ./gradlew :core:ui:check :feature:home:check
Paylaşım metni için birim testi, düğmeye tıklama için UI testi.
OUTPUT: Değişen dosyalar, çalıştırdığın komutlar ve sonuçları, açık sorular.
Önce planı göster, onay verince başla.

ui-reviewer ajanını kullan. Son değişikliği kontrol et. ./gradlew check verifyRoborazziDebug
çalıştır, hataları ve ekran görüntüsü farklarını tek tek anlat. Kod değiştirme.

Özellik 2: Kaydedilenler'de geri al:

android-dev ajanını kullan.

CONTEXT: :feature:saved modülü. Spec A6.5, B3, B12, B13, B19 ve C11-K22.
GOAL: Kaydedilenler'de bir öneri kaldırılınca ekranın altında "Kaldırıldı · Geri al" mesajı çıksın.
Geri al'a basılırsa öneri listede eski yerine dönsün.
Basılmazsa mesaj kapanınca kalıcı olarak silinsin.
CONSTRAINT: Mantık SavedViewModel'de olsun, ekranda değil. Repository arayüzü değişmesin.
Yeni kütüphane yok. Metinler strings.xml'de, Türkçe ve İngilizce.
Ekran görüntüsü kaydetme, commit atma.
VERIFY: ./gradlew :feature:saved:check
SavedViewModel için birim testi: kaldır, geri al, öneri yerinde;
kaldır, süre doldu, unsave çağrıldı.
OUTPUT: Değişen dosyalar, çalıştırdığın komutlar ve sonuçları, açık sorular.
Önce planı göster, onay verince başla.

ui-reviewer ajanını kullan. Son değişikliği kontrol et. ./gradlew check verifyRoborazziDebug
çalıştır, hataları ve ekran görüntüsü farklarını tek tek anlat. Kod değiştirme.

Özellik 3: Kaydedilenler kısayolu

android-dev ajanını kullan.

CONTEXT: :app modülü (MainActivity, NapsamApp.kt, manifest). Spec A3, B5, B19 ve C11-K23.
GOAL: Uygulama simgesine basılı tutunca "Kaydedilenler" kısayolu görünsün (statik kısayol).
Kısayolla açılınca uygulama doğrudan Kaydedilenler'de açılsın; geri tuşu ana ekrana dönsün.
CONSTRAINT: Yeni izin ve yeni kütüphane yok. Kısayol metni strings.xml'de, Türkçe ve İngilizce.
Navigation 3 yapısı korunsun; feature modüllerine dokunma.
Ekran görüntüsü kaydetme, commit atma.
VERIFY: ./gradlew :app:check :app:assembleDebug
Kısayolla açılışta ilk ekranın Kaydedilenler olduğunu gösteren bir test.
OUTPUT: Değişen dosyalar, çalıştırdığın komutlar ve sonuçları, açık sorular.
Önce planı göster, onay verince başla.


ui-reviewer ajanını kullan. Son değişikliği kontrol et. ./gradlew check verifyRoborazziDebug
çalıştır, hataları ve ekran görüntüsü farklarını tek tek anlat. Kod değiştirme.
