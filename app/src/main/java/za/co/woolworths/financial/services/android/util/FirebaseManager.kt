package za.co.woolworths.financial.services.android.util

import com.awfs.coordination.BuildConfig
import com.awfs.coordination.R
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import za.co.woolworths.financial.services.android.contracts.OnCompletionListener
import za.co.woolworths.financial.services.android.contracts.OnResultListener

class FirebaseManager {

    companion object {
        private var instance: FirebaseManager? = null

        fun getInstance(): FirebaseManager{

            if (instance == null){
                instance = FirebaseManager()
            }

            return instance!!
        }
    }


    private var remoteConfig: FirebaseRemoteConfig? = null

    fun getRemoteConfig() :FirebaseRemoteConfig?{
        return remoteConfig
    }

    private fun setupRemoteConfig(){
        if (remoteConfig == null){
            remoteConfig = FirebaseRemoteConfig.getInstance()
            val configSettings = FirebaseRemoteConfigSettings.Builder()
                    .setDeveloperModeEnabled(BuildConfig.DEBUG)
                    .build()
            remoteConfig!!.setConfigSettings(configSettings)
        }
    }

    fun setupRemoteConfig(onResultListener: OnResultListener<FirebaseRemoteConfig>): FirebaseRemoteConfig{
        this.setupRemoteConfig()

        remoteConfig!!.fetch().addOnCompleteListener { task ->
            if (task.isSuccessful){
                remoteConfig!!.activateFetched()
                onResultListener.success(remoteConfig)
            }

            else{
                remoteConfig!!.setDefaults(R.xml.remote_config_defaults)
                onResultListener.failure(task.exception?.message, HttpAsyncTask.HttpErrorCode.UNKOWN_ERROR)
            }
        }

        return remoteConfig!!
    }

    fun setupRemoteConfig(onCompletionListener: OnCompletionListener){
        //in this overloaded method,
        //we're not concerned with the status
        //but rather the completion of the request.

        this.setupRemoteConfig(object :OnResultListener<FirebaseRemoteConfig>{

            override fun success(`object`: FirebaseRemoteConfig?) {

            }

            override fun failure(errorMessage: String?, httpErrorCode: HttpAsyncTask.HttpErrorCode?) {

            }

            override fun complete() {
                onCompletionListener.complete()
            }
        })
    }
}