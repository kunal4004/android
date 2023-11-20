package za.co.woolworths.financial.services.android.shoptoggle.presentation.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.shoptoggle.data.pref.ShopTogglePrefStore
import za.co.woolworths.financial.services.android.shoptoggle.domain.model.LearnMore
import za.co.woolworths.financial.services.android.shoptoggle.domain.model.ToggleModel
import za.co.woolworths.financial.services.android.shoptoggle.domain.usecase.LearnMoreUseCase
import za.co.woolworths.financial.services.android.shoptoggle.domain.usecase.ShopToggleUseCase
import javax.inject.Inject

@HiltViewModel
class ShopToggleViewModel @Inject constructor(
    private val shopToggleUseCase: ShopToggleUseCase,
    private val shopTogglePrefStore: ShopTogglePrefStore,
    private val learnMoreUseCase: LearnMoreUseCase
) : ViewModel() {

    private val _listItem = mutableStateOf<List<ToggleModel>>(emptyList())
    val listItem: State<List<ToggleModel>> = _listItem

    private val _expandedItemId = mutableStateOf<Int?>(null)
    val expandedItemId get() = _expandedItemId.value

    private val _listItemLearnMore = mutableStateOf<List<LearnMore>>(emptyList())
    val listItemLearnMore: State<List<LearnMore>> = _listItemLearnMore


    val isShopToggleScreenFirstTime: StateFlow<Boolean> = shopTogglePrefStore.isShopToggleScreenFirstTime().filter {
        it
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        true
    )


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

    fun getLearnMoreList() {
        _listItemLearnMore.value = learnMoreUseCase.invoke()
    }

    fun disableFirstTimeShopToggleScreen(isToggleScreenDisable:Boolean){
        viewModelScope.launch {
            shopTogglePrefStore.disableShopToggleScreenFirstTime(isToggleScreenDisable)
        }
    }
}