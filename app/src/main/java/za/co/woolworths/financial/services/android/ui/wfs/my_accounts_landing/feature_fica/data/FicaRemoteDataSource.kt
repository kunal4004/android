package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_fica.data

import kotlinx.coroutines.flow.Flow
import za.co.woolworths.financial.services.android.models.dto.account.FicaModel
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.CoreDataSource
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard.WfsApiService
import za.co.woolworths.financial.services.android.ui.wfs.core.NetworkStatusUI
import javax.inject.Inject

interface FicaRemoteDataSource {
    suspend fun queryFicaRemoteService(): Flow<NetworkStatusUI<FicaModel>>
}

class FicaRemoteDataSourceImpl @Inject constructor(private val service: WfsApiService) :
    FicaRemoteDataSource, CoreDataSource() {

    override suspend fun queryFicaRemoteService() =
        network { service.getFica(deviceIdentityToken = getDeviceIdentityToken()) }

}