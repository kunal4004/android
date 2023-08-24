package za.co.woolworths.financial.services.android.models.network

import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.crashlytics.ktx.setCustomKeys
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.JsonParseException
import retrofit2.Response
import za.co.woolworths.financial.services.android.models.dto.NetworkErrorResponse
import za.co.woolworths.financial.services.android.ui.activities.maintenance.NetworkRuntimeExceptionViewController
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.HTTP_NOT_FOUND_404
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.HTTP_SERVICE_UNAVAILABLE_503
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import java.net.SocketTimeoutException

class RetrofitException(
    private var url: String,
    private var exceptionCode: Any?,
    private val exception: Any?,
    private var exceptionResponse: Response<*>?,
) {
    fun show(): Boolean {
        return when {
            exception is SocketTimeoutException -> {
                FirebaseManager.logException(exception)
                NetworkRuntimeExceptionViewController().openSocketTimeOutDialog()
                true
            }
            (exceptionCode == HTTP_NOT_FOUND_404) -> {
                if (!url.contains("mobileconfigs")) {
                    NetworkRuntimeExceptionViewController().openMaintenanceView()
                }
                true
            }
            (exceptionCode == HTTP_SERVICE_UNAVAILABLE_503) -> {
                try {
                    val errorResponse =
                        Gson().fromJson((exceptionResponse)?.errorBody()?.charStream(),
                            NetworkErrorResponse::class.java)
                    if (url.contains("mobileconfigs") || errorResponse.redirectURL.isNullOrEmpty()) {
                        NetworkRuntimeExceptionViewController().openMaintenanceView()
                    } else {
                        NetworkRuntimeExceptionViewController().openWebViewErrorScreen(
                            errorResponse.redirectURL)
                    }
                } catch (jsonException: JsonParseException) {
                    FirebaseManager.logException(jsonException)
                    Firebase.crashlytics.setCustomKeys {
                        key("URL", url)
                        key("ExceptionResponse", exceptionResponse.toString())
                        key("ExceptionMessage", "this exception will be thrown when  NetworkResponse class is getting failed to parse")
                    }
                }
                true
            }
            else -> false
        }
    }
}