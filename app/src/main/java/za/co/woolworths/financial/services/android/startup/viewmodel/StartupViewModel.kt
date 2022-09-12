package za.co.woolworths.financial.services.android.startup.viewmodel

import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.awfs.coordination.BuildConfig
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.firebase.FirebaseConfigUtils
import za.co.woolworths.financial.services.android.firebase.model.ConfigData
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.startup.service.network.StartupApiHelper
import za.co.woolworths.financial.services.android.startup.service.repository.StartUpRepository
import za.co.woolworths.financial.services.android.startup.utils.ConfigResource
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.analytics.AnalyticsManager
import java.util.*

/**
 * Created by Kunal Uttarwar on 23/2/21.
 */
class StartupViewModel(private val startUpRepository: StartUpRepository, private val startupApiHelper: StartupApiHelper) : ViewModel() {
    var isServerMessageShown: Boolean = false
    var isAppMinimized: Boolean = false
    var isVideoPlaying: Boolean = false
    var videoPlayerShouldPlay: Boolean = false

    //var pushNotificationUpdate: String?
    val randomVideoPath: String = ""
    var environment: String? = null
    var appVersion: String? = null

    private lateinit var firebaseRemoteConfig: FirebaseRemoteConfig


    companion object {
        const val APP_SERVER_ENVIRONMENT_KEY = "app_server_environment"
        const val APP_VERSION_KEY = "app_version"
    }


    fun queryServiceGetConfig() = liveData(Dispatchers.IO) {
        emit(ConfigResource.loading(data = null))
        try {
            emit(ConfigResource.success(data = startUpRepository.queryServiceGetConfig()))
        } catch (exception: Exception) {
            emit(ConfigResource.error(data = null, msg = exception.toString()))
        }
    }

    fun setSessionDao(key: SessionDao.KEY, value: String) = startUpRepository.setSessionDao(key, value)

    fun getSessionDao(key: SessionDao.KEY) = startUpRepository.getSessionDao(key)

    fun isConnectedToInternet(context: Context) = startupApiHelper.isConnectedToInternet(context)

    fun clearSharedPreference(context: Context) = startUpRepository.clearSharedPreference(context)

    fun setUpEnvironment(context: Context) {
        try {
            appVersion = context.packageManager.getPackageInfo(context.packageName, 0).versionName
            environment = BuildConfig.ENV
        } catch (e: PackageManager.NameNotFoundException) {
            appVersion = "6.1.0"
            environment = "QA"
        }
    }

    fun setUpFirebaseEvents() {
        setupFirebaseUserProperty()
        if (SessionUtilities.getInstance().isUserAuthenticated) {
            AnalyticsManager.apply {
                val token = SessionUtilities.getInstance().jwt
                token.AtgId?.apply {
                    val atgId = if (this.isJsonArray) this.asJsonArray.first().asString else this.asString
                    setUserId(atgId)
                    setUserProperty(FirebaseManagerAnalyticsProperties.PropertyNames.ATGId, atgId)
                }

                token.C2Id?.apply {
                    setUserProperty(FirebaseManagerAnalyticsProperties.PropertyNames.C2ID, this)
                }
            }
        }
    }

    fun setupFirebaseUserProperty() {
        AnalyticsManager.apply {
            setUserProperty(APP_SERVER_ENVIRONMENT_KEY, if (environment?.isEmpty() == true) "prod" else environment?.toLowerCase(Locale.getDefault()))
            setUserProperty(APP_VERSION_KEY, appVersion)
        }
    }


    fun fetchFirebaseRemoteConifgData(): String {
        firebaseRemoteConfig = getFirebaseRemoteConfigData()
        val jsonString = firebaseRemoteConfig.getString(FirebaseConfigUtils.CONFIG_KEY);
        return  jsonString
    }

    fun parseRemoteconfigData(remoteConfigData: String): ConfigData? {
        val gson = Gson()
        try {
            return gson.fromJson(remoteConfigData, ConfigData::class.java)
        } catch (exception:Exception) {
            return  null
        }
    }

    fun getFirebaseRemoteConfigData() = FirebaseConfigUtils.getFirebaseRemoteConfigInstance()

}