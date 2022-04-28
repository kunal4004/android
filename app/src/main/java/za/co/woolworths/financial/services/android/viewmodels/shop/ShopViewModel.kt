package za.co.woolworths.financial.services.android.viewmodels.shop

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.geolocation.network.model.ValidatePlace
import za.co.woolworths.financial.services.android.models.dto.AddItemToCart
import za.co.woolworths.financial.services.android.models.dto.RootCategories
import za.co.woolworths.financial.services.android.models.dto.SkusInventoryForStoreResponse
import za.co.woolworths.financial.services.android.models.dto.shop.DashCategories
import za.co.woolworths.financial.services.android.models.network.Event
import za.co.woolworths.financial.services.android.models.network.Resource
import za.co.woolworths.financial.services.android.models.network.Status
import za.co.woolworths.financial.services.android.repository.shop.ShopRepository
import java.lang.ref.PhantomReference
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

    private val _onDemandCategories = MutableLiveData<Event<Resource<RootCategories>>>()
    val onDemandCategories: LiveData<Event<Resource<RootCategories>>> = _onDemandCategories

    private val _dashLandingDetails = MutableLiveData<Event<Resource<DashCategories>>>()
    val dashLandingDetails: LiveData<Event<Resource<DashCategories>>> = _dashLandingDetails

    private var validatePlaceResponse: ValidatePlace? = null

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

    fun setLocation(location: Location?) {
        _location.value = location
    }

    fun setAddItemToCart(addItemToCart: AddItemToCart?) {
        _addItemToCart.value = addItemToCart
    }

    fun setOnDemandCategoryData(response: RootCategories) {
        _onDemandCategories.value = Event(Resource.success(response))
        _isOnDemandCategoriesAvailable.value = response.onDemandCategories != null
    }

    fun setValidatePlaceResponse (validateLocationResponse: ValidatePlace){
        validatePlaceResponse = validateLocationResponse
    }

    fun getValidatePlaceResponse() : ValidatePlace?{
        return validatePlaceResponse
    }
}
