package za.co.woolworths.financial.services.android.ui.fragments.account

sealed class MyAccountSection {
    object AccountLanding : MyAccountSection()
    object StoreCardLanding : MyAccountSection()
    object CreditCardLanding : MyAccountSection()
    object PersonalLoanLanding : MyAccountSection()
}
