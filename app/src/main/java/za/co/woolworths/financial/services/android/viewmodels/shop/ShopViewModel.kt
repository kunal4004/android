package za.co.woolworths.financial.services.android.viewmodels.shop

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.awfs.coordination.R
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.RootCategories
import za.co.woolworths.financial.services.android.models.dto.shop.DashCategories
import za.co.woolworths.financial.services.android.models.network.Event
import za.co.woolworths.financial.services.android.models.network.Resource
import za.co.woolworths.financial.services.android.models.network.Status
import za.co.woolworths.financial.services.android.repository.shop.ShopRepository
import za.co.woolworths.financial.services.android.util.KotlinUtils
import javax.inject.Inject

@HiltViewModel
class ShopViewModel @Inject constructor(
    val shopRepository: ShopRepository
) : ViewModel() {

    private val _isOnDemandCategoriesAvailable = MutableLiveData(false)
    val isOnDemandCategoriesAvailable: LiveData<Boolean>
    get() = _isOnDemandCategoriesAvailable

    private val _isDashCategoriesAvailable = MutableLiveData(false)
    val isDashCategoriesAvailable: LiveData<Boolean>
    get() = _isDashCategoriesAvailable

    private val _onDemandCategories = MutableLiveData<Event<Resource<RootCategories>>>()
    val onDemandCategories: LiveData<Event<Resource<RootCategories>>> = _onDemandCategories

    private val _dashCategories = MutableLiveData<Event<Resource<DashCategories>>>()
    val dashCategories: LiveData<Event<Resource<DashCategories>>> = _dashCategories

    fun getDashCategories() {
        _dashCategories.value = Event(Resource.loading(null))
        viewModelScope.launch {
            val response = shopRepository.fetchDashCategories()
            _dashCategories.value = Event(response)
            _isDashCategoriesAvailable.value = response.status == Status.SUCCESS
        }
    }

    fun getOnDemandCategories() {
        _onDemandCategories.value = Event(Resource.loading(null))
        viewModelScope.launch {
            val response = shopRepository.fetchOnDemandCategories()
            _onDemandCategories.value = Event(response)
            _isOnDemandCategoriesAvailable.value = response.status == Status.SUCCESS
        }
    }
}
