package za.co.woolworths.financial.services.android.ui.fragments.account.main.data.repository.storecard

import kotlinx.coroutines.flow.Flow
import za.co.woolworths.financial.services.android.models.dto.EligibilityPlanResponse
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard.collection.CollectionRemoteDataSource
import za.co.woolworths.financial.services.android.ui.fragments.integration.utils.AccountApiResult
import javax.inject.Inject

interface ICollectionRepository {
    suspend fun fetchCollectionCheckEligibility(
        productGroupCode: String?
    ): Flow<AccountApiResult<EligibilityPlanResponse>?>?
}

class CollectionRepository @Inject constructor(private val collectionRemoteDataSource: CollectionRemoteDataSource) :
    ICollectionRepository {

    override suspend fun fetchCollectionCheckEligibility(productGroupCode: String?): Flow<AccountApiResult<EligibilityPlanResponse>?>? {
        productGroupCode ?: return null
        return collectionRemoteDataSource.fetchCollectionCheckEligibility(productGroupCode)
    }

}
