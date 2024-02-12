package za.co.woolworths.financial.services.android.presentation.addtolist

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.awfs.coordination.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.domain.usecase.AddToListByOrderIdUC
import za.co.woolworths.financial.services.android.domain.usecase.AddToListUC
import za.co.woolworths.financial.services.android.domain.usecase.CreateNewListUC
import za.co.woolworths.financial.services.android.domain.usecase.GetMyListsUC
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dto.AddToListRequest
import za.co.woolworths.financial.services.android.models.dto.OrderToShoppingListRequestBody
import za.co.woolworths.financial.services.android.models.dto.ShoppingList
import za.co.woolworths.financial.services.android.models.network.AppContextProviderImpl
import za.co.woolworths.financial.services.android.models.network.NetworkConfig
import za.co.woolworths.financial.services.android.models.network.Status
import za.co.woolworths.financial.services.android.presentation.addtolist.components.AddToListScreenEvents
import za.co.woolworths.financial.services.android.presentation.addtolist.components.AddToListUiState
import za.co.woolworths.financial.services.android.presentation.addtolist.components.AddedToListState
import za.co.woolworths.financial.services.android.presentation.addtolist.components.CreateNewListState
import za.co.woolworths.financial.services.android.recommendations.data.response.request.Event
import za.co.woolworths.financial.services.android.shoppinglist.service.network.CopyItemToListRequest
import za.co.woolworths.financial.services.android.shoppinglist.service.network.ItemDetail
import za.co.woolworths.financial.services.android.shoppinglist.view.MoreOptionDialogFragment
import za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.request.Context
import za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.request.Device
import za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.request.Session
import za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.request.User
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.DyChangeAttribute.Repository.DyReportEventRepository
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.DyChangeAttribute.Request.PrepareChangeAttributeRequestEvent
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.DyChangeAttribute.Request.Properties
import za.co.woolworths.financial.services.android.util.AppConstant.Keys.Companion.BUNDLE_WISHLIST_EVENT_DATA
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.analytics.dto.AddToWishListFirebaseEventData
import java.lang.StringBuilder
import javax.inject.Inject

