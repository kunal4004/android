package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_device_security.data.network

import kotlinx.coroutines.flow.Flow
import za.co.woolworths.financial.services.android.models.dto.linkdevice.ViewAllLinkedDeviceResponse
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.CoreDataSource
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.ViewState
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard.WfsApiService
import javax.inject.Inject

interface DeviceSecurityDataSource {
    suspend fun fetchAllLinkedDevices(): Flow<ViewState<ViewAllLinkedDeviceResponse>>
    suspend fun deleteDevice(deviceIdentityId: String,
                             newPrimaryDeviceIdentityId: String?,
                             otp: String?, otpMethod: String?): Flow<ViewState<ViewAllLinkedDeviceResponse>>}

class DeviceSecurityDataSourceImpl @Inject constructor(private val service: WfsApiService) :
    CoreDataSource(), DeviceSecurityDataSource {

    override suspend fun fetchAllLinkedDevices(): Flow<ViewState<ViewAllLinkedDeviceResponse>> =
        withNetworkAPI { service.getAllLinkedDevices(deviceIdentityToken = getDeviceIdentityToken()) }

    override suspend fun deleteDevice(deviceIdentityId: String,
                                      newPrimaryDeviceIdentityId: String?,
                                     otp: String?, otpMethod: String?): Flow<ViewState<ViewAllLinkedDeviceResponse>>  =
        withNetworkAPI { service.deleteDevice(
            deviceIdentityId = deviceIdentityId,
            newPrimaryDeviceIdentityId = newPrimaryDeviceIdentityId,
            otp = otp,
            otpMethod = otpMethod,
            deviceIdentityToken = getDeviceIdentityToken()) }

}