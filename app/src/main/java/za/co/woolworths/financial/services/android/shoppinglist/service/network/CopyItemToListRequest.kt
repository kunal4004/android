package za.co.woolworths.financial.services.android.shoppinglist.service.network

data class CopyItemToListRequest(
    var items: List<ItemDetail>,
    var giftListIds: List<String>?
)