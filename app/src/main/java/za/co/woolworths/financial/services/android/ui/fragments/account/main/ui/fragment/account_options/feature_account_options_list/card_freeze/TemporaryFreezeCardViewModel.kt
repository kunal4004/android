package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_account_options_list.card_freeze

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.npc.BlockMyCardResponse
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCard
import za.co.woolworths.financial.services.android.ui.fragments.account.device_security.DeviceSecurityFlagState
import za.co.woolworths.financial.services.android.ui.fragments.account.device_security.StoreCardUpsellMessageFlagState
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.ViewState
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.getViewStateFlowForNetworkCall
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard.BlockStoreCardType
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard.IStoreCardDataSource
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard.StoreCardDataSource
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard.StoreCardType
import za.co.woolworths.financial.services.android.util.Utils
import javax.inject.Inject

@HiltViewModel
class TemporaryFreezeCardViewModel @Inject constructor(private val storeCardDataSource: StoreCardDataSource,
 val mDeviceSecurityFlagState: DeviceSecurityFlagState) :
    ViewModel(), IStoreCardDataSource by storeCardDataSource {

    var mStoreCardUpsellMessageFlagState: StoreCardUpsellMessageFlagState =
        StoreCardUpsellMessageFlagState()

    // Store card type visible to user
    var mStoreCardType: StoreCardType = StoreCardType.None

    // Loader for temporary freeze UnFreeze api
    val isTempFreezeUnFreezeLoading: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    // Temporary freeze or unfreeze store card switcher listener value
    val isSwitcherEnabled: MutableLiveData<Boolean> by lazy { MutableLiveData<Boolean>() }

    // ViewPager current position value
    val currentPagePosition: MutableLiveData<Int> by lazy { MutableLiveData<Int>() }

    val showToastMessageOnStoreCardFreeze: MutableLiveData<Int> by lazy { MutableLiveData<Int>() }

    init {
        mDeviceSecurityFlagState.clearRevertSwitcher()
        currentPagePosition.value = 0
    }

    private val _blockMyCardResponse = MutableSharedFlow<ViewState<BlockMyCardResponse>>(replay = 0)
    val blockMyCardResponse: SharedFlow<ViewState<BlockMyCardResponse>> get() = _blockMyCardResponse

    fun stopLoading() {
        viewModelScope.launch { _blockMyCardResponse.emit(ViewState.Loading(false)) }
    }

    fun queryServiceBlockCardTypeFreeze() = viewModelScope.launch {
        mStoreCardType = StoreCardType.PrimaryCard(BlockStoreCardType.FREEZE)
        queryServiceBlockStoreCard(mStoreCardType).collect {
            _blockMyCardResponse.emit(
                it
            )
        }
    }

    fun queryServiceUnBlockCardTypeFreeze() = viewModelScope.launch {
        mStoreCardType = StoreCardType.PrimaryCard(BlockStoreCardType.UNFREEZE)
        queryServiceUnBlockStoreCard(mStoreCardType).collect { _blockMyCardResponse.emit(it) }
    }

    private suspend fun queryServiceBlockStoreCard(storeCardType: StoreCardType) =
        getViewStateFlowForNetworkCall {
            queryServiceBlockStoreCard(
                position = currentPagePosition.value ?: -1,
                storeCardType = storeCardType
            )
        }

    suspend fun queryServiceUnBlockStoreCard(storeCardType: StoreCardType) =
        getViewStateFlowForNetworkCall {
            queryServiceUnBlockStoreCard(
                position = currentPagePosition.value ?: -1,
                storeCardType = storeCardType
            )
        }

    fun isCardNotReceived(storeCard: StoreCard?): Boolean {
        val shouldNotifyUserByEmail =
            Utils.getSessionDaoValue(SessionDao.KEY.CARD_NOT_RECEIVED_DIALOG_WAS_SHOWN)
                .isNullOrEmpty()
        return (storeCard?.cardNotReceived == true && shouldNotifyUserByEmail)
    }

    fun resetCardPosition() {
        currentPagePosition.value = 0
    }

}