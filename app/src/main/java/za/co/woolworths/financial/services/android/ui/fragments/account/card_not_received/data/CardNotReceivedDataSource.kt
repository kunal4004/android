package za.co.woolworths.financial.services.android.ui.fragments.account.card_not_received.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import za.co.woolworths.financial.services.android.models.dto.account.AccountsProductGroupCode
import za.co.woolworths.financial.services.android.models.dto.npc.BlockMyCardResponse
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.CoreDataSource
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard.WfsApiService
import za.co.woolworths.financial.services.android.util.SessionUtilities
import javax.inject.Inject

interface ICardNotReceivedService {
    suspend fun queryServiceNotifyCardNotYetReceived(): Flow<CoreDataSource.IOTaskResult<BlockMyCardResponse>>
}

data class CardNotReceived(
    val emailBody: String? = null,
    val enquiryType: String? = "cardNotReceived",
    val preferredEmail: String?,
    val preferredName: String?,
    val productGroupCode: String? = null
)

class CardNotReceivedDataSource @Inject constructor(@ApplicationContext val context: Context, private val wfsApiService: WfsApiService) :
    CoreDataSource(), ICardNotReceivedService, WfsApiService by wfsApiService {

    override suspend fun queryServiceNotifyCardNotYetReceived() = performSafeNetworkApiCall {

        val jwtDecodedModel = SessionUtilities.getInstance().jwt
        val preferredEmail = jwtDecodedModel.email?.get(0)
        val emailBody = ""
        val preferredName = jwtDecodedModel.name?.get(0)

        queryServiceNotifyCardNotYetReceived(
            emailId = "cardNotReceived",
            body = CardNotReceived(
                preferredEmail = preferredEmail,
                emailBody = emailBody,
                preferredName = preferredName,
                productGroupCode = AccountsProductGroupCode.STORE_CARD.groupCode.uppercase()
            )
        )
    }

}