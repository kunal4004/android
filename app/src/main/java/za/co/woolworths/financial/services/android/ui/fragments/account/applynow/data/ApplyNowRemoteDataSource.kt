package za.co.woolworths.financial.services.android.ui.fragments.account.applynow.data

import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.CoreDataSource
import javax.inject.Inject

class ApplyNowRemoteDataSource @Inject constructor(
    private val service: ApplyNowApiService
) : CoreDataSource(), ApplyNowApiService by service {


    suspend fun queryServiceApplyNow(contentId: String) = applyNowService(contentId)

}