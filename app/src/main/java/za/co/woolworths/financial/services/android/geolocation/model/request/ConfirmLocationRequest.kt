package za.co.woolworths.financial.services.android.geolocation.model.request

import androidx.annotation.Nullable
import za.co.woolworths.financial.services.android.geolocation.model.response.ConfirmLocationAddress
import za.co.woolworths.financial.services.android.util.AppConstant

class ConfirmLocationRequest(
    var deliveryType: String,
    var address: ConfirmLocationAddress,
    var storeId: String? = "",
    @Nullable
    var page: String? = ""
) {

    constructor (deliveryType: String, storeId: String, address: ConfirmLocationAddress) : this(
        deliveryType,
        address,
        storeId,
    ) {
        this.deliveryType = deliveryType
        this.address = address
        this.storeId = storeId
    }

}