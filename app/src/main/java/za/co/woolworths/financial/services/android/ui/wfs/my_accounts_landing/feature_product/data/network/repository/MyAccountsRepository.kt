package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.network.repository

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.wfs.core.NetworkStatusUI
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.model.UserAccountResponse
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.network.datasource.IUserAccountRemote
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_product.data.network.datasource.UserAccountRemoteDataSource
import javax.inject.Inject

interface MyAccountsLandingRemoteService {
    suspend fun queryUserAccountService(
        isRefreshing: Boolean? = false,
        _state: MutableStateFlow<NetworkStatusUI<UserAccountResponse>>
    )
    suspend fun queryUserAccountService(
        isRefreshing: Boolean? = false,
        _state: MutableSharedFlow<NetworkStatusUI<UserAccountResponse>>
    )

    suspend fun queryUserAccountByProductOfferingId(productOfferingId: Int,
                                                    _state: MutableStateFlow<NetworkStatusUI<UserAccountResponse>>)

}

class MyAccountsLandingRemoteServiceImpl @Inject constructor(private val remote: UserAccountRemoteDataSource) :
    MyAccountsLandingRemoteService, IUserAccountRemote by remote {

    override suspend fun queryUserAccountService(isRefreshing: Boolean?,
                                                 _state: MutableStateFlow<NetworkStatusUI<UserAccountResponse>>) {
        OneAppService.forceNetworkUpdate = isRefreshing ?: false
        return getUserAccounts().collect { resource ->
            _state.update { resource }
        }
    }

    override suspend fun queryUserAccountService(isRefreshing: Boolean?,
                                                 _state: MutableSharedFlow<NetworkStatusUI<UserAccountResponse>>) {
        OneAppService.forceNetworkUpdate = isRefreshing ?: false
        return getUserAccounts().collect { resource ->
            _state.emit(resource)
        }
    }

    override suspend fun queryUserAccountByProductOfferingId(productOfferingId: Int,
                                                             _state: MutableStateFlow<NetworkStatusUI<UserAccountResponse>>) =
         getUserAccountByProductOfferingId(productOfferingId).collect { resource ->
             _state.update { resource }
        }

}