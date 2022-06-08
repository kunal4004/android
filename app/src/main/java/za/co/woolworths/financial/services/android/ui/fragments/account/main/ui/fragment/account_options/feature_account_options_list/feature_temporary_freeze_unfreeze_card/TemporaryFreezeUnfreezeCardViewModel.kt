package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_account_options_list.feature_temporary_freeze_unfreeze_card

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.getViewStateFlowForNetworkCall
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard.IStoreCardDataSource
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard.StoreCardDataSource
import javax.inject.Inject

@HiltViewModel
class TemporaryFreezeUnfreezeCardViewModel @Inject constructor(private val storeCardDataSource: StoreCardDataSource) : ViewModel(), IStoreCardDataSource by storeCardDataSource {

    val isLoading: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    val isSwitcherEnabled: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }
    val currentPagePosition: MutableLiveData<Int> by lazy { MutableLiveData<Int>() }
    val isFlipAnimatedEnabled: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    suspend fun queryServiceBlockUnblockStoreCard() = getViewStateFlowForNetworkCall { storeCardDataSource.queryServiceBlockUnBlockStoreCard(position = currentPagePosition.value ?: -1) }
}