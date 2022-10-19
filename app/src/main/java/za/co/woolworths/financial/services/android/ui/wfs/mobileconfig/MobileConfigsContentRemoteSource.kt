package za.co.woolworths.financial.services.android.ui.wfs.mobileconfig

import kotlinx.coroutines.flow.Flow
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.CoreDataSource
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard.WfsApiService
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.model.MobileConfigsContentId
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.model.ContactUsRemoteModel
import javax.inject.Inject

interface IMobileConfigsContentDataSource {
    suspend fun fetchContactUsConfigFromMobileConfig(): Flow<CoreDataSource.IOTaskResult<ContactUsRemoteModel>>
}

class MobileConfigsContentDataSource @Inject constructor(private val service: WfsApiService) :
    IMobileConfigsContentDataSource, CoreDataSource(), WfsApiService by service {

    override suspend fun fetchContactUsConfigFromMobileConfig(): Flow<IOTaskResult<ContactUsRemoteModel>> =
        performSafeNetworkApiCall { service.queryServiceMobileConfigsContent(MobileConfigsContentId.ContactUs.id) }

}