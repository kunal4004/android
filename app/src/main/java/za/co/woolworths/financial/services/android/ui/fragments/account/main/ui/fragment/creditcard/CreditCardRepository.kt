package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.creditcard

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.BaseDataSource
import javax.inject.Inject

interface ICreditCardRepository {

}

class CreditCardRepository @Inject constructor(
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val creditCardDataSource: CreditCardDataSource
) : BaseDataSource(), ICreditCardRepository {

    suspend fun queryCreditCardTokenService() = withContext(defaultDispatcher) { creditCardDataSource.queryServiceCreditCardToken() }

}