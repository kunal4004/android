package za.co.woolworths.financial.services.android.ui.views.maps.model

import com.huawei.hms.maps.model.LatLng as HuaweiLatLng
import com.google.android.gms.maps.model.LatLng as GoogleLatLng

class DynamicLatLng (
    var googleLatLng: GoogleLatLng? = null,
    var huaweiLatLng: HuaweiLatLng? = null
)