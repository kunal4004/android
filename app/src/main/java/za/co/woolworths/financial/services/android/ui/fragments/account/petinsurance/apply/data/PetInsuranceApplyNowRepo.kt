package za.co.woolworths.financial.services.android.ui.fragments.account.petinsurance.apply.data

import kotlinx.coroutines.flow.Flow
import za.co.woolworths.financial.services.android.models.dto.account.AppGUIDModel
import za.co.woolworths.financial.services.android.models.dto.account.AppGUIDRequestModel
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.CoreDataSource
import javax.inject.Inject

interface IPetInsuranceApplyNowRepo{
    suspend fun queryServicetAppGUID(appGUIDRequestModel: AppGUIDRequestModel ): Flow<CoreDataSource.IOTaskResult<AppGUIDModel>>
}

class PetInsuranceApplyNowRepo @Inject constructor(private val service : PetInsuranceApplyNowRemoteDataSource) :
    CoreDataSource(), IPetInsuranceApplyNowRepo {

    override suspend fun queryServicetAppGUID(appGUIDRequestModel: AppGUIDRequestModel) : Flow<IOTaskResult<AppGUIDModel>> = executeSafeNetworkApiCall {
        service.queryServicegetAppGUID(getDeviceIdentityToken(),appGUIDRequestModel) }
}


