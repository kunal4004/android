package za.co.woolworths.financial.services.android.dash.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import com.google.android.gms.location.*
import za.co.woolworths.financial.services.android.util.Utils

class DashCollectionStoreFragment : Fragment(R.layout.layout_dash_collection_store) {

    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var isLocationModalShown: Boolean = false
    private var location: Location? = null
    private var locationRequest: LocationRequest? = createLocationRequest()

    companion object {
        const val REQUEST_CODE_FINE_GPS = 4771
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.apply {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        }

        var isPermissionGranted = false
        activity?.apply {
            isPermissionGranted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        }

        if (isPermissionGranted && Utils.isLocationEnabled(context)) {
            fusedLocationClient?.lastLocation?.addOnSuccessListener {
                this@DashCollectionStoreFragment.location = it

            }
        } else {
            // when permission granted and location is not enabled
            if (isPermissionGranted) {

            }
            //When Location permission not granted.
            else if (!checkLocationPermission() && !isLocationModalShown) {

            }
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            activity?.apply {
                startLocationUpdates()
            }
        }
    }

    @SuppressLint("NewApi")
    private fun checkLocationPermission(): Boolean {
        activity?.apply {
            val perms = arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            return if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            ) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    //Asking only once.
                    ActivityCompat.requestPermissions(this, perms, REQUEST_CODE_FINE_GPS)
                } else {
                    //we can request the permission.
                    ActivityCompat.requestPermissions(this, perms, REQUEST_CODE_FINE_GPS)
                    isLocationModalShown = true
                }
                false
            } else {
                true
            }
        }
        return false
    }

    private var locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult ?: return
            for (location in locationResult.locations) {
                this@DashCollectionStoreFragment.location = location
                stopLocationUpdates()
                break
            }
        }
    }

    private fun startLocationUpdates() {
        context?.apply {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            fusedLocationClient?.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient?.removeLocationUpdates(locationCallback)
    }

    private fun createLocationRequest() = LocationRequest.create().apply {
        interval = 100
        fastestInterval = 1000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }
}