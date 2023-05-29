package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_pet_insurance.data.network

import kotlinx.coroutines.flow.Flow
import za.co.woolworths.financial.services.android.models.dto.account.PetInsuranceModel
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.CoreDataSource
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard.WfsApiService
import za.co.woolworths.financial.services.android.ui.wfs.core.NetworkStatusUI
import javax.inject.Inject

interface PetInsuranceRemoteDataSource {
    suspend fun getPetInsurance(): Flow<NetworkStatusUI<PetInsuranceModel>>
}

class PetInsuranceRemoteDataSourceImpl @Inject constructor(private val service: WfsApiService)
    : PetInsuranceRemoteDataSource, CoreDataSource() {

    override suspend fun getPetInsurance(): Flow<NetworkStatusUI<PetInsuranceModel>>
    = network { service.getPetInsurance(deviceIdentityToken = getDeviceIdentityToken()) }

}