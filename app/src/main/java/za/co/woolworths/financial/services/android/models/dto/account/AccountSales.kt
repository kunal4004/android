package za.co.woolworths.financial.services.android.models.dto.account

import za.co.woolworths.financial.services.android.util.expand.ParentListItem

enum class ApplyNowState { STORE_CARD, GOLD_CREDIT_CARD, BLACK_CREDIT_CARD, PERSONAL_LOAN }

data class CardHeader(val title: String?, val description: String?, val drawables: List<Int>)

data class CardBenefit(val drawableId: Int, val title: String?, val description: String?)

data class MoreBenefit(val drawableId: Int, val name: String?, internal val description: String?) : ParentListItem {
    override fun getChildItemList(): MutableList<String?> = mutableListOf(description)
    override fun isInitiallyExpanded(): Boolean = false
}

data class CardQualifyCriteria(val title: String?)

data class CardCollection(val title: String?)

data class AccountSales(var cardHeader: CardHeader, var cardBenefit: MutableList<CardBenefit>, var moreBenefit: MutableList<MoreBenefit>, var cardQualifyCriteria: MutableList<CardQualifyCriteria>, var cardCollection: MutableList<CardCollection> = mutableListOf())