package za.co.woolworths.financial.services.android.ui.fragments.account.applynow.activities

import android.app.Activity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dto.account.AccountSales
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.models.dto.account.applynow.ApplyNowModel
import za.co.woolworths.financial.services.android.models.dto.account.applynow.ApplyNowSectionReference
import za.co.woolworths.financial.services.android.ui.activities.account.apply_now.AccountSalesModelImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.MyAccountSection
import za.co.woolworths.financial.services.android.ui.fragments.account.applynow.ApplyNowBottomSheetImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.applynow.IApplyNowBottomSheetImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.applynow.data.ApplyNowRepo
import za.co.woolworths.financial.services.android.ui.fragments.account.applynow.data.IApplyNowRepo
import za.co.woolworths.financial.services.android.ui.fragments.account.applynow.utils.getViewStateFlowForNetworkCall
import za.co.woolworths.financial.services.android.util.KotlinUtils
import javax.inject.Inject

@HiltViewModel
class ApplyNowViewModel @Inject constructor(val bottomSheet: ApplyNowBottomSheetImpl,repo: ApplyNowRepo) : ViewModel(),
    IApplyNowBottomSheetImpl by bottomSheet ,IApplyNowRepo by repo{

    suspend fun applyNowResponse(contentId:String) = getViewStateFlowForNetworkCall { queryServiceApplyNow(contentId) }

    var applyNowResponse: MutableStateFlow<ApplyNowModel?> = MutableStateFlow(null)
    var applyNowState = ApplyNowState.GOLD_CREDIT_CARD


    fun contentID():String {
        return when(applyNowState){
            //TODO:future usage for SC&PL
            ApplyNowState.STORE_CARD -> ""
            ApplyNowState.PERSONAL_LOAN -> ""
            else -> "creditCardcApplyNow"
        }
    }

    fun getApplyNowResourcesData(): AccountSales {
        MutableLiveData(AccountSalesModelImpl()).value!!.apply {
            return when(applyNowState){
                ApplyNowState.PERSONAL_LOAN -> getPersonalLoan()
                ApplyNowState.GOLD_CREDIT_CARD-> getCreditCard()[0]
                ApplyNowState.BLACK_CREDIT_CARD-> getCreditCard()[1]
                else->getStoreCard()
            }
        }
    }
    fun onApplyNowButtonTapped(): String? {
        val applyNowLinks = AppConfigSingleton.applyNowLink
        return when (applyNowState) {
            ApplyNowState.STORE_CARD -> applyNowLinks?.storeCard
            ApplyNowState.PERSONAL_LOAN -> applyNowLinks?.personalLoan
            else -> applyNowLinks?.creditCard
        }
    }

    fun viewApplicationStatusLinkInExternalBrowser(): String? {
        val applyNowLink = AppConfigSingleton.applyNowLink
        return when(applyNowState) {
            ApplyNowState.STORE_CARD -> applyNowLink?.storeCard
            ApplyNowState.PERSONAL_LOAN -> applyNowLink?.personalLoan
            else  -> applyNowLink?.creditCard
        }
    }

    fun setApplyNowStateForCC(reference: ApplyNowSectionReference){
        applyNowState = when(reference){
            ApplyNowSectionReference.CREDIT_CARD_GOLD->{
                ApplyNowState.GOLD_CREDIT_CARD
            }
            ApplyNowSectionReference.CREDIT_CARD_BLACK->{
                ApplyNowState.BLACK_CREDIT_CARD
            }
        }
    }
}
