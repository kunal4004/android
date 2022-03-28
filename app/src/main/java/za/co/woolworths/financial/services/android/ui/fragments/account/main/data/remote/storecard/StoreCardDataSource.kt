package za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard

import kotlinx.coroutines.flow.Flow
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.CreditCardTokenResponse
import za.co.woolworths.financial.services.android.models.dto.pma.PaymentMethodsResponse
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsRequestBody
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsResponse
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.CoreDataSource
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.AccountProductLandingDao
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.IAccountProductLandingDao
import javax.inject.Inject

interface IStoreCardDataSource {
    val account: Account?
    suspend fun getCreditCardToken(): Flow<CoreDataSource.IOTaskResult<CreditCardTokenResponse>>
    suspend fun getPaymentPAYUMethod(): Flow<CoreDataSource.IOTaskResult<PaymentMethodsResponse>>
    suspend fun queryServiceGetStoreCards(): Flow<CoreDataSource.IOTaskResult<StoreCardsResponse>>
}

class StoreCardDataSource @Inject constructor(
    private val accountRemoteService: AccountRemoteService,
    private val landingDao: AccountProductLandingDao
) : CoreDataSource(), IStoreCardDataSource, IAccountProductLandingDao by landingDao,
    AccountRemoteService by accountRemoteService {

    override val account: Account? = product

    override suspend fun getCreditCardToken() = performSafeNetworkApiCall {
        getCreditCardToken(
            "", "",
            getSessionToken(),
            getDeviceIdentityToken()
        )
    }

    override suspend fun getPaymentPAYUMethod() = performSafeNetworkApiCall {
        getPaymentPAYUMethod(
            "", "",
            getSessionToken(),
            getDeviceIdentityToken()
        )
    }

    override suspend fun queryServiceGetStoreCards() = performSafeNetworkApiCall {

        val productOfferingId = getProductOfferingId()
        val visionAccountNumber = getVisionAccountNumber()
        val deviceIdentityToken = super.getDeviceIdentityToken()
        val storeCardRequest = StoreCardsRequestBody(
            visionAccountNumber = visionAccountNumber,
            productOfferingId = productOfferingId
        )

        queryServiceStoreCards(
            deviceIdentityToken,
            -33.8899,
            18.5066,
            storeCardRequest
        )

    }
}