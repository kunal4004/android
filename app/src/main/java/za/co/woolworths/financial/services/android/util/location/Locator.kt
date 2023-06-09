package za.co.woolworths.financial.services.android.util.location

import androidx.appcompat.app.AppCompatActivity
import za.co.woolworths.financial.services.android.util.Utils
import java.lang.Exception

sealed class Event {
    data class Location(val locationData: android.location.Location?) : Event()
    data class Permission(val event: EventType, val exception: Exception? = null) : Event()
}

enum class EventType {
    LOCATION_PERMISSION_GRANTED,
    LOCATION_PERMISSION_NOT_GRANTED,
    LOCATION_DISABLED_ON_DEVICE,
    LOCATION_SERVICE_DISCONNECTED
}

class Locator(val activity: AppCompatActivity) {

    private lateinit var locationProvider: LocationProvider

    fun getCurrentLocation(rationale: LocationPermissionRationaleMessage? = null,
                           eventCallback: (Event) -> Unit
    ) {
        initLocationProvider(activity, eventCallback, rationale)
        locationProvider.startLocationDiscoveryOrStartPermissionResolution()
    }

    fun getCurrentLocationSilently(eventCallback: (Event) -> Unit) {
        initLocationProvider(activity, eventCallback, null)
        locationProvider.startSilentLocationDiscovery()
    }

    private fun initLocationProvider(activity: AppCompatActivity,
                                     eventCallback: (Event) -> Unit,
                                     locationPermissionRationaleMessage: LocationPermissionRationaleMessage?
    ) {
        if (!::locationProvider.isInitialized) {
            val locationProviderType = if (Utils.isGooglePlayServicesAvailable()) LocationDelegate.GOOGLE else LocationDelegate.HUAWEI
            locationProvider = LocationProvider(activity, eventCallback, locationProviderType, locationPermissionRationaleMessage)
        }
    }

    fun stopService() {
        if (::locationProvider.isInitialized) {
            locationProvider.stopService()
        }
    }
}


