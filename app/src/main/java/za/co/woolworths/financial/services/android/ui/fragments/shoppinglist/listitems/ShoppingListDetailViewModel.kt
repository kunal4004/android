package za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.listitems

import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
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
import za.co.woolworths.financial.services.android.shoppinglist.model.RemoveItemApiRequest
import za.co.woolworths.financial.services.android.shoppinglist.service.network.CopyItemToListRequest
import za.co.woolworths.financial.services.android.shoppinglist.service.network.CopyListResponse
import za.co.woolworths.financial.services.android.util.Utils
import javax.inject.Inject

@HiltViewModel
class ShoppingListDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val shoppingListDetailRepository: ShoppingListDetailRepository,
) : ViewModel() {

    companion object {
        private const val ARG_LIST_ID: String = "listId"
    }

    var listId: String = ""

    private var isCheckedDontAskAgain: Boolean = false

    private val _shoppingListDetails = MutableLiveData<Event<Resource<ShoppingListItemsResponse>>>()
    val shoppListDetails: LiveData<Event<Resource<ShoppingListItemsResponse>>> =
        _shoppingListDetails

    private val _shoppingListDetailsAfterDelete = MutableLiveData<Event<Resource<ShoppingListItemsResponse>>>()
    val shoppingListDetailsAfterDelete: LiveData<Event<Resource<ShoppingListItemsResponse>>> =
        _shoppingListDetailsAfterDelete

    private val _copyItemsToList = MutableLiveData<Event<Resource<CopyListResponse>>>()
    val copyItemsToList: LiveData<Event<Resource<CopyListResponse>>> =
        _copyItemsToList

    init {
        listId = savedStateHandle[ARG_LIST_ID] ?: ""
        getShoppingListDetails()
    }

    var inventoryCallFailed: Boolean = false
    var mShoppingListItems: ArrayList<ShoppingListItem> = ArrayList(0)
    private var fulfillmentStoreMapArrayList: ArrayList<FulfillmentStoreMap>? = ArrayList(0)
    var mOpenShoppingListItem: ShoppingListItem? = null

    private val _isListUpdated = MutableLiveData(false)
    val isListUpdated: LiveData<Boolean> = _isListUpdated

    private val _inventoryDetails =
        MutableLiveData<Event<Resource<SkusInventoryForStoreResponse>>>()
    val inventoryDetails: LiveData<Event<Resource<SkusInventoryForStoreResponse>>> =
        _inventoryDetails

    fun getShoppingListDetails() {
        _shoppingListDetails.value = Event(Resource.loading(null))
        viewModelScope.launch {
            val response = shoppingListDetailRepository.getShoppingListItems(listId)
            mShoppingListItems = response.data?.listItems?.let { ArrayList(it) } ?: ArrayList(0)
            _shoppingListDetails.value = Event(response)
        }
    }

    fun makeInventoryCalls() {
        _isListUpdated.value = false
        viewModelScope.launch(Dispatchers.IO) {

            val availableList =
                mShoppingListItems.filter {
                    ProductAvailability.AVAILABLE.value.equals(
                        it.availability,
                        ignoreCase = true
                    )
                }

            //separate fulfillmentType and get inventory for items
            availableList.distinctBy { it.fulfillmentType }.forEach { item ->
                val multiSkuList = availableList.filter {
                    item?.fulfillmentType.equals(it?.fulfillmentType)
                }
                // Retrieve storeId for fulfillmentType
                val storeId = Utils.retrieveStoreId(item.fulfillmentType)
                    ?.replace("\"", "") ?: ""
                if (TextUtils.isEmpty(storeId)) {
                    viewModelScope.launch(Dispatchers.Main) {
                        _isListUpdated.value = true
                    }
                    return@launch
                }

                fulfillmentStoreMapArrayList?.add(
                    FulfillmentStoreMap(item.fulfillmentType, storeId, false)
                )

                val skuIds = multiSkuList.map { it.catalogRefId }
                val multiSku = TextUtils.join("-", skuIds)
                getInventoryStockForStore(storeId, multiSku)
            }
        }
    }

    suspend fun getItemsInSharedShoppingList(listId: String, viewOnlyType: Boolean) {
        _shoppingListDetails.value = Event(Resource.loading(null))
        viewModelScope.launch {
            val response = shoppingListDetailRepository.getItemsInSharedShoppingList(listId, viewOnlyType)
            mShoppingListItems = response.data?.listItems?.let { ArrayList(it) } ?: ArrayList(0)
            _shoppingListDetails.value = Event(response)
        }
    }

    private fun getInventoryStockForStore(storeId: String, multiSku: String) {
        inventoryCallFailed = false
        viewModelScope.launch(Dispatchers.Main) {
            _inventoryDetails.value = Event(Resource.loading(null))
        }
        viewModelScope.launch(Dispatchers.IO) {
            val response = shoppingListDetailRepository.getInventorySkuForStore(storeId, multiSku)

            var fulfillmentType = ""
            fulfillmentStoreMapArrayList?.let { storeMap ->
                storeMap.firstOrNull {
                    it.storeID.equals(response.data?.storeId, ignoreCase = true)
                            && !it.inventoryCompletedForStore
                }?.also {
                    fulfillmentType = it.typeID ?: ""
                    it.inventoryCompletedForStore = true
                }
            }
            val skuInventory = response.data?.skuInventory ?: ArrayList(0)
            mShoppingListItems
                .filter { fulfillmentType.equals(it.fulfillmentType, ignoreCase = true) }
                .map { shoppingListItem ->
                    // Reset quantity in stock
                    shoppingListItem.inventoryCallCompleted = true
                    val inventoryItem =
                        skuInventory.firstOrNull {
                            shoppingListItem.catalogRefId.equals(it.sku, ignoreCase = true)
                        }
                    shoppingListItem.quantityInStock = inventoryItem?.quantity ?: -1
                }
            viewModelScope.launch(Dispatchers.Main) {
                _inventoryDetails.value = Event(response)
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

    fun isShoppingListContainsUnavailableItems(): Boolean = mShoppingListItems.any { item ->
        ProductAvailability.UNAVAILABLE.value.equals(item.availability, ignoreCase = true)
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

    fun isItemSelected(items: ArrayList<ShoppingListItem>?): Boolean {
        return items?.any { it.isSelected } ?: false
    }

    fun onDeleteSyncList(adapterList: ArrayList<ShoppingListItem>?) {
        adapterList ?: return

        adapterList.forEach { adapterItem ->
            mShoppingListItems.find { it.catalogRefId == adapterItem.catalogRefId }?.apply {
                // Since the location is not changed.
                inventoryCallCompleted = adapterItem.inventoryCallCompleted
                quantityInStock = adapterItem.quantityInStock
                isSelected = adapterItem.isSelected
                userQuantity = adapterItem.userQuantity
            }
        }
    }

    fun syncListWithAdapter(adapterList: ArrayList<ShoppingListItem>?) {
        adapterList ?: return
        adapterList.forEach { adapterItem ->
            mShoppingListItems.find { it.catalogRefId == adapterItem.catalogRefId }?.apply {
                val isAvailable =
                    ProductAvailability.AVAILABLE.value.equals(availability, ignoreCase = true)
                if (isAvailable) {
                    isSelected = adapterItem.isSelected
                    userQuantity = adapterItem.userQuantity.coerceAtLeast(1)
                }
            }
        }
    }

    fun getIsStockAvailable(): Boolean = mShoppingListItems.any { it.quantityInStock > 0 }

    fun removeMultipleItemsFromList(listId: String, removeItemApiRequest: RemoveItemApiRequest) {
        _shoppingListDetailsAfterDelete.value = Event(Resource.loading(null))
        viewModelScope.launch {
            val response =
                shoppingListDetailRepository.removeMultipleItemsFromList(listId, removeItemApiRequest)
            mShoppingListItems = response.data?.listItems?.let { ArrayList(it) } ?: ArrayList(0)
            _shoppingListDetailsAfterDelete.value = Event(response)
        }
    }


    fun copyMultipleItemsFromList(copyItemToListRequest: CopyItemToListRequest) {
        _copyItemsToList.value = Event(Resource.loading(null))
        viewModelScope.launch {
            val response =
                shoppingListDetailRepository.copyMultipleItemsFromList(copyItemToListRequest)
            _copyItemsToList.value = Event(response)
        }
    }

    fun setIsCheckedDontAskAgain(checkedDontAskAgain: Boolean) {
        isCheckedDontAskAgain = checkedDontAskAgain
    }

    fun isCheckedDontAskAgain() = isCheckedDontAskAgain
}