package za.co.woolworths.financial.services.android.ui.views.shop.dash

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.layout_dash_collection_store.*
import kotlinx.android.synthetic.main.layout_dash_set_address_fragment.*
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.shop.DepartmentsFragment
import za.co.woolworths.financial.services.android.ui.fragments.store.StoresNearbyFragment1
import za.co.woolworths.financial.services.android.util.Utils

class ChangeFullfilmentCollectionStoreFragment : Fragment(R.layout.layout_dash_collection_store) {

    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var isLocationModalShown: Boolean = false
    private var location: Location? = null
    private var locationRequest: LocationRequest? = createLocationRequest()

    companion object {
        const val REQUEST_CODE_FINE_GPS = 4771
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    fun init() {
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
                this@ChangeFullfilmentCollectionStoreFragment.location = it

            }
        } else {
            // when permission granted and location is not enabled
            if (isPermissionGranted) {
                // todo to be handled in separate story.
            }
            //When Location permission not granted.
            else if (!checkLocationPermission() && !isLocationModalShown) {
                // todo to show gps not on dialog.
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == DepartmentsFragment.REQUEST_CODE_FINE_GPS) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    activity?.apply {
                        if (!Utils.isLocationEnabled(context)) {
                            val locIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            startActivityForResult(
                                locIntent,
                                StoresNearbyFragment1.REQUEST_CHECK_SETTINGS
                            )
                            overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
                        } else {
                            hideLocationDisabledUi()
                            stopLocationUpdates()
                        }
                    }
                }
                Activity.RESULT_CANCELED -> {
                    //When user clicks deny location

                }
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
                    showLocationDisabledUi(perms)
                } else {
                    //we can request the permission.
                    showLocationDisabledUi(perms)
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
                this@ChangeFullfilmentCollectionStoreFragment.location = location
                hideLocationDisabledUi()
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

    private fun showLocationDisabledUi(perms: Array<String>) {
        layoutEdgeCaseScreen.visibility = View.VISIBLE
        img_view.setImageResource(R.drawable.location_disabled)
        txt_dash_title.text = bindString(R.string.device_location_service_disabled)
        txt_dash_sub_title.text = bindString(R.string.device_location_service_disabled_subTitle)
        btn_dash_set_address.text = bindString(R.string.btn_turn_on_location_text)
        btn_dash_set_address.setOnClickListener {
            ActivityCompat.requestPermissions(this.requireActivity(), perms, REQUEST_CODE_FINE_GPS)
        }
    }

    private fun hideLocationDisabledUi() {
        layoutEdgeCaseScreen.visibility = View.GONE
    }

    fun scrollToTop() {
            layoutEdgeCaseScreen?.scrollTo(0, 0)
    }
}