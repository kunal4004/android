package za.co.woolworths.financial.services.android.ui.wfs.contact_us.model

import android.os.Parcelable
import com.awfs.coordination.R
import kotlinx.android.parcel.Parcelize
import za.co.woolworths.financial.services.android.models.dto.account.ServerErrorResponse

@Parcelize
data class ContactUsRemoteModel(
    var content: MutableList<Content>?,
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
    var reference: String? = null,
    var type: ContactUsType? = null,
    var children: MutableList<Children> = mutableListOf()
) : Parcelable

@Parcelize
data class Content(
    var order: Float? = null,
    var reference: String? = null,
    var title: String? = null,
    var description: String? = null,
    var imageUrl: String?  = null,
    var children: MutableList<Children> = mutableListOf()
) : Parcelable

enum class ContactUsType(private val type: String?) {
    ACTION_EMAIL_INAPP("ACTION_EMAIL_INAPP"),
    ACTION_CALL("ACTION_CALL"),
    ACTION_WHATSAPP_FS("ACTION_WHATSAPP_FS"),
    ACTION_FAX("ACTION_FAX"),
    NONE("");

    fun iconId() = when (type) {
        ACTION_EMAIL_INAPP.type -> R.drawable.icon_email
        ACTION_CALL.type -> R.drawable.ic_phone
        ACTION_WHATSAPP_FS.type -> R.drawable.icon_whatsapp_black
        ACTION_FAX.type -> R.drawable.ic_fax
        else -> R.drawable.ic_phone
    }

    fun getShimmerModel(): MutableList<Content> {
        val contentList = mutableListOf<Content>()
        val content = Content()
        val children = mutableListOf<Children>()
        children.add(Children(title = ".", description = "."))
        content.children = children
        contentList.add(content)
        val content1 = Content()
        val children1 = mutableListOf<Children>()
        children1.add(Children(title = ".", description = "."))
        children1.add(Children(title = ".", description = "."))
        children1.add(Children(title = ".", description = "."))
        children1.add(Children(title = ".", description = "."))
        children1.add(Children(title = ".", description = "."))
        children1.add(Children(title = ".", description = "."))
        content1.children = children1
        contentList.add(content1)
        return contentList
    }
}