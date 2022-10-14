package za.co.woolworths.financial.services.android.ui.fragments.account.applynow.data

import za.co.woolworths.financial.services.android.ui.fragments.account.applynow.utils.CoreDataSource
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard.WfsApiService
import javax.inject.Inject

class ApplyNowRemoteDataSource @Inject constructor(
    private val service: WfsApiService
) : CoreDataSource(), WfsApiService by service {

    suspend fun queryServiceApplyNow(contentId: String) = applyNowService(contentId)
}