package za.co.woolworths.financial.services.android.models.dto.pma

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import za.co.woolworths.financial.services.android.models.dto.Response
import java.io.Serializable

data class DeleteResponse(
        @SerializedName("httpCode")
        @Expose
        var httpCode: Int,
        @SerializedName("response")
        @Expose
        val response: Response
) : Serializable