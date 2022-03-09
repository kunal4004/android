package za.co.woolworths.financial.services.android.ui.fragments.account.main.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import kotlinx.coroutines.Dispatchers
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.Result.Status.*

/*TODO :
    * databaseQuery will be used to retrieve data from DB
    * saveCallResult will be used to save the network call to database or doing any extra work on response
*/
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
            emit(Result.error(responseStatus.apiError!!, responseStatus.data))
        }
    }