@HiltViewModel
class AddToListViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    val getMyListsUC: GetMyListsUC,
    val addProductsToList: AddToListUC,
    val addToListByOrderIdUC: AddToListByOrderIdUC,
    val createListUC: CreateNewListUC,
    val dyReportEventRepository: DyReportEventRepository
) : ViewModel() {

    private val createNewListState = mutableStateOf(CreateNewListState())
    private val listState = mutableStateOf(AddToListUiState())

    private val items: ArrayList<AddToListRequest> =
        savedStateHandle[ARG_ITEMS_TO_BE_ADDED] ?: ArrayList()
    private var mAddToWishListEventData: AddToWishListFirebaseEventData? =
        savedStateHandle[BUNDLE_WISHLIST_EVENT_DATA]

    private val copyListId: String? = savedStateHandle[MoreOptionDialogFragment.COPY_LIST_ID]

    private val _addedToListState = MutableStateFlow(emptyList<AddedToListState>())
    val addedToList: StateFlow<List<AddedToListState>> = _addedToListState.asStateFlow()

    companion object {
        const val ARG_ITEMS_TO_BE_ADDED = "ARG_ITEMS_TO_BE_ADDED"
        const val ARG_ORDER_ID = "ARG_ORDER_ID"
    }

    init {
        getMyList()
    }

    private fun getMyList() {
        viewModelScope.launch(Dispatchers.IO) {
            getMyListsUC().collect {
                viewModelScope.launch(Dispatchers.Main) {
                    val isError = it.data?.lists.isNullOrEmpty()
                    when (it.status) {
                        Status.SUCCESS -> listState.value = listState.value.copy(
                            isLoading = false,
                            isError = isError,
                            list = it.data?.lists ?: emptyList(),
                            showCreateList = it.data?.lists?.isEmpty() ?: true
                        )
                        Status.ERROR -> listState.value = listState.value.copy(
                            isLoading = false,
                            isError = true
                        )

                        Status.LOADING -> listState.value = listState.value.copy(
                            isLoading = true
                        )
                    }
                }
            }
        }
    }

    fun onEvent(event: AddToListScreenEvents) {

        when (event) {
            AddToListScreenEvents.CreateListClick -> listState.value = listState.value.copy(
                showCreateList = true
            )

            AddToListScreenEvents.RetryClick -> getMyList()
            AddToListScreenEvents.ConfirmClick -> addToList()
            is AddToListScreenEvents.OnItemClick -> onListItemClick(event.item)
            is AddToListScreenEvents.ConfirmCreateList -> createList(event.name)
            AddToListScreenEvents.CreateListBackPressed -> listState.value = listState.value.copy(
                showCreateList = false
            )

            else -> {}
        }
    }

    private fun createList(name: String) {
        if (name.isEmpty()) {
            return
        }
        val isListNamePresent = listState.value.list.any { it.listName.equals(name.trim(), ignoreCase =
        true) }

        if(isListNamePresent) {
            createNewListState.value = createNewListState.value.copy(
                isLoading = false,
                isError = true,
                errorMessage = "",
                errorMessageId = R.string.create_list_name_error
            )
            return
        }

        viewModelScope.launch(Dispatchers.IO) {

            createListUC(name.trim()).collect {
                viewModelScope.launch(Dispatchers.Main) {
                    when (it.status) {
                        Status.SUCCESS -> {
                            createNewListState.value = createNewListState.value.copy(
                                isLoading = false,
                                isSuccess = true,
                                isError = false
                            )
                            it.data?.lists?.getOrNull(0)?.let { item ->
                                listState.value = listState.value.copy(
                                    selectedListItem = listOf(item),
                                    showCreateList = false
                                )
                                addToList()
                            }
                        }

                        Status.ERROR -> createNewListState.value = createNewListState.value.copy(
                            isLoading = false,
                            isError = true,
                            errorMessage = it.data?.response?.let { errResp ->
                                return@let errResp.desc ?: errResp.message ?: ""
                            } ?: ""
                        )

                        Status.LOADING -> createNewListState.value = createNewListState.value.copy(
                            isLoading = true,
                            isError = false,
                            isSuccess = false
                        )
                    }
                }
            }
        }
    }

    private fun addToList() {
        viewModelScope.launch(Dispatchers.IO) {
            _addedToListState.update { emptyList() }
            // Specific to Order Details Page
            val orderId = savedStateHandle.get<String>(ARG_ORDER_ID) ?: ""

            if (orderId.isNotEmpty()) {
                addToListByOrderId(orderId, listState.value.selectedListItem)
                return@launch
            }

            if (items.isEmpty()) {
                return@launch
            }
            val itemList = mutableListOf<ItemDetail>()

            /*todo need to add new firebase events */

            /*created new request from existing request for multi-list api*/
            items.map {
                itemList.add(
                    ItemDetail(
                        skuID = it.skuID?: "",
                        catalogRefId = it.catalogRefId?: "",
                        quantity = "1"
                    )
                )
            }

            val giftListIds = mutableListOf<String>()
            listState.value.selectedListItem.map {
                giftListIds.add(it.listId)
            }

            val copyItemToListRequest =
                CopyItemToListRequest(items = itemList, giftListIds = giftListIds)

            async {
                addProductsToList(copyItemToListRequest).collect {
                    viewModelScope.launch(Dispatchers.Main) {
                        when (it.status) {
                            Status.SUCCESS -> {
                                _addedToListState.update { list ->
                                    val updatedList = list.toMutableList()
                                    updatedList.add(
                                        AddedToListState(
                                            isSuccess = true
                                        )
                                    )
                                    return@update updatedList
                                }
                                val isSuccess = _addedToListState.value.size ==
                                        listState.value.selectedListItem.size
                                listState.value = listState.value.copy(
                                    isAddToListInProgress = false,
                                    isAddToListSuccess = isSuccess
                                )
                                AppConfigSingleton.dynamicYieldConfig?.apply {
                                    if (isDynamicYieldEnabled == true) {
                                        items.forEach { item ->
                                            prepareDyAddToWishListRequestEvent(
                                                item.skuID,
                                                item.size
                                            )
                                        }
                                    }
                                }
                            }

                            Status.ERROR -> {
                                _addedToListState.update { list ->
                                    val updatedList = list.toMutableList()
                                    updatedList.add(
                                        AddedToListState(
                                            isSuccess = false
                                        )
                                    )
                                    return@update updatedList
                                }
                                val isSuccess = _addedToListState.value.size ==
                                        listState.value.selectedListItem.size
                                listState.value = listState.value.copy(
                                    isAddToListInProgress = false,
                                    isAddToListSuccess = isSuccess
                                )
                            }

                            Status.LOADING -> listState.value = listState.value.copy(
                                isAddToListInProgress = true
                            )
                        }
                    }
                }
            }
        }
    }

    private fun prepareDyAddToWishListViewModel(reportEventRequest: PrepareChangeAttributeRequestEvent) {
        viewModelScope.launch {
            val response = dyReportEventRepository.getDyReportEventResponse(reportEventRequest)
            if (response.status == Status.SUCCESS) {
                var value = response.data?.response?.desc
            }
        }
    }

    private fun prepareDyAddToWishListRequestEvent(skuID: String?, size: String?) {
        var dyServerId: String? = null
        var dySessionId: String? = null
        val config = NetworkConfig(AppContextProviderImpl())
        if (Utils.getDyServerId() != null) {
            dyServerId = Utils.getDyServerId()
        }
        if (Utils.getDySessionId() != null) {
            dySessionId = Utils.getDySessionId()
        }
        val user = User(dyServerId,dyServerId)
        val session = Session(dySessionId)
        val device = Device(Utils.IPAddress,config.getDeviceModel())
        val context = Context(device,null, Utils.DY_CHANNEL)
        val properties = Properties(null,null,
            Utils.ADD_TO_WISH_LIST_DY_TYPE,null,null,null,null,skuID,null,null,null,size,null,null,null,null,null,null)
        val eventsDyChangeAttribute = Event(null,null,null,null,null,null,null,null,null,null,null,null,
            Utils.ADD_TO_WISH_LIST_EVENT_NAME,properties)
        val events = mutableListOf(eventsDyChangeAttribute)
        val prepareAddToWishListRequestEvent = PrepareChangeAttributeRequestEvent(
            context,
            events,
            session,
            user
        )
        //return reportEventRequest
        prepareDyAddToWishListViewModel(prepareAddToWishListRequestEvent)

    }

    private suspend fun addToListByOrderId(
        orderId: String,
        listDetails: List<ShoppingList>
    ) {
         val listId = StringBuilder()

        listDetails.forEachIndexed {
                index, shoppingList ->
           listId.apply {
               this.append(shoppingList.listId)
               if (listDetails.lastIndex !=index) {
                   this.append(",")
               }
           }
        }

        viewModelScope.launch {
            addToListByOrderIdUC(
                orderId, OrderToShoppingListRequestBody(
                    shoppingListId = listId.toString()
                )
            ).collect {
                viewModelScope.launch(Dispatchers.Main) {
                    when (it.status) {
                        Status.SUCCESS -> {
                            _addedToListState.update { list ->
                                val updatedList = list.toMutableList()
                                updatedList.add(
                                    AddedToListState(
                                        isSuccess = true
                                    )
                                )
                                return@update updatedList
                            }
                            val isSuccess = _addedToListState.value.size ==
                                    listState.value.selectedListItem.size
                            listState.value = listState.value.copy(
                                isAddToListInProgress = false,
                                isAddToListSuccess = isSuccess
                            )

                            AppConfigSingleton.dynamicYieldConfig?.apply {
                                if (isDynamicYieldEnabled == true) {
                                    items.forEach { item ->
                                        prepareDyAddToWishListRequestEvent(item.skuID, item.size)
                                    }
                                }
                            }
                        }

                        Status.ERROR -> {
                            _addedToListState.update { list ->
                                val updatedList = list.toMutableList()
                                updatedList.add(
                                    AddedToListState(
                                        isSuccess = false
                                    )
                                )
                                return@update updatedList
                            }
                            val isSuccess = _addedToListState.value.size ==
                                    listState.value.selectedListItem.size
                            listState.value = listState.value.copy(
                                isAddToListInProgress = false,
                                isAddToListSuccess = isSuccess
                            )
                        }

                        Status.LOADING -> listState.value = listState.value.copy(
                            isAddToListInProgress = true
                        )
                    }
                }
            }
        }
    }

    private fun onListItemClick(item: ShoppingList) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedList = listState.value.selectedListItem.toMutableList()
            if (updatedList.contains(item)) {
                updatedList.remove(item)
            } else {
                updatedList.add(item)
            }
            viewModelScope.launch(Dispatchers.Main) {
                listState.value = listState.value.copy(
                    selectedListItem = updatedList
                )
            }
        }
    }

    fun getSelectedListForCopyItem():ArrayList<ShoppingList> {
        val list = ArrayList<ShoppingList>()
         getListState().selectedListItem.forEach {
            list.add(it)
        }
        return list
    }

    fun getListState(): AddToListUiState {
        return listState.value
    }

    fun getCreateNewListState(): CreateNewListState {
        return createNewListState.value
    }

    fun getAddedListItems(): List<AddToListRequest> = items.toList()

    fun getCopyListID(): String? = copyListId
    fun getItemsToBeAdded() = items
}