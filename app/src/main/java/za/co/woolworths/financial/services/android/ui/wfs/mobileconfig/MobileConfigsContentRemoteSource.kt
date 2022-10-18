package za.co.woolworths.financial.services.android.ui.wfs.mobileconfig

import kotlinx.coroutines.flow.Flow
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.CoreDataSource
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard.WfsApiService
import javax.inject.Inject

interface IMobileConfigsContentDataSource {
    suspend fun fetchContactUsConfigFromMobileConfig(): Flow<CoreDataSource.IOTaskResult<RemoteMobileConfigModel>>
}

class MobileConfigsContentDataSource @Inject constructor(private val service: WfsApiService) :
    IMobileConfigsContentDataSource, CoreDataSource(), WfsApiService by service {

    override suspend fun fetchContactUsConfigFromMobileConfig(): Flow<IOTaskResult<RemoteMobileConfigModel>> =
        performSafeNetworkApiCall { service.queryServiceMobileConfigsContent(MobileConfigsContentId.ContactUs.id) }

}