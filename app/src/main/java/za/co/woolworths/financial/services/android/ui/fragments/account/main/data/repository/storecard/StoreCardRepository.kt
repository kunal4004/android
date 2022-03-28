package za.co.woolworths.financial.services.android.ui.fragments.account.main.data.repository.storecard

import za.co.woolworths.financial.services.android.ui.fragments.account.main.data.remote.storecard.StoreCardDataSource
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.performGetOperation
import javax.inject.Inject

class StoreCardRepository @Inject constructor(
    private val storeCardDataSource: StoreCardDataSource
) {

//    fun getCreditCardToken() = performGetOperation(
//      //  networkCall = { storeCardDataSource.getCreditCardToken() }
//    )
//    fun getPaymentPAYUMethod() = performGetOperation(
//        networkCall = { storeCardDataSource.getPaymentPAYUMethod() }
//    )

}