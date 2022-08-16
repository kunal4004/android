package za.co.woolworths.financial.services.android.ui.fragments.account.applynow.activities

import android.app.Activity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.models.dto.account.applynow.ApplyNowModel
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
    var applyNowState = ApplyNowState.BLACK_CREDIT_CARD

    fun contentID():String {
        return when(applyNowState){
            //TODO:future usage for SC&PL
            ApplyNowState.STORE_CARD -> ""
            ApplyNowState.PERSONAL_LOAN -> ""
            ApplyNowState.GOLD_CREDIT_CARD, ApplyNowState.BLACK_CREDIT_CARD, ApplyNowState.SILVER_CREDIT_CARD -> "creditCardcApplyNow"
        }
    }

    fun onApplyNowButtonTapped(): String? {
        val applyNowLinks = AppConfigSingleton.applyNowLink
        return when (applyNowState) {
            ApplyNowState.STORE_CARD -> applyNowLinks?.storeCard
            ApplyNowState.GOLD_CREDIT_CARD, ApplyNowState.BLACK_CREDIT_CARD, ApplyNowState.SILVER_CREDIT_CARD -> applyNowLinks?.creditCard
            ApplyNowState.PERSONAL_LOAN -> applyNowLinks?.personalLoan
            else -> throw RuntimeException("OnApplyNowButtonTapped:: Invalid ApplyNowState ## : ${applyNowState}")
        }
    }

    fun viewApplicationStatusLinkInExternalBrowser(): String? {
        var applyNowLink = AppConfigSingleton.applyNowLink
        return when(applyNowState) {
            ApplyNowState.STORE_CARD -> applyNowLink?.storeCard
            ApplyNowState.PERSONAL_LOAN -> applyNowLink?.personalLoan
            ApplyNowState.BLACK_CREDIT_CARD, ApplyNowState.GOLD_CREDIT_CARD, ApplyNowState.SILVER_CREDIT_CARD  -> applyNowLink?.creditCard
            else ->applyNowLink?.applicationStatus
        }
    }

}
