package za.co.woolworths.financial.services.android.util

import com.huawei.agconnect.crash.AGConnectCrash
import za.co.woolworths.financial.services.android.util.Utils
import javax.inject.Inject

class HuaweiQualityCrashInstance @Inject constructor() {

    init {
        if (Utils.isHuaweiMobileServicesAvailable())
            AGConnectCrash.getInstance().enableCrashCollection(true)
    }

}