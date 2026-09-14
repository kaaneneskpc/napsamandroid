package com.kaaneneskpc.napsam.core.model

public enum class Energy(public val key: String) {
    LOW("low"),
    MEDIUM("medium"),
    HIGH("high"),
    ;

    public companion object {
        public fun fromKey(key: String): Energy? = entries.firstOrNull { it.key == key }
    }
}
