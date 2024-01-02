package za.co.woolworths.financial.services.android.models.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.shoppinglist.service.network.CopyItemDetail
import za.co.woolworths.financial.services.android.util.analytics.dto.AnalyticProductItem

@Parcelize
data class AddToListRequest(
    var skuID: String? = null,
    var giftListId: String? = null,
    var catalogRefId: String? = null,
    var quantity: String? = null,
    var listId: String? = null,
    var isGWP: Boolean = false,
    var size: String? = null
) : Parcelable

fun AddToListRequest.toCopyItemDetail(): CopyItemDetail = CopyItemDetail(
    skuID = skuID ?: "",
    catalogRefId = catalogRefId ?: "",
    quantity = "1"
)

fun AddToListRequest.toAnalyticItem(category: String?) = AnalyticProductItem(
    itemId = skuID,
    category = category,
    itemListName = category,
    quantity = 1,
    affiliation = FirebaseManagerAnalyticsProperties.PropertyValues.AFFILIATION_VALUE,
    index = FirebaseManagerAnalyticsProperties.PropertyValues.INDEX_VALUE.toInt()
)
