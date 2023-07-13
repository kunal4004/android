package za.co.woolworths.financial.services.android.ui.fragments.account.applynow.data

import kotlinx.coroutines.flow.Flow
import za.co.woolworths.financial.services.android.models.dto.account.applynow.ApplyNowModel
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.CoreDataSource
import javax.inject.Inject

interface IApplyNowRepo{
    suspend fun queryServiceApplyNow(contentId:String): Flow<CoreDataSource.IOTaskResult<ApplyNowModel>>
}

class ApplyNowRepo @Inject constructor(private val service : ApplyNowRemoteDataSource) :
    CoreDataSource(), IApplyNowRepo {

    override suspend fun queryServiceApplyNow(contentId:String) : Flow<IOTaskResult<ApplyNowModel>> = executeSafeNetworkApiCall { service.queryServiceApplyNow(contentId) }
}


