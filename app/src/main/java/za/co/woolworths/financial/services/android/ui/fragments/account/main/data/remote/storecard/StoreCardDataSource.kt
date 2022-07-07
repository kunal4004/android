package za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard

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

enum class StoreCardType {PRIMARY_CARD, VIRTUAL_TEMP_CARD }

interface IStoreCardDataSource {
    var account: Account?
    suspend fun getCreditCardToken(): Flow<CoreDataSource.IOTaskResult<CreditCardTokenResponse>>
    suspend fun getPaymentPAYUMethod(): Flow<CoreDataSource.IOTaskResult<PaymentMethodsResponse>>
    suspend fun queryServiceGetStoreCards(): Flow<CoreDataSource.IOTaskResult<StoreCardsResponse>>
    suspend fun queryServiceBlockStoreCard(blockReason: Int? = 6, position: Int = 0,storeCardType :StoreCardType): Flow<CoreDataSource.IOTaskResult<BlockMyCardResponse>>
    suspend fun queryServiceUnBlockStoreCard(blockReason: Int? = 6, position: Int = 0,storeCardType :StoreCardType): Flow<CoreDataSource.IOTaskResult<BlockMyCardResponse>>
}

class StoreCardDataSource @Inject constructor(
    private val wfsApiService: WfsApiService,
    private val landingDao: AccountProductLandingDao,
    private val manageCard: ManageCardFunctionalRequirementImpl,
) : CoreDataSource(), IStoreCardDataSource, IAccountProductLandingDao by landingDao,
    WfsApiService by wfsApiService, IManageCardFunctionalRequirement by manageCard {

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

    override suspend fun queryServiceBlockStoreCard(blockReason: Int?, position: Int,storeCardType :StoreCardType) =
        performSafeNetworkApiCall {
            val productOfferingId = getProductOfferingId()
            val visionAccountNumber =  getVisionAccountNumber()
            val deviceIdentityToken = super.getDeviceIdentityToken()
            val blockCardReason = blockReason ?: BLOCK_REASON
            val sequenceNumber =  getSequenceNumber(0)
            val cardNumber : String? = getCardNumber(0)

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

    override suspend fun queryServiceUnBlockStoreCard(
        blockReason: Int?,
        position: Int,storeCardType :StoreCardType): Flow<IOTaskResult<BlockMyCardResponse>> = performSafeNetworkApiCall {

        val productOfferingId = getProductOfferingId()
        val visionAccountNumber = getVisionAccountNumber()
        val deviceIdentityToken = super.getDeviceIdentityToken()
        val cardNumber =  getCardNumber(0)
        val sequenceNumber =  getSequenceNumber(0)

        val blockStoreCardRequestBody = BlockCardRequestBody(
            visionAccountNumber,
            cardNumber ?: "",
            sequenceNumber ?: -1,
            null
        )

        queryServiceUnBlockStoreCard(
            deviceIdentityToken = deviceIdentityToken,
            productOfferingId = productOfferingId.toString(),
            blockStoreCardRequestBody
        )
    }

}