# Bu klasör otomatik üretilir — elle düzenleme

Kaynak: napsamweb, `DATABASE_URL= python manage.py export_mobile --out <klasör>`.
İçerik değişikliği web reposundaki `core/fixtures/suggestions.json` üzerinde yapılır,
içerik doğrulayıcısından geçer ve bu komutla yeniden üretilir.

- `content/` uygulamaya gömülen öneriler ve yerel varsayılanlar
- `parity/` mobil motorun Python motoruyla aynı davrandığını kanıtlayan altın veriler
