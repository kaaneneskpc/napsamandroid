Workshop: kurulum
1. Açılış
   proje katmanındaysan: cd napsam
   dışarıdan terminal ile geliştirme yapıcaksan: cd /Users/kkapici/StudioProjects/napsam (yol sizinki farklı olabilir)
   agy --mode=accept-edits --effort low
   accept-edits dosya düzenlemelerini onay beklemeden yapar. --effort low ajanın gereğinden uzun düşünmesini kısar. Komut çalıştırma izinlerini 4. adımda açıyoruz.

2. Kuralları ajana ver: AGENTS.md
   Antigravity proje kökündeki AGENTS.md dosyasını kendisi okur. agy'ye şunu yaz:
   docs/napsam-android-prompt.md ve README.md dosyalarını oku.
   Proje köküne, kod ajanlarının okuyacağı en fazla 40 satırlık bir AGENTS.md yaz.
   İçinde şunlar olsun: ürün kuralları, modül kuralları, yasaklar, kullanılacak komutlar ve "bitti" tanımı.
   Spec'te olmayan bir kural ekleme. Dosyayı yazmadan önce metni bana göster.
   Metni oku, gereksizi sil, onayla. Kontrol: AGENTS.md'de "Commit atma" satırı olmalı; yoksa elle ekle.

3. İki ajan kur
   Biri kodu yazacak, diğeri sadece kontrol edecek. İkisi de hızlı modelle (flash) çalışıyor. agy'ye şunu yaz:
   .agents/agents/ altına iki ajan oluştur; her biri kendi klasöründe agent.md olsun.

a) android-dev/agent.md
name: android-dev
description: napsam'da Kotlin ve Compose kodu yazan ajan. Kod değişikliği gereken her işte kullan.
tools: [view_file, grep_search, run_command, write_to_file, replace_file_content]
model: flash
Gövde: Önce AGENTS.md'yi oku. Sadece görevde adı geçen dosya ve modüllere dokun.
Golden kaydetme, test silme, commit ve push yapma.
Bitince değişen dosyaları ve test sonucunu raporla.

b) ui-reviewer/agent.md
name: ui-reviewer
description: Bir değişiklik bittiğinde kontrol için kullan. Kod değiştirmez.
tools: [view_file, grep_search, run_command]
model: flash
Gövde: Değişen modülleri git status ile bul. Golden'ı olan modüllerde (core:designsystem,
feature:home, feature:saved, feature:privacy) ./gradlew :<modül>:verifyRoborazziDebug,
diğerlerinde ./gradlew :<modül>:testDebugUnitTest çalıştır. Hataları ve ekran görüntüsü
farklarını listele, sonucu YEŞİL ya da KIRMIZI diye raporla. Kod değiştirme.
Kontrol: /agents yaz; iki ajan listede görünmeli. ui-reviewer'ın araçlarında dosya yazma yok, yani kod değiştiremez. Salona 12. slayttaki yetki tablosunu hatırlat.
Hazır paket: bu iki ajanın tam hâli ve 12 ajan daha proje kökündeki napsam-ajan-skill-paketi.zip içinde
(ayrıntı: README.md → "Yapay zekâ ajanları ve skill'ler"). Workshop'ta iki ajanı elle kur; ne yazdığını
görmek asıl ders. Paketi 8. adımda açıyoruz. Zamanın yetmezse burada açıp 4. adıma geçebilirsin:
unzip -o napsam-ajan-skill-paketi.zip
Not: paketteki ui-reviewer raporu YEŞİL/KIRMIZI değil, UYGUN / KISMEN UYGUN / UYGUN DEĞİL diye verir.

4. İzinler ve hook
   Alt ajanların onay beklemeden çalışması bu adıma bağlı: alt ajanlar ana oturumun komut izinlerini devralıyor. /permissions yaz, Project kapsamını seç ve şu kuralları ekle:
   • allow: command(./gradlew), command(git status), command(git diff)
   • deny: command(git commit), command(git push)
   Deny her zaman allow'dan önce gelir; commit ve push yasak kalır.
   Sonra biçimlendirme hook'u. agy'ye şunu yaz:
   .agents/hooks.json dosyası oluştur. Stop olayında ./gradlew spotlessApply -q komutu çalışsın.
   Oluşması gereken dosya:
   {
   "spotless": {
   "Stop": [{
   "hooks": [{ "type": "command", "command": "./gradlew spotlessApply -q", "timeout": 120 }]
   }]
   }
   }
   Workshop: üç özellik

