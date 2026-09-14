package com.kaaneneskpc.napsam.core.model

import java.math.BigDecimal

/**
 * Uygulamanın uzaktan değişebilen yapılandırması (spesifikasyon C3.3).
 *
 * Her zaman yerel bir varsayılanı vardır (`contract/content/meta.json`); sunucu yanıtı
 * gelmezse uygulama bu varsayılanla tam çalışır.
 */
public data class AppConfig(
    public val wage: BigDecimal,
    public val currency: String,
    public val budgetTiers: List<BudgetTier>,
    /** Sunucunun bu hafta için seçtiği tema; null ise yerel rotasyon kuralı uygulanır. */
    public val weeklyTheme: String?,
    public val themeRotation: List<String>,
    public val aiEnabled: Boolean,
    public val contentVersion: String,
    public val suggestionCount: Int,
    public val dismissCooldownDays: Int,
    public val androidMinVersionCode: Int,
)
