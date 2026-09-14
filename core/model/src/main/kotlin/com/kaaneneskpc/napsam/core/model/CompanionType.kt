package com.kaaneneskpc.napsam.core.model

/** Öneriyi kiminle yapmanın uygun olduğu. */
public enum class CompanionType(public val key: String) {
    SOLO("solo"),
    COUPLE("couple"),
    FRIENDS("friends"),
    FAMILY("family"),
    KIDS("kids"),
    ;

    public companion object {
        public fun fromKey(key: String): CompanionType? = entries.firstOrNull { it.key == key }
    }
}
