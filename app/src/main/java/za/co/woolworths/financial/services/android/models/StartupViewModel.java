package za.co.woolworths.financial.services.android.models;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.awfs.coordination.R;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.Collections;

import retrofit2.Call;
import za.co.woolworths.financial.services.android.contracts.ConfigResponseListener;
import za.co.woolworths.financial.services.android.contracts.IStartupViewModel;
import za.co.woolworths.financial.services.android.contracts.RequestListener;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.AbsaBankingOpenApiServices;
import za.co.woolworths.financial.services.android.models.dto.ConfigResponse;
import za.co.woolworths.financial.services.android.models.dto.InstantCardReplacement;
import za.co.woolworths.financial.services.android.models.dto.VirtualTempCard;
import za.co.woolworths.financial.services.android.models.dto.chat.PresenceInAppChat;
import za.co.woolworths.financial.services.android.models.dto.chat.TradingHours;
import za.co.woolworths.financial.services.android.models.network.CompletionHandler;
import za.co.woolworths.financial.services.android.models.network.OneAppService;
import za.co.woolworths.financial.services.android.util.ScreenManager;
import za.co.woolworths.financial.services.android.util.Utils;

public class StartupViewModel implements IStartupViewModel {

    private final Context mContext;
    private Intent mIntent;
    private String mPushNotificationUpdate;

    private FirebaseAnalytics mFirebaseAnalytics;

    private String appVersion;
    private String environment;

    public static final String APP_SERVER_ENVIRONMENT_KEY = "app_server_environment";
    public static final String APP_VERSION_KEY = "app_version";

    private boolean mVideoPlayerShouldPlay = true;
    private boolean mIsVideoPlaying = false;
    private boolean mIsAppMinimized = false;

    private boolean mIsServerMessageShown = false;
    private boolean mSplashScreenDisplay = false;
    private boolean mSplashScreenPersist = false;
    private String mSplashScreenText = "";

    public StartupViewModel(Context context){
        mContext = context;
    }

    @Override
    public void queryServiceGetConfig(ConfigResponseListener responseListener) {

        Call<ConfigResponse> configResponseCall = OneAppService.INSTANCE.getConfig();
        configResponseCall.enqueue(new CompletionHandler<>(new RequestListener<ConfigResponse>() {

            @Override
            public void onSuccess(ConfigResponse response) {
                if (response.httpCode == 200) {
                    persistGlobalConfig(response);
                    responseListener.onSuccess(response);
                }
            }

            @Override
            public void onFailure(Throwable error) {
                responseListener.onFailure(error);

            }
        }, ConfigResponse.class));

    }

    @Override
    public void setIntent(Intent intent) {
        mIntent = intent;
    }

    @Override
    public Intent getIntent() {
        return mIntent;
    }

    @Override
    public void setPushNotificationUpdate(String pushNotificationUpdate) {
        mPushNotificationUpdate = pushNotificationUpdate;
    }

    @Override
    public FirebaseAnalytics getFirebaseAnalytics() {
        return mFirebaseAnalytics;
    }

    @Override
    public void setFirebaseAnalytics(FirebaseAnalytics firebaseAnalytics) {
        this.mFirebaseAnalytics = firebaseAnalytics;
    }

    @Override
    public String getAppVersion() {
        return appVersion;
    }

    @Override
    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    @Override
    public String getEnvironment() {
        return environment;
    }

    @Override
    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    @Override
    public boolean videoPlayerShouldPlay() {
        return mVideoPlayerShouldPlay;
    }

    @Override
    public void setVideoPlayerShouldPlay(boolean videoPlayerShouldPlay) {
        this.mVideoPlayerShouldPlay = videoPlayerShouldPlay;
    }

    @Override
    public boolean isVideoPlaying() {
        return mIsVideoPlaying;
    }

    @Override
    public void setVideoPlaying(boolean isVideoPlaying) {
        this.mIsVideoPlaying = isVideoPlaying;
    }

    @Override
    public boolean isAppMinimized() {
        return mIsAppMinimized;
    }

    @Override
    public void setAppMinimized(boolean isAppMinimized) {
        this.mIsAppMinimized = isAppMinimized;
    }

    @Override
    public boolean isServerMessageShown() {
        return mIsServerMessageShown;
    }

    @Override
    public void setServerMessageShown(boolean isServerMessageShown) {
        this.mIsServerMessageShown = isServerMessageShown;
    }

    @Override
    public boolean isSplashScreenDisplay() {
        return mSplashScreenDisplay;
    }

    @Override
    public boolean isSplashScreenPersist() {
        return mSplashScreenPersist;
    }

    @Override
    public String getSplashScreenText() {
        return mSplashScreenText;
    }

    @Override
    public void presentNextScreen() {

        String isFirstTime = Utils.getSessionDaoValue(mContext, SessionDao.KEY.ON_BOARDING_SCREEN);
        Uri appLinkData = mIntent.getData();

        if (Intent.ACTION_VIEW.equals(mIntent.getAction()) && appLinkData != null){
            handleAppLink(appLinkData);

        } else{
            Activity activity = (Activity)mContext;
            if (isFirstTime == null || Utils.isAppUpdated(mContext))
                ScreenManager.presentOnboarding(activity);
            else {
                ScreenManager.presentMain(activity, mPushNotificationUpdate);
            }
        }
    }

