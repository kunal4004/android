package za.co.woolworths.financial.services.android.ui.views.maps

import za.co.woolworths.financial.services.android.ui.views.maps.model.DynamicMapMarker

interface DynamicMapDelegate {
    fun onMapReady()
    fun onMarkerClicked(marker: DynamicMapMarker)
}