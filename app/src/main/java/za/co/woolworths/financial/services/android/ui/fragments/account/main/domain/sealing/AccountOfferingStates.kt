package za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.sealing

sealed class AccountOfferingState {
    object AccountInGoodStanding : AccountOfferingState() //when productOfferingGoodStanding == true
    object AccountIsInArrears : AccountOfferingState()//account is in arrears
    object AccountIsChargedOff : AccountOfferingState() //account is in arrears for more than 6 months
    object MakeGetEligibilityCall : AccountOfferingState()
    object ShowViewTreatmentPlanPopupFromConfigForChargedOff : AccountOfferingState()
    object ShowViewTreatmentPlanPopupInArrearsFromConfig : AccountOfferingState()
}
