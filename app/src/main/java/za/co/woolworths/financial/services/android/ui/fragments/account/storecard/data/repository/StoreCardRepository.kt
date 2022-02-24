package za.co.woolworths.financial.services.android.ui.fragments.account.storecard.data.repository

import za.co.woolworths.financial.services.android.ui.fragments.account.storecard.data.remote.StoreCardDataSource
import za.co.woolworths.financial.services.android.ui.fragments.account.storecard.utils.performGetOperation
import javax.inject.Inject

class StoreCardRepository @Inject constructor(
    private val storeCardDataSource: StoreCardDataSource
){
    fun fetchCLIActiveOffer() = performGetOperation(
        networkCall = {storeCardDataSource.TODOService()}
        //TODO : saveCallResult will be used to save the network call to database or doing any extra work on response
//        saveCallResult = {localDataSource.updateOrSaveData(it)}
    )

}