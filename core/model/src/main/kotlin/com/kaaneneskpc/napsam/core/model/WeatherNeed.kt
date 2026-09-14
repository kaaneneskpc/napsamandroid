package com.kaaneneskpc.napsam.core.model

/** Önerinin hava koşulu gereksinimi. Hava bilgisi v1'de istemciden gönderilmez (spesifikasyon C4.3). */
public enum class WeatherNeed(public val key: String) {
    ANY("any"),
    DRY("dry"),
    WARM("warm"),
    INDOOR("indoor"),
    ;

    public companion object {
        public fun fromKey(key: String): WeatherNeed? = entries.firstOrNull { it.key == key }
    }
}
