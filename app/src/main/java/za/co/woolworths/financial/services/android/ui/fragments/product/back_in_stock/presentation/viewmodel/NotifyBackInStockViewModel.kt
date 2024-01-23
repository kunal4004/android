package za.co.woolworths.financial.services.android.ui.fragments.product.back_in_stock.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import za.co.woolworths.financial.services.android.models.dto.OtherSkus
import za.co.woolworths.financial.services.android.shoptoggle.domain.model.ToggleModel
import za.co.woolworths.financial.services.android.ui.fragments.product.back_in_stock.presentation.components.BackInStockScreenEvents
import javax.inject.Inject

@HiltViewModel
class NotifyBackInStockViewModel @Inject constructor(private val savedStateHandle: SavedStateHandle) :
    ViewModel() {

    var backInStockState by mutableStateOf(BackToStockUiState())

    fun getState(): BackToStockUiState {
        return backInStockState
    }

    init {
        getArguments()
    }

    fun getArguments() {
        val selectedSku = savedStateHandle.get<OtherSkus>("selectedSku")
        val selectedGroupKey = savedStateHandle.get<String>("selectedGroupKey")
        backInStockState = backInStockState.copy(
            selectedSku = selectedSku,
            isSizeSelected = selectedSku != null && selectedSku.quantity == 0,
            selectedGroupKey = selectedGroupKey
        )
    }

    data class ToggleScreenState(
        val isLoading: Boolean = false, val data: List<ToggleModel> = emptyList()
    )

    data class BackToStockUiState(
        val isError: Boolean = false,
        val isLoading: Boolean = true,
        val isConfirmInProgress: Boolean = false,
        val isConfirmSuccess: Boolean = false,
        val showCreateList: Boolean = false,
        var isSizeSelected: Boolean = false,
        var selectedSku: OtherSkus? = null,
        var selectedGroupKey: String? = null
    )

    fun onEvent(event: BackInStockScreenEvents) {

        when (event) {
            is BackInStockScreenEvents.OnSizeSelected -> onSizeClick(event.selectedSize)
            is BackInStockScreenEvents.OnColorSelected-> onColorClick(event.selectedColor)
            BackInStockScreenEvents.ConfirmClick -> notifyMe()
            else -> {

            }
        }
    }
    //TODO implement API call here
    private fun notifyMe() {

    }

    private fun onSizeClick(selectedSize: String) {
        val isSizeUpdated = selectedSize.isNotEmpty()
        backInStockState = backInStockState.copy(
            isSizeSelected = isSizeUpdated
        )
    }

    private fun onColorClick(selectedColor: String) {
        backInStockState = backInStockState.copy(
            selectedGroupKey = selectedColor
        )
    }
}