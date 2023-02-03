package za.co.woolworths.financial.services.android.cart.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.cart.network.CartRepository
import za.co.woolworths.financial.services.android.checkout.service.network.SavedAddressResponse
import za.co.woolworths.financial.services.android.models.dto.ChangeQuantity
import za.co.woolworths.financial.services.android.models.dto.ShoppingCartResponse
import za.co.woolworths.financial.services.android.models.dto.SkusInventoryForStoreResponse
import za.co.woolworths.financial.services.android.models.dto.voucher_and_promo_code.CouponClaimCode
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
    private val _removeCartItem =
        MutableLiveData<Event<Resource<ShoppingCartResponse>>>()
    val removeCartItem: LiveData<Event<Resource<ShoppingCartResponse>>> =
        _removeCartItem
    private val _removeAllCartItem =
        MutableLiveData<Event<Resource<ShoppingCartResponse>>>()
    val removeAllCartItem: LiveData<Event<Resource<ShoppingCartResponse>>> =
        _removeAllCartItem

    private val _getInventorySkuForInventory =
        MutableLiveData<Event<Resource<SkusInventoryForStoreResponse>>>()
    val getInventorySkuForInventory: LiveData<Event<Resource<SkusInventoryForStoreResponse>>> =
        _getInventorySkuForInventory

    private val _changeProductQuantity =
        MutableLiveData<Event<Resource<ShoppingCartResponse>>>()
    val changeProductQuantity: LiveData<Event<Resource<ShoppingCartResponse>>> =
        _changeProductQuantity

    private val _onRemovePromoCode =
        MutableLiveData<Event<Resource<ShoppingCartResponse>>>()
    val onRemovePromoCode: LiveData<Event<Resource<ShoppingCartResponse>>> =
        _onRemovePromoCode

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

    fun removeCartItem(commerceId: String) {
        _removeCartItem.value = Event(Resource.loading(null))
        viewModelScope.launch {
            val response = cartRepository.removeCartItem(commerceId)
            _removeCartItem.value = Event(response)
        }
    }

    fun removeAllCartItem() {
        _removeAllCartItem.value = Event(Resource.loading(null))
        viewModelScope.launch {
            val response = cartRepository.removeAllCartItems()
            _removeAllCartItem.value = Event(response)
        }
    }

    fun getInventorySkuForInventory(store_id: String, multipleSku: String, isUserBrowsing: Boolean) {
        _getInventorySkuForInventory.value = Event(Resource.loading(null))
        viewModelScope.launch {
            val response = cartRepository.getInventorySkuForInventory(store_id, multipleSku, isUserBrowsing)
            _getInventorySkuForInventory.value = Event(response)
        }
    }

    fun changeProductQuantityRequest(changeQuantity: ChangeQuantity?) {
        _changeProductQuantity.value = Event(Resource.loading(null))
        viewModelScope.launch {
            val response = cartRepository.changeProductQuantityRequest(changeQuantity)
            _changeProductQuantity.value = Event(response)
        }
    }

    fun onRemovePromoCode(couponClaimCode: CouponClaimCode) {
        _onRemovePromoCode.value = Event(Resource.loading(null))
        viewModelScope.launch {
            val response = cartRepository.onRemovePromoCode(couponClaimCode)
            _onRemovePromoCode.value = Event(response)
        }
    }
}