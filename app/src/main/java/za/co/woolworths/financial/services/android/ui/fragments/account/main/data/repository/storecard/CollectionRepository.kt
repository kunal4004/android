package za.co.woolworths.financial.services.android.ui.fragments.account.main.data.repository.storecard

import kotlinx.coroutines.flow.Flow
import za.co.woolworths.financial.services.android.models.dto.EligibilityPlanResponse
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.CoreDataSource
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard.collection.CollectionRemoteDataSource
import javax.inject.Inject

interface ICollectionRepository {
    suspend fun queryServiceCheckCustomerEligibilityPlan(): Flow<CoreDataSource.IOTaskResult<EligibilityPlanResponse>>
}

class CollectionRepository @Inject constructor(private val service : CollectionRemoteDataSource) :
    CoreDataSource(), ICollectionRepository {

    override suspend fun queryServiceCheckCustomerEligibilityPlan() : Flow<IOTaskResult<EligibilityPlanResponse>> = performSafeNetworkApiCall { service.queryServiceCheckCustomerEligibilityPlan() }
}
