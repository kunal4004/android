package za.co.woolworths.financial.services.android.enhancedSubstitution.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.enhancedSubstitution.model.ProductSubstitution
import za.co.woolworths.financial.services.android.enhancedSubstitution.repository.ProductSubstitutionRepository
import za.co.woolworths.financial.services.android.models.dto.ProductsRequestParams
import za.co.woolworths.financial.services.android.models.network.Event
import za.co.woolworths.financial.services.android.models.network.Resource

class ProductSubstitutionViewModel(
        private val repository: ProductSubstitutionRepository) : ViewModel() {

    private val _productSubstitution = MutableLiveData<Event<Resource<ProductSubstitution>>>()
    val productSubstitution: LiveData<Event<Resource<ProductSubstitution>>>
        get() = _productSubstitution


    fun getProductSubstitution(productId: String?) {
        viewModelScope.launch {
            _productSubstitution.postValue(Event(Resource.loading(null)))
            val result = repository.getProductSubstitution(productId)
            _productSubstitution.value = Event(result)
        }
    }

    fun getAllSearchedSubstitutions(requestParams: ProductsRequestParams) =
            repository.getAllSearchedSubstitutions(requestParams).cachedIn(viewModelScope)

}