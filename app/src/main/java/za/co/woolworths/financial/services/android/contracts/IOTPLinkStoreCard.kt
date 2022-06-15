package za.co.woolworths.financial.services.android.contracts

import za.co.woolworths.financial.services.android.models.dto.account.ServerErrorResponse
import  za.co.woolworths.financial.services.android.ui.fragments.integration.service.model.Response
import za.co.woolworths.financial.services.android.models.dto.npc.OTPMethodType

interface IOTPLinkStoreCard<T> {
    fun onFailureHandler() {}
    fun onFailureHandler(response: ServerErrorResponse?) {}
    fun onFailureHandler(error: Throwable?) {}
    fun showProgress() {}
    fun hideProgress() {}
    fun onSuccessHandler(response: T) {}
    fun requestOTPApi(otpMethodType: OTPMethodType) {}
}