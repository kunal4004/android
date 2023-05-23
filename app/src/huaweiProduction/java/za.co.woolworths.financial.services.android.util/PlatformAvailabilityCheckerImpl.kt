package za.co.woolworths.financial.services.android.util

import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.huawei.hms.api.HuaweiApiAvailability
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

interface PlatformAvailabilityChecker {
    fun isGooglePlayOrHuaweiMobileServicesAvailable(): Boolean
    fun isGooglePlayServicesAvailable(): Boolean
    fun isHuaweiMobileServicesAvailable(): Boolean

}

class PlatformAvailabilityCheckerImpl @Inject constructor(@ApplicationContext private val context: Context) :
    PlatformAvailabilityChecker {

    override fun isGooglePlayOrHuaweiMobileServicesAvailable(): Boolean {
        return isGooglePlayServicesAvailable() || isHuaweiMobileServicesAvailable()
    }

    /**
     * Checks if Google Play Services is available on the device.
     * @return `true` if Google Play Services is available on the device, `false` otherwise.
     */
    override fun isGooglePlayServicesAvailable(): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context)
        return resultCode == ConnectionResult.SUCCESS
    }

    /**
     * Checks if Huawei Mobile Services is available on the device.
     * @return `true` if Huawei Mobile Services is available on the device, `false` otherwise.
     */
    override fun isHuaweiMobileServicesAvailable(): Boolean {
        val resultCode = HuaweiApiAvailability.getInstance().isHuaweiMobileServicesAvailable(context)
        return resultCode == ConnectionResult.SUCCESS
    }

}