package za.co.woolworths.financial.services.android.shoptoggle.presentation.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.geolocation.network.model.ValidateLocationResponse
import za.co.woolworths.financial.services.android.models.dto.UnSellableCommerceItem
import za.co.woolworths.financial.services.android.shoptoggle.data.pref.ShopTogglePrefStore
import za.co.woolworths.financial.services.android.shoptoggle.domain.model.LearnMore
import za.co.woolworths.financial.services.android.shoptoggle.domain.model.ToggleModel
import za.co.woolworths.financial.services.android.shoptoggle.domain.usecase.LearnMoreUseCase
import za.co.woolworths.financial.services.android.shoptoggle.domain.usecase.Resource
import za.co.woolworths.financial.services.android.shoptoggle.domain.usecase.ShopToggleUseCase
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.wenum.Delivery
import javax.inject.Inject

@HiltViewModel
class ShopToggleViewModel @Inject constructor(
    private val shopToggleUseCase: ShopToggleUseCase,
    private val shopTogglePrefStore: ShopTogglePrefStore,
    private val learnMoreUseCase: LearnMoreUseCase
) : ViewModel() {

    private val _state = mutableStateOf(ToggleScreenState())
    val state: State<ToggleScreenState> = _state

    private val _confirmAddressState = mutableStateOf(ConfirmAddressState())
    val confirmAddressState: State<ConfirmAddressState> = _confirmAddressState

    private val _expandedItemId = mutableStateOf<Int?>(null)
    val expandedItemId get() = _expandedItemId.value

    private val _selectedDeliveryTypeItemId = mutableStateOf<Int?>(null)
    val selectedDeliveryTypeItemId get() = _selectedDeliveryTypeItemId.value

    private val _listItemLearnMore = mutableStateOf<List<LearnMore>>(emptyList())
    val listItemLearnMore: State<List<LearnMore>> = _listItemLearnMore


    val isShopToggleScreenFirstTime: StateFlow<Boolean> =
        shopTogglePrefStore.isShopToggleScreenFirstTime().filter {
            it
        }.stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(), true
        )


    init {
        val placeId = KotlinUtils.getDeliveryType()?.address?.placeId
        getToggleScreenData(placeId)
    }

    fun confirmAddress(delivery: Delivery) {
        shopToggleUseCase.sendConfirmLocation(delivery).onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _confirmAddressState.value = ConfirmAddressState(
                        isLoading = true
                    )
                }

                is Resource.Error -> {
                    _confirmAddressState.value = ConfirmAddressState(
                        hasError = true
                    )
                }

                is Resource.Success -> {
                    _confirmAddressState.value = ConfirmAddressState(
                        unsellableItems = result.data,
                        isSuccess = true
                    )
                }
            }

        }.launchIn(viewModelScope)
    }

    private fun getToggleScreenData(placeId: String?) {
        shopToggleUseCase.getValidateLocationDetails(placeId).onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _state.value = ToggleScreenState(isLoading = true)
                }

                is Resource.Error -> {
                    _state.value = ToggleScreenState(data = shopToggleUseCase.getFailureData())
                }

                is Resource.Success -> {
                    _state.value =
                        ToggleScreenState(data = result.data ?: shopToggleUseCase.getFailureData())
                    _selectedDeliveryTypeItemId.value = shopToggleUseCase.getSelectedDeliveryId()
                }
            }
        }.launchIn(viewModelScope)
    }

    fun expandItem(itemId: Int) {
        _expandedItemId.value = itemId
    }

    fun deliveryType(): Delivery {
        return when (_expandedItemId.value) {
            ShopToggleUseCase.STANDARD_DELIVERY_ID -> {
                Delivery.STANDARD
            }

            ShopToggleUseCase.DASH_DELIVERY_ID -> {
                Delivery.DASH
            }

            ShopToggleUseCase.CNC_DELIVERY_ID -> {
                Delivery.CNC
            }

            else -> {
                Delivery.STANDARD
            }
        }
    }

    fun collapseItem() {
        _expandedItemId.value = null
    }

    fun getLearnMoreList() {
        _listItemLearnMore.value = learnMoreUseCase.invoke()
    }

    fun disableFirstTimeShopToggleScreen(isToggleScreenDisable: Boolean) {
        viewModelScope.launch {
            shopTogglePrefStore.disableShopToggleScreenFirstTime(isToggleScreenDisable)
        }
    }

    fun validateLocationResponse(): ValidateLocationResponse? {
        return shopToggleUseCase.validateLocationResponse()
    }
}

data class ToggleScreenState(
    val isLoading: Boolean = false, val data: List<ToggleModel> = emptyList()
)

data class ConfirmAddressState(
    val isLoading: Boolean = false,
    val unsellableItems: List<UnSellableCommerceItem>? = emptyList(),
    val isSuccess: Boolean = false,
    val hasError: Boolean = false,
    val errorMessage: String? = null
)