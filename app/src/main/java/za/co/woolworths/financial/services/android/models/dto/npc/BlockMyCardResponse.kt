package za.co.woolworths.financial.services.android.models.dto.npc

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import za.co.woolworths.financial.services.android.models.dto.Response
import za.co.woolworths.financial.services.android.models.dto.account.ServerErrorResponse

class BlockMyCardResponse {
    @SerializedName("httpCode")
    @Expose
    var httpCode: Int? = null
    @SerializedName("response")
    @Expose
    var response: ServerErrorResponse? = null
}