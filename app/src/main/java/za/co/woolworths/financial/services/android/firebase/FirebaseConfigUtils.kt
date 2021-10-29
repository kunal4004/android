package za.co.woolworths.financial.services.android.firebase

import android.content.Context
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import java.io.IOException

object FirebaseConfigUtils {

    @JvmStatic
    fun getFirebaseRemoteConfigInstance () = FirebaseRemoteConfig.getInstance()

    const val CONFIG_KEY : String = "splashConfig"

    const val FILE_NAME : String = "FirebaseDefaultConfig.json"

    @JvmStatic
    fun getJsonDataFromAsset(context: Context, fileName: String): String? {
        val jsonString: String
        try {
            jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (ioException: IOException) {
            return null
        }
        return jsonString
    }

}