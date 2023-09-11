package za.co.woolworths.financial.services.android.geolocation.network.repository

import kotlinx.coroutines.flow.Flow
import za.co.woolworths.financial.services.android.geolocation.network.validatestoremodel.ValidateStoreResponse
import za.co.woolworths.financial.services.android.models.network.ApiInterface
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.CoreDataSource
import javax.inject.Inject

/**
 * Created by Kunal Uttarwar on 08/09/23.
 */
class ConfirmAddressRepositoryImp @Inject constructor(private val apiInterface: ApiInterface) :
    ConfirmAddressRepository, CoreDataSource(), ApiInterface by apiInterface {
    override suspend fun callValidateStoreInventory(
        placeId: String,
        storeId: String,
    ): Flow<IOTaskResult<ValidateStoreResponse>> =
        executeSafeNetworkApiCall {
            callValidateStoreInventory(
                "",
                "",
                super.getSessionToken(),
                super.getDeviceIdentityToken(),
                placeId,
                storeId
            )
        }
}
