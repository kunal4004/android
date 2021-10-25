package za.co.woolworths.financial.services.android.firebase

import com.google.firebase.remoteconfig.FirebaseRemoteConfig

object FirebaseConfigUtils {

    @JvmStatic
    fun getFirebaseRemoteConfigInstance () = FirebaseRemoteConfig.getInstance()

    const val CONFIG_KEY : String = "splashConfig"
}