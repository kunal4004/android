package za.co.woolworths.financial.services.android.models.dao

import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import com.awfs.coordination.R
import retrofit.RestAdapter
import za.co.wigroup.androidutils.Util
import za.co.woolworths.financial.services.android.contracts.OnCompletionListener
import za.co.woolworths.financial.services.android.contracts.OnResultListener
import za.co.woolworths.financial.services.android.models.ApiInterface
import za.co.woolworths.financial.services.android.models.dto.ConfigResponse
import za.co.woolworths.financial.services.android.util.FirebaseManager
import za.co.woolworths.financial.services.android.util.HttpAsyncTask

class MobileConfigServerDao {

    //static vars & functions

    companion object {

        fun getConfig(context: Context, onResultListener: OnResultListener<ConfigResponse>) {

            var firebaseRemoteConfig = FirebaseManager.getInstance().getRemoteConfig()
            val deviceID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

            if (firebaseRemoteConfig == null){
                //this ensures we're using defaults while the remote config
                //is yet to be retrieved.
                FirebaseManager.getInstance().setupRemoteConfig(object :OnCompletionListener{

                    override fun complete() {
                        getConfig(context, onResultListener)
                    }
                })
                return
            }

            val task = object : HttpAsyncTask<String, String, ConfigResponse>() {
                override fun httpDoInBackground(vararg strings: String): ConfigResponse {
                    val appName = firebaseRemoteConfig?.getString("mcs_appName")
                    var appVersion = ""
                    var environment = ""

                    try {
                        appVersion = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName
                        environment = com.awfs.coordination.BuildConfig.FLAVOR
                    } catch (e: PackageManager.NameNotFoundException) {
                        e.printStackTrace()
                    }

                    //MCS expects empty value for PROD
                    //appName-5.0 = PROD
                    //appName-5.0-qa = QA
                    //appName-5.0-dev = DEV
                    val majorMinorVersion = appVersion.substring(0, 3)
                    val mcsAppVersion = appName + "-" + majorMinorVersion + if (environment == "production") "" else "-$environment"

                    val mApiInterface = RestAdapter.Builder()
                            .setEndpoint(context.getString(R.string.config_endpoint))
                            .setLogLevel(if (Util.isDebug(context)) RestAdapter.LogLevel.FULL else RestAdapter.LogLevel.NONE)
                            .build()
                            .create(ApiInterface::class.java)

                    return mApiInterface.getConfig(firebaseRemoteConfig?.getString("mcs_appApiKey"), deviceID, mcsAppVersion)
                }

                override fun httpError(errorMessage: String, httpErrorCode: HttpAsyncTask.HttpErrorCode): ConfigResponse {
                    onResultListener.failure(errorMessage, httpErrorCode)
                    return ConfigResponse()
                }

                override fun httpDoInBackgroundReturnType(): Class<ConfigResponse> {
                    return ConfigResponse::class.java
                }

                override fun onPostExecute(configResponse: ConfigResponse) {
                    onResultListener.success(configResponse)
                    onResultListener.complete()
                }
            }

            task.execute()
        }
    }
}