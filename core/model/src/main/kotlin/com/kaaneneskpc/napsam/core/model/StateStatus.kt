package com.kaaneneskpc.napsam.core.model

/** Kullanıcının bir öneriyle ilişkisi. */
public enum class StateStatus(public val key: String) {
    SHOWN("shown"),
    SAVED("saved"),
    DISMISSED("dismissed"),
    DONE("done"),
    ;

    public companion object {
        public fun fromKey(key: String): StateStatus? = entries.firstOrNull { it.key == key }
    }
}
