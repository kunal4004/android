package za.co.woolworths.financial.services.android.viewmodels

import android.content.Intent
import android.view.View

import com.google.firebase.analytics.FirebaseAnalytics
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.dto.ConfigResponse

interface StartupViewModel {

    val isSplashScreenPersist: Boolean
    val isSplashScreenDisplay: Boolean
    var isServerMessageShown: Boolean
    var isAppMinimized: Boolean
    var isVideoPlaying: Boolean
    var videoPlayerShouldPlay: Boolean

    var pushNotificationUpdate: String?
    val randomVideoPath: String
    val splashScreenText: String
    var environment: String?
    var appVersion: String?

    var firebaseAnalytics: FirebaseAnalytics?
    var intent: Intent?

    fun queryServiceGetConfig(responseListener: IResponseListener<ConfigResponse?>)
    fun presentNextScreen()
}
