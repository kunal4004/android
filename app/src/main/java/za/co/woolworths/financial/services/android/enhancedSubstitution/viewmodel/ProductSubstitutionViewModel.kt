package za.co.woolworths.financial.services.android.enhancedSubstitution.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.enhancedSubstitution.service.model.*
import za.co.woolworths.financial.services.android.enhancedSubstitution.service.repository.ProductSubstitutionRepository
import za.co.woolworths.financial.services.android.models.dto.PagingResponse
import za.co.woolworths.financial.services.android.models.dto.ProductsRequestParams
import za.co.woolworths.financial.services.android.models.dto.SkusInventoryForStoreResponse
import za.co.woolworths.financial.services.android.models.network.Event
import za.co.woolworths.financial.services.android.models.network.Resource
import javax.inject.Inject

@HiltViewModel
class ProductSubstitutionViewModel @Inject constructor(
    private val repository: ProductSubstitutionRepository,
) : ViewModel() {

    private val _productSubstitution = MutableLiveData<Event<Resource<ProductSubstitution>>>()
    val productSubstitution: LiveData<Event<Resource<ProductSubstitution>>>
        get() = _productSubstitution


    private val _addSubstitutionResponse =
        MutableLiveData<Event<Resource<AddSubstitutionResponse>>>()
    val addSubstitutionResponse: LiveData<Event<Resource<AddSubstitutionResponse>>>
        get() = _addSubstitutionResponse

    private val _kiboProductResponse = MutableLiveData<Event<Resource<KiboProductResponse>>>()
    val kiboProductResponse: LiveData<Event<Resource<KiboProductResponse>>>
        get() = _kiboProductResponse

    private val _stockInventoryResponse =
        MutableLiveData<Event<Resource<SkusInventoryForStoreResponse>>>()
    val stockInventoryResponse: LiveData<Event<Resource<SkusInventoryForStoreResponse>>>
        get() = _stockInventoryResponse


    val _pagingResponse = MutableLiveData<PagingResponse?>()

    fun getProductSubstitution(productId: String?) {
        viewModelScope.launch {
            _productSubstitution.postValue(Event(Resource.loading(null)))
            val result = repository.getProductSubstitution(productId)
            _productSubstitution.value = Event(result)
        }
    }

    fun getAllSearchedSubstitutions(requestParams: ProductsRequestParams, productId: String?) =
        repository.getAllSearchedSubstitutions(requestParams, _pagingResponse, productId).cachedIn(
            viewModelScope
        )

    fun addSubstitutionForProduct(addSubstitutionRequest: AddSubstitutionRequest) {
        viewModelScope.launch {
            _addSubstitutionResponse.postValue(Event(Resource.loading(null)))
            val result = repository.addSubstitution(addSubstitutionRequest)
            _addSubstitutionResponse.value = Event(result)
        }
    }

    fun getKiboProducts(kiboProductRequest: KiboProductRequest) {
        viewModelScope.launch {
            _kiboProductResponse.postValue(Event(Resource.loading(null)))
            val result = repository.fetchKiboProducts(kiboProductRequest)
            _kiboProductResponse.value = Event(result)
        }
    }

    fun getInventoryForKiboProducts(storeId: String, multiSku: String) {
        viewModelScope.launch {
            _stockInventoryResponse.postValue(Event(Resource.loading(null)))
            val result = repository.fetchInventoryForKiboProducts(storeId, multiSku)
            _stockInventoryResponse.value = Event(result)
        }
    }

}