package za.co.woolworths.financial.services.android.ui.fragments.product.detail.DyChangeAttribute.Request

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class PrepareChangeAttributeRequestEvent(
   @SerializedName("context")
    val context: ContextDyChangeAttribute,
   @SerializedName("events")
    val eventDyChangeAttributes: List<EventDyChangeAttribute>,
   @SerializedName("session")
    val session: Session,
   @SerializedName("user")
    val user: User
): Serializable