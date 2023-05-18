package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_appguid.network

import kotlinx.coroutines.flow.Flow
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.CoreDataSource
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.ViewState
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard.WfsApiService
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_appguid.model.AppGUIDRequestModel
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_appguid.model.AppGUIDResponse
import javax.inject.Inject

interface AppGUIDRemoteDataSource {
    suspend fun fetchUserAppGuid(requestModel: AppGUIDRequestModel): Flow<ViewState<AppGUIDResponse>>
}

class AppGUIDRemoteDataSourceImpl @Inject constructor(private val service : WfsApiService)
    : AppGUIDRemoteDataSource, CoreDataSource() {

    override suspend fun fetchUserAppGuid(requestModel: AppGUIDRequestModel)
    = withNetworkAPI { service.fetchAppGUID(deviceIdentityToken = getDeviceIdentityToken(), appGUIDRequestModel = requestModel) }

}