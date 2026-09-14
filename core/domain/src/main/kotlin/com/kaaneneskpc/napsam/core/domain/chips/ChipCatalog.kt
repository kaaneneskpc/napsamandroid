package com.kaaneneskpc.napsam.core.domain.chips

/**
 * Kelime modu chip setleri (spesifikasyon C7, web `core/chips.py`). Set günün saatine göre değişir:
 * gece 03:00'te "kahve" göstermek kullanıcıyı bir kez kaybettirir.
 *
 * Chip metni içerikteki etiketle BİREBİR aynıdır; aksi halde eşleşme sıfır olur.
 * `contract/parity/chips.json` ile test edilir.
 */
public object ChipCatalog {
    public val MORNING: List<String> = listOf(
        "yürümek", "kahve", "dışarı", "yeni bir şey", "sakin",
        "ellerimi kullanayım", "öğrenmek", "kısa sürsün", "ucuz", "yemek",
    )

    public val MIDDAY: List<String> = listOf(
        "dışarı", "yürümek", "arkadaşımla", "hareket", "öğrenmek",
        "fotoğraf", "yemek", "kalabalık olmasın", "yeni bir şey", "ucuz",
    )

    public val EVENING: List<String> = listOf(
        "evde", "sakin", "arkadaşımla", "yemek", "ellerimi kullanayım",
        "kahve", "yeni bir şey", "kısa sürsün", "dışarı", "tek başıma",
    )

    public val NIGHT: List<String> = listOf(
        "evde",
        "sakin",
        "gece",
        "tek başıma",
        "kısa sürsün",
        "ellerimi kullanayım",
        "öğrenmek",
        "ucuz",
    )

    /** 06:00–10:59 sabah · 11:00–16:59 öğle · 17:00–21:59 akşam · diğer saatler gece. */
    public fun forHour(hour: Int): List<String> = when (hour) {
        in 6..10 -> MORNING
        in 11..16 -> MIDDAY
        in 17..21 -> EVENING
        else -> NIGHT
    }
}
