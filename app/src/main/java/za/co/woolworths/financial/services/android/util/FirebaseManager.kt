package za.co.woolworths.financial.services.android.util

import com.awfs.coordination.BuildConfig
import com.awfs.coordination.R
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import za.co.woolworths.financial.services.android.contracts.OnCompletiontListener

class FirebaseManager {

    companion object {
        private lateinit var instance: FirebaseManager

        fun getInstance(): FirebaseManager{

            if (instance == null){
                instance = FirebaseManager()
            }

            return instance
        }
    }


    private var remoteConfig: FirebaseRemoteConfig? = null

    fun setupRemoteConfig(completiontListener: OnCompletiontListener<FirebaseRemoteConfig>): FirebaseRemoteConfig{
        if (remoteConfig == null){
            this.setupRemoteConfig()

            remoteConfig!!.fetch().addOnCompleteListener { task ->
                if (task.isSuccessful){
                    remoteConfig!!.activateFetched()
                    completiontListener.success(remoteConfig)
                }

                else{
                    completiontListener.failure("")
                }
            }
        }

        return remoteConfig!!
    }

    fun setupRemoteConfig(){
        if (remoteConfig == null){
            remoteConfig = FirebaseRemoteConfig.getInstance()
            val configSettings = FirebaseRemoteConfigSettings.Builder()
                    .setDeveloperModeEnabled(BuildConfig.DEBUG)
                    .build()
            remoteConfig!!.setConfigSettings(configSettings)
            remoteConfig!!.setDefaults(R.xml.remote_config_defaults)
        }
    }

    fun getRemoteConfig() :FirebaseRemoteConfig?{
        return remoteConfig
    }
}