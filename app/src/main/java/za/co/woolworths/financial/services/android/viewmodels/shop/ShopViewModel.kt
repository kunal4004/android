package za.co.woolworths.financial.services.android.viewmodels.shop

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.checkout.service.network.ConfirmDeliveryAddressResponse
import za.co.woolworths.financial.services.android.geolocation.model.request.ConfirmLocationRequest
import za.co.woolworths.financial.services.android.geolocation.network.model.ValidateLocationResponse
import za.co.woolworths.financial.services.android.models.dto.AddItemToCart
import za.co.woolworths.financial.services.android.models.dto.AddItemToCartResponse
import za.co.woolworths.financial.services.android.models.dto.RootCategories
import za.co.woolworths.financial.services.android.models.dto.SkusInventoryForStoreResponse
import za.co.woolworths.financial.services.android.models.dto.shop.DashCategories
import za.co.woolworths.financial.services.android.models.network.Event
import za.co.woolworths.financial.services.android.models.network.Resource
import za.co.woolworths.financial.services.android.models.network.Status
import za.co.woolworths.financial.services.android.repository.shop.ShopRepository
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.QueryBadgeCounter
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

    private val _addItemToCart = MutableLiveData<AddItemToCart?>()
    val addItemToCart: LiveData<AddItemToCart?>
    get() = _addItemToCart

    private val _inventorySkuForStore = MutableLiveData<Event<Resource<SkusInventoryForStoreResponse>>>()
    val inventorySkuForStore: LiveData<Event<Resource<SkusInventoryForStoreResponse>>> = _inventorySkuForStore

    private val _addItemToCartResp = MutableLiveData<Event<Resource<AddItemToCartResponse>>>()
    val addItemToCartResp: LiveData<Event<Resource<AddItemToCartResponse>>> = _addItemToCartResp

    private val _onDemandCategories = MutableLiveData<Event<Resource<RootCategories>>>()
    val onDemandCategories: LiveData<Event<Resource<RootCategories>>> = _onDemandCategories

    private val _dashLandingDetails = MutableLiveData<Event<Resource<DashCategories>>>()
    val dashLandingDetails: LiveData<Event<Resource<DashCategories>>> = _dashLandingDetails

    private val _validatePlaceDetails = MutableLiveData<Event<Resource<ValidateLocationResponse>>>()
    val validatePlaceDetails: LiveData<Event<Resource<ValidateLocationResponse>>> = _validatePlaceDetails

    private val _confirmPlaceDetails = MutableLiveData<Event<Resource<ConfirmDeliveryAddressResponse>>>()
    val confirmPlaceDetails: LiveData<Event<Resource<ConfirmDeliveryAddressResponse>>> = _confirmPlaceDetails

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
        _addItemToCartResp.value = Event(Resource.loading(null))
        viewModelScope.launch {
            val response = shopRepository.addItemsToCart(mAddItemsToCart)
            _addItemToCartResp.value = Event(response)
            if(response.data?.httpCode == AppConstant.HTTP_OK) {
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

    fun callConfirmPlace(confirmLocationRequest: ConfirmLocationRequest) {
        _confirmPlaceDetails.value = Event(Resource.loading(null))
        viewModelScope.launch {
            val response = shopRepository.confirmPlace(confirmLocationRequest)
            _confirmPlaceDetails.value = Event(response)
        }
    }

    fun setLocation(location: Location?) {
        _location.value = location
    }

    fun setAddItemToCart(addItemToCart: AddItemToCart?) {
        _addItemToCart.value = addItemToCart
    }
}
