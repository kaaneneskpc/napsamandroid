package com.kaaneneskpc.napsam.core.domain.text

import com.kaaneneskpc.napsam.core.domain.EngineConstants
import com.kaaneneskpc.napsam.core.domain.budget.BudgetCalculator
import com.kaaneneskpc.napsam.core.model.BudgetTier
import com.kaaneneskpc.napsam.core.model.CompanionType
import com.kaaneneskpc.napsam.core.model.Energy
import com.kaaneneskpc.napsam.core.model.PlacePreference
import com.kaaneneskpc.napsam.core.model.SuggestionContext
import java.math.BigInteger

/** Serbest metinden GERÇEKTEN çıkarılabilen alanlar; çıkarılamayanlar null veya boş kalır. */
public data class ParsedText(
    public val budgetKey: String? = null,
    public val durationMax: Int? = null,
    public val companions: CompanionType? = null,
    public val energy: Energy? = null,
    public val placePreference: PlacePreference? = null,
    public val keywords: List<String> = emptyList(),
)

/**
 * "Yaz" modu ayrıştırıcısı (spesifikasyon C6, web `core/textmode.py`). AI değildir, internetsiz çalışır.
 *
 * İfade listeleri ve etiket sözlüğü web kaynağından BİREBİR kopyalanmıştır; sıraları anlamlıdır (ilk
 * eşleşen kazanır). Alt dize eşleşmesinin bilinen yanlış pozitifleri ("açık hava" → yemek) bilinçli
 * olarak korunur (karar K14). `contract/parity/textmode.json` ile test edilir.
 *
 * Python'la aynı sonuç için boşluk ve rakam sınıfları Unicode'dur (`(?U)`), küçük harfe çevirme
 * dilden bağımsızdır ("İ" → "i̇", web `str.lower()` gibi).
 */
public object TextParser {
    private const val PLENTY_KEY = "bol"
    private const val HALF_HOUR = 30
    private const val WHOLE_DAY = 480
    private val MINUTES_PER_HOUR = BigInteger.valueOf(60)
    private val INT_MAX = BigInteger.valueOf(Int.MAX_VALUE.toLong())

    private val FREE_PHRASES = listOf(
        "param yok", "parasız", "parasiz", "bedava", "ücretsiz", "ucretsiz",
        "beş kuruşum yok", "bes kurusum yok", "bütçem yok", "butcem yok",
        "meteliksiz", "cebim delik", "sıfır bütçe", "harcamak istemiyorum",
    )
    private val PLENTY_PHRASES = listOf("param var", "bütçe sorun değil", "butce sorun degil", "fark etmez param")

    private val COMPANION_PHRASES: List<Pair<CompanionType, List<String>>> = listOf(
        CompanionType.COUPLE to listOf("sevgilim", "eşim", "esim", "nişanlım", "nisanlim", "partnerim", "ikimiz"),
        CompanionType.FRIENDS to listOf("arkadaş", "arkadas", "dostum", "kankam", "grupça", "grupca"),
        CompanionType.FAMILY to listOf("ailem", "ailece", "annem", "babam", "kardeşim", "kardesim", "akraba"),
        CompanionType.KIDS to listOf("çocuk", "cocuk", "çocuğum", "cocugum", "yeğen", "yegen"),
        CompanionType.SOLO to listOf(
            "tek başıma",
            "tek basima",
            "yalnızım",
            "yalnizim",
            "kendi başıma",
            "kendi basima",
        ),
    )

    private val LOW_ENERGY = listOf(
        "yorgun", "enerjim yok", "halim yok", "bitkin", "uykusuz", "yoruldum",
        "üşengeç", "usengec", "canım hiçbir şey istemiyor", "canim hicbir sey istemiyor",
    )
    private val HIGH_ENERGY = listOf("enerjim var", "hareket etmek", "kıpırdamak", "kipirdamak", "koşmak", "kosmak")

    private val HOME_PHRASES = listOf("evde", "evden çıkmak istemiyorum", "evden cikmak istemiyorum", "dışarı çıkamam")
    private val OUTSIDE_PHRASES = listOf(
        "dışarı",
        "disari",
        "dışarıda",
        "disarida",
        "sokağa",
        "sokaga",
        "çıkmak istiyorum",
    )

