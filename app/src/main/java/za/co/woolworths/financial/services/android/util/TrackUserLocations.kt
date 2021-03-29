package za.co.woolworths.financial.services.android.util

import android.location.Location
import android.util.Log
import com.google.gson.Gson
import za.co.woolworths.financial.services.android.contracts.ILocationProvider
import za.co.woolworths.financial.services.android.models.WoolworthsApplication

class TrackUserLocations {

    fun oneTimeLocation(oneTimeLocation: (Location?) -> Unit) {
        FuseLocationAPISingleton.addLocationChangeListener(object : ILocationProvider {
            override fun onLocationChange(location: Location?) {
                Log.e("myLocationUpdate", Gson().toJson(location))
                WoolworthsApplication.getAppContext()?.let { context -> Utils.saveLastLocation(location, context) }
                FuseLocationAPISingleton.stopLocationUpdate()
                oneTimeLocation(location)
            }
        })
    }

    fun start() = FuseLocationAPISingleton.startLocationUpdate()

}