package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.network.datasource

import kotlinx.coroutines.flow.Flow
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.CoreDataSource
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard.WfsApiService
import za.co.woolworths.financial.services.android.ui.wfs.core.NetworkStatusUI
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.model.UserAccountResponse
import javax.inject.Inject

interface IUserAccountRemote {
    suspend fun getUserAccounts(): Flow<NetworkStatusUI<UserAccountResponse>>
    suspend fun getUserAccountByProductOfferingId(productOfferingId: Int): Flow<NetworkStatusUI<UserAccountResponse>>
}

class UserAccountRemoteDataSource @Inject constructor(private val service: WfsApiService) :
    CoreDataSource(), IUserAccountRemote {

    override suspend fun getUserAccounts(): Flow<NetworkStatusUI<UserAccountResponse>> =
        network { service.getAccounts(deviceIdentityToken = getDeviceIdentityToken()) }

    override suspend fun getUserAccountByProductOfferingId(productOfferingId: Int) =
        network {
            service.getUserAccountByProductOfferingId(
                deviceIdentityToken = getDeviceIdentityToken(),
                productOfferingId = productOfferingId.toString()
            )
        }
}