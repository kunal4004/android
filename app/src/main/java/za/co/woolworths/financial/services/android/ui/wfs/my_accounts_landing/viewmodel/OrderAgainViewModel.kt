package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.awfs.coordination.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.domain.usecase.AddToCartUC
import za.co.woolworths.financial.services.android.domain.usecase.MultiSkuInventoryUC
import za.co.woolworths.financial.services.android.domain.usecase.OrderAgainUC
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.SkuInventory
import za.co.woolworths.financial.services.android.models.dto.order_again.Item
import za.co.woolworths.financial.services.android.models.dto.order_again.OrderAgainResponse
import za.co.woolworths.financial.services.android.models.dto.order_again.ProductItem
import za.co.woolworths.financial.services.android.models.dto.order_again.toAddItemToCart
import za.co.woolworths.financial.services.android.models.dto.order_again.toProductItem
import za.co.woolworths.financial.services.android.models.network.Status
import za.co.woolworths.financial.services.android.presentation.common.delivery_location.DeliveryLocationViewState
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_order_again.ui.schema.OrderAgainScreenEvents
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_order_again.ui.schema.OrderAgainScreenState
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_order_again.ui.schema.OrderAgainUiState
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.capitaliseFirstLetterInEveryWord
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import za.co.woolworths.financial.services.android.util.wenum.Delivery
import javax.inject.Inject


