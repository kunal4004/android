package za.co.woolworths.financial.services.android.models.dto.order_again

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class ImpressionReporting {
    @SerializedName("is_control")
    @Expose
    var isControl: Boolean? = null

    @SerializedName("has_targets")
    @Expose
    var hasTargets: Boolean? = null

    @SerializedName("tags")
    @Expose
    var tags: List<Any>? = null

    @SerializedName("experience_type")
    @Expose
    var experienceType: String? = null

    @SerializedName("experience_name")
    @Expose
    var experienceName: String? = null

    @SerializedName("experience_id")
    @Expose
    var experienceId: Int? = null

    @SerializedName("experience_label")
    @Expose
    var experienceLabel: String? = null

    @SerializedName("variant_label")
    @Expose
    var variantLabel: String? = null

    @SerializedName("control_allocation")
    @Expose
    var controlAllocation: Int? = null
}