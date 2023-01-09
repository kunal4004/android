package za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.listitems

import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.models.dto.FulfillmentStoreMap
import za.co.woolworths.financial.services.android.models.dto.ShoppingListItem
import za.co.woolworths.financial.services.android.models.dto.ShoppingListItemsResponse
import za.co.woolworths.financial.services.android.models.dto.SkusInventoryForStoreResponse
import za.co.woolworths.financial.services.android.models.network.Event
import za.co.woolworths.financial.services.android.models.network.Resource
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.wenum.Delivery
import javax.inject.Inject

@HiltViewModel
class ShoppingListDetailViewModel @Inject constructor(
    private val shoppingListDetailRepository: ShoppingListDetailRepository
) : ViewModel() {

    var inventoryCallFailed: Boolean = false
    var mShoppingListItems: ArrayList<ShoppingListItem> = ArrayList(0)
    private var fulfillmentStoreMapArrayList: ArrayList<FulfillmentStoreMap>? = ArrayList(0)
    var mOpenShoppingListItem: ShoppingListItem? = null
    private val _shoppingListDetails = MutableLiveData<Event<Resource<ShoppingListItemsResponse>>>()
    val shoppListDetails: LiveData<Event<Resource<ShoppingListItemsResponse>>> =
        _shoppingListDetails
    private val _inventoryDetails =
        MutableLiveData<Event<Resource<SkusInventoryForStoreResponse>>>()
    val inventoryDetails: LiveData<Event<Resource<SkusInventoryForStoreResponse>>> =
        _inventoryDetails

    fun getShoppingListDetails(listId: String) {
        _shoppingListDetails.value = Event(Resource.loading(null))
        viewModelScope.launch {
            val response = shoppingListDetailRepository.getShoppingListItems(listId)
            mShoppingListItems = response.data?.listItems?.let { ArrayList(it) } ?: ArrayList(0)
            _shoppingListDetails.value = Event(response)
        }
    }

    /**
     * Step 1: Get all different fulfillment types
     * For each fulfillment type
     * Step 2: Retrieve storeId based on fulfillment type
     * Step 3: If storeId not present make all items matching fulfillment type as unavailable
     * Step 4: else filter list matching fulfillment type
     * Step 5: create list of catalogRefId
     * Step 6: make Inventory call with storeId and catalogRefIds
     **/
    fun makeInventoryCalls() {
        mShoppingListItems.map { it.fulfillmentType }.distinct().forEach { fulfillmentType ->
            val multiSkuList =
                mShoppingListItems.filter { fulfillmentType.equals(it.fulfillmentType) }
            // Retrieve storeId for fulfillmentType
            val storeId = Utils.retrieveStoreId(fulfillmentType)
                ?.replace("\"", "") ?: ""
            if (TextUtils.isEmpty(storeId)) {
                setAllUnavailable(multiSkuList)
            } else {
                fulfillmentStoreMapArrayList?.add(
                    FulfillmentStoreMap(fulfillmentType, storeId, false)
                )
                val skuIds = getSKUIdsByDeliveryType(multiSkuList)
                setUnavailable(multiSkuList, skuIds)
                val multiSku = TextUtils.join("-", skuIds)
                getInventoryStockForStore(storeId, multiSku)
            }
        }
    }

    private fun getInventoryStockForStore(storeId: String, multiSku: String) {
        inventoryCallFailed = false
        _inventoryDetails.value = Event(Resource.loading(null))
        viewModelScope.launch {
            val response = shoppingListDetailRepository.getInventorySkuForStore(
                storeId,
                multiSku,
                isUserBrowsing = false
            )
            var fulfillmentType = ""
            fulfillmentStoreMapArrayList?.let { storeMap ->
                for (mapId in storeMap) {
                    if (mapId.storeID.equals(
                            response.data?.storeId,
                            ignoreCase = true
                        )
                        && !mapId.inventoryCompletedForStore
                    ) {
                        fulfillmentType = mapId.typeID ?: ""
                        mapId.inventoryCompletedForStore = true
                        break
                    }
                }
            }
            val skuInventory = response.data?.skuInventory ?: ArrayList(0)
            for (shoppingListItem in mShoppingListItems) {
                if (fulfillmentType.equals(shoppingListItem.fulfillmentType, ignoreCase = true)) {
                    // Reset quantity in stock
                    shoppingListItem.inventoryCallCompleted = true
                    shoppingListItem.quantityInStock = -1
                    val otherSkuId = shoppingListItem.catalogRefId
                    for (inventorySku in skuInventory) {
                        // Update quantity in stock.
                        if (otherSkuId.equals(inventorySku.sku, ignoreCase = true)) {
                            shoppingListItem.quantityInStock = inventorySku.quantity
                            break
                        }
                    }
                }
            }
            _inventoryDetails.value = Event(response)
        }
    }

    private fun getSKUIdsByDeliveryType(multiSkuList: List<ShoppingListItem>): List<String> {
        val type = Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.deliveryType?.let {
            Delivery.getType(it)
        }
        return when (type) {
            Delivery.DASH -> {
                multiSkuList.filter { item ->
                    ProductVisibility.VISIBILITY_ALL.visibility.equals(
                        item.visibility,
                        ignoreCase = true
                    )
                            || ProductVisibility.VISIBILITY_DASH_ONLY.visibility.equals(
                        item.visibility,
                        ignoreCase = true
                    )
                }.map { it.catalogRefId }
            }
            else -> {
                multiSkuList.map { it.catalogRefId }
            }
        }
    }

    fun setOutOfStock() {
        // Hide quantity progress bar indicator
        if (mShoppingListItems.isNotEmpty()) {
            for (shoppingListItem in mShoppingListItems) {
                shoppingListItem.inventoryCallCompleted = true
                shoppingListItem.quantityInStock = -1
            }
        }
    }

    private fun setAllUnavailable(shoppingListItemsCollection: List<ShoppingListItem>) {
        for (item in shoppingListItemsCollection) {
            for (inventoryItems in mShoppingListItems) {
                if (inventoryItems.catalogRefId.equals(
                        item.catalogRefId,
                        ignoreCase = true
                    )
                ) {
                    inventoryItems.inventoryCallCompleted = true
                    inventoryItems.quantityInStock = -1
                    inventoryItems.unavailable = true
                }
            }
        }
    }

    /**
     * Set items in collection for fulfillment type that are not available in skuIds as
     * unavailable/available
     */
    fun setUnavailable(
        shoppingListItemsCollection: MutableCollection<ShoppingListItem>,
        availableSkuIds: List<String>
    ) {
        for (shoppingItem in shoppingListItemsCollection) {
            val index = mShoppingListItems.indexOf(shoppingItem)
            if (index < 0 || index >= mShoppingListItems.size) continue
            mShoppingListItems[index].apply {
                if (!availableSkuIds.contains(shoppingItem.catalogRefId)) {
                    unavailable = true
                    inventoryCallCompleted = true
                    quantityInStock = -1
                } else {
                    unavailable = false
                    inventoryCallCompleted = false
                }
            }
        }
    }

    fun setUnavailable(
        shoppingListItemsCollection: List<ShoppingListItem>,
        availableSkuIds: List<String>
    ) {
        for (shoppingItem in shoppingListItemsCollection) {
            val index = mShoppingListItems.indexOf(shoppingItem)
            if (index < 0 || index >= mShoppingListItems.size) continue
            mShoppingListItems[index].apply {
                if (!availableSkuIds.contains(shoppingItem.catalogRefId)) {
                    unavailable = true
                    inventoryCallCompleted = true
                    quantityInStock = -1
                } else {
                    unavailable = false
                    inventoryCallCompleted = false
                }
            }
        }
    }

    fun setItem(updatedItem: ShoppingListItem) {
        viewModelScope.launch(Dispatchers.IO) {
            if (mShoppingListItems.isEmpty()) return@launch
            val itemList = mShoppingListItems.filter {
                it.catalogRefId.equals(
                    updatedItem.catalogRefId,
                    ignoreCase = true
                )
            }
            if (itemList.isNotEmpty()) {
                val index = mShoppingListItems.indexOf(itemList[0])
                if (index < 0 || index >= mShoppingListItems.size) return@launch
                mShoppingListItems[index] = updatedItem
            }
        }
    }

    /**
     * Requirement: Sort list as Unavailable -> Out of stock -> Available products.
     */
    fun sortList() {
        viewModelScope.launch(Dispatchers.IO) {
            mShoppingListItems = ArrayList(
                mShoppingListItems.sortedWith(
                    compareBy(
                        { it.unavailable },
                        { it.quantityInStock <= 0 })
                ).reversed()
            )
        }
    }

    fun isItemSelected(items: ArrayList<ShoppingListItem>): Boolean {
        return items.any { it.isSelected }
    }

    enum class ProductVisibility(val visibility: String) {
        VISIBILITY_DASH_ONLY("Dash only"), VISIBILITY_ALL("all")
    }
}