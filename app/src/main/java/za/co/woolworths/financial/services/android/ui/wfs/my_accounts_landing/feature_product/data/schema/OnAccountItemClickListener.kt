package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.schema

import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.models.dto.account.FicaModel
import za.co.woolworths.financial.services.android.models.dto.account.InsuranceProducts
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.CreditCardDeliveryStatusResponse

typealias ApplyNowStateMapToAccountBinNumber =  Pair<ApplyNowState, String>?

sealed interface OnAccountItemClickListener

sealed class AccountLandingInstantLauncher : OnAccountItemClickListener {
    data class ScheduleCreditCardDelivery(
        val response: CreditCardDeliveryStatusResponse,
        val applyNowStateToAccountBinNumber: ApplyNowStateMapToAccountBinNumber
    ) : AccountLandingInstantLauncher()

    data class FicaResultListener(val ficaModel: FicaModel): AccountLandingInstantLauncher()

    data class PetInsuranceNotCoveredAwarenessModel(val insuranceProducts: InsuranceProducts): AccountLandingInstantLauncher()

    object  BiometricIsRequired : AccountLandingInstantLauncher()
}

sealed interface OfferClickEvent : OnAccountItemClickListener {
    object StoreCardApplyNow : OfferClickEvent
    object CreditCardApplyNow : OfferClickEvent
    object PersonalLoanApplyNow : OfferClickEvent
    object PersonalLoanSignedOutApplyNow : OfferClickEvent
    object StoreCardInArrears : OfferClickEvent
    object CreditCardInArrears : OfferClickEvent
    object PersonalLoanInArrears : OfferClickEvent
    object ViewApplicationStatus : OfferClickEvent
    object ViewFreeCreditReport : OfferClickEvent
    object BlackCreditCardApplyNow : OfferClickEvent
    object PetInsurance : OfferClickEvent
    object WoolworthStoreCardApplyNow : OfferClickEvent
}

sealed class MyProfile : OnAccountItemClickListener {
    object Detail : MyProfile()
    object Order : MyProfile()
    object Message : MyProfile()
    object ShoppingList : MyProfile()
    object OrderAgain : MyProfile()
}
sealed class General : OnAccountItemClickListener {
    object StoreLocator : General()
    object NeedHelp : General()
    object ContactUs : General()
    object UpdatePassword : General()
    object Preferences : General()
    object SignOut : General()
}

sealed class ManageLoginRegister : OnAccountItemClickListener {
    object SignIn : ManageLoginRegister()
    object SignOut : ManageLoginRegister()
    object Register : ManageLoginRegister()
}

object RefreshAccountItem : OnAccountItemClickListener

