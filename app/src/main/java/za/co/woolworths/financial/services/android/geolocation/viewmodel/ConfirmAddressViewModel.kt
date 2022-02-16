package za.co.woolworths.financial.services.android.geolocation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import za.co.woolworths.financial.services.android.geolocation.network.apihelper.GeoLocationApiHelper

class ConfirmAddressViewModel( private val geoLocationApiHelper: GeoLocationApiHelper):ViewModel() {

     suspend fun getSavedAddress() =
         geoLocationApiHelper.getSavedAddress()

}