package za.co.woolworths.financial.services.android.shoppinglist.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.awfs.coordination.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import za.co.woolworths.financial.services.android.common.ResourcesProvider
import za.co.woolworths.financial.services.android.domain.repository.MyListRepository
import za.co.woolworths.financial.services.android.domain.usecase.DeleteShoppingListUC
import za.co.woolworths.financial.services.android.domain.usecase.GetMyListsUC
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.ShoppingDeliveryLocation
import za.co.woolworths.financial.services.android.models.dto.ShoppingList
import za.co.woolworths.financial.services.android.models.dto.ShoppingListsResponse
import za.co.woolworths.financial.services.android.models.network.Status
import za.co.woolworths.financial.services.android.shoppinglist.component.AppbarUiState
import za.co.woolworths.financial.services.android.shoppinglist.component.EmptyStateData
import za.co.woolworths.financial.services.android.shoppinglist.component.ListDataState
import za.co.woolworths.financial.services.android.shoppinglist.component.LocationDetailsState
import za.co.woolworths.financial.services.android.shoppinglist.component.MyLIstUIEvents
import za.co.woolworths.financial.services.android.shoppinglist.component.MyListScreenEvents
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.mapNetworkCallToViewStateFlow
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.renderFailure
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.renderLoading
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.renderSuccess
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.wenum.Delivery
import javax.inject.Inject
import kotlin.math.roundToInt


/**
 * Created by Kunal Uttarwar on 26/09/23.
 */

