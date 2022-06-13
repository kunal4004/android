package za.co.woolworths.financial.services.android.models.dto.npc

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import za.co.woolworths.financial.services.android.models.dto.account.ServerErrorResponse
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.model.Response

class LinkNewCardResponse {
    @SerializedName("httpCode")
    @Expose
    var httpCode: Int? = null
    @SerializedName("response")
    @Expose
    var response: ServerErrorResponse? = null

}