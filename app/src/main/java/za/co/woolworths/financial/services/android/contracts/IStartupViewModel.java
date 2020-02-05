package za.co.woolworths.financial.services.android.contracts;

import android.content.Intent;

import com.google.firebase.analytics.FirebaseAnalytics;

public interface IStartupViewModel {

    void queryServiceGetConfig(ConfigResponseListener responseListener);
    void presentNextScreen();
    void setServerMessageShown(boolean isServerMessageShown);
    void setAppMinimized(boolean isAppMinimized);
    void setVideoPlaying(boolean isVideoPlaying);
    void setVideoPlayerShouldPlay(boolean videoPlayerShouldPlay);
    void setEnvironment(String environment);
    void setAppVersion(String appVersion);
    void setFirebaseAnalytics(FirebaseAnalytics firebaseAnalytics);
    void setIntent(Intent intent);
    void setPushNotificationUpdate(String pushNotificationUpdate);

    boolean isSplashScreenPersist();
    boolean isSplashScreenDisplay();
    boolean isServerMessageShown();
    boolean isAppMinimized();
    boolean isVideoPlaying();
    boolean videoPlayerShouldPlay();

    String getRandomVideoPath();
    String getSplashScreenText();
    String getEnvironment();
    String getAppVersion();

    FirebaseAnalytics getFirebaseAnalytics();
    Intent getIntent();
}
