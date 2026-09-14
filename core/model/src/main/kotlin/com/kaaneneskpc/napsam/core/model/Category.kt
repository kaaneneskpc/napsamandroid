package com.kaaneneskpc.napsam.core.model

/**
 * Öneri kategorisi. [key] web ve içerik dosyasındaki anahtardır; görünen etiket veri
 * (içerik meta dosyası) ya da arayüz kaynaklarından gelir, burada tutulmaz.
 */
public enum class Category(public val key: String) {
    EV_ICI("ev_ici"),
    MUTFAK("mutfak"),
    DISARI_YAKIN("disari_yakin"),
    YEME_ICME("yeme_icme"),
    HAREKET("hareket"),
    KULTUR("kultur"),
    SOSYAL("sosyal"),
    OGRENME("ogrenme"),
    URETME("uretme"),
    DOGA("doga"),
    KENDINE_BAKIM("kendine_bakim"),
    IYILIK("iyilik"),
    OYUN("oyun"),
    UFAK_ISLER("ufak_isler"),
    ;

    public companion object {
        public fun fromKey(key: String): Category? = entries.firstOrNull { it.key == key }
    }
}
