package com.kaaneneskpc.napsam.core.domain.budget

import com.kaaneneskpc.napsam.core.domain.TestContract
import com.kaaneneskpc.napsam.core.domain.string
import com.kaaneneskpc.napsam.core.model.ContractFixtures
import java.math.BigDecimal
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BudgetParityTest {
    @Test
    fun `kademe tutarlari bes ucret degerinde web ile ayni`() {
        val cases = TestContract.cases("budget.json")
        assertEquals(5, cases.size)
        cases.forEach { case ->
            val wage = case.string("wage")
            val tiers = case.getValue("tiers").jsonArray.map { it.jsonObject }
            val calculator = BudgetCalculator(tiers.map(ContractFixtures::budgetTier), BigDecimal(wage))
            tiers.forEach { assertTier("ücret $wage", calculator, it) }
        }
    }

    @Test
    fun `gomulu varsayilan kademeler meta dosyasindaki tutarlari veriyor`() {
        val meta = ContractFixtures.json("content/meta.json").jsonObject
        meta.getValue("budgetTiers").jsonArray.forEach { assertTier("meta", TestContract.budget, it.jsonObject) }
    }

    @Test
    fun `kademeler order sirasiyla`() {
        val calculator = BudgetCalculator(ContractFixtures.budgetTiers.reversed(), ContractFixtures.wage)
        assertEquals(listOf("bedava", "az", "orta", "iyi", "bol"), calculator.tiers.map { it.key })
    }

    @Test
    fun `bilinmeyen kademe bedava tavanina duser, bol kademede tavan yok`() {
        val budget = TestContract.budget
        assertEquals(0, budget.ceiling("uydurma-kademe"))
        assertEquals(0, budget.bandLow("uydurma-kademe"))
        assertNull(budget.ceiling("bol"))
        assertEquals(150, budget.bandLow("orta"))
        assertEquals(500, budget.ceiling("orta"))
    }

    private fun assertTier(label: String, calculator: BudgetCalculator, json: JsonObject) {
        val tier = requireNotNull(calculator.tier(json.string("key")))
        val message = "$label / ${tier.key}"
        assertEquals(message, json.getValue("amountMin").jsonPrimitive.int, calculator.amountMin(tier))
        assertEquals(message, json.getValue("amountMax").jsonPrimitive.intOrNull, calculator.amountMax(tier))
        assertEquals(message, json.string("amountText"), calculator.amountText(tier))
    }
}
