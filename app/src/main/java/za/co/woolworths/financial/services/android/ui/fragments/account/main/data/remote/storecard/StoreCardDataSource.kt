package za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard

import android.location.Location
import kotlinx.coroutines.flow.Flow
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.CreditCardTokenResponse
import za.co.woolworths.financial.services.android.models.dto.npc.BlockCardRequestBody
import za.co.woolworths.financial.services.android.models.dto.npc.BlockMyCardResponse
import za.co.woolworths.financial.services.android.models.dto.npc.UnblockStoreCardRequestBody
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

enum class BlockStoreCardType { BLOCK, UNBLOCK, FREEZE, UNFREEZE, NONE }

sealed class StoreCardType {
    data class PrimaryCard(val block: BlockStoreCardType) : StoreCardType()
    data class VirtualTempCard(val block: BlockStoreCardType) : StoreCardType()
    object None : StoreCardType()
}

interface IStoreCardDataSource {
    var account: Account?
    suspend fun queryServiceCreditCardToken(): Flow<CoreDataSource.IOTaskResult<CreditCardTokenResponse>>
    suspend fun getPaymentPAYUMethod(): Flow<CoreDataSource.IOTaskResult<PaymentMethodsResponse>>
    suspend fun queryServiceGetStoreCards(): Flow<CoreDataSource.IOTaskResult<StoreCardsResponse>>
    suspend fun queryServiceBlockStoreCard(blockReason: Int? = 6, position: Int = 0,storeCardType :StoreCardType): Flow<CoreDataSource.IOTaskResult<BlockMyCardResponse>>
    suspend fun queryServiceUnBlockStoreCard(blockReason: Int? = 6, position: Int = 0,storeCardType :StoreCardType): Flow<CoreDataSource.IOTaskResult<BlockMyCardResponse>>
}

class StoreCardDataSource @Inject constructor(
    private val wfsApiService: WfsApiService,
    internal val landingDao: AccountProductLandingDao,
    private val manageCard: ManageCardFunctionalRequirementImpl,
) : CoreDataSource(), IStoreCardDataSource, IAccountProductLandingDao by landingDao,
    WfsApiService by wfsApiService, IManageCardFunctionalRequirement by manageCard {

    companion object {
        const val BLOCK_REASON = 6
    }

    override var account: Account? = product

    override suspend fun queryServiceCreditCardToken() = executeSafeNetworkApiCall {
        getCreditCardToken(
            "", "",
            getSessionToken(),
            getDeviceIdentityToken()
        )
    }

    override suspend fun getPaymentPAYUMethod() = executeSafeNetworkApiCall {
        getPaymentPAYUMethod(
            "", "",
            getSessionToken(),
            getDeviceIdentityToken()
        )
    }

    override suspend fun queryServiceGetStoreCards() = executeSafeNetworkApiCall {

        val productOfferingId = getProductOfferingId()
        val visionAccountNumber = getVisionAccountNumber()
        val deviceIdentityToken = super.getDeviceIdentityToken()
        val storeCardRequest = StoreCardsRequestBody(
            visionAccountNumber = visionAccountNumber,
            productOfferingId = productOfferingId
        )
        val location : Location? = Utils.getLastSavedLocation()
        //TODO :: Retrieve Locations...
        requestPostAccountsStoreCardCards(
            deviceIdentityToken,
            location?.latitude,
            location?.longitude,
            storeCardRequest
        )

    }

    override suspend fun queryServiceBlockStoreCard(blockReason: Int?, position: Int,storeCardType :StoreCardType) =
        executeSafeNetworkApiCall {
            val productOfferingId = getProductOfferingId()
            val visionAccountNumber =  getVisionAccountNumber()
            val deviceIdentityToken = super.getDeviceIdentityToken()
            val blockCardReason = blockReason ?: BLOCK_REASON
            val sequenceNumber =  getSequenceNumber(0)
            val cardNumber : String? = getCardNumber(0)
            val virtualTempCard = getVirtualCard()
            val blockStoreCardRequestBody =   when (storeCardType){
                is StoreCardType.VirtualTempCard -> BlockCardRequestBody(
                    visionAccountNumber,
                    virtualTempCard?.number ?: "",
                    virtualTempCard?.sequence ?: -1,
                    blockCardReason
                )
                else -> BlockCardRequestBody(
                    visionAccountNumber,
                    cardNumber ?: "",
                    sequenceNumber ?: -1,
                    blockCardReason
                )
            }

            queryServiceBlockStoreCard(
                deviceIdentityToken = deviceIdentityToken,
                productOfferingId = productOfferingId.toString(),
                blockStoreCardRequestBody
            )

        }


    override suspend fun queryServiceUnBlockStoreCard(
        blockReason: Int?,
        position: Int,storeCardType :StoreCardType): Flow<IOTaskResult<BlockMyCardResponse>> = executeSafeNetworkApiCall {

        val productOfferingId = getProductOfferingId()
        val visionAccountNumber = getVisionAccountNumber()
        val deviceIdentityToken = super.getDeviceIdentityToken()
        val cardNumber =  getCardNumber(0)
        val sequenceNumber =  getSequenceNumber(0)
        val virtualCard = getVirtualCard()

        val unblockStoreCardRequest : UnblockStoreCardRequestBody = when(storeCardType){
            is StoreCardType.VirtualTempCard -> UnblockStoreCardRequestBody(
                visionAccountNumber = visionAccountNumber ,
                cardNumber = virtualCard?.number ?: "" ,
                sequenceNumber  = virtualCard?.sequence?.toString() ?: "")

            else -> UnblockStoreCardRequestBody(
                visionAccountNumber,
                cardNumber ?: "",
                (sequenceNumber ?: -1).toString())
        }

        queryServiceUnBlockStoreCard(
            deviceIdentityToken = deviceIdentityToken,
            productOfferingId = productOfferingId.toString(),
            unblockStoreCardRequest
        )
    }

}