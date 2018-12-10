package za.co.woolworths.financial.services.android.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.location.Location
import android.os.Looper
import android.provider.Settings
import com.google.android.gms.location.*
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import com.google.android.gms.location.LocationServices
import com.awfs.coordination.R

object FuseLocationAPISingleton {

    interface OnLocationChangeCompleteListener {
        fun onLocationChanged(location: Location)
    }

    @SuppressLint("StaticFieldLeak")
    private var mGetFusedLocationProviderClient: FusedLocationProviderClient? = null
    private lateinit var mOnLocationChangeCompletedListener: OnLocationChangeCompleteListener
    private lateinit var mLocationCallback: LocationCallback

    private var mLocationRequest: LocationRequest? = null
    private const val UPDATE_INTERVAL: Long = 1 * 1000  /* 1 sec */
    private const val FASTEST_INTERVAL: Long = 2 * 1000 /* 2 secs */
    private const val MAX_WAIT_TIME: Long = 3 * 1000 /* 3 secs */

    fun addOnLocationCompleteListener(onLocationChangeCompleteListener: OnLocationChangeCompleteListener) {
        this.mOnLocationChangeCompletedListener = onLocationChangeCompleteListener
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdate() {

        val woolworthInstance: WoolworthsApplication = WoolworthsApplication.getInstance() ?: return

        // Create the location request to start receiving updates
        mLocationRequest = LocationRequest()
        mLocationRequest!!.apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = UPDATE_INTERVAL
            fastestInterval = FASTEST_INTERVAL
            maxWaitTime = MAX_WAIT_TIME
        }

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
        mGetFusedLocationProviderClient?.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())
    }


    fun stopLocationUpdate() {
        mGetFusedLocationProviderClient?.removeLocationUpdates(mLocationCallback)
    }

    /**
    0 = LOCATION_MODE_OFF
    1 = LOCATION_MODE_SENSORS_ONLY
    2 = LOCATION_MODE_BATTERY_SAVING
    3 = LOCATION_MODE_HIGH_ACCURACY
     */

    fun getLocationMode(context: Context): Boolean {
        val locationMethod = Settings.Secure.getInt(context.contentResolver, Settings.Secure.LOCATION_MODE)
        return (locationMethod == 3)
    }

    fun detectDeviceOnlyGPSLocation(activity: Activity) {
        Utils.displayDialogActionSheet(activity, R.string.high_accuracy_location_err_desc, R.string.ok)
    }
}
