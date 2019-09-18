package za.co.woolworths.financial.services.android.contracts

import za.co.woolworths.financial.services.android.models.dto.npc.LinkNewCardOTP
import za.co.woolworths.financial.services.android.models.dto.npc.OTPMethodType

interface IOTPLinkStoreCard<T> {
    fun onFailureHandler() {}
    fun startLoading() {}
    fun loadComplete() {}
    fun onSuccessHandler(response: T) {}
    fun requestOTPApi(otpMethodType: OTPMethodType) {}
    fun navigateToEnterOTPScreen(data: LinkNewCardOTP) {}
}