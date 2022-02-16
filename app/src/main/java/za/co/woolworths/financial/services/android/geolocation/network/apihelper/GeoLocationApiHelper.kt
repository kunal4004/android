package za.co.woolworths.financial.services.android.geolocation.network.apihelper

import retrofit2.await
import za.co.woolworths.financial.services.android.models.network.RetrofitConfig

class GeoLocationApiHelper(): RetrofitConfig() {

    suspend fun getSavedAddress()=
        mApiInterface.getSavedAddresses("","",getSessionToken(), getDeviceIdentityToken()).await()

}