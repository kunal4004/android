package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_credit_limit_increase

enum class CreditLimitIncreaseStates(private val status: String) {
    CONSENTS("Consents"),
    OFFER("Offer"),
    POI_REQUIRED("POI Required"),
    DECLINE("Decline"),
    CONTACT_US("Contact Us"),
    IN_PROGRESS("In Progress"),
    INCOME_AND_EXPENSE("I \u0026 E"),
    UNAVAILABLE("unavailable"),
    CLI_CONCLUDED("cli-concluded"),
    COMPLETE("complete");

    val type: String
        get() = status.lowercase()
}

class CreditLimitIncreaseTag {


}