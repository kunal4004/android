package za.co.woolworths.financial.services.android.ui.fragments.product.back_in_stock.models

import com.google.gson.annotations.SerializedName
import za.co.woolworths.financial.services.android.models.dto.Response

class NotifyMeResponse {
    var httpCode = 0
    var response: Response? = null
    @SerializedName("stockNotification")
    var stockNotification: NotifyMe? = null
}
