package za.co.woolworths.financial.services.android.ui.wfs.mobileconfig

import android.os.Parcelable
import com.awfs.coordination.R
import kotlinx.android.parcel.Parcelize
import za.co.woolworths.financial.services.android.models.dto.account.ServerErrorResponse

@Parcelize
data class MobileConfigRemoteContentModel(
    var content: MutableList<Content>,
    var response: ServerErrorResponse,
    var httpCode: Int
) : Parcelable

@Parcelize
data class Children(
    var order: Float? = null,
    var title: String? = null,
    var type: ContactUsType? = null,
    var description: String? = null,
    var children: MutableList<ChildrenItem> = mutableListOf()
) : Parcelable

@Parcelize
data class ChildrenItem(
    var order: Float? = null,
    var title: String? = null,
    var description: String? = null,
    var type: ContactUsType? = null,
    var children: MutableList<Children> = mutableListOf()
) : Parcelable

@Parcelize
data class Content(
    var order: Float,
    var reference: String,
    var title: String,
    var description: String?,
    var imageUrl: String,
    var children: MutableList<Children>
) : Parcelable

enum class ContactUsType(private val type: String?) {
    ACTION_EMAIL_INAPP("ACTION_EMAIL_INAPP"),
    ACTION_CALL("ACTION_CALL"),
    ACTION_WHATSAPP_FS("ACTION_WHATSAPP_FS"),
    NONE("");

    fun iconId() = when (type) {
        ACTION_EMAIL_INAPP.type -> R.drawable.icon_email
        ACTION_CALL.type -> R.drawable.ic_phone
        ACTION_WHATSAPP_FS.type -> R.drawable.icon_whatsapp_black
        else -> R.drawable.ic_phone
    }
}