    @Override
    public String getRandomVideoPath() {
        ArrayList<String> listOfVideo = new ArrayList<>();
        String rawFolderPath = "android.resource://" + mContext.getPackageName() + "/";
        listOfVideo.add(rawFolderPath + R.raw.food_broccoli);
        listOfVideo.add(rawFolderPath + R.raw.food_chocolate);
        Collections.shuffle(listOfVideo);
        return listOfVideo.get(0);
    }

    private void handleAppLink(final Uri appLinkData){
        //1. check URL
        //2. navigate to facet that URL corresponds to
    }

    private void persistGlobalConfig(ConfigResponse response){
        mSplashScreenText = response.configs.enviroment.splashScreenText;
        mSplashScreenDisplay = response.configs.enviroment.splashScreenDisplay;
        mSplashScreenPersist = response.configs.enviroment.splashScreenPersist;

        WoolworthsApplication.setStoreCardBlockReasons(response.configs.enviroment.storeCardBlockReasons);
        WoolworthsApplication.setSsoRedirectURI(response.configs.enviroment.getSsoRedirectURI());
        WoolworthsApplication.setStsURI(response.configs.enviroment.getStsURI());
        WoolworthsApplication.setSsoRedirectURILogout(response.configs.enviroment.getSsoRedirectURILogout());
        WoolworthsApplication.setSsoUpdateDetailsRedirectUri(response.configs.enviroment.getSsoUpdateDetailsRedirectUri());
        WoolworthsApplication.setWwTodayURI(response.configs.enviroment.getWwTodayURI());
        WoolworthsApplication.setAuthenticVersionReleaseNote(response.configs.enviroment.getAuthenticVersionReleaseNote());
        WoolworthsApplication.setAuthenticVersionStamp(response.configs.enviroment.getAuthenticVersionStamp());
        WoolworthsApplication.setRegistrationTCLink(response.configs.defaults.getRegisterTCLink());
        WoolworthsApplication.setFaqLink(response.configs.defaults.getFaqLink());
        WoolworthsApplication.setWrewardsLink(response.configs.defaults.getWrewardsLink());
        WoolworthsApplication.setRewardingLink(response.configs.defaults.getRewardingLink());
        WoolworthsApplication.setHowToSaveLink(response.configs.defaults.getHowtosaveLink());
        WoolworthsApplication.setWrewardsTCLink(response.configs.defaults.getWrewardsTCLink());
        WoolworthsApplication.setCartCheckoutLink(response.configs.defaults.getCartCheckoutLink());
        WoolworthsApplication.setQuickShopDefaultValues(response.configs.quickShopDefaultValues);
        WoolworthsApplication.setWhitelistedDomainsForQRScanner(response.configs.whitelistedDomainsForQRScanner);
        WoolworthsApplication.setStsValues(response.configs.sts);

        WoolworthsApplication.setApplyNowLink(response.configs.applyNowLinks);

        AbsaBankingOpenApiServices absaBankingOpenApiServices = response.configs.absaBankingOpenApiServices;
        if (absaBankingOpenApiServices == null) {
            absaBankingOpenApiServices = new AbsaBankingOpenApiServices(false, "", "", "", "");
        } else {
            absaBankingOpenApiServices.setEnabled(Utils.isFeatureEnabled(absaBankingOpenApiServices.getMinimumSupportedAppBuildNumber()));
        }
        PresenceInAppChat presenceInAppChat = response.configs.presenceInAppChat;
        if (presenceInAppChat == null) {
            presenceInAppChat = new PresenceInAppChat(new ArrayList<TradingHours>(), "", false);
        } else {
            presenceInAppChat.setEnabled(Utils.isFeatureEnabled(presenceInAppChat.getMinimumSupportedAppBuildNumber()));
        }

        InstantCardReplacement instantCardReplacement = response.configs.instantCardReplacement;
        if (instantCardReplacement != null) {
            instantCardReplacement.setEnabled(Utils.isFeatureEnabled(instantCardReplacement.getMinimumSupportedAppBuildNumber()));
        }

        VirtualTempCard virtualTempCard = response.configs.virtualTempCard;
        if (virtualTempCard != null) {
            virtualTempCard.setEnabled(Utils.isFeatureEnabled(virtualTempCard.getMinimumSupportedAppBuildNumber()));
        }

        WoolworthsApplication.setAbsaBankingOpenApiServices(absaBankingOpenApiServices);
        WoolworthsApplication.setPresenceInAppChat(presenceInAppChat);

        WoolworthsApplication.setInstantCardReplacement(instantCardReplacement);
        WoolworthsApplication.setVirtualTempCard(virtualTempCard);

        WoolworthsApplication.getInstance().getWGlobalState().setStartRadius(response.configs.enviroment.getStoreStockLocatorConfigStartRadius());
        WoolworthsApplication.getInstance().getWGlobalState().setEndRadius(response.configs.enviroment.getStoreStockLocatorConfigEndRadius());
    }
}
