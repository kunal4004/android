package za.co.woolworths.financial.services.android.ui.activities.account.sign_in.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.EligibilityPlanResponse
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.*
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard.IStoreCardDataSource
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard.StoreCardDataSource
import za.co.woolworths.financial.services.android.models.dto.account.AccountsProductGroupCode
import za.co.woolworths.financial.services.android.models.dto.npc.BlockMyCardResponse
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsResponse
import za.co.woolworths.financial.services.android.ui.fragments.account.card_not_received.data.CardNotReceivedDataSource
import za.co.woolworths.financial.services.android.ui.fragments.account.card_not_received.data.ICardNotReceivedService
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.main.StoreCardFeatureType
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.utils.RetryNetworkRequest
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.ApiResult
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.capitaliseFirstLetterInEveryWord
import javax.inject.Inject

enum class LoaderType {
    LANDING, FREEZE_CARD
}

@HiltViewModel
class MyAccountsRemoteApiViewModel @Inject constructor(
    private val collection: TreatmentPlanDataSource,
    val dataSource: StoreCardDataSource,
    private val cardNotReceived: CardNotReceivedDataSource

) : ViewModel(), IStoreCardDataSource by dataSource,ICardNotReceivedService by cardNotReceived {

    var mStoreCardFeatureType: StoreCardFeatureType? = null
    var loaderType : LoaderType = LoaderType.LANDING

    @Inject lateinit var retryNetworkRequest: RetryNetworkRequest

    val cardHolderName = KotlinUtils.getCardHolderNameSurname()?.capitaliseFirstLetterInEveryWord()

    var listOfStoreCardFeatureType : MutableList<StoreCardFeatureType>? = mutableListOf()

    private val _viewState = MutableStateFlow(CoreDataSource.IOTaskResult.Empty)
    val viewState = _viewState.asStateFlow()

    private val _notifyCardNotReceived = MutableSharedFlow<ViewState<BlockMyCardResponse>>(0)
    val notifyCardNotReceived: SharedFlow<ViewState<BlockMyCardResponse>> = _notifyCardNotReceived

    private val _storeCardResponseResult = MutableStateFlow<ViewState<StoreCardsResponse>>(ViewState.Loading(true))
    val storeCardResponseResult: StateFlow<ViewState<StoreCardsResponse>> get() = _storeCardResponseResult

    private val _onCardTapEvent = MutableSharedFlow<StoreCardFeatureType>(0)
    val onCardTapEvent: SharedFlow<StoreCardFeatureType> get() = _onCardTapEvent

    private val _onViewPagerPageChangeListener = MutableSharedFlow<Pair<StoreCardFeatureType?, Int>>(0)
    val onViewPagerPageChangeListener: SharedFlow<Pair<StoreCardFeatureType?,Int>> get() = _onViewPagerPageChangeListener

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
        getViewStateFlowForNetworkCall { dataSource.queryServiceGetStoreCards() }.collect{
            _storeCardResponseResult.value = it
        }
    }

    fun handleStoreCardResponseResult(response: StoreCardsResponse): MutableList<StoreCardFeatureType>? {
        SaveResponseDao.setValue(SessionDao.KEY.STORE_CARD_RESPONSE_PAYLOAD, response)
        dataSource.refreshStoreCardsData()
        val listOfStoreCards = dataSource.filterPrimaryCardsGetOneVirtualCardAndOnePrimaryCardOrBoth()
        listOfStoreCardFeatureType = listOfStoreCards
        return listOfStoreCards
    }

    fun queryServiceCardNotYetReceived() = viewModelScope.launch {
        getViewStateFlowForNetworkCall {  cardNotReceived.queryServiceNotifyCardNotYetReceived()}.collect{
            _notifyCardNotReceived.emit(it)
        }
    }

    fun onCardPagerPageSelected(storeCardFeatureType: StoreCardFeatureType?, position: Int) {
        viewModelScope.launch {
            mStoreCardFeatureType = storeCardFeatureType
            _onViewPagerPageChangeListener.emit(Pair(storeCardFeatureType, position))
        }
    }

}