package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_version_info.logic
import android.content.Context
import com.awfs.coordination.BuildConfig
import com.awfs.coordination.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
interface IApplicationInfo {
    fun getAppVersion(): String
    fun getFspNumberInfo(): String
}

class ApplicationInfoImpl @Inject constructor(@ApplicationContext private val context: Context) :
    IApplicationInfo {

    override fun getAppVersion(): String = context.getString(
        R.string.app_version_info,
        BuildConfig.VERSION_NAME,
        BuildConfig.VERSION_CODE.toString()
    )

    override fun getFspNumberInfo(): String = context.getString(R.string.fsp_number_info)

}