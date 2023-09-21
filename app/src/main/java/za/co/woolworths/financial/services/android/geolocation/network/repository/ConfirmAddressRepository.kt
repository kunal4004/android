package za.co.woolworths.financial.services.android.geolocation.network.repository

import kotlinx.coroutines.flow.Flow
import za.co.woolworths.financial.services.android.geolocation.network.validatestoremodel.ValidateStoreResponse
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.CoreDataSource

/**
 * Created by Kunal Uttarwar on 08/09/23.
 */
interface ConfirmAddressRepository {
    suspend fun callValidateStoreInventory(
        placeId: String,
        storeId: String,
    ): Flow<CoreDataSource.IOTaskResult<ValidateStoreResponse>>
}
