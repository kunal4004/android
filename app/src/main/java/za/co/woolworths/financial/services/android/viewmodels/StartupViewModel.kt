package za.co.woolworths.financial.services.android.viewmodels

import android.content.Intent

import com.google.firebase.analytics.FirebaseAnalytics

import za.co.woolworths.financial.services.android.contracts.ConfigResponseListener

interface StartupViewModel {

    val isSplashScreenPersist: Boolean
    val isSplashScreenDisplay: Boolean
    var isServerMessageShown: Boolean
    var isAppMinimized: Boolean
    var isVideoPlaying: Boolean

    val randomVideoPath: String
    val splashScreenText: String
    var environment: String?
    var appVersion: String?

    var firebaseAnalytics: FirebaseAnalytics?
    var intent: Intent?

    fun queryServiceGetConfig(responseListener: ConfigResponseListener)
    fun presentNextScreen()
    fun setVideoPlayerShouldPlay(videoPlayerShouldPlay: Boolean)
    fun setPushNotificationUpdate(pushNotificationUpdate: String)
    fun videoPlayerShouldPlay(): Boolean
}
