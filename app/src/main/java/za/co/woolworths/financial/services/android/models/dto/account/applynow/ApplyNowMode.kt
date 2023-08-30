package za.co.woolworths.financial.services.android.models.dto.account.applynow

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import za.co.woolworths.financial.services.android.util.expand.ParentListItem


@Parcelize
data class ApplyNowModel (
    var content : List<Content>,
    var response : Response,
    var httpCode : Int
):Parcelable

@Parcelize
data class Children (
    var order : Int,
    var title : String,
    var description : String?,
    var type : String,
    var sectionType: ApplyNowSectionType = ApplyNowSectionType.valueOf(type),
    var children : List<ChildrenItems>
):Parcelable

@Parcelize
data class ChildrenItems (
    var title : String?,
    var description : String,
    var imageUrl : String?
):Parcelable,ParentListItem {
    override fun getChildItemList(): MutableList<String?> = mutableListOf(description)
    override fun isInitiallyExpanded(): Boolean = false
}

@Parcelize
data class Content (

    var order : Int,
    var reference : String,
    var title : String,
    var description : String,
    var imageUrl : String,
    var children : List<Children>
):Parcelable

@Parcelize
data class Response (

    var code : Int,
    var desc : String
):Parcelable


sealed class ApplyNowTypesModel:Parcelable {
    @Parcelize
    data class LeftIconContent(
        val title : String,
        val description : String,
        val imageUrl : String
    ) : ApplyNowTypesModel()
    @Parcelize
    data class LeftIconExpandable(
        val title : String,
        val description : String,
        val imageUrl : String
    ) : ApplyNowTypesModel()
    @Parcelize
    data class ListUnordered(
        val description : String,
    ) : ApplyNowTypesModel()
}

enum class ApplyNowSectionType(val value: String) {
    LEFT_ICON_WITH_CONTENT("LEFT_ICON_WITH_CONTENT"),
    LEFT_ICON_WITH_CONTENT_EXPANDABLE("LEFT_ICON_WITH_CONTENT_EXPANDABLE"),
    LIST_UNORDERED("LIST_UNORDERED")
}
enum class ApplyNowSectionReference(val value: String) {
    CREDIT_CARD_GOLD("CREDIT_CARD_GOLD"),
    CREDIT_CARD_BLACK("CREDIT_CARD_BLACK"),
    PERSONAL_LOAN("PERSONAL_LOAN"),
    STORE_CARD("STORE_CARD")
}