5. Her özellikte aynı döngü
   Ajanlar artık onay beklemiyor; sen sadece planı okuyorsun, kontrol ettiriyorsun ve commit atıyorsun.
   a. Prompt'u ver (ilk özellikte /plan ile).
   b. android-dev kodu yazıp sadece kendi modülünün testlerini koşsun.
   c. ui-reviewer'a kontrol ettir. Her özellikte aynı prompt:
   ui-reviewer ajanını kullan. Son değişikliği kontrol et. Kod değiştirme.
   d. Ekran görüntüsü farkı varsa kendin bak; doğruysa sadece o modülde kaydet (örnek: ./gradlew :feature:home:recordRoborazziDebug).
   e. Uygulamayı Android Studio'dan emülatörde aç ve özelliği elle dene.
   f. Commit'i sen at, ayrı terminalde.

6. Özellik 1: Kartı paylaş
   Bunu salonla birlikte, döngünün her adımını anlatarak yap. Plan onayı sadece burada var; "önce plan" alışkanlığını gösteriyoruz. Dosyaları prompt'ta verdiğimiz için ajan kod aramakla vakit kaybetmiyor.
   /plan android-dev ajanını kullan.

CONTEXT: Spec A6.4, B12, B19 ve C11-K21. Dokunulacak dosyalar:
core/designsystem/src/main/kotlin/com/kaaneneskpc/napsam/core/designsystem/icon/NapsamIcons.kt
core/designsystem/src/main/kotlin/com/kaaneneskpc/napsam/core/designsystem/component/Buttons.kt
core/ui/src/main/kotlin/com/kaaneneskpc/napsam/core/ui/SuggestionCard.kt
core/ui/src/main/res/values/strings.xml
feature/home/src/main/kotlin/com/kaaneneskpc/napsam/feature/home/HomeScreen.kt
GOAL: Öneri kartında kalbin yanında bir "Paylaş" ikon düğmesi olsun. Basınca Android paylaşım penceresi açılsın.
Paylaşılan metin: başlık, numaralı adımlar ve "Olmazsa: ..." satırı.
CONSTRAINT: Ana düğme tek kalsın ("Tamam, yapıyorum"). Yeni kütüphane ve izin yok.
Testler dışında bu dosyaların dışına çıkma. Ekran görüntüsü kaydetme, commit atma.
VERIFY: ./gradlew :core:ui:testDebugUnitTest :feature:home:testDebugUnitTest
OUTPUT: Değişen dosyalar ve test sonucu.
Beklenen: ui-reviewer KIRMIZI döner, çünkü kart değişti ve ekran görüntüleri farklı. Bu normal. Farkı aç (modülün build/outputs/roborazzi klasörü), düğme %200 yazıda taşıyor mu bak, doğruysa kaydet:
./gradlew :core:designsystem:recordRoborazziDebug :feature:home:recordRoborazziDebug
git add -A && git commit -m "Kartı paylaş"
ui-reviewer ajanını kullan. Son değişikliği kontrol et. Kod değiştirme.

7. Özellik 2 ve 3: paralel
   İki özellik ayrı modüllerde (feature:saved ve app), birbirine dokunmuyor. Tek prompt'la iki android-dev alt ajanı aynı anda çalışıyor. Onlar çalışırken salona /agents panelini aç, iki ajanın aynı anda ilerlediğini göster.
   İki işi aynı anda yap: iki ayrı android-dev alt ajanı başlat, birbirini beklemesinler.
   Plan için onay bekleme; planı rapora yaz ve uygula.

İŞ A · Kaydedilenler'de geri al (spec A6.5, C11-K22)
Dosyalar: feature/saved/src/main/kotlin/com/kaaneneskpc/napsam/feature/saved/SavedViewModel.kt,
SavedScreen.kt ve feature/saved/src/main/res/values/strings.xml
GOAL: Öneri kaldırılınca altta "Kaldırıldı · Geri al" mesajı çıksın. Geri al'a basılırsa öneri
eski yerine dönsün; basılmazsa mesaj kapanınca kalıcı silinsin.
CONSTRAINT: Silmeyi mesaj kapanana kadar beklet; hemen silip yeniden kaydetme, sıra bozulur.
Mantık SavedViewModel'de. Repository arayüzü değişmesin.
VERIFY: ./gradlew :feature:saved:testDebugUnitTest
SavedViewModelTest'e test: kaldır, geri al, öneri yerinde; kaldır, süre doldu, unsave çağrıldı.

İŞ B · Kaydedilenler kısayolu (spec B5, C11-K23)
Dosyalar: app/src/main/AndroidManifest.xml, app/src/main/kotlin/com/kaaneneskpc/napsam/MainActivity.kt,
NapsamApp.kt, yeni app/src/main/res/xml/shortcuts.xml ve app/src/main/res/values/strings.xml
GOAL: Simgeye basılı tutunca statik "Kaydedilenler" kısayolu. Kısayolla açılınca doğrudan
Kaydedilenler açılsın, geri tuşu ana ekrana dönsün.
CONSTRAINT: Feature modüllerine dokunma. Navigation 3 yapısı korunsun.
VERIFY: ./gradlew :app:testDebugUnitTest :app:assembleDebug

