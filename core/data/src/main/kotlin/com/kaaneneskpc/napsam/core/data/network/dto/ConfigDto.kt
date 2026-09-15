package com.kaaneneskpc.napsam.core.data.network.dto

import com.kaaneneskpc.napsam.core.model.AppConfig
import com.kaaneneskpc.napsam.core.model.BudgetTier
import java.math.BigDecimal
import kotlinx.serialization.Serializable

/** `GET /api/v1/config/` yanıtı (spesifikasyon C3.3). Ücret ve oranlar hassasiyet kaybı olmasın diye metindir. */
@Serializable
internal data class ConfigDto(
    val schema: Int,
    val wage: String,
    val currency: String,
    val budgetTiers: List<BudgetTierDto>,
    val weeklyTheme: String? = null,
    val themeRotation: List<String>,
    val aiEnabled: Boolean,
    val contentVersion: String,
    val suggestionCount: Int,
    val dismissCooldownDays: Int,
    val androidMinVersionCode: Int = 1,
) {
    companion object {
        const val SUPPORTED_SCHEMA = 1
    }
}

/** Kademe tutarları istemcide hesaplanır (`BudgetCalculator`); yanıttaki `amount*` alanları okunmaz. */
@Serializable
internal data class BudgetTierDto(
    val key: String,
    val label: String,
    val hint: String = "",
    val order: Int,
    val ratioMin: String,
    val ratioMax: String? = null,
)

internal fun BudgetTierDto.toModel(): BudgetTier = BudgetTier(
    key = key,
    label = label,
    hint = hint,
    order = order,
    ratioMin = BigDecimal(ratioMin),
    ratioMax = ratioMax?.let { BigDecimal(it) },
)

/** Desteklenmeyen şema, boş kademe listesi veya sayıya çevrilemeyen tutar: null (önbelleğe alınmaz). */
internal fun ConfigDto.toModelOrNull(): AppConfig? {
    if (schema != ConfigDto.SUPPORTED_SCHEMA || budgetTiers.isEmpty()) return null
    return try {
        AppConfig(
            wage = BigDecimal(wage),
            currency = currency,
            budgetTiers = budgetTiers.map { it.toModel() },
            weeklyTheme = weeklyTheme,
            themeRotation = themeRotation,
            aiEnabled = aiEnabled,
            contentVersion = contentVersion,
            suggestionCount = suggestionCount,
            dismissCooldownDays = dismissCooldownDays,
            androidMinVersionCode = androidMinVersionCode,
        )
    } catch (e: NumberFormatException) {
        null
    }
}
