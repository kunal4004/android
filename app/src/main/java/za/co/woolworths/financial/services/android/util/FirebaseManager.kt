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

    fun setupRemoteConfig(completiontListener: OnResultListener<FirebaseRemoteConfig>): FirebaseRemoteConfig{
        this.setupRemoteConfig()

        remoteConfig!!.fetch().addOnCompleteListener { task ->
            if (task.isSuccessful){
                remoteConfig!!.activateFetched()
                completiontListener.success(remoteConfig)
            }

            else{
                remoteConfig!!.setDefaults(R.xml.remote_config_defaults)
                completiontListener.failure("")
            }
        }

        return remoteConfig!!
    }

    fun setupRemoteConfig(onCompletionListener: OnCompletionListener){
        this.setupRemoteConfig(object :OnResultListener<FirebaseRemoteConfig>{

            override fun success(`object`: FirebaseRemoteConfig?) {

            }

            override fun failure(errorMessage: String?) {

            }

            override fun complete() {
                onCompletionListener.complete()
            }
        })
    }

    fun setupRemoteConfig(){
        if (remoteConfig == null){
            remoteConfig = FirebaseRemoteConfig.getInstance()
            val configSettings = FirebaseRemoteConfigSettings.Builder()
                    .setDeveloperModeEnabled(BuildConfig.DEBUG)
                    .build()
            remoteConfig!!.setConfigSettings(configSettings)
        }
    }

    fun getRemoteConfig() :FirebaseRemoteConfig?{
        return remoteConfig
    }
}