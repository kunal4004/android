package za.co.woolworths.financial.services.android.viewmodels.shop

import android.location.Location
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.geolocation.network.model.ValidateLocationResponse
import za.co.woolworths.financial.services.android.models.dto.AddItemToCart
import za.co.woolworths.financial.services.android.models.dto.AddItemToCartResponse
import za.co.woolworths.financial.services.android.models.dto.DashRootCategories
import za.co.woolworths.financial.services.android.models.dto.LocationResponse
import za.co.woolworths.financial.services.android.models.dto.ProductList
import za.co.woolworths.financial.services.android.models.dto.SkusInventoryForStoreResponse
import za.co.woolworths.financial.services.android.models.dto.dash.LastOrderDetailsResponse
import za.co.woolworths.financial.services.android.models.dto.shop.DashCategories
import za.co.woolworths.financial.services.android.models.network.Event
import za.co.woolworths.financial.services.android.models.network.Resource
import za.co.woolworths.financial.services.android.models.network.Status
import za.co.woolworths.financial.services.android.repository.shop.ShopRepository
import za.co.woolworths.financial.services.android.ui.fragments.shop.component.ShopTooltipUiState
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.QueryBadgeCounter
import za.co.woolworths.financial.services.android.util.analytics.AnalyticsManager
import za.co.woolworths.financial.services.android.util.wenum.Delivery
import javax.inject.Inject

