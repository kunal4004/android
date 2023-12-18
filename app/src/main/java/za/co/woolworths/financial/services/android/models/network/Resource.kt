package za.co.woolworths.financial.services.android.models.network

import androidx.annotation.StringRes
import com.awfs.coordination.R
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.HttpException
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.NetworkAPIInvoke
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
    try {
        emit(Resource.loading(null))
        val response = apiCall.invoke()
        if (response.isSuccessful && response.body() != null) {
            emit(Resource.success(response.body()))
        } else {
            val errorBodyString = response.errorBody()?.string() ?: "{}"
//            val errorBodyString = "<html> </html>"
            val parsedErrorBody = Gson().fromJson(errorBodyString, T::class.java)
            emit(Resource.error(msgInt = R.string.error_occured, data = parsedErrorBody))
        }
    } catch (e: JsonSyntaxException) {
        emit(Resource.error(R.string.error_occured, null))
    } catch (e: HttpException) {
        emit(Resource.error(R.string.error_occured, null))
    } catch (e: IOException) {
        emit(Resource.error(R.string.error_internet_connection, null))
    } catch (e: ConnectException) {
        emit(Resource.error(R.string.error_internet_connection, null))
    } catch (e : Exception) {
        emit(Resource.error(R.string.error_occured, null))
    }
}.flowOn(Dispatchers.IO)