package za.co.woolworths.financial.services.android.util

import android.annotation.SuppressLint
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.*
import za.co.woolworths.financial.services.android.models.WoolworthsApplication

object FuseLocationAPISingleton {

    interface OnLocationChangeCompleteListener {
        fun onLocationChanged(location: Location)
    }

    @SuppressLint("StaticFieldLeak")
    private lateinit var mGetFusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var mOnLocationChangeCompletedListener: OnLocationChangeCompleteListener
    private lateinit var mLocationCallback: LocationCallback

    private var mLocationRequest: LocationRequest? = null
    private val UPDATE_INTERVAL: Long = 1 * 1000  /* 1 sec */
    private val FASTEST_INTERVAL: Long = 2 * 1000 /* 2 secs */
    private val MAX_WAIT_TIME: Long = 3 * 1000 /* 3 secs */

    fun addOnLocationCompleteListener(onLocationChangeCompleteListener: OnLocationChangeCompleteListener) {
        this.mOnLocationChangeCompletedListener = onLocationChangeCompleteListener
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdate() {

        val woolworthInstance: WoolworthsApplication = WoolworthsApplication.getInstance() ?: return

        // Create the location request to start receiving updates
        mLocationRequest = LocationRequest()
        mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest!!.interval = UPDATE_INTERVAL
        mLocationRequest!!.fastestInterval = FASTEST_INTERVAL
        mLocationRequest!!.maxWaitTime = MAX_WAIT_TIME

        // Create LocationSettingsRequest object using location request
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(mLocationRequest!!)
        val locationSettingsRequest = builder.build()

        // Check whether location settings are satisfied
        val settingsClient = LocationServices.getSettingsClient(woolworthInstance)
        settingsClient.checkLocationSettings(locationSettingsRequest)

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        mGetFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(woolworthInstance)

        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    // Update UI with location data
                    mOnLocationChangeCompletedListener.onLocationChanged(location)
                }
            }
        }
        mGetFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())
    }


    fun stopLocationUpdate() {
        mGetFusedLocationProviderClient.removeLocationUpdates(mLocationCallback)
    }
}
