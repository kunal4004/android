package za.co.woolworths.financial.services.android.models.dao

import android.content.Context
import android.text.TextUtils
import com.awfs.coordination.BuildConfig
import retrofit.RestAdapter
import za.co.wigroup.androidutils.Util
import za.co.woolworths.financial.services.android.contracts.OnResultListener
import za.co.woolworths.financial.services.android.models.ApiInterface
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.ConfigResponse
import za.co.woolworths.financial.services.android.util.HttpAsyncTask
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.Utils


class MobileConfigServerDao {

    //static vars & functions

    companion object {

        fun getConfig(appInstance: WoolworthsApplication, onResultListener: OnResultListener<ConfigResponse>) {

            val task = object : HttpAsyncTask<String, String, ConfigResponse>() {
                override fun httpDoInBackground(vararg strings: String): ConfigResponse {

                    val mApiInterface = RestAdapter.Builder()
                            .setEndpoint(BuildConfig.HOST)
                            .build()
                            .create(ApiInterface::class.java)

                    return mApiInterface.getConfig(
                            WoolworthsApplication.getApiId(),
                            WoolworthsApplication.getSha1Password(),
                            getDeviceManufacturer(),
                            getDeviceModel(),
                            getNetworkCarrier(appInstance),
                            getOS(),
                            getOsVersion(),
                            getSessionToken(),
                            WoolworthsApplication.getAppVersionName()
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
            return WoolworthsApplication.getApiId()
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