@HiltViewModel
class OrderAgainViewModel @Inject constructor(
    val orderAgainUC: OrderAgainUC,
    val orderAgainInventoryUC: MultiSkuInventoryUC,
    val addToCartUC: AddToCartUC
) : ViewModel() {

    private var _orderAgainUiState = MutableStateFlow(OrderAgainUiState())
    val orderAgainUiState = _orderAgainUiState.asStateFlow()

    private var _onScreenEvent: MutableStateFlow<OrderAgainScreenEvents> = MutableStateFlow(OrderAgainScreenEvents.Idle)
    val onScreenEvent = _onScreenEvent.asStateFlow()

    init {
        setDeliveryLocation()
        callOrderAgainApi()
    }

    fun onEvent(events: OrderAgainScreenEvents) {
        when (events) {
            is OrderAgainScreenEvents.ProductItemCheckedChange -> {
                onProductCheckedChange(events.isChecked, events.productItem)
            }

            is OrderAgainScreenEvents.ChangeProductQuantityBy -> {
                onChangeProductQuantity(events.count, events.item)
            }

            OrderAgainScreenEvents.AddToCartClicked -> onAddToCartClicked()
            is OrderAgainScreenEvents.ListItemRevealed -> addToRevealItems(events.item)
            is OrderAgainScreenEvents.ListItemCollapsed -> collapseRevealItems(events.item)

            OrderAgainScreenEvents.SelectAllClick -> {
                onSelectAllClick()
            }

            else -> {}
        }
    }

    private fun onAddToCartClicked() {
        viewModelScope.launch {
            val items = _orderAgainUiState.value.orderList
                .filter { it.isSelected }
                .map { it.toAddItemToCart() }

            addToCartUC(items).collectLatest {
                when (it.status) {
                    Status.SUCCESS -> {
                        val productCountMap = it.data?.data?.getOrNull(0)?.productCountMap
                        _orderAgainUiState.update  { state ->
                            val orderList = state.orderList.toMutableList()
                            orderList.filter { item -> item.isSelected }.map { item ->
                                item.isSelected = false
                                item.quantity = 1
                            }
                            state.copy(
                                orderList = orderList,
                                showAddToCart = false,
                                maxItemLimit = productCountMap?.quantityLimit?.foodMaximumQuantity ?: 0
                            )
                        }
                        _onScreenEvent.update {
                            OrderAgainScreenEvents.HideBottomBar(false)
                        }
                        delay(500L)
                        _onScreenEvent.update {
                            OrderAgainScreenEvents.ShowSnackBar(
                                count = _orderAgainUiState.value.itemsToBeAddedCount,
                                maxItemLimit = productCountMap?.quantityLimit?.foodMaximumQuantity ?: 0
                            )
                        }
                    }
                    Status.ERROR -> {}
                    Status.LOADING -> {

                    }
                }
            }
        }
    }

    private fun onSelectAllClick() {
        viewModelScope.launch(Dispatchers.Default) {

            _onScreenEvent.update {
                OrderAgainScreenEvents.HideBottomBar(
                    _orderAgainUiState.value.headerState.rightButtonRes == R.string.select_all
                )
            }

            _orderAgainUiState.update {
                val updatedList = it.orderList.toMutableList()
                var count = 0
                updatedList.map { item ->
                    if (item.quantityInStock > 0) {
                        item.quantity =
                            item.quantity.coerceAtLeast(1).coerceAtMost(item.quantityInStock)
                        item.isSelected = it.headerState.rightButtonRes == R.string.select_all
                        // Calculate items count
                        if (item.isSelected) {
                            count = count.plus(item.quantity)
                        }
                    }
                }

                it.copy(
                    orderList = updatedList,
                    headerState = it.headerState.copy(
                        rightButtonRes = if (it.headerState.rightButtonRes == R.string.select_all)
                            R.string.deselect_all
                        else R.string.select_all
                    ),
                    showAddToCart = updatedList.any { item -> item.isSelected },
                    itemsToBeAddedCount = count
                )
            }
        }
    }

    private fun addToRevealItems(item: ProductItem) {
        viewModelScope.launch {
            _orderAgainUiState.update {
                val newList = it.revealedList.toMutableList()
                newList.add(item.id)
                it.copy(revealedList = newList)
            }
        }
    }

    private fun collapseRevealItems(item: ProductItem) {
        viewModelScope.launch {
            _orderAgainUiState.update {
                val newList = it.revealedList.toMutableList()
                newList.remove(item.id)
                it.copy(revealedList = newList)
            }
        }
    }


    private fun onChangeProductQuantity(count: Int, productItem: ProductItem) {
        viewModelScope.launch {
            _orderAgainUiState.update {
                val updatedList = it.orderList.toMutableList()
                updatedList.find { item -> item.id == productItem.id }?.let { item ->
                    if (item.id == productItem.id) {
                        item.quantity =
                            (item.quantity + count).coerceAtLeast(1)
                                .coerceAtMost(item.quantityInStock)
                    }
                }
                it.copy(orderList = updatedList)
            }
            updateAddToListItemCount()
        }
    }

    private fun updateAddToListItemCount() {
        viewModelScope.launch {
            _orderAgainUiState.update {
                val totalItemsCount = it.orderList.filter { item -> item.isSelected }
                    .sumOf { item -> item.quantity }
                it.copy(
                    itemsToBeAddedCount = totalItemsCount
                )
            }
        }
    }

    fun setDeliveryLocation() {
        viewModelScope.launch {
            Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.apply {
                var textDeliveryLocation = ""
                val deliveryTypeResource: Int = when (Delivery.getType(deliveryType)) {
                    Delivery.STANDARD -> {

//                        val fullAddress = KotlinUtils.capitaliseFirstLetter(address?.address1 ?: "")
//
//                        val formmmatedNickName = KotlinUtils.getFormattedNickName(
//                            address?.nickname,
//                            fullAddress, context
//                        )
//
//                        formmmatedNickName.append(fullAddress)
                        textDeliveryLocation = address?.nickname?.plus(address.address1) ?: ""
                        R.string.standard_delivery
                    }

                    Delivery.CNC -> {
                        textDeliveryLocation = storeName ?: ""
                        R.string.click_and_collect
                    }

                    Delivery.DASH -> {
                        textDeliveryLocation =
                            WoolworthsApplication.getValidatePlaceDetails()?.placeDetails?.address1
                                ?: address?.address1 ?: ""
                        R.string.dash_delivery
                    }

                    null -> {
                        R.string.standard_delivery
                    }
                }
                _orderAgainUiState.update {
                    it.copy(
                        deliveryState = DeliveryLocationViewState(
                            resDeliveryType = deliveryTypeResource,
                            textDeliveryLocation = textDeliveryLocation.capitaliseFirstLetterInEveryWord()
                        )
                    )
                }
            }
        }
    }

    private fun callOrderAgainApi() {
        viewModelScope.launch {
            val plistId = KotlinUtils.extractPlistFromDeliveryDetails() ?: ""
            if (plistId.isEmpty()) {
                FirebaseManager.logException(Exception("Invalid plistId on Order Again Api."))
                return@launch
            }

            orderAgainUC(plistId).collectLatest {
                _orderAgainUiState.update { state ->
                    when (it.status) {
                        Status.SUCCESS -> {

                            // Get all product Ids for inventory call.
                            val productIds = getProductIds(it.data)

                            // If no food product available in response show empty screen.
                            if (productIds.isEmpty()) {
                                state.copy(screenState = OrderAgainScreenState.ShowEmptyScreen)
                            } else {
                                // get all product ids and make inventory call
                                callInventoryApi(productIds)
                                state.copy(screenState = OrderAgainScreenState.Loading)
                            }
                        }

                        Status.ERROR -> state.copy(screenState = OrderAgainScreenState.ShowErrorScreen)

                        Status.LOADING -> state.copy(screenState = OrderAgainScreenState.Loading)
                    }
                }
            }
        }
    }

    private fun getProductIds(response: OrderAgainResponse?): List<String> {
        val items = getOrderList(response)
        val updatedList = items.map { it.toProductItem() }
        _orderAgainUiState.update {
            it.copy(
                orderList = updatedList
            )
        }
        return items.mapNotNull { item -> item.id }
    }

    private fun getOrderList(response: OrderAgainResponse?): List<Item> {
        return response?.data?.responses?.getOrNull(0)
            ?.actions?.getOrNull(0)
            ?.items ?: emptyList()
    }

    private fun callInventoryApi(productIds: List<String>) {
        viewModelScope.launch {
            val skuIds = productIds.joinToString("-")
            // Fetching store id for fulfillment type 01 since all are food items
            val storeId = Utils.retrieveStoreId("01")
            // If store id is not found meaning all product are unavailable.
            if (storeId.isNullOrEmpty()) {
                _orderAgainUiState.value = _orderAgainUiState.value.copy(
                    screenState = OrderAgainScreenState.ShowErrorScreen
                )
                return@launch
            }

            orderAgainInventoryUC(storeId, skuIds).collectLatest {
                _orderAgainUiState.value = when (it.status) {
                    Status.SUCCESS -> {
                        updateInventoryStock(it.data?.skuInventory)
                        orderAgainUiState.value.copy(
                            screenState = OrderAgainScreenState.ShowOrderList
                        )
                    }

                    Status.ERROR -> orderAgainUiState.value.copy(
                        screenState = OrderAgainScreenState.ShowErrorScreen
                    )

                    Status.LOADING -> orderAgainUiState.value.copy(
                        screenState = OrderAgainScreenState.Loading
                    )
                }
            }
        }
    }

    private fun updateInventoryStock(skuInventory: List<SkuInventory>?) {
        viewModelScope.launch(Dispatchers.Default) {
            _orderAgainUiState.update {
                val updatedList = it.orderList.toMutableList()
                var count = 0
                updatedList.map { productItem ->
                    val availableQuantity = getQuantity(productItem.id, skuInventory)
                    productItem.quantityInStock = availableQuantity
                    productItem.isSelected =
                        if (availableQuantity <= 0) false else productItem.isSelected
                    productItem.quantity = when {
                        productItem.quantity > availableQuantity -> availableQuantity
                        availableQuantity > productItem.quantity -> productItem.quantity
                        else -> 1
                    }.coerceAtLeast(1).coerceAtMost(productItem.quantityInStock)

                    if (productItem.isSelected) {
                        count = count.plus(productItem.quantity)
                    }

                    val delivery =
                        Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.deliveryType
                    val deliveryType = Delivery.getType(delivery)
                    productItem.productAvailabilityResource = when (availableQuantity) {
                        -1 -> when (deliveryType) {
                            Delivery.CNC -> R.string.unavailable_with_collection
                            Delivery.DASH -> R.string.unavailable_with_dash
                            else -> R.string.out_of_stock
                        }

                        0 -> R.string.out_of_stock
                        else -> R.string.empty
                    }
                }
                val isAnyUnselected = isAnyUnselected()
                it.copy(
                    orderList = updatedList,
                    showAddToCart = updatedList.any { item -> item.isSelected },
                    itemsToBeAddedCount = count,
                    headerState = it.headerState.copy(
                        rightButtonRes = if (isAnyUnselected) R.string.select_all else R.string.deselect_all
                    )
                )
            }
        }
    }

    private fun isAnyUnselected(): Boolean = orderAgainUiState.value.orderList.any{
        !it.isSelected && it.quantityInStock > 0
    }

    private fun getQuantity(id: String, skuInventory: List<SkuInventory>?): Int {
        skuInventory ?: return -1
        val item = skuInventory.find { id == it.sku }
        return item?.quantity ?: -1
    }

    private fun onProductCheckedChange(isChecked: Boolean, productItem: ProductItem) {
        _orderAgainUiState.update {
            val updatedList = it.orderList.toMutableList()
            updatedList.find { item -> item.id == productItem.id }?.let { item ->
                item.quantity =
                    item.quantity.coerceAtLeast(1).coerceAtMost(item.quantityInStock)
                item.isSelected = isChecked
            }
            // Determine toolbar button text
            val isAnyUnselected = isAnyUnselected()
            // Determine Add to cart button visibility and bottom nav bar
            val isAnySelected = updatedList.any { item -> item.isSelected }

            _onScreenEvent.update {
                OrderAgainScreenEvents.HideBottomBar(isAnySelected)
            }

            it.copy(
                orderList = updatedList,
                showAddToCart = isAnySelected,
                headerState = it.headerState.copy(
                    rightButtonRes = if (isAnyUnselected) R.string.select_all else R.string.deselect_all
                )
            )
        }
        updateAddToListItemCount()
    }

    fun refreshInventory() {
        val productIds = orderAgainUiState.value.orderList.map { it.id }
        if (productIds.isEmpty()) return
        callInventoryApi(productIds)
    }
}