package za.co.woolworths.financial.services.android.ui.fragments.account.main.data.repository.storecard

import kotlinx.coroutines.flow.Flow
import za.co.woolworths.financial.services.android.models.dto.CreditCardTokenResponse
import za.co.woolworths.financial.services.android.models.dto.EligibilityPlanResponse
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.CoreDataSource
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard.collection.CollectionRemoteApiService
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard.collection.CollectionRemoteDataSource
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.creditcard.CreditCardService
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.creditcard.ICreditCardDataSource
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.AccountApiResult
import javax.inject.Inject

interface ICollectionRepository {
    suspend fun queryServiceCheckCustomerEligibilityPlan(
        productGroupCode: String
    ):  Flow<CoreDataSource.IOTaskResult<EligibilityPlanResponse>>
}

class CollectionRepository @Inject constructor(private val collectionRemoteApiService: CollectionRemoteDataSource) :
    CoreDataSource(), ICollectionRepository {

    override suspend fun queryServiceCheckCustomerEligibilityPlan(productGroupCode: String):Flow<IOTaskResult<EligibilityPlanResponse>> =
        performSafeNetworkApiCall { collectionRemoteApiService.queryServiceCheckCustomerEligibilityPlan(productGroupCode) }
}