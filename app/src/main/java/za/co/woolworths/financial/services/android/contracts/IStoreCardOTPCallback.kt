package za.co.woolworths.financial.services.android.contracts

import za.co.woolworths.financial.services.android.models.dto.npc.OTPMethodType

interface IStoreCardOTPCallback<T> {
    fun loadStart() {}
    fun loadComplete() {}
    fun onSuccess(response: T) {}
    fun requestOTPApi(otpMethodType: OTPMethodType) {}
}