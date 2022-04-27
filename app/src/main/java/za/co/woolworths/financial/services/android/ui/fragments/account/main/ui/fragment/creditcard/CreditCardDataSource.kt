package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.creditcard

import kotlinx.coroutines.flow.Flow
import za.co.woolworths.financial.services.android.models.dto.CreditCardTokenResponse
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.CoreDataSource
import javax.inject.Inject

interface ICreditCardDataSource {
    suspend fun queryServiceCreditCardToken(): Flow<CoreDataSource.IOTaskResult<CreditCardTokenResponse>>
}

class CreditCardDataSource @Inject constructor(private val creditCardService: CreditCardService) :
    CoreDataSource(), ICreditCardDataSource {

    override suspend fun queryServiceCreditCardToken(): Flow<IOTaskResult<CreditCardTokenResponse>> =
        performSafeNetworkApiCall { creditCardService.getCreditCardToken() }
}