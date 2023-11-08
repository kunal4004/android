package za.co.woolworths.financial.services.android.models.dto.order_again

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Action {
    @SerializedName("impressionReporting")
    @Expose
    var impressionReporting: List<ImpressionReporting>? = null

    @SerializedName("actionType")
    @Expose
    var actionType: String? = null

    @SerializedName("actionId")
    @Expose
    var actionId: Int? = null

    @SerializedName("isControl")
    @Expose
    var isControl: Boolean? = null

    @SerializedName("items")
    @Expose
    var items: List<Item>? = null

    @SerializedName("actionEvents")
    @Expose
    var actionEvents: List<String>? = null

    @SerializedName("component")
    @Expose
    var component: String? = null
}