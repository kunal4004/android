package za.co.woolworths.financial.services.android.models.network

import androidx.annotation.StringRes
import com.awfs.coordination.R
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.HttpException
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.crashlytics.ktx.setCustomKeys
import com.google.firebase.ktx.Firebase
import com.google.gson.JsonParseException
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.NetworkAPIInvoke
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import java.io.IOException
import java.net.ConnectException

data class Resource<out T>(val status: Status, val data: T?, @StringRes val message: Int) {
    companion object {
        fun <T> success(data: T?): Resource<T> {
            return Resource(Status.SUCCESS, data, -1)
        }

        fun <T> error(@StringRes msgInt: Int, data: T?): Resource<T> {
            return Resource(Status.ERROR, data, msgInt)
        }

        fun <T> loading(data: T?): Resource<T> {
            return Resource(Status.LOADING, data, -1)
        }
    }
}

enum class Status {
    SUCCESS,
    ERROR,
    LOADING
}

suspend inline fun <reified T : Any> convertToResource(
    crossinline apiCall: NetworkAPIInvoke<T>
): Flow<Resource<T>> = flow {
    var errorBodyString = ""
    try {
        emit(Resource.loading(null))
        val response = apiCall.invoke()
        if (response.isSuccessful && response.body() != null) {
            emit(Resource.success(response.body()))
        } else {
            errorBodyString = response.errorBody()?.string()?: "{}"
            val parsedErrorBody = Gson().fromJson(errorBodyString, T::class.java)
            emit(Resource.error(msgInt = R.string.error_occured, data = parsedErrorBody))
        }
    } catch (e: Exception) {
        when (e) {
            is HttpException ->
                emit(Resource.error(R.string.error_occured, null))
            is IOException, is ConnectException ->
                emit(Resource.error(R.string.error_internet_connection, null))
            is JsonParseException, is IllegalStateException -> {
                val token = SessionUtilities.getInstance().jwt
                FirebaseManager.logException(e)
                Firebase.crashlytics.setCustomKeys {
                    key(
                        FirebaseManagerAnalyticsProperties.CrashlyticsKeys.ExceptionResponse,
                        errorBodyString
                    )
                    key(
                        FirebaseManagerAnalyticsProperties.CrashlyticsKeys.ExceptionMessage,
                        e.message.toString()
                    )
                    token?.C2Id?.let {
                        key(FirebaseManagerAnalyticsProperties.PropertyNames.C2ID, it)
                    }
                }
                emit(Resource.error(R.string.error_occured, null))
            }
            else ->
                emit(Resource.error(R.string.error_occured, null))
        }
    }
}.flowOn(Dispatchers.IO)