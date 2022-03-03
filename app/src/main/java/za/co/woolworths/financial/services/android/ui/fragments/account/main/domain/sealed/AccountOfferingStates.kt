package za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.sealed

sealed class AccountOfferingStates {
    object AccountInGoodStanding : AccountOfferingStates() //when productOfferingGoodStanding == true
    object AccountIsInArrears : AccountOfferingStates()//account is in arrears
    object AccountIsChargedOff : AccountOfferingStates() //account is in arrears for more than 6 months
    object MakeGetEligibilityCall : AccountOfferingStates()
    object ShowViewTreatmentPlanPopupFromConfigForChargedOff : AccountOfferingStates()
    object ShowViewTreatmentPlanPopupInArrearsFromConfig : AccountOfferingStates()
}