@HiltViewModel
class ShopViewModel @Inject constructor(
    private val shopRepository: ShopRepository
) : ViewModel() {

    private val _isOnDemandCategoriesAvailable = MutableLiveData(false)
    val isOnDemandCategoriesAvailable: LiveData<Boolean>
        get() = _isOnDemandCategoriesAvailable

    private val _isDashCategoriesAvailable = MutableLiveData(false)
    val isDashCategoriesAvailable: LiveData<Boolean>
        get() = _isDashCategoriesAvailable

    private val _location = MutableLiveData<Location?>()
    val location: LiveData<Location?>
        get() = _location

    private val _productList = MutableLiveData<ProductList?>()
    val productList: LiveData<ProductList?>
        get() = _productList

    private val _addItemToCart = MutableLiveData<AddItemToCart?>()
    val addItemToCart: LiveData<AddItemToCart?>
        get() = _addItemToCart

    private val _inventorySkuForStore =
        MutableLiveData<Event<Resource<SkusInventoryForStoreResponse>>>()
    val inventorySkuForStore: LiveData<Event<Resource<SkusInventoryForStoreResponse>>> =
        _inventorySkuForStore

    private val _addItemToCartResp = MutableLiveData<Event<Resource<AddItemToCartResponse>>>()
    val addItemToCartResp: LiveData<Event<Resource<AddItemToCartResponse>>> = _addItemToCartResp

    private val _onDemandCategories = MutableLiveData<Event<Resource<DashRootCategories>>>()
    val onDemandCategories: LiveData<Event<Resource<DashRootCategories>>> = _onDemandCategories

    private val _dashLandingDetails = MutableLiveData<Event<Resource<DashCategories>>>()
    val dashLandingDetails: LiveData<Event<Resource<DashCategories>>> = _dashLandingDetails

    private val _validatePlaceDetails = MutableLiveData<Event<Resource<ValidateLocationResponse>>>()
    val validatePlaceDetails: LiveData<Event<Resource<ValidateLocationResponse>>> =
        _validatePlaceDetails


    private val _productStoreFinder = MutableLiveData<Event<Resource<LocationResponse>>>()
    val productStoreFinder: LiveData<Event<Resource<LocationResponse>>> = _productStoreFinder

    private val _lastDashOrder = MutableLiveData<Event<Resource<LastOrderDetailsResponse>>>()
    val lastDashOrder: LiveData<Event<Resource<LastOrderDetailsResponse>>> = _lastDashOrder

    private var _tooltipUiState: MutableStateFlow<ShopTooltipUiState> =
        MutableStateFlow(ShopTooltipUiState.Hidden)
    val tooltipUiState: StateFlow<ShopTooltipUiState> = _tooltipUiState.stateIn(
        scope = viewModelScope,
        initialValue = ShopTooltipUiState.Hidden,
        started = SharingStarted.WhileSubscribed(5000L)
    )

    private val _lastDashOrderInProgress = MutableLiveData(false)
    val lastDashOrderInProgress: LiveData<Boolean>
        get() = _lastDashOrderInProgress

    fun getDashLandingDetails() {
        _dashLandingDetails.value = Event(Resource.loading(null))
        viewModelScope.launch {
            val response = shopRepository.fetchDashLandingDetails()
            _dashLandingDetails.value = Event(response)
            _isDashCategoriesAvailable.value = response.status == Status.SUCCESS
        }
    }

    fun getOnDemandCategories() {
        _onDemandCategories.value = Event(Resource.loading(null))
        viewModelScope.launch {
            val response = shopRepository.fetchOnDemandCategories(location.value)
            _onDemandCategories.value = Event(response)
            _isOnDemandCategoriesAvailable.value = response.status == Status.SUCCESS
        }
    }

    fun fetchInventorySkuForStore(mStoreId: String, referenceId: String) {
        _inventorySkuForStore.value = Event(Resource.loading(null))
        viewModelScope.launch {
            val response = shopRepository.fetchInventorySkuForStore(mStoreId, referenceId)
            _inventorySkuForStore.value = Event(response)
        }
    }

    fun callToAddItemsToCart(mAddItemsToCart: MutableList<AddItemToCart>) {

        // set updated value for _addItemToCart
        mAddItemsToCart?.get(0)?.let {
            _addItemToCart.value = it
        }

        _addItemToCartResp.value = Event(Resource.loading(null))
        viewModelScope.launch {
            val response = shopRepository.addItemsToCart(mAddItemsToCart)
            _addItemToCartResp.value = Event(response)
            if (response.data?.httpCode == AppConstant.HTTP_OK) {
                // Ensure counter is always updated after a successful add to cart
                QueryBadgeCounter.instance.queryCartSummaryCount()
            }
        }
    }

    fun getValidateLocationResponse(placeId: String) {
        _validatePlaceDetails.value = Event(Resource.loading(null))
        viewModelScope.launch {
            val response = shopRepository.validateLocation(placeId)
            _validatePlaceDetails.value = Event(response)
        }
    }

    fun callStoreFinder(sku: String, startRadius: String?, endRadius: String?) {
        _productStoreFinder.value = Event(Resource.loading(null))
        viewModelScope.launch {
            val response = shopRepository.callStoreFinder(sku, startRadius, endRadius)
            _productStoreFinder.value = Event(response)
        }
    }

    fun setLocation(location: Location?) {
        _location.value = location
    }

    fun setAddItemToCart(addItemToCart: AddItemToCart?) {
        _addItemToCart.value = addItemToCart
    }

    fun setProductList(productList: ProductList) {
        _productList.value = productList
    }

    fun getLastDashOrderDetails() {
        _lastDashOrder.value = Event(Resource.loading(null))
        _lastDashOrderInProgress.value = true
        viewModelScope.launch {
            val response = shopRepository.fetchLastDashOrderDetails()
            _lastDashOrder.value = Event(response)
            _lastDashOrderInProgress.value = false
        }
    }

    fun onTabClick(validateLocationResponse: ValidateLocationResponse? = null, position: Int) {
        viewModelScope.launch(Dispatchers.IO) {

            _tooltipUiState.update { currentState ->
                when (position) {
                    // TODO: Include Standard and CNC
                    0 -> { ShopTooltipUiState.StandardTooltip }
                    1 -> { ShopTooltipUiState.CNCTooltip }
                    2 -> {  // Index of dash tab

                        KotlinUtils.browsingDeliveryType = Delivery.DASH
                        setEventsForSwitchingBrowsingType(Delivery.DASH.name)
                        validateLocationResponse?.validatePlace?.onDemand?.let {
                            var visibility =
                                (it.deliverable && !it.firstAvailableFoodDeliveryTime.isNullOrEmpty())
                            // Close button isn't clicked
                            visibility = !(KotlinUtils.isDashTabCrossClicked ?: false) && visibility

                            val changeButtonVisibility =
                                KotlinUtils.getDeliveryType() == null || Delivery.getType(
                                    KotlinUtils.getDeliveryType()?.deliveryType
                                )?.type == Delivery.DASH.type

                            val timeSlot =
                                if (!(it.deliveryTimeSlots.isNullOrEmpty() && it.deliverable)) {
                                    it.firstAvailableFoodDeliveryTime ?: ""
                                } else ""

                            ShopTooltipUiState.DashTooltip(
                                visibility = visibility,
                                changeButtonVisibility = changeButtonVisibility,
                                timeslotText = timeSlot,
                                itemLimit = it.quantityLimit?.foodMaximumQuantity ?: 0,
                                deliveryFee = it.firstAvailableFoodDeliveryCost ?: 0
                            )
                        } ?: ShopTooltipUiState.Hidden
                    }
                    else -> ShopTooltipUiState.Hidden
                }
            }
        }
    }

    private fun setEventsForSwitchingBrowsingType(browsingType: String?) {
        if (KotlinUtils.getPreferredDeliveryType() == null) {
            return
        }
        val dashParams = bundleOf(
            FirebaseManagerAnalyticsProperties.PropertyNames.DELIVERY_MODE to KotlinUtils.getPreferredDeliveryType()?.name,
            FirebaseManagerAnalyticsProperties.PropertyNames.BROWSE_MODE to browsingType
        )
        AnalyticsManager.setUserProperty(
            FirebaseManagerAnalyticsProperties.PropertyNames.DELIVERY_MODE,
            KotlinUtils.getPreferredDeliveryType()?.type
        )
        browsingType?.let {
            AnalyticsManager.setUserProperty(
                FirebaseManagerAnalyticsProperties.PropertyNames.BROWSE_MODE,
                browsingType
            )
        }
        AnalyticsManager.logEvent(
            FirebaseManagerAnalyticsProperties.DASH_SWITCH_BROWSE_MODE,
            dashParams
        )

    }
}