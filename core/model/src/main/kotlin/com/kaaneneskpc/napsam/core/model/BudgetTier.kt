package com.kaaneneskpc.napsam.core.model

import java.math.BigDecimal

/**
 * Bütçe kademesi (spesifikasyon A6.2).
 *
 * Tutarlar saklanmaz; net asgari ücrete oran olarak saklanır ve tutar çalışma anında
 * hesaplanır. [ratioMax] null ise üst sınır yoktur ("Bol"). Oranlar hassasiyet kaybı
 * olmasın diye [BigDecimal]'dır.
 */
public data class BudgetTier(
    public val key: String,
    public val label: String,
    public val hint: String,
    public val order: Int,
    public val ratioMin: BigDecimal,
    public val ratioMax: BigDecimal?,
) {
    public companion object {
        /** Kullanıcı bütçe seçmediyse varsayılan; ücretli öneri gösterilmez (spesifikasyon A7.2). */
        public const val DEFAULT_KEY: String = "bedava"
    }
}
