package com.kaaneneskpc.napsam.core.model

/** Kullanıcının ev/dışarı tercihi. Seçilmediyse bağlamda null'dır. */
public enum class PlacePreference(public val key: String) {
    /** Yalnızca [Place.HOME] öneriler. */
    HOME("ev"),

    /** [Place.HOME] dışındaki öneriler. */
    OUTSIDE("disari"),
    ;

    public companion object {
        public fun fromKey(key: String): PlacePreference? = entries.firstOrNull { it.key == key }
    }
}
