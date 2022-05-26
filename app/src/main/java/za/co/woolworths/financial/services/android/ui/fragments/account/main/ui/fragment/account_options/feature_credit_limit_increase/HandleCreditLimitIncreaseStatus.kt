package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_credit_limit_increase

import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.models.dto.OfferActive
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.ui.extension.bindString
import javax.inject.Inject

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

    val value: String
        get() = status.lowercase()
}

interface IHandleCreditLimitIncreaseStatus {
    fun getStatus(offerActive: OfferActive?): CLILandingUIState
    fun isOfferDisabled(offerActive: OfferActive?): Boolean
}

sealed class CLILandingUIState {
    data class Consent(var offerActive: OfferActive?) : CLILandingUIState()
    data class CommonStatus(var offerActive: OfferActive?) : CLILandingUIState()
    data class Unavailable(var offerActive: OfferActive?) : CLILandingUIState()
}

data class CreditLimitIncreaseLanding(
    val productOfferingId: Int,
    val offerActive: OfferActive?,
    val applyNowState: ApplyNowState
)

class HandleCreditLimitIncreaseStatus @Inject constructor() : IHandleCreditLimitIncreaseStatus {
    override fun getStatus(offerActive: OfferActive?): CLILandingUIState {
        return when (offerActive?.nextStep?.lowercase()) {
            CreditLimitIncreaseStates.CONSENTS.value -> CLILandingUIState.Consent(offerActive)
            CreditLimitIncreaseStates.COMPLETE.value,
            CreditLimitIncreaseStates.IN_PROGRESS.value,
            CreditLimitIncreaseStates.CONTACT_US.value,
            CreditLimitIncreaseStates.DECLINE.value,
            CreditLimitIncreaseStates.POI_REQUIRED.value,
            CreditLimitIncreaseStates.INCOME_AND_EXPENSE.value,
            CreditLimitIncreaseStates.OFFER.value -> CLILandingUIState.CommonStatus(offerActive)
            else -> CLILandingUIState.Unavailable(offerActive?.apply {
                messageSummary = bindString(R.string.status_unavailable)
            })
        }
    }

    override fun isOfferDisabled(offerActive: OfferActive?): Boolean {
        val nextStep = offerActive?.nextStep
        val cliStatus = offerActive?.cliStatus
        return nextStep.isNullOrEmpty() ||
                nextStep == CreditLimitIncreaseStates.IN_PROGRESS.value ||
                nextStep == CreditLimitIncreaseStates.DECLINE.value ||
                nextStep == CreditLimitIncreaseStates.CONTACT_US.value ||
                nextStep == CreditLimitIncreaseStates.UNAVAILABLE.value ||
                (nextStep == CreditLimitIncreaseStates.COMPLETE.value
                        && cliStatus != CreditLimitIncreaseStates.CLI_CONCLUDED.value)
    }

}