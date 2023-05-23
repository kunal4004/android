package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_message.data.network

import kotlinx.coroutines.flow.Flow
import za.co.woolworths.financial.services.android.models.dto.MessageResponse
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.CoreDataSource
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.ViewState
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard.WfsApiService
import javax.inject.Inject

interface MessageRemoteDataSource {
    suspend fun fetchAllMessages(pageSize : Int? = null, pageNumber: Int? = null): Flow<ViewState<MessageResponse>>
}

class MessageRemoteDataSourceImpl @Inject constructor(private val service: WfsApiService) : MessageRemoteDataSource, CoreDataSource() {

    override suspend fun fetchAllMessages(pageSize : Int?, pageNumber: Int?): Flow<ViewState<MessageResponse>> = withNetworkAPI {
        service.getMessages(deviceIdentityToken = getDeviceIdentityToken(), pageSize = pageSize ?: 5,pageNumber = pageNumber ?: 1)
    }

}