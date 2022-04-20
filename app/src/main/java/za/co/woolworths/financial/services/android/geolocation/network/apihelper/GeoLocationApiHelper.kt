package za.co.woolworths.financial.services.android.geolocation.network.apihelper

import android.content.Context
import retrofit2.await
import za.co.woolworths.financial.services.android.geolocation.model.request.ConfirmLocationRequest
import za.co.woolworths.financial.services.android.geolocation.model.request.SaveAddressLocationRequest
import za.co.woolworths.financial.services.android.models.network.RetrofitConfig
import za.co.woolworths.financial.services.android.util.NetworkManager

class GeoLocationApiHelper : RetrofitConfig() {

    suspend fun getSavedAddress() =
        mApiInterface.getSavedAddresses("", "", getSessionToken(), getDeviceIdentityToken()).await()

    suspend fun getValidateLocation(placeId: String) =
        mApiInterface.geoValidateLocation("",
            getEnvironment(),
            "",
            getSessionToken(),
            getDeviceIdentityToken(),
            placeId).await()

    suspend fun postConfirmLocation(confirmLocationRequest: ConfirmLocationRequest) =
        mApiInterface.confirmLocation("",
            "",
            getEnvironment(),
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
            getEnvironment(),
            getSessionToken(),
            getDeviceIdentityToken(),
            confirmLocationRequest
        )

    fun validateLocation(placeId: String) =
        mApiInterface.validateLocation("",
            getEnvironment(),
            "",
            getSessionToken(),
            getDeviceIdentityToken(),
            placeId)

    fun isConnectedToInternet(context: Context) =
        NetworkManager.getInstance().isConnectedToNetwork(context)
}