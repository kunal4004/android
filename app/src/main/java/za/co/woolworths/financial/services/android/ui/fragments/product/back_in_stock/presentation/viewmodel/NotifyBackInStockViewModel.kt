package za.co.woolworths.financial.services.android.ui.fragments.product.back_in_stock.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dao.AppInstanceObject
import za.co.woolworths.financial.services.android.models.dto.OtherSkus
import za.co.woolworths.financial.services.android.models.network.Status
import za.co.woolworths.financial.services.android.ui.fragments.product.back_in_stock.domain.usecases.NotifyMeUC
import za.co.woolworths.financial.services.android.ui.fragments.product.back_in_stock.models.NotifyMeRequest
import za.co.woolworths.financial.services.android.ui.fragments.product.back_in_stock.presentation.NotifyBackInStockFragment
import za.co.woolworths.financial.services.android.ui.fragments.product.back_in_stock.presentation.NotifyBackInStockFragment.Companion.OTHER_SKUSBYGROUP_KEY
import za.co.woolworths.financial.services.android.ui.fragments.product.back_in_stock.presentation.NotifyBackInStockFragment.Companion.SELECTED_GROUP_KEY
import za.co.woolworths.financial.services.android.ui.fragments.product.back_in_stock.presentation.NotifyBackInStockFragment.Companion.SELECTED_SKU
import za.co.woolworths.financial.services.android.ui.fragments.product.back_in_stock.presentation.components.BackInStockScreenEvents
import za.co.woolworths.financial.services.android.util.Utils
import javax.inject.Inject

@HiltViewModel
class NotifyBackInStockViewModel @Inject constructor(
    private val notifyMeUC: NotifyMeUC,
    private val savedStateHandle: SavedStateHandle
) :
    ViewModel() {

    var backInStockState by mutableStateOf(BackToStockUiState())

    private val _notifyMeState = MutableStateFlow(BackToStockUiState())
    val notifyMeState: StateFlow<BackToStockUiState> = _notifyMeState.asStateFlow()

    fun getState(): BackToStockUiState {
        return backInStockState
    }

    init {
        getArguments()
    }

    fun getArguments() {
        val selectedSku = savedStateHandle.get<OtherSkus>(SELECTED_SKU)
        val selectedGroupKey = savedStateHandle.get<String>(SELECTED_GROUP_KEY)
        val otherSKUsByGroupKey = savedStateHandle.get<LinkedHashMap<String, ArrayList<OtherSkus>>>(OTHER_SKUSBYGROUP_KEY)
        backInStockState = backInStockState.copy(
            selectedSku = selectedSku,
            isColourOrSizeSelected = selectedSku != null && selectedSku.quantity == 0,
            selectedGroupKey = selectedGroupKey,
            otherSKUsByGroupKey = otherSKUsByGroupKey
        )
    }

    data class BackToStockUiState(
        val isError: Boolean = false,
        var isSuccess: Boolean = false,
        val isLoading: Boolean = false,
        val isConfirmSuccess: Boolean = false,
        var isColourOrSizeSelected: Boolean = false,
        val errorMessage: String = "",
        var selectedSku: OtherSkus? = null,
        var selectedGroupKey: String? = null,
        var otherSKUsByGroupKey: LinkedHashMap<String, ArrayList<OtherSkus>>? = linkedMapOf()
    )

    fun onEvent(
        hasColor: Boolean,
        hasSize: Boolean,
        event: BackInStockScreenEvents,
        productId: String?,
        storeId: String?
    ) {

        when (event) {
            is BackInStockScreenEvents.OnSizeSelected -> onSizeClick(event.selectedSize)
            is BackInStockScreenEvents.OnOtherSKusSelected -> onOtherSKusClick(event.otherSkus)
            is BackInStockScreenEvents.OnColorSelected -> onColorClick(
                event.selectedColor,
                hasColor,
                hasSize
            )
            BackInStockScreenEvents.ConfirmClick -> notifyMe(productId, storeId)
            else -> {}
        }
    }

    private fun notifyMe(productId: String?, storeId: String?) {
        val notifyMeRequest = NotifyMeRequest(
            AppInstanceObject.getCurrentUsersID(),
            productId,
            backInStockState.selectedSku?.sku,
            storeId,
            Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.address?.placeId,
            AppConfigSingleton.backInStock?.notification_pending_count?.toIntOrNull(),
            AppConfigSingleton.backInStock?.frequency_hours?.toIntOrNull(),
            NotifyBackInStockFragment.SOURCE_SYSTEM
        )

        viewModelScope.launch(Dispatchers.IO) {

            notifyMeUC(notifyMeRequest).collect {
                viewModelScope.launch(Dispatchers.Main) {
                    when (it.status) {
                        Status.SUCCESS -> {
                            _notifyMeState.value = _notifyMeState.value.copy(
                                isLoading = false,
                                isSuccess = true,
                                isError = false
                            )
                        }

                        Status.ERROR -> {
                            _notifyMeState.value = _notifyMeState.value.copy(
                                isLoading = false,
                                isError = true,
                                isSuccess = false,
                                errorMessage = it.data?.response?.let {
                                    return@let it.desc ?: it.message ?: ""
                                } ?: ""
                            )
                        }

                        Status.LOADING -> {
                            _notifyMeState.value = _notifyMeState.value.copy(
                                isLoading = true,
                                isError = false,
                                isSuccess = false
                            )
                        }
                    }
                }
            }
        }
    }

    private fun onSizeClick(selectedSize: String) {
        val isSizeSelected = selectedSize.isNotEmpty()
        backInStockState = backInStockState.copy(
            isColourOrSizeSelected = isSizeSelected
        )
    }

    private fun onOtherSKusClick(otherSkus: OtherSkus) {
        if(backInStockState.selectedSku == null) {
            backInStockState = backInStockState.copy(
                selectedSku = otherSkus
            )
        }
    }

    private fun onColorClick(selectedColor: String, hasColor: Boolean, hasSize: Boolean) {
        if (hasColor && !hasSize) {
            val isColorSelected = selectedColor.isNotEmpty()
            backInStockState = backInStockState.copy(
                isColourOrSizeSelected = isColorSelected
            )
            if(backInStockState.selectedSku == null && backInStockState.otherSKUsByGroupKey?.get(selectedColor)?.size!! > 0) {
                backInStockState = backInStockState.copy(
                    selectedSku = backInStockState.otherSKUsByGroupKey?.get(selectedColor)?.get(0)
                )
            }
        }
        backInStockState = backInStockState.copy(
            selectedGroupKey = selectedColor,
        )
    }
}