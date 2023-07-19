package za.co.woolworths.financial.services.android.ui.activities.account.sign_in.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.models.dto.EligibilityPlanResponse
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.*
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard.IStoreCardDataSource
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard.StoreCardDataSource
import za.co.woolworths.financial.services.android.models.dto.account.AccountsProductGroupCode
import za.co.woolworths.financial.services.android.models.dto.npc.BlockMyCardResponse
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsResponse
import za.co.woolworths.financial.services.android.ui.fragments.account.card_not_received.data.CardNotReceivedDataSource
import za.co.woolworths.financial.services.android.ui.fragments.account.card_not_received.data.ICardNotReceivedService
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard.BlockStoreCardType
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard.StoreCardType
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.main.StoreCardFeatureType
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.utils.RetryNetworkRequest
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.router.CallBack
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.SingleLiveEvent
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.ApiResult
import za.co.woolworths.financial.services.android.util.DateHelper
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.capitaliseFirstLetterInEveryWord
import za.co.woolworths.financial.services.android.util.MyDateHelper
import javax.inject.Inject

enum class LoaderType {
    LANDING, FREEZE_CARD
}

data class RefreshApiModel(
    val refreshRequestStoreCardCards: Boolean = false,
    val refreshRequestCliActiveOffer: Boolean = false
)

data class StoreCardInfo(
    val feature: StoreCardFeatureType?,
    val position: Int,
    var isPopupVisibleInAccountLanding: Boolean,
    var isPopupVisibleInCardDetailLanding: Boolean
)

@HiltViewModel
class MyAccountsRemoteApiViewModel @Inject constructor(
    private val collection: TreatmentPlanDataSource,
    val dataSource: StoreCardDataSource,
    private val cardNotReceived: CardNotReceivedDataSource,
    private val dateHelper: MyDateHelper
) : ViewModel(), IStoreCardDataSource by dataSource,ICardNotReceivedService by cardNotReceived ,
    DateHelper by dateHelper
{

    var isStoreCardNotReceivedDialogFragmentVisible: Boolean  = false
    var mStoreCardType: StoreCardType = StoreCardType.None
    var mStoreCardFeatureType: StoreCardFeatureType? = null
    var loaderType : LoaderType = LoaderType.LANDING
    var refreshApiModel : RefreshApiModel  = RefreshApiModel()

    fun setRefreshRequestStoreCardCards(isActive : Boolean){
        refreshApiModel =  RefreshApiModel(refreshRequestStoreCardCards = isActive)
    }

    @Inject lateinit var retryNetworkRequest: RetryNetworkRequest

    val cardHolderName = KotlinUtils.getCardHolderNameSurname()?.capitaliseFirstLetterInEveryWord()

    var listOfStoreCardFeatureType : MutableList<StoreCardFeatureType>? = mutableListOf()

    private val _viewState = MutableStateFlow(CoreDataSource.IOTaskResult.Empty)
    val viewState = _viewState.asStateFlow()

    private val _payWithCard = MutableSharedFlow<Boolean>(0)
    val payWithCardTap: SharedFlow<Boolean> = _payWithCard

    fun setPayWithCard(wasTapped: Boolean){
        viewModelScope.launch {
            _payWithCard.emit(wasTapped)
        }
    }

    private val _notifyCardNotReceived = MutableSharedFlow<ViewState<BlockMyCardResponse>>(0)
    val notifyCardNotReceived: SharedFlow<ViewState<BlockMyCardResponse>> = _notifyCardNotReceived

    private val _payWithCardUnBlockCardResponse = MutableSharedFlow<ViewState<BlockMyCardResponse>>(0)
    val payWithCardUnBlockCardResponse: SharedFlow<ViewState<BlockMyCardResponse>> = _payWithCardUnBlockCardResponse

    private val _storeCardResponseResult = MutableStateFlow<ViewState<StoreCardsResponse>>(ViewState.Loading(true))
    val storeCardResponseResult: StateFlow<ViewState<StoreCardsResponse>> get() = _storeCardResponseResult

    private val _onCardTapEvent = MutableSharedFlow<StoreCardFeatureType>(0)
    val onCardTapEvent: SharedFlow<StoreCardFeatureType> get() = _onCardTapEvent

    private val _onViewPagerPageChangeListener = MutableSharedFlow<StoreCardInfo>(0)
    val onViewPagerPageChangeListener: SharedFlow<StoreCardInfo> get() = _onViewPagerPageChangeListener

    fun emitEventOnCardTap(storeCardFeatureType : StoreCardFeatureType?){
        viewModelScope.launch {
            storeCardFeatureType?.let  {
                _onCardTapEvent.emit(it)
            }
        }
    }

    fun getState() = AccountsProductGroupCode.getEnum(account?.productGroupCode)

    fun fetchCheckEligibilityTreatmentPlan(
        productGroupCode: String,
        successHandler: (EligibilityPlanResponse) -> Unit,
        failureHandler: (String) -> Unit
    ) {
        viewModelScope.launch {
            when (val checkEligibilityResponse =
                collection.fetchTreatmentPlanEligibility(productGroupCode.uppercase())) {
                is ApiResult.Success -> {
                    successHandler(checkEligibilityResponse.data)
                }
                is ApiResult.Error -> {
                    failureHandler(checkEligibilityResponse.exception.toString())
                }
                else -> Unit
            }
        }
    }

    fun requestGetStoreCardCards() = viewModelScope.launch {
        mapNetworkCallToViewStateFlow { dataSource.queryServiceGetStoreCards() }.collect{
            _storeCardResponseResult.value = it
        }
    }

    fun handleStoreCardResponseResult(response: StoreCardsResponse): MutableList<StoreCardFeatureType>? {
        dataSource.landingDao.storeCardsData = response
        val listOfStoreCards = dataSource.filterPrimaryCardsGetOneVirtualCardAndOnePrimaryCardOrBoth()
        listOfStoreCardFeatureType = listOfStoreCards
        return listOfStoreCards
    }

    fun queryServiceCardNotYetReceived() = viewModelScope.launch {
        mapNetworkCallToViewStateFlow {  cardNotReceived.queryServiceNotifyCardNotYetReceived()}.collect{
            _notifyCardNotReceived.emit(it)
        }
    }

    fun onManageCardPagerFragmentSelected(
        storeCardFeatureType: StoreCardFeatureType?,
        position: Int,
        isPopupVisibleInAccountLanding: Boolean,
        isPopupVisibleInCardDetailLanding: Boolean) {
        viewModelScope.launch {
            mStoreCardFeatureType = storeCardFeatureType
            _onViewPagerPageChangeListener.emit(StoreCardInfo(storeCardFeatureType, position, isPopupVisibleInAccountLanding, isPopupVisibleInCardDetailLanding))
        }
    }

    fun queryServiceBlockPayWithCardStoreCard() = viewModelScope.launch {
        mStoreCardType = StoreCardType.VirtualTempCard(block = BlockStoreCardType.BLOCK)
        mapNetworkCallToViewStateFlow {
            queryServiceBlockStoreCard(storeCardType = mStoreCardType)
        }.collect {
        }
    }

    fun queryServiceUnBlockPayWithCardStoreCard() = viewModelScope.launch {
        mapNetworkCallToViewStateFlow {
            mStoreCardType = StoreCardType.VirtualTempCard(block = BlockStoreCardType.UNBLOCK)
            queryServiceUnBlockStoreCard(storeCardType = mStoreCardType)
        }.collect {
            _payWithCardUnBlockCardResponse.emit(it)
        }
    }

}