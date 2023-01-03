package za.co.woolworths.financial.services.android.ui.wfs.contact_us.usecase

import kotlinx.coroutines.flow.Flow
import za.co.woolworths.financial.services.android.models.network.GenericResponse
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.CoreDataSource
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.ViewState
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.getViewStateFlowForNetworkCall
import za.co.woolworths.financial.services.android.ui.fragments.contact_us.enquiry.EmailUsRequest
import za.co.woolworths.financial.services.android.ui.wfs.mobileconfig.IMobileConfigsContentDataSource
import za.co.woolworths.financial.services.android.ui.wfs.mobileconfig.MobileConfigsContentDataSource
import za.co.woolworths.financial.services.android.ui.wfs.contact_us.model.ContactUsRemoteModel
import javax.inject.Inject

interface IContactUsRepository {
    suspend fun queryServiceContactUsContentFromMobileConfig(): Flow<ViewState<ContactUsRemoteModel>>
    suspend fun  queryServicePostContactUsEmail(emailUsRequest: EmailUsRequest?): Flow<ViewState<GenericResponse>>
}

class ContactUsRepository @Inject constructor(private val source: MobileConfigsContentDataSource) :
    IContactUsRepository, IMobileConfigsContentDataSource by source {
    override suspend fun queryServiceContactUsContentFromMobileConfig()
    = getViewStateFlowForNetworkCall { fetchContactUsConfigFromMobileConfig() }

    override suspend fun queryServicePostContactUsEmail(emailUsRequest: EmailUsRequest?)
    = getViewStateFlowForNetworkCall { postContactUsEmail(emailUsRequest) }

}