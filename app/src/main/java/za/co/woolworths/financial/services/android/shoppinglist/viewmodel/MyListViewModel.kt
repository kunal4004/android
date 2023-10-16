package za.co.woolworths.financial.services.android.shoppinglist.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.awfs.coordination.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import za.co.woolworths.financial.services.android.common.ResourcesProvider
import za.co.woolworths.financial.services.android.domain.repository.MyListRepository
import za.co.woolworths.financial.services.android.domain.usecase.GetMyListsUC
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.ShoppingDeliveryLocation
import za.co.woolworths.financial.services.android.models.dto.ShoppingList
import za.co.woolworths.financial.services.android.models.dto.ShoppingListsResponse
import za.co.woolworths.financial.services.android.models.network.Status
import za.co.woolworths.financial.services.android.shoppinglist.component.EmptyStateData
import za.co.woolworths.financial.services.android.shoppinglist.component.ListDataState
import za.co.woolworths.financial.services.android.shoppinglist.component.LocationDetailsState
import za.co.woolworths.financial.services.android.shoppinglist.component.MyLIstUIEvents
import za.co.woolworths.financial.services.android.shoppinglist.service.network.ProductListDetails
import za.co.woolworths.financial.services.android.ui.extension.deviceWidth
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.mapNetworkCallToViewStateFlow
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.renderFailure
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.renderLoading
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.renderSuccess
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.Utils.convertPixelsToDp
import za.co.woolworths.financial.services.android.util.wenum.Delivery
import javax.inject.Inject


/**
 * Created by Kunal Uttarwar on 26/09/23.
 */

@HiltViewModel
class MyListViewModel @Inject constructor(
    private val resources: ResourcesProvider,
    val getMyListsUC: GetMyListsUC,
    private val myListRepository: MyListRepository,
) : ViewModel() {

    var deliveryDetailsState = mutableStateOf(LocationDetailsState())
    private var _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    val listDataState = mutableStateOf(ListDataState())
    var myListState = mutableStateOf(EmptyStateData())

    init {
        onInit()
    }

    fun onEvent(events: MyLIstUIEvents) {
        when (events) {
            is MyLIstUIEvents.SetDeliveryLocation -> setDeliveryDetails()
            is MyLIstUIEvents.ListItemRevealed -> addToRevealItems(events.item)
            is MyLIstUIEvents.ListItemCollapsed -> collapseRevealItems(events.item)
            is MyLIstUIEvents.OnSwipeDeleteAction -> {} //TODO: Implement in upcoming sprint
            is MyLIstUIEvents.SignedOutStateEvent -> showSignedOutState()
            is MyLIstUIEvents.OnNewListCreatedEvent -> getShoppingList()
            is MyLIstUIEvents.SignInClick -> {
                // Rare scenario where user logged-in in MyList screen
                onInit()
            }

            else -> Unit
        }
    }

    private fun addToRevealItems(item: ShoppingList) {
        viewModelScope.launch {
            withContext(Dispatchers.Default){
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
            withContext(Dispatchers.Default){
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
                        }

                        Status.ERROR -> {
                            listDataState.value = listDataState.value.copy(
                                isError = true
                            )
                            _isLoading.value = false
                        }

                        Status.LOADING -> {
                            _isLoading.value = true
                        }
                    }
                }
            }
        }
    }

    private fun setListData(shoppingListResponse: ShoppingListsResponse?) {
        listDataState.value = listDataState.value.copy(
            list = shoppingListResponse?.lists?.let { getUpdatedList(it) } ?: emptyList()
        )
    }

    private fun getUpdatedList(list: List<ShoppingList>): List<ShoppingList> {
        val imageCountInRow = getProductCount()
        list.mapIndexed { index, shoppingList ->
            shoppingList.modifiedListCount = "(" + shoppingList.listCount + ")"
            shoppingList.noOfProductInRow = imageCountInRow
            shoppingList.productImageList = getImageListData(index, shoppingList)
        }
        return list
    }

    private fun getImageListData(
        index: Int,
        shoppingList: ShoppingList,
    ): ArrayList<ProductListDetails> {
        // todo Once we receive API response we will remove this function.
        val mockListDetails = ArrayList<ProductListDetails>()
        val productListDetails = ProductListDetails().apply {
            imgUrl = when (index) {
                0 -> "https://assets.woolworthsstatic.co.za/Split-Neck-Cropped-Tencel-Shirt-BLACK-506262324-hero.jpg?V=ab0h&o=eyJidWNrZXQiOiJ3dy1vbmxpbmUtaW1hZ2UtcmVzaXplIiwia2V5IjoiaW1hZ2VzL2VsYXN0aWNlcmEvcHJvZHVjdHMvaGVyby8yMDIyLTEwLTE3LzUwNjI2MjMyNF9CTEFDS19oZXJvLmpwZyJ9&"

                1 -> "https://assets.woolworthsstatic.co.za/Mini-Oat-Crunchies-150-g-6009223195009.jpg?V=buKH&o=eyJidWNrZXQiOiJ3dy1vbmxpbmUtaW1hZ2UtcmVzaXplIiwia2V5IjoiaW1hZ2VzL2VsYXN0aWNlcmEvcHJvZHVjdHMvaGVyby8yMDIxLTA2LTAyLzYwMDkyMjMxOTUwMDlfaGVyby5qcGcifQ&"

                2 -> "https://assets.woolworthsstatic.co.za/Mini-Chocolate-Digestives-30-g-6009189506246.jpg?V=fur0&o=eyJidWNrZXQiOiJ3dy1vbmxpbmUtaW1hZ2UtcmVzaXplIiwia2V5IjoiaW1hZ2VzL2VsYXN0aWNlcmEvcHJvZHVjdHMvaGVyby8yMDIxLTA2LTI0LzYwMDkxODk1MDYyNDZfaGVyby5qcGcifQ&"

                3 -> "https://assets.woolworthsstatic.co.za/Frill-Balloon-Sleeve-Blouse-BLACK-506629130-hero.jpg?V=raxB&o=eyJidWNrZXQiOiJ3dy1vbmxpbmUtaW1hZ2UtcmVzaXplIiwia2V5IjoiaW1hZ2VzL2VsYXN0aWNlcmEvcHJvZHVjdHMvaGVyby8yMDIzLTA1LTA4LzUwNjYyOTEzMF9CTEFDS19oZXJvLmpwZyJ9&"

                4 -> "https://assets.woolworthsstatic.co.za/Easy-Care-Check-Shirt-NATURAL-506536382.jpg?V=7SRx&o=eyJidWNrZXQiOiJ3dy1vbmxpbmUtaW1hZ2UtcmVzaXplIiwia2V5IjoiaW1hZ2VzL2VsYXN0aWNlcmEvcHJvZHVjdHMvaGVyby8yMDIzLTAyLTIxLzUwNjUzNjM4Ml9OQVRVUkFMX2hlcm8uanBnIn0&"

                else -> ""
            }
        }

        if (shoppingList.listCount > 0) {
            for (i in 0..shoppingList.listCount) {
                mockListDetails.add(productListDetails)
            }
        }
        return mockListDetails
    }

    private fun getProductCount(): Int {
        val deviceWidthInDp = convertPixelsToDp(deviceWidth())
        val usableDeviceWidth = (deviceWidthInDp - 60) // 60 is the left and right margin
        return (usableDeviceWidth / 54) // 54 is the width of productImage
    }
}