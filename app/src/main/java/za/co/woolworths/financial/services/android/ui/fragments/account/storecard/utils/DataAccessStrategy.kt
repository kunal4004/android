package za.co.woolworths.financial.services.android.ui.fragments.account.storecard.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import kotlinx.coroutines.Dispatchers
import za.co.woolworths.financial.services.android.ui.fragments.account.storecard.utils.Result.Status.*

fun <T> performGetOperation(
    networkCall: suspend () -> Result<T>
): LiveData<Result<T>> =
    liveData(Dispatchers.IO) {
        emit(Result.loading())

        val responseStatus = networkCall.invoke()
        if (responseStatus.status == SUCCESS) {
//            saveCallResult(responseStatus.data!!)
            emit(Result.success(responseStatus.data!!))

        } else if (responseStatus.status == ERROR) {
            emit(Result.error(responseStatus.message!!))
        }
    }