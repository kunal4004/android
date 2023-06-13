package za.co.woolworths.financial.services.android.models.network

import android.content.Context
import android.location.Location
import android.os.Build
import com.awfs.coordination.BuildConfig
import za.co.wigroup.androidutils.Util
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.Utils

open class NetworkConfig(private val appContextProvider: AppContextProviderInterface) {

    private val appContext: Context
        get() = appContextProvider.appContext()

    fun getDeviceManufacturer(): String = Build.MANUFACTURER

    fun getDeviceModel(): String = Build.MODEL

    fun getSha1Password(): String = BuildConfig.SHA1

    fun getOS(): String = "Android"

    fun getApiId(): String = WoolworthsApplication.getApiId()

    fun getNetworkCarrier(): String {
        val networkCarrier = Util.getNetworkCarrier(appContext)
        return if (networkCarrier.isEmpty()) "Unavailable" else Utils.removeUnicodesFromString(networkCarrier)
    }

    fun getOsVersion(): String {
        var osVersion = Util.getOsVersion()
        if (osVersion == null || osVersion.isEmpty()) {
            val sdkVersion = Build.VERSION.SDK_INT // e.g. sdkVersion := 8;
            osVersion = sdkVersion.toString()
        }
        return osVersion
    }

    fun getSessionToken(): String {
        val sessionToken = SessionUtilities.getInstance().sessionToken
        return if (sessionToken.isEmpty()) "." else sessionToken
    }

    fun getMyLocation(): Location = Utils.getLastSavedLocation()
            ?: Location("myLocation")

    fun getDeviceIdentityToken(): String {
        return SessionUtilities.getInstance().deviceIdentityToken
    }

    fun getAppVersion(): String= WoolworthsApplication.getAppVersionName()

    fun getPageSize(): Int? {
        return AppConfigSingleton.searchApiSettings?.pageSize
    }


}
