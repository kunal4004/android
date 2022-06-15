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
import javax.inject.Inject

@HiltViewModel
class TemporaryFreezeCardViewModel @Inject constructor(private val storeCardDataSource: StoreCardDataSource) : ViewModel(), IStoreCardDataSource by storeCardDataSource {

    val isTempFreezeUnFreezeLoading: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    val isSwitcherEnabled: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    val currentPagePosition: MutableLiveData<Int> by lazy { MutableLiveData<Int>() }


    private val _blockMyCardResponse = MutableSharedFlow<ViewState<BlockMyCardResponse>>(replay = 0)
    val blockMyCardResponse: SharedFlow<ViewState<BlockMyCardResponse>> get() = _blockMyCardResponse

    fun stopLoading() {
        viewModelScope.launch { _blockMyCardResponse.emit(ViewState.Loading(false)) }
    }

    fun queryServiceBlockCardTypeFreeze() = viewModelScope.launch {
        queryServiceBlockStoreCard().collect { _blockMyCardResponse.emit(it) }
    }

    fun queryServiceUnBlockCardTypeFreeze() = viewModelScope.launch {
        queryServiceUnBlockStoreCard().collect { _blockMyCardResponse.emit(it) }
    }

    suspend fun queryServiceBlockStoreCard() = getViewStateFlowForNetworkCall {
        queryServiceBlockStoreCard(position = currentPagePosition.value ?: -1)
    }

    suspend fun queryServiceUnBlockStoreCard() = getViewStateFlowForNetworkCall {
        queryServiceUnBlockStoreCard(position = currentPagePosition.value ?: -1)
    }

}