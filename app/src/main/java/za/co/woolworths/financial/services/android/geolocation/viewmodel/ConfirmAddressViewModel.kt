package za.co.woolworths.financial.services.android.geolocation.viewmodel

import androidx.lifecycle.ViewModel
import za.co.woolworths.financial.services.android.geolocation.network.apihelper.GeoLocationApiHelper

class ConfirmAddressViewModel(private val geoLocationApiHelper: GeoLocationApiHelper) :
    ViewModel() {

    suspend fun getSavedAddress() =
        geoLocationApiHelper.getSavedAddress()

    suspend fun getValidateLocation(placeId: String, latitude: Double?, longitude: Double?) =
        geoLocationApiHelper.getValidateLocation(placeId, latitude, longitude)

}