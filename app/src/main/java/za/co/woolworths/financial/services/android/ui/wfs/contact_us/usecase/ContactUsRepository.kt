package za.co.woolworths.financial.services.android.ui.wfs.contact_us.usecase

import kotlinx.coroutines.flow.Flow
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.ViewState
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.getViewStateFlowForNetworkCall
import za.co.woolworths.financial.services.android.ui.wfs.mobileconfig.IMobileConfigsContentDataSource
import za.co.woolworths.financial.services.android.ui.wfs.mobileconfig.MobileConfigsContentDataSource
import za.co.woolworths.financial.services.android.ui.wfs.mobileconfig.RemoteMobileConfigModel
import javax.inject.Inject

interface IContactUsRepository {
    suspend fun queryServiceContactUsContentFromMobileConfig(): Flow<ViewState<RemoteMobileConfigModel>>
}

class ContactUsRepository @Inject constructor(private val source: MobileConfigsContentDataSource) :
    IContactUsRepository,
    IMobileConfigsContentDataSource by source {

    override suspend fun queryServiceContactUsContentFromMobileConfig() =
        getViewStateFlowForNetworkCall { fetchContactUsConfigFromMobileConfig() }

}