package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_device_security.data.network

import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.models.repository.AppStateRepository
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.renderSuccess
import javax.inject.Inject

interface DeviceSecurityRemoteRepository{
    suspend fun getAllLinkedDevices(isRefresh : Boolean = false)
}

class DeviceSecurityRemoteRepositoryImpl @Inject constructor(private val service : DeviceSecurityDataSourceImpl) :
    DeviceSecurityRemoteRepository {
    override suspend fun getAllLinkedDevices(isRefresh : Boolean) {
        OneAppService.forceNetworkUpdate  = isRefresh
        service.fetchAllLinkedDevices().collect {
            it.renderSuccess {
                AppStateRepository().saveLinkedDevices(output.userDevices)
            }
        }
    }

}