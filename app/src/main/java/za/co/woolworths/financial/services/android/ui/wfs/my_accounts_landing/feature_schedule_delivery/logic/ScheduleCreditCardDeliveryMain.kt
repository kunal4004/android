package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_schedule_delivery.logic

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.account.CreditCardDeliveryStatus
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.CreditCardDeliveryStatusResponse
import za.co.woolworths.financial.services.android.ui.wfs.core.NetworkStatusUI
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions.fetchFromLocalDatabase
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.enumtype.CreditCardType
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.model.UserAccountResponse
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.schema.ApplyNowStateMapToAccountBinNumber
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_schedule_delivery.data.network.CreditCardDeliveryRequestBuilder
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_schedule_delivery.data.network.CreditCardDeliveryRequestResponseBuilderImpl
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_schedule_delivery.data.network.CreditCardDeliveryRemoteDataSource
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_schedule_delivery.data.network.CreditCardDeliveryRemoteDataSourceImpl

import javax.inject.Inject

interface ScheduleCreditCardDeliveryMain {
    suspend fun queryServiceScheduleCreditCardDelivery(userAccountResponse: UserAccountResponse?, _state : MutableStateFlow<NetworkStatusUI<CreditCardDeliveryStatusResponse>>)
    fun onGetCreditCardDeliveryStatusSuccess(userAccountResponse: UserAccountResponse?, creditCardDeliveryResponse : CreditCardDeliveryStatusResponse?) : ApplyNowStateMapToAccountBinNumber

}

class ScheduleCreditCardDeliveryMainImpl @Inject constructor(
    private val service: CreditCardDeliveryRemoteDataSourceImpl,
    private val request: CreditCardDeliveryRequestResponseBuilderImpl
) : ScheduleCreditCardDeliveryMain,
    CreditCardDeliveryRemoteDataSource by service,
    CreditCardDeliveryRequestBuilder by request {

    override suspend fun queryServiceScheduleCreditCardDelivery(userAccountResponse: UserAccountResponse?, _state : MutableStateFlow<NetworkStatusUI<CreditCardDeliveryStatusResponse>>) {

        val key = fetchFromLocalDatabase(SessionDao.KEY.SCHEDULE_CREDIT_CARD_DELIVERY_ON_ACCOUNT_LANDING)
        if (userAccountResponse != null
            && (userAccountResponse.products?.size ?: 0) != 0
            && isScheduleCreditCardDeliveryServiceTriggered(userAccountResponse)
            && key.isNullOrEmpty()
         ){
            val params = getProductOfferingIdAndEnvelopeNumber(userAccountResponse)
            val productOfferingId = params.first?.toString()  ?: ""
            val envelopeNumber = params.second ?: ""
           getAccountsCardDeliveryStatus(productOfferingId = productOfferingId, envelopeReference = envelopeNumber).collect { result ->
               _state.update { result }
           }
        }
    }

    override fun onGetCreditCardDeliveryStatusSuccess(
        userAccountResponse: UserAccountResponse?,
        creditCardDeliveryResponse: CreditCardDeliveryStatusResponse?
    ): ApplyNowStateMapToAccountBinNumber {
        val statusDescription = creditCardDeliveryResponse?.statusResponse?.deliveryStatus?.statusDescription
        if (userAccountResponse != null && statusDescription?.equals(CreditCardDeliveryStatus.CARD_RECEIVED.name, ignoreCase = true) == true) {
            val accountNumberBin = getCreditCardAccount(userAccountResponse)?.accountNumberBin
            if (accountNumberBin != null) {
                val applyNowState = CreditCardType.getApplyNowState(accountNumberBin)
                return applyNowState to accountNumberBin
            }
        }

        return null
    }
}