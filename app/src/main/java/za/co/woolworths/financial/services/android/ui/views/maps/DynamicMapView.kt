package za.co.woolworths.financial.services.android.ui.views.maps

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.FragmentManager
import com.awfs.coordination.R
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.view_dynamic_map.view.*
import za.co.woolworths.financial.services.android.ui.adapters.MapWindowAdapter
import za.co.woolworths.financial.services.android.util.Utils

class DynamicMapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    defStyleRes: Int = 0
) : ConstraintLayout(context, attrs, defStyle, defStyleRes),
    OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    companion object {
        var CAMERA_ANIMATION_DURATION_FAST = 350
        var CAMERA_ANIMATION_DURATION_SLOW = 500
    }

    private var isGooglePlayServicesAvailable = false

    private var delegate: DynamicMapDelegate? = null
    private var googleMapView: MapView? = null
    private var googleMap: GoogleMap? = null

//    private var huaweiMapView: com.huawei.hms.maps.MapView? = null

    init {
        isGooglePlayServicesAvailable = Utils.isGooglePlayServicesAvailable()
        LayoutInflater.from(context).inflate(R.layout.view_dynamic_map, this, true)
    }

    fun initializeMap(savedInstanceState: Bundle?, delegate: DynamicMapDelegate) {
        if (isGooglePlayServicesAvailable) {
            googleMapView = MapView(context)
            googleMapView?.layoutParams =
                LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            addView(googleMapView)
            this.delegate = delegate
            googleMapView?.onCreate(savedInstanceState)
            googleMapView?.getMapAsync(this)
        } else {
            // TODO
        }
    }

    fun isMapInstantiated(): Boolean {
        if (isGooglePlayServicesAvailable) {
            return googleMap != null
        } else {
            return false // TODO
        }
    }

    // region Google Maps Delegate

    override fun onMapReady(map: GoogleMap) {
        if (isGooglePlayServicesAvailable) {
            googleMap = map
            //If permission is not granted, request permission.
            googleMap?.setInfoWindowAdapter(MapWindowAdapter(context))
            googleMap?.setOnMarkerClickListener(this)
        } else {
            // TODO
        }
        delegate?.onMapReady()
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        delegate?.onMarkerClicked(marker)
        return true
    }

    // endregion

    fun setScrollGesturesEnabled(isEnabled: Boolean) {
        if (isGooglePlayServicesAvailable) {
            googleMap?.uiSettings?.isScrollGesturesEnabled = isEnabled
        } else {
            // TODO
        }
    }

    @SuppressLint("MissingPermission")
    fun setMyLocationEnabled(isEnabled: Boolean) {
        if (isGooglePlayServicesAvailable) {
            googleMap?.isMyLocationEnabled = isEnabled
        } else {
            // TODO
        }
    }

    fun getCameraPositionTargetLatitude(): Double? {
        if (isGooglePlayServicesAvailable) {
            return googleMap?.cameraPosition?.target?.latitude
        } else {
            return null // TODO
        }
    }

    fun getVisibleRegionNortheastLatitude(): Double? {
        if (isGooglePlayServicesAvailable) {
            return googleMap?.projection?.visibleRegion?.latLngBounds?.northeast?.latitude
        } else {
            return null // TODO
        }
    }

    fun addMarker(point: LatLng, bitmapDescriptor: BitmapDescriptor?): Marker? {
        if (isGooglePlayServicesAvailable) {
            val markerOptions = MarkerOptions()
            markerOptions.position(point)
            markerOptions.icon(bitmapDescriptor)
            return googleMap?.addMarker(markerOptions)
        } else {
            return null // TODO
        }
    }

    fun animateCamera(marker: Marker?, duration: Int = CAMERA_ANIMATION_DURATION_FAST) {
        if (isGooglePlayServicesAvailable) {
            googleMap
                ?.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(marker?.position, 13f),
                    duration,
                    null
                )
        } else {
            // TODO
        }
    }

    fun animateCamera(cameraUpdate: CameraUpdate, duration: Int = CAMERA_ANIMATION_DURATION_FAST) {
        if (isGooglePlayServicesAvailable) {
            googleMap
                ?.animateCamera(
                    cameraUpdate,
                    duration,
                    null
                )
        } else {
            // TODO
        }
    }

    fun onResume() {
        if (isGooglePlayServicesAvailable) {
            googleMapView?.onResume()
        } else {
            // TODO
        }
    }

    fun onPause() {
        if (isGooglePlayServicesAvailable) {
            googleMapView?.onPause()
        } else {
            // TODO
        }
    }

    fun onDestroy() {
        if (isGooglePlayServicesAvailable) {
            googleMapView?.onDestroy()
        } else {
            // TODO
        }
    }

    fun onLowMemory() {
        if (isGooglePlayServicesAvailable) {
            googleMapView?.onLowMemory()
        } else {
            // TODO
        }
    }

    fun onSaveInstanceState(outState: Bundle) {
        if (isGooglePlayServicesAvailable) {
            googleMapView?.onSaveInstanceState(outState)
        } else {
            // TODO
        }
    }
}

