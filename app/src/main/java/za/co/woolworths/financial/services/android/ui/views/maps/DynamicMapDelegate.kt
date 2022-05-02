package za.co.woolworths.financial.services.android.ui.views.maps

import za.co.woolworths.financial.services.android.ui.views.maps.model.DynamicMapMarker
import com.huawei.hms.maps.model.Marker as HuaweiMarker
import com.google.android.gms.maps.model.Marker as GoogleMarker

interface DynamicMapDelegate {
    fun onMapReady()
    fun onMarkerClicked(marker: DynamicMapMarker)
}