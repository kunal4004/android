package za.co.woolworths.financial.services.android.enhancedSubstitution.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.enhancedSubstitution.model.AddSubstitutionRequest
import za.co.woolworths.financial.services.android.enhancedSubstitution.model.AddSubstitutionResponse
import za.co.woolworths.financial.services.android.enhancedSubstitution.model.ProductSubstitution
import za.co.woolworths.financial.services.android.enhancedSubstitution.repository.ProductSubstitutionRepository
import za.co.woolworths.financial.services.android.models.dto.PagingResponse
import za.co.woolworths.financial.services.android.models.dto.ProductsRequestParams
import za.co.woolworths.financial.services.android.models.dto.SkusInventoryForStoreResponse
import za.co.woolworths.financial.services.android.models.network.Event
import za.co.woolworths.financial.services.android.models.network.Resource

class ProductSubstitutionViewModel(
        private val repository: ProductSubstitutionRepository) : ViewModel() {

    private val _productSubstitution = MutableLiveData<Event<Resource<ProductSubstitution>>>()
    val productSubstitution: LiveData<Event<Resource<ProductSubstitution>>>
        get() = _productSubstitution

    private val _inventorySubstitution = MutableLiveData<Event<Resource<SkusInventoryForStoreResponse>>>()
    val inventorySubstitution: LiveData<Event<Resource<SkusInventoryForStoreResponse>>>
        get() = _inventorySubstitution

    private val _addSubstitutionResponse = MutableLiveData<Event<Resource<AddSubstitutionResponse>>>()
    val addSubstitutionResponse: LiveData<Event<Resource<AddSubstitutionResponse>>>
        get() = _addSubstitutionResponse

     val _pagingResponse = MutableLiveData<PagingResponse>()


    fun getProductSubstitution(productId: String?) {
        viewModelScope.launch {
            _productSubstitution.postValue(Event(Resource.loading(null)))
             val result = repository.getProductSubstitution(productId)
            _productSubstitution.value = Event(result)
        }
    }

    fun getInventoryForSubstitution(storeId: String, multisku:String) {
        viewModelScope.launch {
            _inventorySubstitution.postValue(Event(Resource.loading(null)))
            val result = repository.getInventoryForSubstitution(storeId, multisku)
            _inventorySubstitution.value = Event(result)
        }
    }

    fun getAllSearchedSubstitutions(requestParams: ProductsRequestParams) =
            repository.getAllSearchedSubstitutions(requestParams, _pagingResponse).flow.cachedIn(viewModelScope)


    fun addSubstitutionForProduct(addSubstitutionRequest: AddSubstitutionRequest) {
        viewModelScope.launch {
            _addSubstitutionResponse.postValue(Event(Resource.loading(null)))
            val result = repository.addSubstitution(addSubstitutionRequest)
            _addSubstitutionResponse.value = Event(result)
        }
    }

}