package za.co.woolworths.financial.services.android.ui.fragments.account.storecard.data.remote

import javax.inject.Inject

class StoreCardDataSource @Inject constructor(
    private val storeCardService: StoreCardService
): BaseDataSource(){
    suspend fun TODOService() = getResult { storeCardService.TODOService()}
}