package za.co.woolworths.financial.services.android.models.network

import retrofit2.Call
import za.co.woolworths.financial.services.android.models.dto.*

object MockOneAppService : MockRetrofitConfig() {

    var forceNetworkUpdate: Boolean = false

    fun login(loginRequest: LoginRequest): Call<LoginResponse> {
        return mApiInterface.login(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), loginRequest)
    }

    fun getAccounts(): Call<AccountsResponse> {
        return mApiInterface.getAccounts(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken())
    }

    fun getAccountsByProductOfferingId(productOfferingId: String): Call<AccountsResponse> {
        return mApiInterface.getAccountsByProductOfferingId(getApiId(), getSha1Password(), getDeviceManufacturer(), getDeviceModel(), getNetworkCarrier(), getOS(), getOsVersion(), "", "", getSessionToken(), productOfferingId)
    }

}