package za.co.woolworths.financial.services.android.ui.fragments.account.main.sealing

sealed class ProductLandingGroupCode {
    data class PersonalLoan(val name: String = AccountCardType.PERSONAL_LOAN.type) :
        ProductLandingGroupCode()

    data class StoreCard(val name: String = AccountCardType.STORE_CARD.type) :
        ProductLandingGroupCode()

    data class CreditCard(
        val name: String = AccountCardType.CREDIT_CARD.type,
        val type: CreditCardType = CreditCardType.GOLD_CARD
    ) : ProductLandingGroupCode()

    object BlackCreditCard : ProductLandingGroupCode()
    object SilverCard : ProductLandingGroupCode()
    object GoldCreditCard : ProductLandingGroupCode()
    object UnsupportedProductGroupCode : ProductLandingGroupCode()
}


enum class AccountCardType(val type: String) {
    STORE_CARD("SC"),
    PERSONAL_LOAN("PL"),
    CREDIT_CARD("CC")
}

enum class CreditCardType(val card: String) {
    BLACK_CARD("410375"),
    SILVER_CARD("400154"),
    GOLD_CARD("410374")
}
