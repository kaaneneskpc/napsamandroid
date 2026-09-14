package com.kaaneneskpc.napsam.core.model

/** Öneri isteğinin giriş modu (spesifikasyon A6.1). */
public enum class SuggestionMode(public val key: String) {
    /** NAPSAM? düğmesi: filtre yok, soru yok. */
    QUICK("quick"),

    /** Kelime seç. */
    CHIPS("chips"),

    /** Serbest metin ("Yaz"). */
    TEXT("text"),
    ;

    public companion object {
        public fun fromKey(key: String): SuggestionMode? = entries.firstOrNull { it.key == key }
    }
}
