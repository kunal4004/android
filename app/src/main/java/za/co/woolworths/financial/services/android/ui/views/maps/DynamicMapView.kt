package za.co.woolworths.financial.services.android.ui.views.maps

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import com.awfs.coordination.R
import com.google.android.gms.maps.GoogleMap
import com.huawei.hms.maps.HuaweiMap
import za.co.woolworths.financial.services.android.ui.views.maps.adapter.GoogleMapWindowAdapter
import za.co.woolworths.financial.services.android.ui.views.maps.adapter.HuaweiMapWindowAdapter
import za.co.woolworths.financial.services.android.ui.views.maps.model.DynamicMapMarker
import za.co.woolworths.financial.services.android.util.Utils
import com.google.android.gms.maps.CameraUpdateFactory as GoogleCameraUpdateFactory
import com.google.android.gms.maps.MapView as GoogleMapView
import com.google.android.gms.maps.OnMapReadyCallback as GoogleOnMapReadyCallback
import com.google.android.gms.maps.model.CameraPosition as GoogleCameraPosition
import com.google.android.gms.maps.model.LatLng as GoogleLatLng
import com.google.android.gms.maps.model.Marker as GoogleMarker
import com.google.android.gms.maps.model.MarkerOptions as GoogleMarkerOptions
import com.huawei.hms.maps.CameraUpdateFactory as HuaweiCameraUpdateFactory
import com.huawei.hms.maps.MapView as HuaweiMapView
import com.huawei.hms.maps.MapsInitializer as HuaweiMapsInitializer
import com.huawei.hms.maps.OnMapReadyCallback as HuaweiOnMapReadyCallback
import com.huawei.hms.maps.model.CameraPosition as HuaweiCameraPosition
import com.huawei.hms.maps.model.LatLng as HuaweiLatLng
import com.huawei.hms.maps.model.Marker as HuaweiMarker
import com.huawei.hms.maps.model.MarkerOptions as HuaweiMarkerOptions

class DynamicMapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    defStyleRes: Int = 0
) : ConstraintLayout(context, attrs, defStyle, defStyleRes),
    GoogleOnMapReadyCallback, GoogleMap.OnMarkerClickListener,
    HuaweiOnMapReadyCallback, HuaweiMap.OnMarkerClickListener {

    companion object {
        var CAMERA_ANIMATION_DURATION_FAST = 350
        var CAMERA_ANIMATION_DURATION_SLOW = 500
        val MAPVIEW_HUAWEI_BUNDLE_KEY = "MapViewBundleKey"
    }

    private var isGooglePlayServicesAvailable = false

    private var delegate: DynamicMapDelegate? = null
    private var googleMapView: GoogleMapView? = null
    private var googleMap: GoogleMap? = null

    private var huaweiMapView: HuaweiMapView? = null
    private var huaweiMap: HuaweiMap? = null

    init {
        isGooglePlayServicesAvailable = Utils.isGooglePlayServicesAvailable()
        LayoutInflater.from(context).inflate(R.layout.view_dynamic_map, this, true)
    }

    fun initializeMap(savedInstanceState: Bundle?, delegate: DynamicMapDelegate) {
        if (isGooglePlayServicesAvailable) {
            googleMapView = GoogleMapView(context)
            googleMapView?.layoutParams =
                LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            addView(googleMapView)
            googleMapView?.onCreate(savedInstanceState)
            googleMapView?.getMapAsync(this)
        } else {
            HuaweiMapsInitializer.setApiKey(resources.getString(R.string.maps_huawei_api_key))
            huaweiMapView = HuaweiMapView(context)
            huaweiMapView?.layoutParams =
                LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            addView(huaweiMapView)
            huaweiMapView?.onCreate(savedInstanceState?.getBundle(MAPVIEW_HUAWEI_BUNDLE_KEY))
            huaweiMapView?.getMapAsync(this)
        }
        this.delegate = delegate
    }

    fun isMapInstantiated(): Boolean {
        return if (isGooglePlayServicesAvailable) {
            googleMap != null
        } else {
            huaweiMap != null
        }
    }

    // region Google Maps Delegate

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        //If permission is not granted, request permission.
        googleMap?.setInfoWindowAdapter(
            GoogleMapWindowAdapter(
                context
            )
        )
        googleMap?.uiSettings?.isZoomControlsEnabled = false
        googleMap?.setOnMarkerClickListener(this)
        delegate?.onMapReady()
    }

    override fun onMarkerClick(marker: GoogleMarker): Boolean {
        delegate?.onMarkerClicked(DynamicMapMarker(googleMarker = marker))
        return true
    }

    // endregion

    // region Huawei Maps Delegate

    override fun onMapReady(map: HuaweiMap?) {
        huaweiMap = map
        huaweiMap?.setInfoWindowAdapter(
            HuaweiMapWindowAdapter(
                context
            )
        )
        huaweiMap?.uiSettings?.isZoomControlsEnabled = false
        huaweiMap?.setOnMarkerClickListener(this)
        delegate?.onMapReady()
    }

    override fun onMarkerClick(marker: HuaweiMarker): Boolean {
        delegate?.onMarkerClicked(DynamicMapMarker(huaweiMarker = marker))
        return true
    }

    // endregion

    fun setScrollGesturesEnabled(isEnabled: Boolean) {
        if (isGooglePlayServicesAvailable) {
            googleMap?.uiSettings?.isScrollGesturesEnabled = isEnabled
        } else {
            huaweiMap?.uiSettings?.isScrollGesturesEnabled = isEnabled
        }
    }

    fun setAllGesturesEnabled(isEnabled: Boolean) {
        if (isGooglePlayServicesAvailable) {
            googleMap?.uiSettings?.setAllGesturesEnabled(isEnabled)
        } else {
            huaweiMap?.uiSettings?.setAllGesturesEnabled(isEnabled)
        }
    }

    @SuppressLint("MissingPermission")
    fun setMyLocationEnabled(isEnabled: Boolean) {
        if (isGooglePlayServicesAvailable) {
            googleMap?.isMyLocationEnabled = isEnabled
        } else {
            huaweiMap?.isMyLocationEnabled = isEnabled
        }
    }

    fun getCameraPositionTargetLatitude(): Double? {
        return if (isGooglePlayServicesAvailable) {
            googleMap?.cameraPosition?.target?.latitude
        } else {
            huaweiMap?.cameraPosition?.target?.latitude
        }
    }

    fun getCameraPositionTargetLongitude(): Double? {
        return if (isGooglePlayServicesAvailable) {
            googleMap?.cameraPosition?.target?.longitude
        } else {
            huaweiMap?.cameraPosition?.target?.longitude
        }
    }

    fun getVisibleRegionNortheastLatitude(): Double? {
        return if (isGooglePlayServicesAvailable) {
            googleMap?.projection?.visibleRegion?.latLngBounds?.northeast?.latitude
        } else {
            huaweiMap?.projection?.visibleRegion?.latLngBounds?.northeast?.latitude
        }
    }

    fun addMarker(context: Context, latitude: Double?, longitude: Double?, @DrawableRes icon: Int?): DynamicMapMarker? {
        if (latitude == null || longitude == null) return null
        return if (isGooglePlayServicesAvailable) {
            val markerOptions = GoogleMarkerOptions()
            markerOptions.position(GoogleLatLng(latitude, longitude))
            var marker = DynamicMapMarker(googleMarker = googleMap?.addMarker(markerOptions))
            marker.setIcon(context, icon)
            marker
        } else {
            val markerOptions = HuaweiMarkerOptions()
            markerOptions.position(HuaweiLatLng(latitude, longitude))
            var marker = DynamicMapMarker(huaweiMarker = huaweiMap?.addMarker(markerOptions))
            marker.setIcon(context, icon)
            marker
        }
    }

    fun animateCamera(marker: DynamicMapMarker?, duration: Int = CAMERA_ANIMATION_DURATION_FAST) {
        if (isGooglePlayServicesAvailable) {
            googleMap
                ?.animateCamera(
                    GoogleCameraUpdateFactory.newLatLngZoom(marker?.googleMarker?.position, 13f),
                    duration,
                    null
                )
        } else {
            huaweiMap
                ?.animateCamera(
                    HuaweiCameraUpdateFactory.newLatLngZoom(marker?.huaweiMarker?.position, 13f),
                    duration,
                    null
                )
        }
    }

    fun animateCamera(
        latitude: Double?,
        longitude: Double?,
        duration: Int = CAMERA_ANIMATION_DURATION_FAST
    ) {
        if (latitude == null || longitude == null) return
        if (isGooglePlayServicesAvailable) {
            googleMap
                ?.animateCamera(
                    GoogleCameraUpdateFactory.newLatLng(GoogleLatLng(latitude, longitude)),
                    duration,
                    null
                )
        } else {
            huaweiMap
                ?.animateCamera(
                    HuaweiCameraUpdateFactory.newLatLng(HuaweiLatLng(latitude, longitude)),
                    duration,
                    null
                )
        }
    }

    fun animateCamera(
        latitude: Double?,
        longitude: Double?,
        zoom: Float? = null,
        bearing: Float? = null,
        tilt: Float? = null,
        duration: Int = CAMERA_ANIMATION_DURATION_FAST
    ) {
        if (latitude == null || longitude == null) return
        if (isGooglePlayServicesAvailable) {
            var cameraPositionBuilder =
                GoogleCameraPosition.builder().target(GoogleLatLng(latitude, longitude))
            if (zoom != null) {
                cameraPositionBuilder.zoom(zoom)
            }
            if (bearing != null) {
                cameraPositionBuilder.bearing(bearing)
            }
            if (tilt != null) {
                cameraPositionBuilder.tilt(tilt)
            }

            googleMap
                ?.animateCamera(
                    GoogleCameraUpdateFactory.newCameraPosition(
                        cameraPositionBuilder.build()
                    ),
                    duration,
                    null
                )
        } else {
            var cameraPositionBuilder =
                HuaweiCameraPosition.builder().target(HuaweiLatLng(latitude, longitude))
            if (zoom != null) {
                cameraPositionBuilder.zoom(zoom)
            }
            if (bearing != null) {
                cameraPositionBuilder.bearing(bearing)
            }
            if (tilt != null) {
                cameraPositionBuilder.tilt(tilt)
            }

            huaweiMap
                ?.animateCamera(
                    HuaweiCameraUpdateFactory.newCameraPosition(
                        cameraPositionBuilder.build()
                    ),
                    duration,
                    null
                )
        }
    }

    fun moveCamera(
        latitude: Double?,
        longitude: Double?,
        zoom: Float
    ) {
        if (latitude == null || longitude == null) return
        if (isGooglePlayServicesAvailable) {
            googleMap
                ?.moveCamera(
                    GoogleCameraUpdateFactory.newLatLngZoom(
                        GoogleLatLng(latitude, longitude),
                        zoom
                    )
                )
        } else {
            huaweiMap
                ?.moveCamera(
                    HuaweiCameraUpdateFactory.newLatLngZoom(
                        HuaweiLatLng(latitude, longitude),
                        zoom
                    )
                )
        }
    }

    fun setOnCameraMoveListener(listener: () -> Unit) {
        if (isGooglePlayServicesAvailable) {
            googleMap
                ?.setOnCameraMoveListener(listener)
        } else {
            huaweiMap
                ?.setOnCameraMoveListener(listener)
        }
    }

    fun setOnCameraIdleListener(listener: () -> Unit) {
        if (isGooglePlayServicesAvailable) {
            googleMap
                ?.setOnCameraIdleListener(listener)
        } else {
            huaweiMap
                ?.setOnCameraIdleListener(listener)
        }
    }

    fun onResume() {
        if (isGooglePlayServicesAvailable) {
            googleMapView?.onResume()
        } else {
            huaweiMapView?.onResume()
        }
    }

    fun onPause() {
        if (isGooglePlayServicesAvailable) {
            googleMapView?.onPause()
        } else {
            huaweiMapView?.onPause()
        }
    }

    fun onDestroy() {
        if (isGooglePlayServicesAvailable) {
            googleMapView?.onDestroy()
        } else {
            huaweiMapView?.onDestroy()
        }
    }

    fun onLowMemory() {
        if (isGooglePlayServicesAvailable) {
            googleMapView?.onLowMemory()
        } else {
            huaweiMapView?.onLowMemory()
        }
    }

    fun onSaveInstanceState(outState: Bundle) {
        if (isGooglePlayServicesAvailable) {
            googleMapView?.onSaveInstanceState(outState)
        } else {
            huaweiMapView?.onSaveInstanceState(outState)
        }
    }
}

