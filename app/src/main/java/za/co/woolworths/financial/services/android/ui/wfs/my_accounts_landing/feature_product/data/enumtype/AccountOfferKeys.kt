package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.enumtype

enum class AccountOfferKeys(val value: String) {
    PetInsurance("Pet"),
    StoreCardApplyNow("StoreCardApplyNow"),
    CreditCardApplyNow("CreditCardApplyNow"),
    PersonalLoanApplyNow("PersonalLoanApplyNow"),
    ViewApplicationStatus("ViewApplicationStatus"),
    CreditReport("CreditReport");
    companion object {
        fun items() = AccountOfferKeys.values()
    }
}

sealed interface MyAccountAuthenticationState
object Authenticated : MyAccountAuthenticationState
object NotAuthenticated : MyAccountAuthenticationState