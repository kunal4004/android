package za.co.woolworths.financial.services.android.geolocation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import za.co.woolworths.financial.services.android.geolocation.network.apihelper.GeoLocationApiHelper
import za.co.woolworths.financial.services.android.geolocation.network.model.Store

class ConfirmAddressViewModel(private val geoLocationApiHelper: GeoLocationApiHelper) :
    ViewModel() {

    private var mStore: Store? = null

    private var _storeDetails = MutableLiveData<Store?>()

    val storeDetailsData: LiveData<Store?> get() = _storeDetails

    suspend fun getSavedAddress() =
        geoLocationApiHelper.getSavedAddress()

    suspend fun getValidateLocation(placeId: String, latitude: Double?, longitude: Double?) =
        geoLocationApiHelper.getValidateLocation(placeId, latitude, longitude)

    fun setStoreDetails( store: Store?) {
        _storeDetails.value = store
    }

    fun getStoreDetails() =  mStore
}