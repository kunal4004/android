package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_account_options_list.card_freeze

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.models.dto.npc.BlockMyCardResponse
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.ViewState
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.getViewStateFlowForNetworkCall
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard.IStoreCardDataSource
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard.StoreCardDataSource
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard.StoreCardType
import javax.inject.Inject

@HiltViewModel
class TemporaryFreezeCardViewModel @Inject constructor(private val storeCardDataSource: StoreCardDataSource) : ViewModel(), IStoreCardDataSource by storeCardDataSource {

    val isTempFreezeUnFreezeLoading: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    val isSwitcherEnabled: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    val currentPagePosition: MutableLiveData<Int> by lazy { MutableLiveData<Int>() }

    init {
        currentPagePosition.value = 0
    }

    private val _blockMyCardResponse = MutableSharedFlow<ViewState<BlockMyCardResponse>>(replay = 0)
    val blockMyCardResponse: SharedFlow<ViewState<BlockMyCardResponse>> get() = _blockMyCardResponse

    fun stopLoading() {
        viewModelScope.launch { _blockMyCardResponse.emit(ViewState.Loading(false)) }
    }

    fun queryServiceBlockCardTypeFreeze() = viewModelScope.launch {
        queryServiceBlockStoreCard(StoreCardType.PRIMARY_CARD).collect { _blockMyCardResponse.emit(it) }
    }

    fun queryServiceUnBlockCardTypeFreeze(storeCardType: StoreCardType) = viewModelScope.launch {
        queryServiceUnBlockStoreCard(storeCardType).collect { _blockMyCardResponse.emit(it) }
    }

    suspend fun queryServiceBlockStoreCard(storeCardType: StoreCardType) = getViewStateFlowForNetworkCall {
        queryServiceBlockStoreCard(position = currentPagePosition.value ?: -1, storeCardType = storeCardType)
    }

    suspend fun queryServiceUnBlockStoreCard(storeCardType: StoreCardType) = getViewStateFlowForNetworkCall {
        queryServiceUnBlockStoreCard(position = currentPagePosition.value ?: -1,storeCardType = storeCardType)
    }

}