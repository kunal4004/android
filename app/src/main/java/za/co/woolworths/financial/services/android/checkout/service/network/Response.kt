package za.co.woolworths.financial.services.android.checkout.service.network

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import java.io.Serializable

class ErrorResponse : Serializable {
    @SerializedName("code")
    var code: String? = null

    @SerializedName("desc")
    var desc: String? = null

    @SerializedName("message")
    var message: String? = null
}

fun Response<*>.toErrorResponse(): ErrorResponse?{
    val s = errorBody()?.string()
    return try {
        return if(s != null){
            Gson().fromJson(s, ErrorResponse::class.java)
        }
        else {
            null
        }
    }
    catch (j: JsonSyntaxException){

        null
    }
    catch (i: IllegalArgumentException){
        null
    }
    catch (e: Exception) {
        null
    }
}