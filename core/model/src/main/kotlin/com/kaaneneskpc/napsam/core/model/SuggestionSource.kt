package com.kaaneneskpc.napsam.core.model

public enum class SuggestionSource(public val key: String) {
    /** Uygulamayla gelen, doğrulayıcıdan geçmiş içerik havuzu. */
    SEED("seed"),

    /** Sunucudaki AI katmanının "Yaz" modunda ürettiği öneri; yerel havuzda bulunmaz. */
    AI("ai"),
    ;

    public companion object {
        public fun fromKey(key: String): SuggestionSource? = entries.firstOrNull { it.key == key }
    }
}