Her iki iş için: yeni kütüphane ve izin yok, metinler strings.xml'de, ekran görüntüsü kaydetme,
commit atma. Bitince her iş için değişen dosyaları ve test sonucunu ayrı ayrı raporla.
ui-reviewer ajanını kullan. Son değişikliği kontrol et. Kod değiştirme.

Workshop: tam ekip

8. Ajan ve skill paketini aç
   İki ajanla döngüyü gördük. Gerçek projede işi daha küçük rollere bölüyoruz: biri okur, biri ölçer, biri yazar,
   dördü kapı tutar. Hepsi hazır: napsam-ajan-skill-paketi.zip (README.md → "Yapay zekâ ajanları ve skill'ler").
   unzip -o napsam-ajan-skill-paketi.zip
   -o, 3. adımda elle yazdığın android-dev ve ui-reviewer'ın üzerine paketteki tam hâllerini yazar.
   Kontrol: /agents yaz; 14 ajan görünmeli. .agents/skills/ altında 7 skill olmalı.
   Paketteki her ajan önce AGENTS.md'yi okur (2. adımda yazdık) ve aynı ortak kuralları taşır:
   git'te sadece okuma, test silme yok, sonuç exit code'dan, riskli alanlarda önce sor.

   Salona yetki tablosunu göster:
   • Dosya yazan (6): android-dev, navigation-engineer, stitch-designer, test-writer, error-fixer, doc-updater
   • Salt okunur (8): issue-reader, spec-reader, analyzer, code-reviewer, ui-reviewer, parity-guard,
     security-reviewer, release-guard
   • Model: analyzer, code-reviewer, error-fixer, security-reviewer, release-guard "inherit" (ana oturumun
     modeli, derin düşünme gereken işler); kalan 9 ajan flash.

   Yeni izinler: issue-reader GitHub'ı gh ile okur. /permissions → Project kapsamına ekle:
   • allow: command(gh issue view), command(gh pr view)
   Commit ve push deny kuralları yerinde kalıyor; paketteki hiçbir ajan commit atmaz.

9. Tam ekiple bir iş
   Akış: issue-reader → spec-reader → analyzer → android-dev → test-writer → code-reviewer
   → kapılar (ui-reviewer, parity-guard, security-reviewer, release-guard) → doc-updater.
   Kapı kırmızıysa error-fixer kök nedeni bulur, test-writer önce hatayı yakalayan testi yazar.
   Örnek prompt (6. adımdaki "Kartı paylaş" işini sıfırdan, ekip olarak):
   Önce analyzer ajanını kullan: "Kartı paylaş" (spec A6.4, C11-K21) için SuggestionCard'ın bütün çağrı
   yerlerini, etkilenen golden'ları ve testleri listele. Dosya değiştirme.
   Sonra android-dev uygulasın, test-writer testleri yazsın, code-reviewer diff'i incelesin ve hangi
   kapıların çalışması gerektiğini söylesin. O kapıları çalıştır. En sonda doc-updater README'yi güncellesin.
   Commit mesajını napsam-commit-message skill'iyle öner; commit'i ben atacağım.
   Beklenen: code-reviewer UI değiştiği için ui-reviewer'ı ister; motor ve ağ değişmediği için parity-guard
   ve security-reviewer gerekmez. Salona bunu göster: her kapı her işte çalışmaz, diff hangisini istediğini söyler.

10. Skill'ler: ajanın tarif kitabı
    Ajan "kim", skill "nasıl". Skill'ler ilgili iş gelince kendiliğinden yüklenir:
    • napsam-build-verify: bitirmeden önce değişen modüller + CI komutu (./gradlew check verifyRoborazziDebug)
    • napsam-compose-screen: yeni ekran/UiState (feature/home referans)
    • napsam-design-sync: tasarımdan token taşıma (stitch-designer kullanır)
    • napsam-ui-defect-map: görsel hata → mekanizma
    • napsam-local-repro: cihazda yeniden üretme (süreç ölümü, %200 yazı, koyu tema, uçak modu)
    • napsam-content-update: contract/ ve içerik güncelleme akışı
    • napsam-commit-message: commit mesajı ve commit öncesi engeller
    Kontrol: "Derleniyor mu, testler geçiyor mu?" yaz; ajan napsam-build-verify'ı yükleyip önce değişen
    modülleri çıkarmalı, bütün projeyi değil.


