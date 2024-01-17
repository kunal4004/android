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
        backInStockState = backInStockState.copy(
            selectedSku = selectedSku,
            isSizeSelected = selectedSku != null && selectedSku.quantity == 0
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
        var selectedSku: OtherSkus? = null
    )

    fun onEvent(event: BackInStockScreenEvents) {

        when (event) {
            /* BackInStockScreenEvents.CreateListClick -> backInStockState.value = backInStockState.value.copy(
                 showCreateList = true
             )*/
            is BackInStockScreenEvents.onSizeSelected -> onSizeClick(event.selectedSize)

            //  BackInStockScreenEvents.RetryClick -> getMyList()
            BackInStockScreenEvents.ConfirmClick -> notifyMe()
            /* is AddToListScreenEvents.OnItemClick -> onListItemClick(event.item)
             is AddToListScreenEvents.ConfirmCreateList -> createList(event.name)
             BackInStockScreenEvents.CreateListBackPressed -> listState.value = listState.value.copy(
                 showCreateList = false
             )*/

            else -> {}
        }
    }

    private fun notifyMe() {

    }

    private fun onSizeClick(selectedSize: String) {
        // viewModelScope.launch(Dispatchers.Default) {
        val isSizeUpdated = selectedSize.isNotEmpty()
        //   viewModelScope.launch(Dispatchers.Main) {
        backInStockState = backInStockState.copy(
            isSizeSelected = isSizeUpdated
        )
        //      }
        // }
    }
}