    /** Kullanıcının yazdığı kelime → içerikteki etiket. */
    private val TAG_DICTIONARY: List<Pair<String, String>> = listOf(
        "yürü" to "yürümek", "yuru" to "yürümek", "yürüyüş" to "yürümek", "yuruyus" to "yürümek",
        "sakin" to "sakin", "sessiz" to "sakin", "huzur" to "sakin", "dingin" to "sakin",
        "deniz" to "dışarı", "su" to "dışarı", "doğa" to "dışarı", "doga" to "dışarı",
        "kahve" to "kahve", "çay" to "kahve", "cay" to "kahve",
        "yemek" to "yemek", "aç" to "yemek", "yemek yapmak" to "yemek", "pişir" to "yemek",
        "öğren" to "öğrenmek", "ogren" to "öğrenmek", "kurs" to "öğrenmek", "merak" to "öğrenmek",
        "fotoğraf" to "fotoğraf", "fotograf" to "fotoğraf", "çek" to "fotoğraf",
        "kalabalık istemiyorum" to "kalabalık olmasın", "tenha" to "kalabalık olmasın",
        "el işi" to "ellerimi kullanayım", "elimle" to "ellerimi kullanayım",
        "üret" to "ellerimi kullanayım", "uret" to "ellerimi kullanayım",
        "yeni" to "yeni bir şey", "farklı" to "yeni bir şey", "farkli" to "yeni bir şey",
        "kısa" to "kısa sürsün", "kisa" to "kısa sürsün", "çabuk" to "kısa sürsün",
        "gece" to "gece", "akşam" to "gece", "aksam" to "gece",
        "spor" to "hareket", "hareket" to "hareket", "egzersiz" to "hareket",
        "ucuz" to "ucuz", "bedava" to "ucuz",
    )

    private val WHITESPACE = Regex("(?U)\\s+")
    private val HOURS = Regex("(?U)(\\d+)\\s*saat")
    private val MINUTES = Regex("(?U)(\\d+)\\s*(dakika|dk)")
    private val AMOUNT = Regex("(?U)(\\d+)\\s*(tl|lira|₺)")

    public fun parse(text: String, budget: BudgetCalculator): ParsedText {
        val normalized = WHITESPACE.replace(text.trim().lowercase(), " ")
        if (normalized.isEmpty()) return ParsedText()
        return ParsedText(
            budgetKey = parseBudget(normalized, budget),
            durationMax = parseDuration(normalized),
            companions = COMPANION_PHRASES.firstOrNull { (_, phrases) -> phrases.any { it in normalized } }?.first,
            energy = when {
                LOW_ENERGY.any { it in normalized } -> Energy.LOW
                HIGH_ENERGY.any { it in normalized } -> Energy.HIGH
                else -> null
            },
            placePreference = when {
                HOME_PHRASES.any { it in normalized } -> PlacePreference.HOME
                OUTSIDE_PHRASES.any { it in normalized } -> PlacePreference.OUTSIDE
                else -> null
            },
            keywords = TAG_DICTIONARY.filter { (phrase, _) -> phrase in normalized }.map { it.second }.distinct(),
        )
    }

    /**
     * Metni bağlama işler (web `build_context`): metin [EngineConstants.MAX_TEXT_LEN] karakterde kesilir.
     * - Kelimeler: seçilenler + metinden çıkanlar, sıra korunur, tekrarsız.
     * - Bütçe: metinde bütçe ifadesi varsa seçili kademeyi EZER.
     * - Süre, kiminle, enerji, ev/dışarı: yalnızca kullanıcı seçmediyse metinden gelir.
     */
    public fun applyTo(context: SuggestionContext, text: String, budget: BudgetCalculator): SuggestionContext {
        val raw = text.takeCodePoints(EngineConstants.MAX_TEXT_LEN).trim()
        if (raw.isEmpty()) return context
        val parsed = parse(raw, budget)
        return context.copy(
            budgetKey = parsed.budgetKey ?: context.budgetKey,
            keywords = if (parsed.keywords.isEmpty()) {
                context.keywords
            } else {
                (context.keywords + parsed.keywords).distinct()
            },
            durationMax = context.durationMax ?: parsed.durationMax,
            companions = context.companions ?: parsed.companions,
            energy = context.energy ?: parsed.energy,
            placePreference = context.placePreference ?: parsed.placePreference,
        )
    }

    private fun parseBudget(text: String, budget: BudgetCalculator): String? {
        if (FREE_PHRASES.any { it in text }) return BudgetTier.DEFAULT_KEY
        if (PLENTY_PHRASES.any { it in text }) return PLENTY_KEY
        val amount = AMOUNT.find(text)?.groupValues?.get(1)?.let { BigInteger(it) } ?: return null
        return budget.tiers.firstOrNull { tier ->
            val top = budget.amountMax(tier)
            top == null || amount <= BigInteger.valueOf(top.toLong())
        }?.key
    }

    private fun parseDuration(text: String): Int? {
        if ("yarım saat" in text || "yarim saat" in text) return HALF_HOUR
        if ("bütün gün" in text || "butun gun" in text || "tüm gün" in text) return WHOLE_DAY
        HOURS.find(text)?.let { return saturated(BigInteger(it.groupValues[1]).multiply(MINUTES_PER_HOUR)) }
        MINUTES.find(text)?.let { return saturated(BigInteger(it.groupValues[1])) }
        return null
    }

    /** Python tamsayısı sınırsızdır; motor yalnızca "süre ≤ sınır" karşılaştırdığı için tavana sabitlemek anlamı korur. */
    private fun saturated(value: BigInteger): Int = value.min(INT_MAX).toInt()

    /** Python dilimlemesi gibi kod noktasıyla keser; emoji ortadan bölünmez. */
    private fun String.takeCodePoints(count: Int): String =
        if (codePointCount(0, length) <= count) this else substring(0, offsetByCodePoints(0, count))
}
