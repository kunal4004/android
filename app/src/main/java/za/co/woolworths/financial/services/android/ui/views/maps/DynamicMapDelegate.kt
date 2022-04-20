package za.co.woolworths.financial.services.android.ui.views.maps

import com.google.android.gms.maps.model.Marker

interface DynamicMapDelegate {
    fun onMapReady()
    fun onMarkerClicked(marker: Marker)
}