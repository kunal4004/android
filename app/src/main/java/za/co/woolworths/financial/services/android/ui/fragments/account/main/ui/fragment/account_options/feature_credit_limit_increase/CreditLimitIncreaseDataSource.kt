package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_credit_limit_increase

import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.CoreDataSource
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard.WfsApiService
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.AccountProductLandingDao
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.IAccountProductLandingDao
import javax.inject.Inject

class CreditLimitIncreaseDataSource @Inject constructor(
    private val wfsApiService: WfsApiService,
    private val accountProductLandingDao: AccountProductLandingDao
) : CoreDataSource(),
    WfsApiService by wfsApiService,
    IAccountProductLandingDao by accountProductLandingDao,
    ICreditLimitIncrease {

    override fun isCliFlowHiddenForProductNotInGoodStanding(): Boolean = !isProductInGoodStanding()

    override suspend fun queryCliServiceOfferActive() = performSafeNetworkApiCall {
        val productOfferingId = getProductOfferingId().toString()
        queryServiceCliOfferActive(
            deviceIdentityToken = getDeviceIdentityToken(),
            productOfferingId = productOfferingId
        )
    }
}