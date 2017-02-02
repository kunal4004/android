package za.co.woolworths.financial.services.android.models;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.multidex.MultiDex;

import com.awfs.coordination.R;
import com.crittercism.app.Crittercism;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONObject;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import za.co.wigroup.androidutils.Util;
import za.co.woolworths.financial.services.android.models.dto.UpdateBankDetail;


public class WoolworthsApplication extends Application {

    public static String LANDING_STORE_CARD = "LANDING_STORE_CARD";
    public static String LANDING_CREDIT_CARD = "LANDING_CREDIT_CARD";
    public static String LANDING_LOAN_CARD = "LANDING_LOAN_CARD";
    public static String LANDING_REWARDS_CARD = "LANDING_REWARDS_CARD";
    private static Context context;
    private static Context mContextApplication;
    private UserManager mUserManager;
    private WfsApi mWfsApi;
    private Tracker mTracker;
    private boolean swapSecondFragment = false;
    private static String applyNowLink;
    private static String registrationTCLink;
    private static String faqLink;
    private static String wrewardsLink;
    private static String rewardingLink;
    private static String howToSaveLink;
    private static String wrewardsTCLink;

    private int cliCardPosition;
    private static String baseURL;
    private static String apiKey;
    private static String sha1Password;
    private static String ssoRedirectURI;
    private static String stsURI;
    private boolean isDEABank = false;
    private boolean isOther = false;
    private int productOfferingId;

    private static int NumVouchers = 0;

    public UpdateBankDetail updateBankDetail;

    public static void setSha1Password(String sha1Password) {
        WoolworthsApplication.sha1Password = sha1Password;
    }

    public static String getSha1Password() {
        return sha1Password;
    }

    public static void setApiKey(String apiKey) {
        WoolworthsApplication.apiKey = apiKey;
    }

    public static void setBaseURL(String baseURL) {
        WoolworthsApplication.baseURL = baseURL;
    }

    public static String getApiKey() {
        return apiKey;
    }

    public static String getBaseURL() {
        return baseURL;
    }

    public static void setNumVouchers(int numVouchers) {
        NumVouchers = numVouchers;
    }

    public static int getNumVouchers() {
        return NumVouchers;
    }

    public static String getRegistrationTCLink() {
        return registrationTCLink;
    }

    public static String getFaqLink() {
        return faqLink;
    }

    public static String getHowToSaveLink() {
        return howToSaveLink;
    }

    public static String getRewardingLink() {
        return rewardingLink;
    }

    public static String getWrewardsLink() {
        return wrewardsLink;
    }

    public static String getWrewardsTCLink() {
        return wrewardsTCLink;
    }

    public static void setFaqLink(String faqLink) {
        WoolworthsApplication.faqLink = faqLink;
    }

    public static void setHowToSaveLink(String howToSaveLink) {
        WoolworthsApplication.howToSaveLink = howToSaveLink;
    }

    public static void setRegistrationTCLink(String registrationTCLink) {
        WoolworthsApplication.registrationTCLink = registrationTCLink;
    }

    public static void setRewardingLink(String rewardingLink) {
        WoolworthsApplication.rewardingLink = rewardingLink;
    }

    public static void setWrewardsLink(String wrewardsLink) {
        WoolworthsApplication.wrewardsLink = wrewardsLink;
    }

    public static void setWrewardsTCLink(String wrewardsTCLink) {
        WoolworthsApplication.wrewardsTCLink = wrewardsTCLink;
    }

    public static String getApplyNowLink() {
        return applyNowLink;
    }

    public static void setApplyNowLink(String applyNowLink) {
        WoolworthsApplication.applyNowLink = applyNowLink;
    }

    public static String getSsoRedirectURI() {
        return ssoRedirectURI;
    }

    public static void setSsoRedirectURI(String ssoRedirectURI) {
        WoolworthsApplication.ssoRedirectURI = ssoRedirectURI;
    }

    public static String getStsURI() {
        return stsURI;
    }

    public static void setStsURI(String stsURI) {
        WoolworthsApplication.stsURI = stsURI;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        updateBankDetail = new UpdateBankDetail();
        WoolworthsApplication.context = this.getApplicationContext();
        // set app context
        mContextApplication = getApplicationContext();
        Crittercism.initialize(getApplicationContext(), getResources().getString(R.string.crittercism_app_id));
        CalligraphyConfig.initDefault("fonts/WFutura-medium.ttf", R.attr.fontPath);
        getTracker();
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {

            @Override
            public void onActivityCreated(Activity activity,
                                          Bundle savedInstanceState) {

                // new activity created; force its orientation to portrait
                activity.setRequestedOrientation(
                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            }

            @Override
            public void onActivityStarted(Activity activity) {
            }

            @Override
            public void onActivityResumed(Activity activity) {
            }

            @Override
            public void onActivityPaused(Activity activity) {
            }

            @Override
            public void onActivityStopped(Activity activity) {
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
            }


        });
        Fresco.initialize(this);
    }

    public UserManager getUserManager() {
        if (mUserManager == null) {
            mUserManager = new UserManager(this);
        }
        return mUserManager;
    }

    public WfsApi getApi() {
        if (mWfsApi == null) {
            mWfsApi = new WfsApi(this);
        }
        return mWfsApi;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mUserManager = null;
        mWfsApi = null;
    }

    public Tracker getTracker() {
        if (mTracker == null) {
            GoogleAnalytics instance = GoogleAnalytics.getInstance(this);
            // When dry run is set, hits will not be dispatched, but will still be logged as
            // though they were dispatched.
            instance.setDryRun((Util.isDebug(this) ? false : false));
            instance.getLogger().setLogLevel(Util.isDebug(this) ? com.google.android.gms.analytics.Logger.LogLevel.VERBOSE : com.google.android.gms.analytics.Logger.LogLevel.ERROR);
            instance.setLocalDispatchPeriod(15);
            mTracker = instance.newTracker(R.xml.global_tracker);
        }
        return mTracker;
    }

    public static void setConfig(JSONObject config) {

        SharedPreferences settings = context.getSharedPreferences("config_file", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("jsondata", config.toString());
        editor.commit();

    }

    public static JSONObject config() {

        SharedPreferences settings = context.getSharedPreferences("config_file", 0);
        try {
            return new JSONObject(settings.getString("jsondata", ""));
        } catch (Exception e) {
            return null;
        }

    }

    public static long getConfig_expireLastNotify() {

        SharedPreferences settings = context.getSharedPreferences("config", 0);
        try {
            return Long.parseLong(settings.getString("Config_expireLastNotify", "").toString());
        } catch (Exception e) {
            return Long.parseLong("0");
        }


    }

    public static void setConfig_expireLastNotify() {

        SharedPreferences settings = context.getSharedPreferences("config", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("Config_expireLastNotify", Long.toString(System.currentTimeMillis()));
        editor.commit();
    }

    public boolean isDEABank() {
        return isDEABank;
    }

    public void setDEABank(boolean DEABank) {
        this.isDEABank = DEABank;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public int getCliCardPosition() {
        return cliCardPosition;
    }

    public void setCliCardPosition(int cliCardPosition) {
        this.cliCardPosition = cliCardPosition;
    }

    public boolean isOther() {
        return isOther;
    }

    public void setOther(boolean other) {
        isOther = other;
    }

    public int getProductOfferingId() {
        return productOfferingId;
    }

    public void setProductOfferingId(int productOfferingId) {
        this.productOfferingId = productOfferingId;
    }

    /**
     * retrieve application context
     *
     * @return Context
     */
    public static Context getAppContext() {
        return mContextApplication;
    }

}
