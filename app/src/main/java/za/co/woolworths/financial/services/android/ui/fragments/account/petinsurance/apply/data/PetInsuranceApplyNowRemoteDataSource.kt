package za.co.woolworths.financial.services.android.ui.fragments.account.petinsurance.apply.data

import za.co.woolworths.financial.services.android.models.dto.account.AppGUIDRequestModel
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.CoreDataSource
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard.WfsApiService
import javax.inject.Inject

class PetInsuranceApplyNowRemoteDataSource @Inject constructor(
    private val service: WfsApiService
) : CoreDataSource(), WfsApiService by service {

    suspend fun queryServicegetAppGUID(deviceIdentityToken:String,appGUIDRequestModel: AppGUIDRequestModel) = getAppGUID( deviceIdentityToken,appGUIDRequestModel )
}