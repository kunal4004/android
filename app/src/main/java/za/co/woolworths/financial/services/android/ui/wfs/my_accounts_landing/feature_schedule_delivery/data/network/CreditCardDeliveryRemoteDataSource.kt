package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_schedule_delivery.data.network

import kotlinx.coroutines.flow.Flow
import za.co.woolworths.financial.services.android.models.dto.credit_card_delivery.CreditCardDeliveryStatusResponse
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.CoreDataSource
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard.WfsApiService
import za.co.woolworths.financial.services.android.ui.wfs.core.NetworkStatusUI
import javax.inject.Inject

interface CreditCardDeliveryRemoteDataSource {
    suspend fun getAccountsCardDeliveryStatus(
        envelopeReference: String,
        productOfferingId: String
    ): Flow<NetworkStatusUI<CreditCardDeliveryStatusResponse>>
}

class CreditCardDeliveryRemoteDataSourceImpl @Inject constructor(private val service: WfsApiService) :
    CreditCardDeliveryRemoteDataSource, CoreDataSource(), WfsApiService by service {

    override suspend fun getAccountsCardDeliveryStatus(
        envelopeReference: String,
        productOfferingId: String
    ) = network {
        service.getAccountsCardDeliveryStatus(
            deviceIdentityToken = getDeviceIdentityToken(),
            envelopeReference = envelopeReference,
            productOfferingId = productOfferingId
        )
    }
}