package za.co.woolworths.financial.services.android.models.network

import android.content.Context
import android.os.Build
import android.text.TextUtils
import za.co.wigroup.androidutils.Util
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.Utils

open class NetworkConfig {

    fun appContext(): Context = WoolworthsApplication.getAppContext()

    fun getDeviceManufacturer(): String = Build.MANUFACTURER

    fun getDeviceModel(): String = Build.MODEL

    fun getOS(): String = "Android"

    fun getNetworkCarrier(context: Context): String {
        val networkCarrier = Util.getNetworkCarrier(context)
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
}