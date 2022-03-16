package za.co.woolworths.financial.services.android.geolocation.network.apihelper

import retrofit2.await
import za.co.woolworths.financial.services.android.geolocation.model.request.ConfirmLocationRequest
import za.co.woolworths.financial.services.android.geolocation.model.request.SaveAddressLocationRequest
import za.co.woolworths.financial.services.android.models.network.RetrofitConfig

class GeoLocationApiHelper : RetrofitConfig() {

    suspend fun getSavedAddress() =
        mApiInterface.getSavedAddresses("", "", getSessionToken(), getDeviceIdentityToken()).await()

    suspend fun getValidateLocation(placeId: String) =
        mApiInterface.validateLocation("",
            "",
            getSessionToken(),
            getDeviceIdentityToken(),
            "SIT2",
            placeId).await()

    suspend fun postConfirmLocation(confirmLocationRequest: ConfirmLocationRequest) =
        mApiInterface.confirmLocation("",
            "",
            getSessionToken(),
            getDeviceIdentityToken(),
            confirmLocationRequest
            ).await()

    suspend fun postSaveAddress(saveAddressLocationRequest: SaveAddressLocationRequest) =
        mApiInterface.saveLocation("",
            "",
            getSessionToken(),
            getDeviceIdentityToken(),
            saveAddressLocationRequest
        ).await()

    fun initConfirmLocation(confirmLocationRequest: ConfirmLocationRequest) =
        mApiInterface.confirmLocation("",
            "",
            getSessionToken(),
            getDeviceIdentityToken(),
            confirmLocationRequest
        )
}