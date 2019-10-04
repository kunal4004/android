package za.co.woolworths.financial.services.android.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.IntentSender
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import za.co.woolworths.financial.services.android.contracts.ILocationProvider

object FuseLocationAPISingleton {

    /**
     * Constant used in the location settings dialog.
     */
    const val REQUEST_CHECK_SETTINGS = 1402

    private val TAG = FuseLocationAPISingleton.javaClass.simpleName
    /**
     * Provides access to the Fused Location Provider API.
     */
    @SuppressLint("StaticFieldLeak")
    private var mFusedLocationClient: FusedLocationProviderClient? = null

    /**
     * Provides access to the Location Settings API.
     */
    @SuppressLint("StaticFieldLeak")
    private var mSettingsClient: SettingsClient? = null

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    private var mLocationRequest: LocationRequest? = null

    /**
     * Stores the types of location services the client is interested in using. Used for checking
     * settings to determine if the device has optimal location settings.
     */
    private var mLocationSettingsRequest: LocationSettingsRequest? = null

    /**
     * Callback for Location events.
     */
    private var mLocationCallback: LocationCallback? = null

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private const val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 5000

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    private const val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2

    private lateinit var mLocationCompletedProvider: ILocationProvider

    fun addLocationChangeListener(ILocationProvider: ILocationProvider) {
        this.mLocationCompletedProvider = ILocationProvider
        val mWoolworthInstance: WoolworthsApplication = WoolworthsApplication.getInstance()
                ?: return

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mWoolworthInstance)
        mSettingsClient = LocationServices.getSettingsClient(mWoolworthInstance)

        // Kick off the process of building the LocationCallback, LocationRequest, and
        // LocationSettingsRequest objects.
        createLocationCallback()
        createLocationRequest()
        buildLocationSettingsRequest()
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdate(activity: Activity) {
        // Begin by checking if the device has the necessary location settings.
        mSettingsClient?.checkLocationSettings(mLocationSettingsRequest)
                ?.addOnSuccessListener(activity) {
                    mFusedLocationClient?.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())
                }
                ?.addOnFailureListener(activity) { e ->
                    when ((e as? ApiException)?.statusCode) {
                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                            Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " + "location settings ")
                            try {
                                mLocationCompletedProvider.onPopUpLocationDialogMethod()
                                // Show the dialog by calling startResolutionForResult(), and check the
                                // result in onActivityResult().
                                val rae = e as ResolvableApiException
                                rae.startResolutionForResult(activity, REQUEST_CHECK_SETTINGS)
                            } catch (sie: IntentSender.SendIntentException) {
                                Log.i(TAG, "PendingIntent unable to execute request.")
                            }

                        }
                        LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                            val errorMessage = "Location settings are inadequate, and cannot be " + "fixed here. Fix in Settings."
                            Log.e(TAG, errorMessage)
                            Toast.makeText(activity, errorMessage, Toast.LENGTH_LONG).show()
                        }
                    }
                }
    }

    /**
     * Creates a callback for receiving location events.
     */
    private fun createLocationCallback() {
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                locationResult?.lastLocation?.let { lastLocation -> mLocationCompletedProvider.onLocationChange(lastLocation) }
            }
        }
    }

    /**
     * Sets up the location request. Android has two location request settings:
     * `ACCESS_COARSE_LOCATION` and `ACCESS_FINE_LOCATION`. These settings control
     * the accuracy of the current location. WOneApp uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     *
     *
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     *
     *
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    private fun createLocationRequest() {
        mLocationRequest = LocationRequest()

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest?.interval = UPDATE_INTERVAL_IN_MILLISECONDS

        // Sets the fastest rate for active location updates. This interval is exact, and the
        // application will never receive updates faster than this value.
        mLocationRequest?.fastestInterval = FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS

        mLocationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    /**
     * Uses a [com.google.android.gms.location.LocationSettingsRequest.Builder] to build
     * a [com.google.android.gms.location.LocationSettingsRequest] that is used for checking
     * if a device has the needed location settings.
     */
    private fun buildLocationSettingsRequest() {
        val builder = LocationSettingsRequest.Builder()
        mLocationRequest?.let { loc -> builder.addLocationRequest(loc) }
        mLocationSettingsRequest = builder.build()
    }

    fun stopLocationUpdate() {
        mFusedLocationClient?.removeLocationUpdates(mLocationCallback)
    }
}
