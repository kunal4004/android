package za.co.woolworths.financial.services.android.ui.fragments.account.storecard.data.repository

import za.co.woolworths.financial.services.android.ui.fragments.account.storecard.data.remote.StoreCardDataSource
import za.co.woolworths.financial.services.android.ui.fragments.account.storecard.utils.performGetOperation
import javax.inject.Inject

class StoreCardRepository @Inject constructor(
    private val storeCardDataSource: StoreCardDataSource
) {

    fun getCreditCardToken() = performGetOperation(
        networkCall = { storeCardDataSource.getCreditCardToken() }
    )


}