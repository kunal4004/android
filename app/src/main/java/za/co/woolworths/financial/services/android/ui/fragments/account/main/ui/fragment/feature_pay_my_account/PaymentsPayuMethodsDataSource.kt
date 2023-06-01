package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.feature_pay_my_account

import kotlinx.coroutines.flow.Flow
import za.co.woolworths.financial.services.android.models.dto.pma.PaymentMethodsResponse
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.CoreDataSource
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard.WfsApiService
import javax.inject.Inject

interface  IPaymentsPayuMethodsDataSource {
   suspend fun requestPaymentsPayuMethods() : Flow<CoreDataSource.IOTaskResult<PaymentMethodsResponse>>
}

class PaymentsPayuMethodsDataSource  @Inject constructor(val wfsApiService: WfsApiService) : IPaymentsPayuMethodsDataSource, CoreDataSource() {

    override suspend fun requestPaymentsPayuMethods() = executeSafeNetworkApiCall {
        wfsApiService.getPaymentsPayUMethods(getDeviceIdentityToken())
    }

}