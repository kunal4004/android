package za.co.woolworths.financial.services.android.ui.fragments.product.back_in_stock.presentation.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import za.co.woolworths.financial.services.android.models.dto.ShoppingList
import za.co.woolworths.financial.services.android.shoptoggle.domain.model.ToggleModel
import za.co.woolworths.financial.services.android.ui.fragments.product.back_in_stock.presentation.components.BackInStockScreenEvents
import javax.inject.Inject

@HiltViewModel
class NotifyBackInStockViewModel @Inject constructor(

) : ViewModel() {

    private val listState = mutableStateOf(BackToStockUiState())

    fun getState(): BackToStockUiState {
        return listState.value
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
            val selectedListItem: List<ShoppingList> = emptyList()
    )

    fun onEvent(event: BackInStockScreenEvents) {

        when (event) {
            BackInStockScreenEvents.CreateListClick -> listState.value = listState.value.copy(
                showCreateList = true
            )

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
}