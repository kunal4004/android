package za.co.woolworths.financial.services.android.recommendations.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.models.dto.AddItemToCart
import za.co.woolworths.financial.services.android.models.dto.AddItemToCartResponse
import za.co.woolworths.financial.services.android.models.dto.LocationResponse
import za.co.woolworths.financial.services.android.models.dto.SkusInventoryForStoreResponse
import za.co.woolworths.financial.services.android.models.network.Event
import za.co.woolworths.financial.services.android.models.network.Resource
import za.co.woolworths.financial.services.android.recommendations.data.repository.RecommendationsRepository
import za.co.woolworths.financial.services.android.recommendations.data.response.getresponse.Product
import za.co.woolworths.financial.services.android.recommendations.data.response.getresponse.RecommendationResponse
import za.co.woolworths.financial.services.android.recommendations.data.response.request.RecommendationRequest
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.QueryBadgeCounter
import javax.inject.Inject

@HiltViewModel
class RecommendationViewModel @Inject constructor(
    private val recommendationsRepository: RecommendationsRepository
) : ViewModel() {


    private val _recommendationResponseData =
        MutableLiveData<Event<Resource<RecommendationResponse>>>()
    val recommendationResponseData: LiveData<Event<Resource<RecommendationResponse>>> =
        _recommendationResponseData


    private val _addItemToCartResp = MutableLiveData<Event<Resource<AddItemToCartResponse>>>()
    val addItemToCartResp: LiveData<Event<Resource<AddItemToCartResponse>>> = _addItemToCartResp

    private val _addItemToCart = MutableLiveData<AddItemToCart?>()
    val addItemToCart: LiveData<AddItemToCart?>
        get() = _addItemToCart

    fun setAddItemToCart(addItemToCart: AddItemToCart?) {
        _addItemToCart.value = addItemToCart
    }

    private val _productList = MutableLiveData<Product?>()
    val productList: LiveData<Product?>
        get() = _productList

    fun setProductList(productList: Product) {
        _productList.value = productList
    }

    private val _inventorySkuForStore =
        MutableLiveData<Event<Resource<SkusInventoryForStoreResponse>>>()
    val inventorySkuForStore: LiveData<Event<Resource<SkusInventoryForStoreResponse>>> =
        _inventorySkuForStore

    private val _productStoreFinder = MutableLiveData<Event<Resource<LocationResponse>>>()
    val productStoreFinder: LiveData<Event<Resource<LocationResponse>>> = _productStoreFinder

    fun fetchInventorySkuForStore(mStoreId: String, referenceId: String) {
        _inventorySkuForStore.value = Event(Resource.loading(null))
        viewModelScope.launch {
            val response =
                recommendationsRepository.fetchInventorySkuForStore(mStoreId, referenceId)
            _inventorySkuForStore.value = Event(response)
        }
    }


    fun callStoreFinder(sku: String, startRadius: String?, endRadius: String?) {
        _productStoreFinder.value = Event(Resource.loading(null))
        viewModelScope.launch {
            val response = recommendationsRepository.callStoreFinder(sku, startRadius, endRadius)
            _productStoreFinder.value = Event(response)
        }
    }

    fun getRecommendationResponse(recommendationRequest: RecommendationRequest) {
        viewModelScope.launch {
            val response =
                recommendationsRepository.getRecommendationResponse(recommendationRequest)
            _recommendationResponseData.value = Event(response)
        }
    }

    fun callToAddItemsToCart(mAddItemsToCart: MutableList<AddItemToCart>) {

        // set updated value for _addItemToCart
        mAddItemsToCart?.get(0)?.let {
            _addItemToCart.value = it
        }

        _addItemToCartResp.value = Event(Resource.loading(null))
        viewModelScope.launch {
            val response = recommendationsRepository.addItemsToCart(mAddItemsToCart)
            _addItemToCartResp.value = Event(response)
            if (response.data?.httpCode == AppConstant.HTTP_OK) {
                // Ensure counter is always updated after a successful add to cart
                QueryBadgeCounter.instance.queryCartSummaryCount()
            }
        }
    }
}