package za.co.woolworths.financial.services.android.geolocation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class LocationProviderBroadcastReceiver : BroadcastReceiver() {

    private var locationProviderInterface: LocationProviderInterface? = null

    interface LocationProviderInterface {
        fun onLocationProviderChange(context: Context?, intent: Intent?)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        locationProviderInterface?.onLocationProviderChange(context, intent)
    }

    fun registerCallback(locationProviderInterface: LocationProviderInterface) {
        this.locationProviderInterface = locationProviderInterface
    }
}