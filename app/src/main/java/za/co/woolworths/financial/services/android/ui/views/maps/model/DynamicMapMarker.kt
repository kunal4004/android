package za.co.woolworths.financial.services.android.ui.views.maps.model

import android.content.Context
import androidx.annotation.DrawableRes
import com.google.android.gms.maps.model.Marker as GoogleMarker
import com.huawei.hms.maps.model.Marker as HuaweiMarker
import com.google.android.gms.maps.model.BitmapDescriptorFactory as GoogleBitmapDescriptorFactory
import com.huawei.hms.maps.model.BitmapDescriptorFactory as HuaweiBitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng as GoogleLatLng
import com.huawei.hms.maps.model.LatLng as HuaweiLatLng
import com.huawei.hms.maps.MapsInitializer as HuaweiMapsInitializer

class DynamicMapMarker (
    var googleMarker: GoogleMarker? = null,
    var huaweiMarker: HuaweiMarker? = null
) {
    fun getId(): String? {
        return googleMarker?.id ?: huaweiMarker?.id
    }

    fun setIcon(context: Context, @DrawableRes icon: Int?) {
        try {
            icon?.let { icon ->
                googleMarker?.let {
                    it.setIcon(GoogleBitmapDescriptorFactory.fromResource(icon))
                } ?: huaweiMarker?.let {
                    HuaweiMapsInitializer.initialize(context)
                    it.setIcon(HuaweiBitmapDescriptorFactory.fromResource(icon))
                }
            }
        } catch (ignored: NullPointerException) {}
    }

    fun setVisible(isVisible: Boolean) {
        googleMarker?.let {
            it.isVisible = isVisible
        } ?: huaweiMarker?.let {
            it.isVisible = isVisible
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