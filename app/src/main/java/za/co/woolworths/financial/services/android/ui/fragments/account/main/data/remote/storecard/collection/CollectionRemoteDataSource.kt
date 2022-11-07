package za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard.collection

import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.BaseDataSource
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard.WfsApiService
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.AccountProductLandingDao
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.IAccountProductLandingDao
import javax.inject.Inject

class CollectionRemoteDataSource @Inject constructor(
    private val landingDao: AccountProductLandingDao,
    private val service: WfsApiService
) : BaseDataSource(), WfsApiService by service, IAccountProductLandingDao by landingDao {

    suspend fun queryServiceCheckCustomerEligibilityPlan() = queryServiceCheckCustomerEligibilityPlan(getProductGroupCode(), super.getDeviceIdentityToken())

}