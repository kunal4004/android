package za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.listitems

import za.co.woolworths.financial.services.android.models.dto.ShoppingListItemsResponse
import za.co.woolworths.financial.services.android.models.dto.SkusInventoryForStoreResponse
import za.co.woolworths.financial.services.android.models.network.Resource
import za.co.woolworths.financial.services.android.shoppinglist.model.RemoveItemApiRequest
import za.co.woolworths.financial.services.android.shoppinglist.service.network.CopyItemToListRequest
import za.co.woolworths.financial.services.android.shoppinglist.service.network.CopyListResponse

interface ShoppingListDetailRepository {
    suspend fun getShoppingListItems(listId: String): Resource<ShoppingListItemsResponse>
    suspend fun getInventorySkuForStore(
        storeId: String,
        multiSku: String,
        isUserBrowsing: Boolean = false
    ): Resource<SkusInventoryForStoreResponse>

    suspend fun removeMultipleItemsFromList(listId: String, removeItemApiRequest: RemoveItemApiRequest): Resource<ShoppingListItemsResponse>

    suspend fun copyMultipleItemsFromList(copyItemToListRequest: CopyItemToListRequest): Resource<CopyListResponse>

    suspend fun getItemsInSharedShoppingList(listId: String) : Resource<ShoppingListItemsResponse>
}