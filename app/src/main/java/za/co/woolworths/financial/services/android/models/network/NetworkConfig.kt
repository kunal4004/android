package za.co.woolworths.financial.services.android.models.network

import android.content.Context
import android.location.Location
import android.os.Build
import android.text.TextUtils
import com.awfs.coordination.BuildConfig
import za.co.wigroup.androidutils.Util
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.Utils

open class NetworkConfig {

    fun appContext(): Context = WoolworthsApplication.getAppContext()

    fun getDeviceManufacturer(): String = Build.MANUFACTURER

    fun getDeviceModel(): String = Build.MODEL

    fun getSha1Password(): String = BuildConfig.SHA1

    fun getOS(): String = "Android"

    fun getApiId(): String = WoolworthsApplication.getApiId()

    fun getNetworkCarrier(): String {
        val networkCarrier = Util.getNetworkCarrier(appContext())
        return if (networkCarrier.isEmpty()) "Unavailable" else Utils.removeUnicodesFromString(networkCarrier)
    }

    fun getOsVersion(): String {
        var osVersion = Util.getOsVersion()
        if (TextUtils.isEmpty(osVersion)) {
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
}
