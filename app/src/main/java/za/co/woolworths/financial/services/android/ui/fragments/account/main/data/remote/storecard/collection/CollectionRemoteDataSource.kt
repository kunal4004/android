package za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard.collection

import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.BaseDataSource
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.toResultFlow
import za.co.woolworths.financial.services.android.ui.fragments.account.storecard.data.remote.collection.CollectionRemoteApiService
import javax.inject.Inject

class CollectionRemoteDataSource @Inject constructor(private val collectionRemoteApiService: CollectionRemoteApiService) :
    BaseDataSource() {

    suspend fun queryServiceCheckCustomerEligibilityPlan(productGroupCode: String) = toResultFlow {
        collectionRemoteApiService.queryServiceCheckCustomerEligibilityPlan(
            "",
            "",
            getSessionToken(),
            productGroupCode,
            getDeviceIdentityToken()
        )
    }
}