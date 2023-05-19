package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_appguid.data

import kotlinx.coroutines.flow.MutableStateFlow
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.ViewState
import za.co.woolworths.financial.services.android.ui.wfs.core.NetworkStatusUI
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_appguid.model.AppGUIDRequestType
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_appguid.model.AppGUIDResponse
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_appguid.network.AppGUIDRemoteDataSource
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_appguid.network.AppGUIDRemoteDataSourceImpl
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_appguid.network.AppGuidRequestResponseHandler
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_appguid.network.AppGuidRequestResponseHandlerImpl
import javax.inject.Inject

interface AppGuidProducer {
    suspend fun queryAppGuidRemoteService(state: MutableStateFlow<NetworkStatusUI<AppGUIDResponse>>)
}

class AppGuidProducerImpl @Inject constructor(
    private val dataSource: AppGUIDRemoteDataSourceImpl,
    private val handler: AppGuidRequestResponseHandlerImpl
) :AppGuidProducer,
    AppGUIDRemoteDataSource by dataSource,
    AppGuidRequestResponseHandler by handler {

    override suspend fun queryAppGuidRemoteService(state: MutableStateFlow<NetworkStatusUI<AppGUIDResponse>>) {
        val requestParam = AppGUIDRequestType.AppGUIDRequestSealed.PETINSURANCE.toRequestModel()
        fetchUserAppGuid(requestParam).collect { result ->
            when (result) {
                is ViewState.Loading -> isAppGuidInProgress(result, state)
                is ViewState.RenderSuccess -> handleAppGuidSuccess(result.output, state)
                else -> state.value
            }
        }
    }
}