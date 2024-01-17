package za.co.woolworths.financial.services.android.shoppinglist.model

import za.co.woolworths.financial.services.android.models.dto.AddToListRequest
import za.co.woolworths.financial.services.android.models.dto.ShoppingList

sealed class EditOptionType {
    object RemoveItemFromList : EditOptionType()
    data class CopyItemFromList(
        var list: ArrayList<ShoppingList>,
        val itemsToBeAdded: ArrayList<AddToListRequest>
    ) : EditOptionType()

    data class MoveItemFromList (var list:ArrayList<ShoppingList>) : EditOptionType()
}