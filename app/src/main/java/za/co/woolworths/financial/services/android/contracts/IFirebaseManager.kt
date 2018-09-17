package za.co.woolworths.financial.services.android.contracts

import com.google.firebase.remoteconfig.FirebaseRemoteConfig

interface IFirebaseManager {

    fun getRemoteConfig(): FirebaseRemoteConfig
    fun setupRemoteConfig(onCompletionListener: OnCompletionListener)
    fun setupRemoteConfig(onResultListener: OnResultListener<FirebaseRemoteConfig>): FirebaseRemoteConfig
}
