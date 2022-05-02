package za.co.woolworths.financial.services.android.ui.views.maps.model

import com.google.android.gms.maps.model.Marker as GoogleMarker
import com.huawei.hms.maps.model.Marker as HuaweiMarker

class DynamicMapMarker (
    var googleMarker: GoogleMarker? = null,
    var huaweiMarker: HuaweiMarker? = null
)