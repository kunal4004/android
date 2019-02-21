package za.co.woolworths.financial.services.android.contracts

import za.co.woolworths.financial.services.android.models.dto.ShoppingList

interface IShoppingList {
    fun onShoppingListItemDeleted(shoppingList: ShoppingList, position: Int)
    fun onShoppingListItemSelected(shoppingList: ShoppingList)
}