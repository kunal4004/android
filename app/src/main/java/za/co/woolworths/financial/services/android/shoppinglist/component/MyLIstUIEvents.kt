package za.co.woolworths.financial.services.android.shoppinglist.component

import za.co.woolworths.financial.services.android.models.dto.ShoppingList

/**
 * Created by Kunal Uttarwar on 21/09/23.
 */
sealed class MyLIstUIEvents {
    data class ListItemClick(val item: ShoppingList) : MyLIstUIEvents()
    object CreateListClick : MyLIstUIEvents()
    object ChangeLocationClick : MyLIstUIEvents()
}