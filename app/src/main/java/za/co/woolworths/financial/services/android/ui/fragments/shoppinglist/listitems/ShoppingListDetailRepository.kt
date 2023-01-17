package za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.listitems

import za.co.woolworths.financial.services.android.models.dto.ShoppingListItemsResponse
import za.co.woolworths.financial.services.android.models.dto.SkusInventoryForStoreResponse
import za.co.woolworths.financial.services.android.models.network.Resource

interface ShoppingListDetailRepository {
    suspend fun getShoppingListItems(listId: String): Resource<ShoppingListItemsResponse>
    suspend fun getInventorySkuForStore(
        storeId: String,
        multiSku: String,
        isUserBrowsing: Boolean
    ): Resource<SkusInventoryForStoreResponse>
}