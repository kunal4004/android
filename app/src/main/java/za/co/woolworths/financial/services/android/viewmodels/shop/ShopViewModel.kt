package za.co.woolworths.financial.services.android.viewmodels.shop

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.models.dto.RootCategories
import za.co.woolworths.financial.services.android.models.network.Event
import za.co.woolworths.financial.services.android.models.network.Resource
import za.co.woolworths.financial.services.android.models.network.Status
import za.co.woolworths.financial.services.android.repository.shop.ShopRepository
import javax.inject.Inject

@HiltViewModel
class ShopViewModel @Inject constructor(
    val shopRepository: ShopRepository
) : ViewModel() {

    private val _isCategoriesAvailable = MutableLiveData(false)
    val isCategoriesAvailable: LiveData<Boolean>
    get() = _isCategoriesAvailable

    private val _categories = MutableLiveData<Event<Resource<RootCategories>>>()
    val categories: LiveData<Event<Resource<RootCategories>>> = _categories

    fun getDashCategories() {
        _categories.value = Event(Resource.loading(null))
        viewModelScope.launch {
            val response = shopRepository.fetchDashCategories()
            _categories.value = Event(response)
            _isCategoriesAvailable.value = response.status == Status.SUCCESS
        }
    }
}
