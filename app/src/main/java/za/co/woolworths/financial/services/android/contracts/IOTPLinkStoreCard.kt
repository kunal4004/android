package za.co.woolworths.financial.services.android.contracts

import za.co.woolworths.financial.services.android.models.dto.Response
import za.co.woolworths.financial.services.android.models.dto.npc.OTPMethodType

interface IOTPLinkStoreCard<T> {
    fun onFailureHandler() {}
    fun onFailureHandler(response: Response?) {}
    fun onFailureHandler(error: Throwable?) {}
    fun showProgress() {}
    fun hideProgress() {}
    fun onSuccessHandler(response: T) {}
    fun requestOTPApi(otpMethodType: OTPMethodType) {}
}