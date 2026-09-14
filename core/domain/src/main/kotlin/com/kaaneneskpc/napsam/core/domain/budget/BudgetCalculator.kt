package com.kaaneneskpc.napsam.core.domain.budget

import com.kaaneneskpc.napsam.core.model.BudgetTier
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.absoluteValue

/**
 * Bütçe kademelerinin TL tutarları (spesifikasyon A6.2, web `BudgetTier.amount_min/amount_max/describe`).
 *
 * Tutar = oran × net asgari ücret, en yakın 10 TL'ye yuvarlanır; tam ortada kalan değer çift olana
 * yuvarlanır (Python `Decimal.quantize` varsayılanı HALF_EVEN). Asgari ücret değişince kademeler
 * kendiliğinden kayar. `contract/parity/budget.json` ile test edilir.
 */
public class BudgetCalculator(tiers: List<BudgetTier>, public val wage: BigDecimal) {
    /** Etkin kademeler, `order` sırasıyla. */
    public val tiers: List<BudgetTier> = tiers.sortedBy { it.order }

    public fun tier(key: String): BudgetTier? = tiers.firstOrNull { it.key == key }

    public fun amountMin(tier: BudgetTier): Int = roundToTen(tier.ratioMin.multiply(wage))

    /** null: üst sınır yok ("Bol"). */
    public fun amountMax(tier: BudgetTier): Int? = tier.ratioMax?.let { roundToTen(it.multiply(wage)) }

    /** Kademenin altında görünen tutar metni: "0 TL", "~1-150 TL", "1.500 TL+". */
    public fun amountText(tier: BudgetTier): String {
        val low = amountMin(tier)
        val high = amountMax(tier) ?: return "${grouped(low)} TL+"
        if (low == 0 && high == 0) return "0 TL"
        return "~${grouped(maxOf(low, 1))}-${grouped(high)} TL"
    }

    /** Sert bütçe tavanı; null = tavan yok. Bilinmeyen kademe en güvenli tarafa, bedavaya (0) düşer. */
    internal fun ceiling(budgetKey: String): Int? {
        val tier = tier(budgetKey) ?: return 0
        return amountMax(tier)
    }

    /** Bütçe bandının alt sınırı; bilinmeyen kademede 0. */
    internal fun bandLow(budgetKey: String): Int = tier(budgetKey)?.let { amountMin(it) } ?: 0

    private companion object {
        fun roundToTen(amount: BigDecimal): Int = amount.divide(BigDecimal.TEN)
            .setScale(0, RoundingMode.HALF_EVEN)
            .multiply(BigDecimal.TEN)
            .intValueExact()

        /** Binlik ayırıcı nokta: 1500 → "1.500" (web `f"{n:,}".replace(",", ".")`). */
        fun grouped(value: Int): String {
            val digits = value.toLong().absoluteValue.toString()
            val body = digits.reversed().chunked(3).joinToString(".").reversed()
            return if (value < 0) "-$body" else body
        }
    }
}
