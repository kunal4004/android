package za.co.woolworths.financial.services.android.util

import com.awfs.coordination.BuildConfig
import com.awfs.coordination.R
import com.google.firebase.FirebaseApp
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import za.co.woolworths.financial.services.android.contracts.IFirebaseManager
import za.co.woolworths.financial.services.android.contracts.OnCompletionListener
import za.co.woolworths.financial.services.android.contracts.OnResultListener
import za.co.woolworths.financial.services.android.models.WoolworthsApplication

class FirebaseManager: IFirebaseManager {

    companion object {
        //Firebase forms part of base functionality
        //of the app. It's without a doubt going to be
        //used and therefor, initializing a singleton here is acceptable.
        private var instance = FirebaseManager()

        fun getInstance(): IFirebaseManager{
            return instance
        }
    }

    private var remoteConfig: FirebaseRemoteConfig? = null

    constructor(){
        val context = WoolworthsApplication.getInstance()
        FirebaseApp.initializeApp(context)
    }

    override fun getRemoteConfig(): FirebaseRemoteConfig {

        if (remoteConfig == null){
            remoteConfig = FirebaseRemoteConfig.getInstance()
            val configSettings = FirebaseRemoteConfigSettings.Builder().setDeveloperModeEnabled(BuildConfig.DEBUG).build()
            remoteConfig!!.setConfigSettings(configSettings)
        }
        return remoteConfig!!;
    }

    override fun setupRemoteConfig(onResultListener: OnResultListener<FirebaseRemoteConfig>): FirebaseRemoteConfig{

        getRemoteConfig().fetch().addOnCompleteListener { task ->
            if (task.isSuccessful){
                getRemoteConfig().activateFetched()
                onResultListener.success(getRemoteConfig())
            }

            else{
                getRemoteConfig().setDefaults(R.xml.remote_config_defaults)
                onResultListener.failure(task.exception?.message, HttpAsyncTask.HttpErrorCode.UNKOWN_ERROR)
            }

            onResultListener.complete()
        }

        return getRemoteConfig()
    }

    override fun setupRemoteConfig(onCompletionListener: OnCompletionListener){
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