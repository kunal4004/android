package za.co.woolworths.financial.services.android.shoppinglist.model

data class RemoveApiRequest (
    val giftListId:String?,
    val removalGiftItemIds:List<String>
)
