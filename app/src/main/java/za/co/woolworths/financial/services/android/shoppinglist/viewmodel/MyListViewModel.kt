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
import za.co.woolworths.financial.services.android.common.ResourcesProvider
import za.co.woolworths.financial.services.android.domain.repository.MyListRepository
import za.co.woolworths.financial.services.android.domain.usecase.GetMyListsUC
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.ShoppingDeliveryLocation
import za.co.woolworths.financial.services.android.models.dto.ShoppingList
import za.co.woolworths.financial.services.android.models.dto.ShoppingListsResponse
import za.co.woolworths.financial.services.android.models.network.Status
import za.co.woolworths.financial.services.android.shoppinglist.component.ListDataState
import za.co.woolworths.financial.services.android.shoppinglist.component.LocationDetailsState
import za.co.woolworths.financial.services.android.shoppinglist.component.MyLIstUIEvents
import za.co.woolworths.financial.services.android.shoppinglist.service.network.ProductListDetails
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.mapNetworkCallToViewStateFlow
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.renderFailure
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.renderLoading
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.renderSuccess
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.Utils
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

    init {
        onInit()
    }

    fun onEvent(events: MyLIstUIEvents) {
        when (events) {
            is MyLIstUIEvents.SetDeliveryLocation -> setDeliveryDetails()
            else -> Unit
        }
    }

    private fun setDeliveryDetails() {
        // update the data class of location details to show location details on UI.
        Utils.getPreferredDeliveryLocation().fulfillmentDetails?.let {
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
            getMyListsUC().collect { shoppingListResponse ->
                viewModelScope.launch(Dispatchers.Main) {
                    when (shoppingListResponse.status) {
                        Status.SUCCESS -> {
                            _isLoading.value = false
                            setListData(shoppingListResponse.data)
                        }

                        Status.ERROR -> {
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
        list.mapIndexed { index, shoppingList ->
            shoppingList.modifiedListCount = "(" + shoppingList.listCount + ")"
            shoppingList.productImageList = getImageListData(index)
        }
        return list
    }

    private fun getImageListData(index: Int): ArrayList<ProductListDetails> {
        var mockListDetails = ArrayList<ProductListDetails>()
        when (index) {
            0 -> {
                val productListDetails = ProductListDetails().apply {
                    imgUrl =
                        "https://assets.woolworthsstatic.co.za/Mini-Ginger-Cookies-30-g-6009182707657.jpg?V=kb1C&o=eyJidWNrZXQiOiJ3dy1vbmxpbmUtaW1hZ2UtcmVzaXplIiwia2V5IjoiaW1hZ2VzL2VsYXN0aWNlcmEvcHJvZHVjdHMvaGVyby8yMDE4LTEwLTExLzYwMDkxODI3MDc2NTdfaGVyby5qcGcifQ&"
                }
                mockListDetails.add(productListDetails)
                mockListDetails.add(productListDetails)
                mockListDetails.add(productListDetails)
                mockListDetails.add(productListDetails)
                mockListDetails.add(productListDetails)

            }

            1 -> {
                val productListDetails = ProductListDetails().apply {
                    imgUrl =
                        "https://assets.woolworthsstatic.co.za/Mini-Oat-Crunchies-150-g-6009223195009.jpg?V=buKH&o=eyJidWNrZXQiOiJ3dy1vbmxpbmUtaW1hZ2UtcmVzaXplIiwia2V5IjoiaW1hZ2VzL2VsYXN0aWNlcmEvcHJvZHVjdHMvaGVyby8yMDIxLTA2LTAyLzYwMDkyMjMxOTUwMDlfaGVyby5qcGcifQ&"
                }
                mockListDetails.add(productListDetails)
                mockListDetails.add(productListDetails)
                mockListDetails.add(productListDetails)
                mockListDetails.add(productListDetails)
                mockListDetails.add(productListDetails)
                mockListDetails.add(productListDetails)
                mockListDetails.add(productListDetails)
                mockListDetails.add(productListDetails)
                mockListDetails.add(productListDetails)
                mockListDetails.add(productListDetails)
                mockListDetails.add(productListDetails)
                mockListDetails.add(productListDetails)
                mockListDetails.add(productListDetails)
                mockListDetails.add(productListDetails)
                mockListDetails.add(productListDetails)
            }

            2 -> {
                val productListDetails = ProductListDetails().apply {
                    imgUrl =
                        "https://assets.woolworthsstatic.co.za/Mini-Chocolate-Digestives-30-g-6009189506246.jpg?V=fur0&o=eyJidWNrZXQiOiJ3dy1vbmxpbmUtaW1hZ2UtcmVzaXplIiwia2V5IjoiaW1hZ2VzL2VsYXN0aWNlcmEvcHJvZHVjdHMvaGVyby8yMDIxLTA2LTI0LzYwMDkxODk1MDYyNDZfaGVyby5qcGcifQ&"
                }
                mockListDetails.add(productListDetails)
                mockListDetails.add(productListDetails)
                mockListDetails.add(productListDetails)
                mockListDetails.add(productListDetails)
                mockListDetails.add(productListDetails)
                mockListDetails.add(productListDetails)
                mockListDetails.add(productListDetails)
                mockListDetails.add(productListDetails)
                mockListDetails.add(productListDetails)
            }

            3 -> {
                val productListDetails = ProductListDetails().apply {
                    imgUrl =
                        "https://assets.woolworthsstatic.co.za/Yoghurt-Digestives-200-g-6009175106443.jpg?V=Xzz6&o=eyJidWNrZXQiOiJ3dy1vbmxpbmUtaW1hZ2UtcmVzaXplIiwia2V5IjoiaW1hZ2VzL2VsYXN0aWNlcmEvcHJvZHVjdHMvaGVyby8yMDIxLTA2LTI0LzYwMDkxNzUxMDY0NDNfaGVyby5qcGcifQ&"
                }
                mockListDetails.add(productListDetails)
                mockListDetails.add(productListDetails)
                mockListDetails.add(productListDetails)
                mockListDetails.add(productListDetails)
                mockListDetails.add(productListDetails)
                mockListDetails.add(productListDetails)
            }

            else -> {
                val productListDetails = ProductListDetails().apply {
                    imgUrl = ""
                }
                mockListDetails.add(productListDetails)
                mockListDetails.add(productListDetails)
                mockListDetails.add(productListDetails)
            }
        }

        return mockListDetails
    }
}