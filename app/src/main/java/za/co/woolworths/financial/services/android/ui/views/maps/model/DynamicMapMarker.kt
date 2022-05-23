package za.co.woolworths.financial.services.android.ui.views.maps.model

import androidx.annotation.DrawableRes
import com.google.android.gms.maps.model.Marker as GoogleMarker
import com.huawei.hms.maps.model.Marker as HuaweiMarker
import com.google.android.gms.maps.model.BitmapDescriptorFactory as GoogleBitmapDescriptorFactory
import com.huawei.hms.maps.model.BitmapDescriptorFactory as HuaweiBitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng as GoogleLatLng
import com.huawei.hms.maps.model.LatLng as HuaweiLatLng

class DynamicMapMarker (
    var googleMarker: GoogleMarker? = null,
    var huaweiMarker: HuaweiMarker? = null
) {
    fun getId(): String? {
        return googleMarker?.id ?: huaweiMarker?.id
    }

    fun setIcon(@DrawableRes icon: Int?) {
        icon?.let{ icon ->
            googleMarker?.let {
                it.setIcon(GoogleBitmapDescriptorFactory.fromResource(icon))
            } ?: huaweiMarker?.let {
                it.setIcon(HuaweiBitmapDescriptorFactory.fromResource(icon))
            }
        }
    }

    fun showInfoWindow() {
        googleMarker?.apply {
            showInfoWindow()
        } ?: huaweiMarker?.apply {
            showInfoWindow()
        }
    }

    fun getPositionLatitude(): Double? {
        return googleMarker?.position?.latitude ?: huaweiMarker?.position?.latitude
    }

    fun getPositionLongitude(): Double? {
        return googleMarker?.position?.longitude ?: huaweiMarker?.position?.longitude
    }

    fun setVisibility(isVisible: Boolean) {
        googleMarker?.let { marker ->
            marker.isVisible = isVisible
        } ?: huaweiMarker?.let { marker ->
            marker.isVisible = isVisible
        }
    }

    fun setPosition(latitude: Double, longitude: Double) {
        googleMarker?.apply {
            position = GoogleLatLng(latitude, longitude)
        } ?: huaweiMarker?.apply {
            position = HuaweiLatLng(latitude, longitude)
        }
    }
}