@HiltViewModel
class MyListViewModel @Inject constructor(
    private val resources: ResourcesProvider,
    val getMyListsUC: GetMyListsUC,
    val deleteShoppingListUC: DeleteShoppingListUC,
    private val myListRepository: MyListRepository,
) : ViewModel() {

    private var _onScreenEvents: MutableStateFlow<MyListScreenEvents> =
        MutableStateFlow(MyListScreenEvents.None)
    val onScreenEvents: StateFlow<MyListScreenEvents> = _onScreenEvents.asStateFlow()

    private var isCheckedDontAskAgain: Boolean = false
    private var isClickedOnSharedList: Boolean = false
    var deliveryDetailsState = mutableStateOf(LocationDetailsState())
    private var _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    val listDataState = mutableStateOf(ListDataState())
    var myListState = mutableStateOf(EmptyStateData())
    var appBarUIState = mutableStateOf(AppbarUiState())

    init {
        onInit()
    }

    fun onEvent(events: MyLIstUIEvents) {
        when (events) {
            is MyLIstUIEvents.SetDeliveryLocation -> setDeliveryDetails()
            is MyLIstUIEvents.ListItemRevealed -> addToRevealItems(events.item)
            is MyLIstUIEvents.ListItemCollapsed -> collapseRevealItems(events.item)
            is MyLIstUIEvents.OnDeleteListConfirm -> deleteShoppingList(
                events.item,
                events.position
            )

            is MyLIstUIEvents.SignedOutStateEvent -> showSignedOutState()
            is MyLIstUIEvents.OnNewListCreatedEvent -> getShoppingList()
            is MyLIstUIEvents.SignInClick -> {
                // Rare scenario where user logged-in in MyList screen
                onInit()
            }

            is MyLIstUIEvents.OnRefreshEvent -> {
                onInit()
            }

            is MyLIstUIEvents.OnToolbarEditClick -> {
                if (listDataState.value.isEditMode) {
                    onDoneButtonClick()
                } else {
                    onEditButtonClick()
                }
            }

            else -> Unit
        }
    }

    private fun onDoneButtonClick() {
        viewModelScope.launch(Dispatchers.Default) {
            appBarUIState.value = appBarUIState.value.copy(
                rightButtonRes = R.string.edit
            )
            listDataState.value = listDataState.value.copy(
                isEditMode = false
            )
        }
    }

    private fun onEditButtonClick() {
        viewModelScope.launch(Dispatchers.Default) {
            appBarUIState.value = appBarUIState.value.copy(
                rightButtonRes = R.string.done
            )
            listDataState.value = listDataState.value.copy(
                isEditMode = true
            )
        }
    }

    private fun deleteShoppingList(item: ShoppingList, position: Int) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                deleteShoppingListUC(item.listId).collectLatest { shoppingListResponse ->
                    withContext(Dispatchers.Default) {
                        when (shoppingListResponse.status) {
                            Status.SUCCESS -> {
                                if (!isClickedOnShareLists()) {
                                    val updatedList = listDataState.value.list.toMutableList()
                                    updatedList.remove(item)
                                    listDataState.value = listDataState.value.copy(
                                        list = updatedList
                                    )
                                } else {
                                    val updatedList = listDataState.value.shareList.toMutableList()
                                    updatedList.remove(item)
                                    listDataState.value = listDataState.value.copy(
                                        shareList = updatedList
                                    )
                                }
                                _onScreenEvents.emit(
                                    MyListScreenEvents.DismissDialog(
                                        true, item.listName
                                    )
                                )
                            }

                            Status.ERROR -> {
                                _onScreenEvents.emit(
                                    MyListScreenEvents.DismissDialog(
                                        false, item.listName
                                    )
                                )
                            }

                            Status.LOADING -> {}
                        }
                    }
                }
            }
        }
    }

    private fun addToRevealItems(item: ShoppingList) {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                val newList = listDataState.value.revealedList.toMutableList()
                newList.add(item.listId)
                listDataState.value = listDataState.value.copy(
                    revealedList = newList
                )
            }
        }
    }

    private fun collapseRevealItems(item: ShoppingList) {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                val newList = listDataState.value.revealedList.toMutableList()
                newList.remove(item.listId)
                listDataState.value = listDataState.value.copy(
                    revealedList = newList
                )
            }
        }
    }

    private fun setDeliveryDetails() {
        // update the data class of location details to show location details on UI.
        Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.let {
            deliveryDetailsState.value = when (Delivery.getType(it?.deliveryType)) {
                Delivery.CNC -> {
                    deliveryDetailsState.value.copy(
                        icon = R.drawable.ic_collection_circle,
                        deliveryType = resources.getString(R.string.collecting_from),
                        deliveryLocation = it?.storeName ?: ""
                    )
                }

                Delivery.DASH -> {
                    var timeSlot: String? =
                        WoolworthsApplication.getValidatePlaceDetails()?.onDemand?.firstAvailableFoodDeliveryTime
                    timeSlot =
                        if (timeSlot?.isNullOrEmpty() == true || WoolworthsApplication.getValidatePlaceDetails()?.onDemand?.deliveryTimeSlots?.isNullOrEmpty() == true) {
                            resources?.getString(R.string.dash_delivery_bold) + "\t" + resources?.getString(
                                R.string.no_timeslots_available_title
                            )
                        } else {
                            resources?.getString(R.string.dash_delivery_bold)
                                .plus("\t" + timeSlot)
                        }


                    deliveryDetailsState.value.copy(
                        icon = R.drawable.ic_dash_delivery_circle,
                        deliveryType = timeSlot,
                        deliveryLocation = WoolworthsApplication.getValidatePlaceDetails()?.placeDetails?.address1
                            ?: ""
                    )
                }

                else -> {
                    deliveryDetailsState.value.copy(
                        icon = R.drawable.ic_delivery_circle,
                        deliveryType = resources.getString(R.string.standard_delivery),
                        deliveryLocation = it?.address?.address1 ?: ""
                    )
                }
            }
        }
    }

    private fun showSignedOutState() {
        myListState.value = myListState.value.copy(
            isSignedOut = true,
            icon = R.drawable.ic_shopping_list_sign_out,
            title = R.string.shop_sign_out_order_title,
            description = R.string.shop_sign_out_order_desc,
            buttonText = R.string.sign_in
        )
    }
     fun updateEmptyScreenForList() {
        myListState.value = myListState.value.copy(
            icon = R.drawable.empty_list_icon,
            title = R.string.title_no_shopping_lists,
            description = R.string.description_no_shopping_lists,
            isButtonVisible = true
        )
    }

    fun updateEmptyScreenForSharedList() {
        myListState.value = myListState.value.copy(
            icon = R.drawable.empty_list_icon,
            title = R.string.share_list_empty_title,
            description = R.string.share_list_empty_desc,
            isButtonVisible = false
        )
    }

    private fun onInit() {
        if (SessionUtilities.getInstance().isUserAuthenticated) {
            if (Utils.getPreferredDeliveryLocation() == null) {
                viewModelScope.launch {
                    // It's mostly a new user who don't have location.
                    getCartSummary()
                }
            } else if (WoolworthsApplication.getValidatePlaceDetails() == null && isDashDelivery()) {
                // call validate Place API only in case of Dash Delivery as we need timeslots.
                viewModelScope.launch {
                    val placeId =
                        Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.address?.placeId
                    if (!placeId.isNullOrEmpty())
                        callValidatePlaceDetails(placeId)
                }
            }
            getShoppingList()
        }
    }

    private fun isDashDelivery(): Boolean {
        val deliveryType = Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.deliveryType
        return if (deliveryType != null) {
            Delivery.DASH == Delivery.getType(deliveryType)
        } else false
    }

    suspend fun getCartSummary() =
        viewModelScope.launch {
            mapNetworkCallToViewStateFlow {
                myListRepository.getCartSummary()
            }.collectLatest { cartSummaryResponse ->
                with(cartSummaryResponse) {
                    renderSuccess {
                        output?.data?.getOrNull(0)?.fulfillmentDetails?.apply {
                            this.deliveryType?.let {
                                Utils.savePreferredDeliveryLocation(ShoppingDeliveryLocation(this))
                                setDeliveryDetails()
                            }
                        }
                    }
                    renderLoading {
                        _isLoading.value = this.isLoading
                    }
                    renderFailure {
                        setDeliveryDetails()
                    }
                }
            }
        }

    private suspend fun callValidatePlaceDetails(placeId: String) =
        viewModelScope.launch {
            mapNetworkCallToViewStateFlow {
                myListRepository.callValidatePlaceDetails(placeId)
            }.collectLatest { validateLocationResponse ->
                with(validateLocationResponse) {
                    renderSuccess {
                        if (WoolworthsApplication.getCncBrowsingValidatePlaceDetails() == null) {
                            WoolworthsApplication.setCncBrowsingValidatePlaceDetails(
                                output?.validatePlace
                            )
                        }
                        WoolworthsApplication.setValidatedSuburbProducts(
                            output?.validatePlace
                        )
                        setDeliveryDetails()
                    }
                    renderLoading {
                        _isLoading.value = this.isLoading
                    }
                }
            }
        }

    private fun getShoppingList() {
        viewModelScope.launch(Dispatchers.IO) {
            getMyListsUC().collectLatest { shoppingListResponse ->
                viewModelScope.launch(Dispatchers.Main) {
                    when (shoppingListResponse.status) {
                        Status.SUCCESS -> {
                            listDataState.value = listDataState.value.copy(
                                isSuccessResponse = true
                            )
                            setListData(shoppingListResponse.data)
                            _isLoading.value = false
                            appBarUIState.value = appBarUIState.value.copy(
                                showRightButton = listDataState.value.list.isNotEmpty()
                            )
                        }

                        Status.ERROR -> {
                            listDataState.value = listDataState.value.copy(
                                isError = true
                            )
                            _isLoading.value = false
                            appBarUIState.value = appBarUIState.value.copy(
                                showRightButton = false
                            )
                        }

                        Status.LOADING -> {
                            _isLoading.value = true
                            appBarUIState.value = appBarUIState.value.copy(
                                showRightButton = false
                            )
                        }
                    }
                }
            }
        }
    }

    private fun setListData(shoppingListResponse: ShoppingListsResponse?) {
        if (isClickedOnSharedList) {
            listDataState.value = listDataState.value.copy(
                shareList = shoppingListResponse?.sharedLists?.let { getUpdatedList(it) } ?: emptyList()
            )
        } else {
            listDataState.value = listDataState.value.copy(
                list = shoppingListResponse?.lists?.let { getUpdatedList(it) } ?: emptyList(),
                shareList = shoppingListResponse?.sharedLists?.let { getUpdatedList(it) }?: emptyList()
            )
        }
    }

    private fun getUpdatedList(list: List<ShoppingList>): List<ShoppingList> {
        list.mapIndexed { index, shoppingList ->
            shoppingList.modifiedListCount = "(" + shoppingList.listCount + ")"
        }
        return list
    }

    fun setIsCheckedDontAskAgain(checkedDontAskAgain: Boolean) {
        isCheckedDontAskAgain = checkedDontAskAgain
    }

    fun isCheckedDontAskAgain() = isCheckedDontAskAgain

    fun setIsClickedOnShareLists(isClickedOnSharedListOption: Boolean) {
        isClickedOnSharedList = isClickedOnSharedListOption
    }

    fun isClickedOnShareLists() = isClickedOnSharedList
}