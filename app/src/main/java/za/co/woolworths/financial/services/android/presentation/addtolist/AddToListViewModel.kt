package za.co.woolworths.financial.services.android.presentation.addtolist

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import za.co.woolworths.financial.services.android.models.dto.AddToListRequest
import za.co.woolworths.financial.services.android.models.dto.OrderToShoppingListRequestBody
import za.co.woolworths.financial.services.android.models.dto.ShoppingList
import za.co.woolworths.financial.services.android.models.network.Status
import za.co.woolworths.financial.services.android.presentation.addtolist.components.AddToListScreenEvents
import za.co.woolworths.financial.services.android.presentation.addtolist.components.AddToListUiState
import za.co.woolworths.financial.services.android.presentation.addtolist.components.AddedToListState
import za.co.woolworths.financial.services.android.presentation.addtolist.components.CreateNewListState
import za.co.woolworths.financial.services.android.util.AppConstant.Keys.Companion.BUNDLE_WISHLIST_EVENT_DATA
import za.co.woolworths.financial.services.android.util.analytics.FirebaseAnalyticsEventHelper
import za.co.woolworths.financial.services.android.util.analytics.dto.AddToWishListFirebaseEventData
import javax.inject.Inject

@HiltViewModel
class AddToListViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    val getMyListsUC: GetMyListsUC,
    val addProductsToList: AddToListUC,
    val addToListByOrderIdUC: AddToListByOrderIdUC,
    val createListUC: CreateNewListUC
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
                            list = it.data?.lists ?: emptyList()
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
        viewModelScope.launch(Dispatchers.IO) {
            if (name.isEmpty()) {
                return@launch
            }

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