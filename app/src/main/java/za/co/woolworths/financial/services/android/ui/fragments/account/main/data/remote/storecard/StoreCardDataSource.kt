package za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard

import android.location.Location
import kotlinx.coroutines.flow.Flow
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.CreditCardTokenResponse
import za.co.woolworths.financial.services.android.models.dto.npc.BlockCardRequestBody
import za.co.woolworths.financial.services.android.models.dto.npc.BlockMyCardResponse
import za.co.woolworths.financial.services.android.models.dto.pma.PaymentMethodsResponse
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsRequestBody
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsResponse
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.CoreDataSource
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.AccountProductLandingDao
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.IAccountProductLandingDao
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.main.IManageCardFunctionalRequirement
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.main.ManageCardFunctionalRequirementImpl
import za.co.woolworths.financial.services.android.util.Utils
import javax.inject.Inject

interface IStoreCardDataSource {
    var account: Account?
    suspend fun getCreditCardToken(): Flow<CoreDataSource.IOTaskResult<CreditCardTokenResponse>>
    suspend fun getPaymentPAYUMethod(): Flow<CoreDataSource.IOTaskResult<PaymentMethodsResponse>>
    suspend fun queryServiceGetStoreCards(): Flow<CoreDataSource.IOTaskResult<StoreCardsResponse>>
    suspend fun queryServiceBlockUnBlockStoreCard(
        blockReason: Int? = 6,
        position: Int
    ): Flow<CoreDataSource.IOTaskResult<BlockMyCardResponse>>
}

class StoreCardDataSource @Inject constructor(
    private val accountRemoteService: AccountRemoteService,
    private val landingDao: AccountProductLandingDao,
    private val manageCard: ManageCardFunctionalRequirementImpl,
) : CoreDataSource(), IStoreCardDataSource, IAccountProductLandingDao by landingDao,
    AccountRemoteService by accountRemoteService, IManageCardFunctionalRequirement by manageCard {

    companion object {
        const val BLOCK_REASON = 6
    }

    override var account: Account? = product

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
        val location = Utils.getLastSavedLocation()
        //TODO :: Retrieve Locations...
        queryServiceStoreCards(
            deviceIdentityToken,
            location.latitude,
            location.longitude,
            storeCardRequest
        )

    }

    override suspend fun queryServiceBlockUnBlockStoreCard(blockReason: Int?, position: Int) =
        performSafeNetworkApiCall {
            val productOfferingId = getProductOfferingId()
            val visionAccountNumber = getVisionAccountNumber()
            val deviceIdentityToken = super.getDeviceIdentityToken()
            val blockCardReason = blockReason ?: BLOCK_REASON
            val cardNumber = getCardNumber(position)
            val sequenceNumber = getSequenceNumber(position)

            val blockStoreCardRequestBody = BlockCardRequestBody(
                visionAccountNumber,
                cardNumber ?: "",
                sequenceNumber ?: -1,
                blockCardReason
            )

            queryServiceBlockStoreCard(
                deviceIdentityToken = deviceIdentityToken,
                productOfferingId = productOfferingId.toString(),
                blockStoreCardRequestBody
            )

        }
}