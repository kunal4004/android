package za.co.woolworths.financial.services.android.models.dao

import retrofit.RestAdapter
import za.co.woolworths.financial.services.android.contracts.OnCompletionListener
import za.co.woolworths.financial.services.android.contracts.OnResultListener
import za.co.woolworths.financial.services.android.models.ApiInterface
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.ConfigResponse
import za.co.woolworths.financial.services.android.util.FirebaseManager
import za.co.woolworths.financial.services.android.util.HttpAsyncTask
import android.content.pm.PackageManager
import android.R.attr.versionName
import android.content.Context
import android.content.pm.PackageInfo
import android.text.TextUtils
import com.awfs.coordination.BuildConfig
import za.co.wigroup.androidutils.Util
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.Utils


class MobileConfigServerDao {

    //static vars & functions

    companion object {
        private val mcsUrl = "http://wfs-appserver-qa.wigroup.co:8080/wfs/app/v4"

        fun getConfig(appInstance: WoolworthsApplication, onResultListener: OnResultListener<ConfigResponse>) {

            val firebaseManager = FirebaseManager.getInstance()
            var firebaseRemoteConfig = firebaseManager.getRemoteConfig()

            if (firebaseRemoteConfig == null){
                //this ensures we're using defaults while the remote config
                //is yet to be retrieved.
                firebaseManager.setupRemoteConfig(object :OnCompletionListener{
                    override fun complete() {
                        getConfig(appInstance, onResultListener)
                    }
                })
                return
            }

            var pinfo: PackageInfo? = null
            try {
                pinfo = appInstance.packageManager.getPackageInfo(appInstance.packageName, 0)
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }

            var apiId = "iOS_v" // TODO: Prefix needs to be changed
            if (pinfo!!.versionName.length > 3) {
                apiId += pinfo!!.versionName.substring(0, 3)
            } else {
                apiId += pinfo!!.versionName
            }

            val task = object : HttpAsyncTask<String, String, ConfigResponse>() {
                override fun httpDoInBackground(vararg strings: String): ConfigResponse {

                    val mApiInterface = RestAdapter.Builder()
                            .setEndpoint(mcsUrl)
                            .build()
                            .create(ApiInterface::class.java)

                    return mApiInterface.getConfig(
                            apiId,
                            "aa78ee62cf4f89b6efc63689848fd36eb0680148",
                            getDeviceManufacturer(),
                            getDeviceModel(),
                            getNetworkCarrier(appInstance),
                            getOS(),
                            getOsVersion(),
                            getSessionToken(),
                            pinfo!!.versionName
                    )
                }

                override fun httpError(errorMessage: String, httpErrorCode: HttpAsyncTask.HttpErrorCode): ConfigResponse {
                    onResultListener.failure(errorMessage, httpErrorCode)
                    onResultListener.complete()
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


        private fun getOsVersion(): String {
            var osVersion = Util.getOsVersion()
            if (TextUtils.isEmpty(osVersion)) {
                val sdkVersion = android.os.Build.VERSION.SDK_INT // e.g. sdkVersion := 8;
                osVersion = sdkVersion.toString()
            }
            return osVersion
        }

        fun getOS(): String {
            return "Android"
        }

        private fun getNetworkCarrier(context: Context): String {
            val networkCarrier = Util.getNetworkCarrier(context)
            return if (networkCarrier.isEmpty()) "Unavailable" else Utils.removeUnicodesFromString(networkCarrier)
        }

        private fun getDeviceModel(): String {
            return Util.getDeviceModel()
        }

        private fun getDeviceManufacturer(): String {
            return Util.getDeviceManufacturer()
        }

        private fun getSha1Password(): String {
            return WoolworthsApplication.getSha1Password()
        }

        private fun getApiId(): String {
            return WoolworthsApplication.getApiKey()
        }

        private fun getSessionToken(): String {
            val sessionToken = SessionUtilities.getInstance().sessionToken
            return if (sessionToken.isEmpty())
                "."
            else
                sessionToken
        }
    }
}