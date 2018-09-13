package za.co.woolworths.financial.services.android.models.dao

import retrofit.RestAdapter
import za.co.woolworths.financial.services.android.contracts.OnCompletionListener
import za.co.woolworths.financial.services.android.contracts.OnResultListener
import za.co.woolworths.financial.services.android.models.ApiInterface
import za.co.woolworths.financial.services.android.models.dto.ConfigResponse
import za.co.woolworths.financial.services.android.util.FirebaseManager
import za.co.woolworths.financial.services.android.util.HttpAsyncTask

class MobileConfigServerDao {

    //static vars & functions

    companion object {
        private val mcsUrl = "https://mobileconfig.wigroup.co/config-server/rest/mobile/android/"

        fun getConfig(mcsAppVersion: String, deviceID: String, onResultListener: OnResultListener<ConfigResponse>) {

            val firebaseManager = FirebaseManager.getInstance()
            var firebaseRemoteConfig = firebaseManager.getRemoteConfig()

            if (firebaseRemoteConfig == null){
                //this ensures we're using defaults while the remote config
                //is yet to be retrieved.
                firebaseManager.setupRemoteConfig(object :OnCompletionListener{
                    override fun complete() {
                        getConfig(mcsAppVersion, deviceID, onResultListener)
                    }
                })
                return
            }

            val task = object : HttpAsyncTask<String, String, ConfigResponse>() {
                override fun httpDoInBackground(vararg strings: String): ConfigResponse {
                    val appName = firebaseRemoteConfig.getString("mcs_appName")
                    val apiKey = firebaseRemoteConfig.getString("mcs_appApiKey")

                    //MCS expects empty value for PROD
                    //appName-5.0 = PROD
                    //appName-5.0-qa = QA
                    //appName-5.0-dev = DEV

                    val mApiInterface = RestAdapter.Builder()
                            .setEndpoint(mcsUrl)
                            .build()
                            .create(ApiInterface::class.java)

                    return mApiInterface.getConfig(apiKey, deviceID, "$appName-$mcsAppVersion")
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
    }
}