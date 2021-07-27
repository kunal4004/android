package za.co.woolworths.financial.services.android.util.application

import com.awfs.coordination.BuildConfig
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.extension.bindString

class ApplicationInfoImpl : ApplicationInfoInterface {

    override fun appVersionInfo(): String {
        return bindString(
            R.string.app_version_info,
            BuildConfig.VERSION_NAME,
            BuildConfig.VERSION_CODE.toString()
        )
    }
}