package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.enumtype

import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState

enum class AccountProductKeys(val value: String) {
    StoreCard("SC"),
    PersonalLoan("PL"),
    BlackCreditCard("CC"),
    GoldCreditCard("410374"),
    SilverCreditCard("400154"),
    ViewApplicationStatus("ViewApplicationStatus"),
    PetInsurance("Pet");

    companion object {
        private val map = AccountProductKeys.values().associateBy(AccountProductKeys::value)
        fun fromString(value: String) = map[value]
        fun items() = AccountProductKeys.values()
    }
}

enum class CreditCardType(val rawValue: String) {
    SILVER_CARD("400154"),
    GOLD_CARD("410374"),
    BLACK_CARD("410375");

    companion object {
        private val map = values().associateBy(CreditCardType::rawValue)
        fun fromString(value: String) = map[value]
        fun items() = CreditCardType.values()
        fun getApplyNowState(value: String) : ApplyNowState = when(fromString(value)){
               SILVER_CARD -> ApplyNowState.SILVER_CREDIT_CARD
               GOLD_CARD -> ApplyNowState.GOLD_CREDIT_CARD
               else ->  ApplyNowState.BLACK_CREDIT_CARD
        }
    }
}
