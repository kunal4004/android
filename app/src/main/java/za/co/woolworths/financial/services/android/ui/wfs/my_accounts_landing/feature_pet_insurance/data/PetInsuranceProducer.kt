package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_pet_insurance.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import za.co.woolworths.financial.services.android.models.dto.account.PetInsuranceModel
import za.co.woolworths.financial.services.android.ui.wfs.core.NetworkStatusUI
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_pet_insurance.data.network.PetInsuranceRemoteDataSource
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_pet_insurance.data.network.PetInsuranceRemoteDataSourceImpl
import javax.inject.Inject

interface PetInsuranceDelegate {
    suspend fun queryPetInsurance(
        _state: MutableStateFlow<NetworkStatusUI<PetInsuranceModel>>)
}

class PetInsuranceDelegateImpl @Inject constructor(
    private val petRemote: PetInsuranceRemoteDataSourceImpl,
    private val petInsurance: PetInsuranceImpl
) : PetInsuranceDelegate,
    PetInsuranceRemoteDataSource by petRemote,
    PetInsurance by petInsurance {

    override suspend fun queryPetInsurance(_state : MutableStateFlow<NetworkStatusUI<PetInsuranceModel>>) {
        if (isPetInsuranceEnabled()) {
            getPetInsurance().collect { result -> _state.update { result } }
        }
    }
}
