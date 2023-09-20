package za.co.woolworths.financial.services.android.ui.fragments.integration.utils

import android.content.Context
import android.os.Build
import android.util.Base64
import android.view.View
import android.view.WindowManager
import androidx.annotation.ColorRes
import androidx.annotation.NavigationRes
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.awfs.coordination.R
import retrofit2.HttpException
import za.co.absa.openbankingapi.AsymmetricCryptoHelper
import za.co.absa.openbankingapi.DecryptionFailureException
import za.co.absa.openbankingapi.SymmetricCipher
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.ui.fragments.integration.helper.AbsaTemporaryDataSourceSingleton
import za.co.woolworths.financial.services.android.ui.fragments.integration.helper.Aes256DecryptSymmetricCipherDelegate
import za.co.woolworths.financial.services.android.ui.fragments.integration.helper.Aes256EncryptSymmetricCipherDelegate
import za.co.woolworths.financial.services.android.ui.fragments.integration.service.model.AbsaProxyResponseProperty
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import za.co.woolworths.financial.services.android.util.NetworkManager
import java.util.*

fun resultOf(absaProxyResponseProperty: AbsaProxyResponseProperty): NetworkState<AbsaProxyResponseProperty> {
    return when (NetworkManager.getInstance().isConnectedToNetwork(WoolworthsApplication.getAppContext())) {
        true -> {
            NetworkState.Loading
            try {
                NetworkState.Success(absaProxyResponseProperty)
            } catch (h: HttpException) {
                NetworkState.Error(AbsaApiFailureHandler.HttpException(h.message(), h.code()))
            } catch (e: Exception) {
                NetworkState.Error(AbsaApiFailureHandler.Exception(e.message, e.hashCode()))
            }
        }
        false -> NetworkState.Error(AbsaApiFailureHandler.NoInternetApiFailure)
    }
}

fun ByteArray.toHex(separator: String = " "): String = joinToString(separator = separator) { eachByte -> "%02x".format(eachByte) }

fun ByteArray.toEncryptedHex(): String? {
   val logPublicKey =  AppConfigSingleton.logPublicKey
   return if (logPublicKey!=null) AsymmetricCryptoHelper().encryptToString(this.toHex(), AppConfigSingleton.logPublicKey) else null
}

fun String.contentLength(): Int? {
    AbsaTemporaryDataSourceSingleton.contentLength = length
    return AbsaTemporaryDataSourceSingleton.contentLength
}

fun String.toAes256Encrypt(): String {
    var encrypt: String? by Aes256EncryptSymmetricCipherDelegate()
    encrypt = this
    return encrypt ?: ""
}

fun String.toAes256Decrypt(): String {
    var decrypt: String? by Aes256DecryptSymmetricCipherDelegate()
    decrypt = this
    return decrypt ?: ""
}

fun String?.toFloatOrZero(): Float {
    return try {
        this?.toFloat() ?: 0F
    } catch (e: Exception) {
        0F
    }
}

fun String.toAes256DecryptBase64BodyToByteArray(): ByteArray? {
    val derivedSeed = AbsaTemporaryDataSourceSingleton.deriveSeeds
    val response = Base64.decode(this, Base64.DEFAULT)
    val ivForDecrypt = Arrays.copyOfRange(response, 0, 16)
    val encryptedResponse = Arrays.copyOfRange(response, 16, response.size)
    try {
        return SymmetricCipher.Aes256Decrypt(
            derivedSeed,
            encryptedResponse,
            ivForDecrypt
        )
    } catch (e: DecryptionFailureException) {
        FirebaseManager.logException(e)
    }
    return null
}

sealed class ApiResult<out T : Any> {
    data class Success<out T : Any>(val data: T) : ApiResult<T>()
    data class Failure<out T : Any>(val data: T) : ApiResult<T>()
    data class Error(val exception: Exception) : ApiResult<Nothing>()

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is Failure<*> ->  "Failure[data=$data]"
            is Error -> "Error[exception=$exception]"
        }
    }
}

fun Fragment.setNavigationBarColor(colorId: Int) {
    activity?.apply {
        window?.apply {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            navigationBarColor = ContextCompat.getColor(this.context, colorId)
        }
    }
}

/** Changes the System Bar Theme.  */
@RequiresApi(api = Build.VERSION_CODES.M)
private fun Fragment.setSystemBarTheme(isStatusBarFontDark: Boolean) {
    // Fetch the current flags.
    activity?.apply {
        val lFlags = window.decorView.systemUiVisibility
        // Update the SystemUiVisibility depending on whether we want a Light or Dark theme.
        window.decorView.systemUiVisibility =
            if (isStatusBarFontDark) lFlags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv() else lFlags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }
}


fun Fragment.updateStatusBarColor(@ColorRes colorId: Int, isStatusBarFontDark: Boolean = true) {
    activity?.apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = ContextCompat.getColor(this, colorId)
            setSystemBarTheme(isStatusBarFontDark)
        }
    }
}

/**
 * Accessing graph-scoped ViewModel of child NavHostFragment
 * using by navGraphViewModels
 */

inline fun <reified T: ViewModel> NavController.viewModel(@NavigationRes navGraphId: Int): T {
    val storeOwner = getViewModelStoreOwner(navGraphId)
    return ViewModelProvider(storeOwner)[T::class.java]
}

inline fun <T> T?.whenNull(block: T?.() -> Unit): T? {
    if (this == null) block()
    return this@whenNull
}

inline fun <T> T?.whenNonNull(block: T.() -> Unit): T? {
    this?.block()
    return this@whenNonNull
}


enum class ApiStatus{
    SUCCESS,
    ERROR,
    LOADING
}

sealed class AccountApiResult <out T> (val status: ApiStatus, val data: T?, val message:String?) {

    data class Success<out R>(val _data: R?): AccountApiResult<R>(
        status = ApiStatus.SUCCESS,
        data = _data,
        message = null
    )

    data class Error(val exception: String): AccountApiResult<Nothing>(
        status = ApiStatus.ERROR,
        data = null,
        message = exception
    )

    data class Loading<out R>(val _data: R?, val isLoading: Boolean): AccountApiResult<R>(
        status = ApiStatus.LOADING,
        data = _data,
        message = null
    )
}

//as extension function
fun ViewPager2.disableNestedScrolling() {
    (getChildAt(0) as? RecyclerView)?.apply {
        isNestedScrollingEnabled = false
        overScrollMode = View.OVER_SCROLL_NEVER
    }
}

fun String.getAccessibilityIdWithAppendedString(content: String, appendString: String): String {
    return content.replace(" ", "_").plus(if (appendString.isEmpty()) "" else "_".plus(appendString) ).lowercase()
}

fun Context.displayLabel() : String? = resources?.getString(R.string.view_your_payment_plan)

fun String.toMaskABSAPhoneNumber() = this.replace("\\d(?!\\d{0,2}\$|\\d{7,10}\$)".toRegex(), "*")