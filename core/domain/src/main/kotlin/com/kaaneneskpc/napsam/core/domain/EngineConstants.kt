package com.kaaneneskpc.napsam.core.domain

/**
 * Öneri motoru sabitleri (spesifikasyon C4.2). Web `core/engine.py` ve web istemcisiyle birebir aynıdır;
 * `contract/parity/engine.json` ile test edilir, burada tek başına değiştirilmez.
 *
 * Web'de tanımlı ama kullanılmayan `W_IN_BAND` ve `W_BELOW_BAND` port edilmedi.
 */
public object EngineConstants {
    public const val NIGHT_START_HOUR: Int = 22
    public const val NIGHT_END_HOUR: Int = 6

    /** Mekanlar bu saatler dışında büyük olasılıkla kapalıdır (gece filtresi). */
    public const val VENUE_OPEN_FROM: Int = 8
    public const val VENUE_OPEN_UNTIL: Int = 23

    public const val W_KEYWORD: Double = 3.0

    /** [NEAR_SCORE]'dan küçük kalmalı; büyük olduğunda tema sert filtre gibi davranıyordu. */
    public const val W_THEME: Double = 1.0
    public const val W_ENERGY: Double = 2.0
    public const val W_COMPANION: Double = 2.0
    public const val W_PLACE_PREF: Double = 2.5
    public const val W_FREE_BONUS: Double = 0.5
    public const val W_FREE_WHEN_PAID: Double = -2.0
    public const val W_RECENT_CATEGORY_PENALTY: Double = -2.5
    public const val W_ALREADY_SHOWN_PENALTY: Double = -1.5
    public const val W_JITTER: Double = 1.2

    /** Seçim, en iyi puana bu kadar yakın adaylar arasından yapılır. */
    public const val NEAR_SCORE: Double = 1.5

    /** Örnekleme kapağı havuzla ölçeklenir: en az [TOP_N_MIN], en az havuzun %30'u. */
    public const val TOP_N_FRACTION: Double = 0.30
    public const val TOP_N_MIN: Int = 15

    /** Geçilen öneri bu kadar gün tekrar gösterilmez. */
    public const val DISMISS_COOLDOWN_DAYS: Int = 30

    /** "Son kayıtlar" cezası için bakılan kullanıcı kaydı sayısı. */
    public const val RECENT_STATES: Int = 5

    /** Oturumda tekrar gösterilmeyen son öneri sayısı (web istemcisi). */
    public const val SESSION_EXCLUDED: Int = 30

    /** Web isteğinin kabul ettiği üst sınırlar. */
    public const val MAX_KEYWORDS: Int = 8
    public const val MAX_EXCLUDED: Int = 40
    public const val MAX_TEXT_LEN: Int = 280
}
