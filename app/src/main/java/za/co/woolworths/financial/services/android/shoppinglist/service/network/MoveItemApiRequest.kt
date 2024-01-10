package za.co.woolworths.financial.services.android.shoppinglist.service.network

data class MoveItemApiRequest(
    val giftListIds: List<String>,
    val items: List<ItemDetail>,
    val removalGiftItemIds: List<String>,
    val sourceGiftListId: String
)