package za.co.woolworths.financial.services.android.shoptoggle.presentation.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import za.co.woolworths.financial.services.android.shoptoggle.domain.model.ToggleModel
import za.co.woolworths.financial.services.android.shoptoggle.domain.usecase.ShopToggleUseCase
import javax.inject.Inject

@HiltViewModel
class ShopToggleViewModel @Inject constructor(
    private val shopToggleUseCase: ShopToggleUseCase,
) : ViewModel() {

    private val _listItem = mutableStateOf<List<ToggleModel>>(emptyList())
    val listItem: State<List<ToggleModel>> = _listItem

    private val _expandedItemId = mutableStateOf<Int?>(null)
    val expandedItemId get() = _expandedItemId.value

    init {
        getListData()
    }

    fun expandItem(itemId: Int) {
        _expandedItemId.value = itemId
    }

    fun collapseItem() {
        _expandedItemId.value = null
    }

    private fun getListData() {
        val itemList = shopToggleUseCase.invoke()
        _listItem.value = itemList
    }

}