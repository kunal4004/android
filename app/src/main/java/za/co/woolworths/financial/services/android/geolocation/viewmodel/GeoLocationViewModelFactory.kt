package za.co.woolworths.financial.services.android.geolocation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import za.co.woolworths.financial.services.android.geolocation.network.apihelper.GeoLocationApiHelper

class GeoLocationViewModelFactory(
    private val geoLocationApiHelper: GeoLocationApiHelper)
    : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConfirmAddressViewModel::class.java)) {
            return ConfirmAddressViewModel(geoLocationApiHelper) as T
        }
        throw IllegalArgumentException("Unknown class name")
    }
}