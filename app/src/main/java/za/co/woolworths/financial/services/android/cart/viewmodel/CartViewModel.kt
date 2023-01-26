package za.co.woolworths.financial.services.android.cart.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.cart.network.CartRepository
import za.co.woolworths.financial.services.android.checkout.service.network.SavedAddressResponse
import za.co.woolworths.financial.services.android.models.dto.ShoppingCartResponse
import javax.inject.Inject
import za.co.woolworths.financial.services.android.models.network.Event
import za.co.woolworths.financial.services.android.models.network.Resource


/**
 * Created by Kunal Uttarwar on 24/01/23.
 */

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository,
) : ViewModel() {

    private val _getCarV2 =
        MutableLiveData<Event<Resource<ShoppingCartResponse>>>()
    val getCarV2: LiveData<Event<Resource<ShoppingCartResponse>>> =
        _getCarV2
    private val _getSavedAddress =
        MutableLiveData<Event<Resource<SavedAddressResponse>>>()
    val getSavedAddress: LiveData<Event<Resource<SavedAddressResponse>>> =
        _getSavedAddress

    fun getShoppingCartV2() {
        _getCarV2.value = Event(Resource.loading(null))
        viewModelScope.launch {
            val response = cartRepository.getShoppingCartV2()
            _getCarV2.value = Event(response)
        }
    }

    fun getSavedAddress() {
        _getSavedAddress.value = Event(Resource.loading(null))
        viewModelScope.launch {
            val response = cartRepository.getSavedAddress()
            _getSavedAddress.value = Event(response)
        }
    }
}