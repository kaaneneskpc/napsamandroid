package com.kaaneneskpc.napsam.core.model

/**
 * Önerinin yapıldığı yerin TÜRÜ (spesifikasyon A4). Şehir, ilçe veya koordinat değildir
 * ve hiçbir modelde böyle bir alan bulunmaz.
 */
public enum class Place(public val key: String) {
    /** Evde, hiçbir yere gitmeden. */
    HOME("ev"),

    /** Yürüme mesafesinde; her yerleşim yerinde karşılığı var. */
    NEARBY("yakin"),

    /** Belirli tipte mekan gerekir; tür `venueType` alanında, işletme adı yok. */
    VENUE("mekanli"),
    ;

    public companion object {
        public fun fromKey(key: String): Place? = entries.firstOrNull { it.key == key }
    }
}
