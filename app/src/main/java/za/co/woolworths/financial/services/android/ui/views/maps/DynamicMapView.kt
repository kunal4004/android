package za.co.woolworths.financial.services.android.ui.views.maps

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentManager
import com.awfs.coordination.R
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import za.co.woolworths.financial.services.android.ui.adapters.MapWindowAdapter

class DynamicMapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    defStyleRes: Int = 0,
    var delegate: DynamicMapDelegate? = null
) : ConstraintLayout(context, attrs, defStyle, defStyleRes), OnMapReadyCallback,
    GoogleMap.OnMarkerClickListener {

    companion object {
        var CAMERA_ANIMATION_DURATION_FAST = 350
        var CAMERA_ANIMATION_DURATION_SLOW = 500
    }

    private var googleMapFragment: SupportMapFragment? = null
    var googleMap: GoogleMap? = null // TODO: make var private and create functions for usage of this

    init {
        LayoutInflater.from(context).inflate(R.layout.view_dynamic_map, this, true)
    }

    fun initializeMap(fragmentManager: FragmentManager, delegate: DynamicMapDelegate) {
        this.delegate = delegate
        if (googleMap == null) {
            googleMapFragment = fragmentManager.findFragmentById(R.id.googleMapFragment) as SupportMapFragment?
            googleMapFragment?.getMapAsync(this)
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        //If permission is not granted, request permission.
        googleMap?.setInfoWindowAdapter(MapWindowAdapter(context))
        googleMap?.setOnMarkerClickListener(this)
        delegate?.onMapReady()
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        delegate?.onMarkerClicked(marker)
        return true
    }

    fun addMarker(point: LatLng, bitmapDescriptor: BitmapDescriptor?): Marker? {
        val markerOptions = MarkerOptions()
        markerOptions.position(point)
        markerOptions.icon(bitmapDescriptor)
        return googleMap?.addMarker(markerOptions)
    }

    fun animateCamera(marker: Marker?, duration: Int = CAMERA_ANIMATION_DURATION_FAST) {
        googleMap
            ?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(marker?.position, 13f),
                duration,
                null
            )
    }

    fun animateCamera(cameraUpdate: CameraUpdate, duration: Int = CAMERA_ANIMATION_DURATION_FAST, callback: GoogleMap.CancelableCallback? = null) {
        googleMap
            ?.animateCamera(
                cameraUpdate,
                duration,
                callback
            )
    }
}