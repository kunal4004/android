package za.co.woolworths.financial.services.android.startup.viewmodel

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.Dispatchers
import za.co.woolworths.financial.services.android.models.dto.ConfigResponse
import za.co.woolworths.financial.services.android.startup.service.repository.StartUpRepository
import za.co.woolworths.financial.services.android.startup.utils.Resource

/**
 * Created by Kunal Uttarwar on 23/2/21.
 */
class StartupViewModel(private val startUpRepository: StartUpRepository) : ViewModel() {
    var isSplashScreenPersist: Boolean = false
    var isSplashScreenDisplay: Boolean = false
    var isServerMessageShown: Boolean = false
    var isAppMinimized: Boolean = false
    var isVideoPlaying: Boolean = false
    var videoPlayerShouldPlay: Boolean = false

    //var pushNotificationUpdate: String?
    val randomVideoPath: String = ""
    var splashScreenText: String = ""
    var environment: String? = null
    var appVersion: String? = null

    var firebaseAnalytics: FirebaseAnalytics? = null
    var intent: Intent? = null

    fun queryServiceGetConfig() = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = ConfigResponse()))
        try {
            emit(Resource.success(data = startUpRepository.queryServiceGetConfig()))
        } catch (exception: Exception) {
            emit(Resource.error(data = ConfigResponse(), msg = exception.toString()))
        }
    }

    companion object {

        const val APP_SERVER_ENVIRONMENT_KEY = "app_server_environment"
        const val APP_VERSION_KEY = "app_version"
    }
}