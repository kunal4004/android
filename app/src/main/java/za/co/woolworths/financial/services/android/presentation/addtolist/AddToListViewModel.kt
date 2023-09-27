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
import za.co.woolworths.financial.services.android.models.dao.SessionDao
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
import za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.request.Context
import za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.request.Device
import za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.request.Session
import za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.request.User
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.DyChangeAttribute.Repository.DyReportEventRepository
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.DyChangeAttribute.Request.PrepareChangeAttributeRequestEvent
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.DyChangeAttribute.Request.Properties
import za.co.woolworths.financial.services.android.util.AppConstant.Keys.Companion.BUNDLE_WISHLIST_EVENT_DATA
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.analytics.FirebaseAnalyticsEventHelper
import za.co.woolworths.financial.services.android.util.analytics.dto.AddToWishListFirebaseEventData
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
        val isListNamePresent = listState.value.list.any { it.listName.equals(name, ignoreCase =
        false) }

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
            listState.value.selectedListItem.forEach {

                mAddToWishListEventData?.let { eventData ->
                    if (it.listName.isNotEmpty()){
                        eventData.shoppingListName = it.listName
                        FirebaseAnalyticsEventHelper.addToWishlistEvent(eventData)
                    }
                }

                if (orderId.isNotEmpty()) {
                    addToListByOrderId(orderId, it)
                    return@launch
                }

                if (items.isEmpty()) {
                    return@launch
                }
                val listId = it.listId
                val skuID: String? = null
                val size: String? = null
                // If giftListId is empty pass listId as giftListId
                items.map { item -> if(item.giftListId.isNullOrEmpty()) { item.giftListId = listId } }
                items.map {item -> if (item.skuID?.isNotEmpty() == true) {item.skuID = skuID} }
                items.map {item -> if (item.size?.isNotEmpty() == true) {item.size = size} }

                async {
                    addProductsToList(listId, items.toList()).collect {
                        viewModelScope.launch(Dispatchers.Main) {
                            when (it.status) {
                                Status.SUCCESS -> {
                                    _addedToListState.update { list ->
                                        val updatedList = list.toMutableList()
                                        updatedList.add(
                                            AddedToListState(
                                                isSuccess = true,
                                                listId = listId
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
                                    prepareDyAddToWishListRequestEvent(skuID, size)
                                }

                                Status.ERROR -> {
                                    _addedToListState.update { list ->
                                        val updatedList = list.toMutableList()
                                        updatedList.add(
                                            AddedToListState(
                                                isSuccess = false,
                                                listId = listId
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
        if (Utils.getSessionDaoDyServerId(SessionDao.KEY.DY_SERVER_ID) != null) {
            dyServerId = Utils.getSessionDaoDyServerId(SessionDao.KEY.DY_SERVER_ID)
        }
        if (Utils.getSessionDaoDySessionId(SessionDao.KEY.DY_SESSION_ID) != null) {
            dySessionId = Utils.getSessionDaoDySessionId(SessionDao.KEY.DY_SESSION_ID)
        }
        val user = User(dyServerId,dyServerId)
        val session = Session(dySessionId)
        val device = Device(Utils.IPAddress,config?.getDeviceModel())
        val context = Context(device,null, Utils.DY_CHANNEL)
        val properties = Properties(null,null,
            Utils.ADD_TO_WISH_LIST_DY_TYPE,null,null,null,null,skuID,null,null,null,size,null,null,null,null,null,null)
        val eventsDyChangeAttribute = Event(null,null,null,null,null,null,null,null,null,null,null,null,
            Utils.ADD_TO_WISH_LIST_EVENT_NAME,properties)
        val events = ArrayList<Event>()
        events.add(eventsDyChangeAttribute);
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
        listDetails: ShoppingList
    ) {
        viewModelScope.launch {
            addToListByOrderIdUC(
                orderId, OrderToShoppingListRequestBody(
                    shoppingListId = listDetails.listId,
                    shoppingListName = listDetails.listName
                )
            ).collect {
                viewModelScope.launch(Dispatchers.Main) {
                    when (it.status) {
                        Status.SUCCESS -> {
                            _addedToListState.update { list ->
                                val updatedList = list.toMutableList()
                                updatedList.add(
                                    AddedToListState(
                                        isSuccess = true,
                                        listId = listDetails.listId
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

                        Status.ERROR -> {
                            _addedToListState.update { list ->
                                val updatedList = list.toMutableList()
                                updatedList.add(
                                    AddedToListState(
                                        isSuccess = false,
                                        listId = listDetails.listId
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

    fun getListState(): AddToListUiState {
        return listState.value
    }

    fun getCreateNewListState(): CreateNewListState {
        return createNewListState.value
    }

    fun getAddedListItems(): List<AddToListRequest> = items.toList()
}