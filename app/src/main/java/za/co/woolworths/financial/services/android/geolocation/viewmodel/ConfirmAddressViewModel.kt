package za.co.woolworths.financial.services.android.geolocation.viewmodel


import androidx.lifecycle.ViewModel
import za.co.woolworths.financial.services.android.geolocation.model.request.ConfirmLocationRequest
import za.co.woolworths.financial.services.android.geolocation.model.request.SaveAddressLocationRequest
import za.co.woolworths.financial.services.android.geolocation.network.apihelper.GeoLocationApiHelper

class ConfirmAddressViewModel(private val geoLocationApiHelper: GeoLocationApiHelper) :
    ViewModel() {

    suspend fun getSavedAddress() =
        geoLocationApiHelper.getSavedAddress()

    suspend fun getValidateLocation(placeId: String, latitude: Double?, longitude: Double?) =
        geoLocationApiHelper.getValidateLocation(placeId, latitude, longitude)

    suspend fun postConfirmAddress(confirmLocationRequest: ConfirmLocationRequest) =
        geoLocationApiHelper.postConfirmLocation(confirmLocationRequest)

    suspend fun postSaveAddress(saveAddressLocationRequest: SaveAddressLocationRequest) =
        geoLocationApiHelper.postSaveAddress(saveAddressLocationRequest)

}