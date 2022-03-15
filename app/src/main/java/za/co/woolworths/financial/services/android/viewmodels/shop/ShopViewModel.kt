package za.co.woolworths.financial.services.android.viewmodels.shop

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.models.dto.RootCategories
import za.co.woolworths.financial.services.android.models.network.Event
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.models.network.Resource
import za.co.woolworths.financial.services.android.models.network.Status
import java.io.IOException

class ShopViewModel : ViewModel() {

    private val _isCategoriesAvailable = MutableLiveData(false)
    val isCategoriesAvailable: LiveData<Boolean>
    get() = _isCategoriesAvailable

    private val _categories = MutableLiveData<Event<Resource<RootCategories>>>()
    val categories: LiveData<Event<Resource<RootCategories>>> = _categories

    fun getDashCategories() {
        _categories.value = Event(Resource.loading(null))
        viewModelScope.launch {
            val response = fetchDashCategories()
            _categories.value = Event(response)
            _isCategoriesAvailable.value = response.status == Status.SUCCESS
        }
    }

    private suspend fun fetchDashCategories(): Resource<RootCategories> {
        return try {
            val response = OneAppService.getDashCategory()
            if (response.isSuccessful) {
                response.body()?.let {
                    return@let Resource.success(it)
                } ?: Resource.error("An unknown error occured", null)
            } else {
                Resource.error("An unknown error occured", null)
            }
        } catch (e: IOException) {
            Log.e("EXCEPTION", "EXCEPTION:", e)
            Resource.error("Couldn't reach the server. Check your internet connection", null)
        }
    }
}
