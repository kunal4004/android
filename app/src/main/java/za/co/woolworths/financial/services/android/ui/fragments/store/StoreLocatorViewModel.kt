package za.co.woolworths.financial.services.android.ui.fragments.store

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import za.co.woolworths.financial.services.android.contracts.ILocationProvider
import za.co.woolworths.financial.services.android.contracts.RequestListener
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.LocationResponse
import za.co.woolworths.financial.services.android.models.dto.StoreDetails
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.util.FuseLocationAPISingleton
import za.co.woolworths.financial.services.android.util.Utils

class StoreLocatorViewModel : ViewModel() {

    private val locationResult = MutableLiveData<MutableList<StoreDetails>>()

    fun requestLocationUpdate() {
        FuseLocationAPISingleton.addLocationChangeListener(object : ILocationProvider {
            override fun onLocationChange(location: Location?) {
                FuseLocationAPISingleton.stopLocationUpdate()
                WoolworthsApplication.getAppContext()?.let { context -> Utils.saveLastLocation(location, context) }
                val requestLocationCall =  OneAppService.queryServiceGetStore(location?.latitude ?: 0.0, location?.longitude ?: 0.0, "")
                requestLocationCall.enqueue(CompletionHandler(object : RequestListener<LocationResponse> {
                    override fun onSuccess(locationResponse: LocationResponse?) {
                        locationResult.postValue(locationResponse?.Locations ?: mutableListOf())
                    }

                    override fun onFailure(error: Throwable?) {
                    }

                }, LocationResponse::class.java))
            }

            override fun onPopUpLocationDialogMethod() {
            }
        })

        FuseLocationAPISingleton.startLocationUpdate()
    }

    fun getStoreLocationResult(): LiveData<MutableList<StoreDetails>> = locationResult

}