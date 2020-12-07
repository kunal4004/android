package za.co.woolworths.financial.services.android.models.dto.account

import za.co.woolworths.financial.services.android.util.expand.ParentListItem
import java.util.*

enum class ApplyNowState {STORE_CARD, GOLD_CREDIT_CARD, BLACK_CREDIT_CARD, PERSONAL_LOAN, SILVER_CREDIT_CARD }

enum class CreditCardType { GOLD_CREDIT_CARD, BLACK_CREDIT_CARD }

data class CardHeader(val title: String?, val description: String?, val drawables: List<Int>)

data class CardBenefit(val drawableId: Int, val title: String?, val description: String?,var cardBenefitTitle: String? = "")

data class MoreBenefit(val drawableId: Int, val name: String?, internal val description: String?) : ParentListItem {
    override fun getChildItemList(): MutableList<String?> = mutableListOf(description)
    override fun isInitiallyExpanded(): Boolean = false
}

data class CardQualifyCriteria(val title: String?)

data class CardCollection(val title: String?)

data class AccountHelpInformation(val title: String?, val description: String?)

data class PayMyCardHeaderItem(val title: Int, val description : Int, val card: Int)

data class AccountSales(var cardHeader: CardHeader, var cardBenefit: MutableList<CardBenefit>, var moreBenefit: MutableList<MoreBenefit>, var cardQualifyCriteria: MutableList<CardQualifyCriteria>, var cardCollection: MutableList<CardCollection> = mutableListOf())

enum class CreditCardActivationState(val value: String) {
    FAILED("FAILED"), AVAILABLE("ACTIVATE"), ACTIVATED("ACTIVATED"), UNAVAILABLE("ACTIVATE")
}

enum class AccountsProductGroupCode(val groupCode: String) {
    STORE_CARD("SC"), CREDIT_CARD("CC"), PERSONAL_LOAN("PL");

    fun equals(code: String): Boolean{
        return groupCode.equals(code, ignoreCase = true)
    }

    companion object{
        fun getEnum(code: String?): AccountsProductGroupCode? = values().find { it.groupCode == code?.toUpperCase(Locale.getDefault()) }
    }
}
