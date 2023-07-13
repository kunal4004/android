package za.co.woolworths.financial.services.android.models;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Base64;

import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.awfs.coordination.BuildConfig;
import com.awfs.coordination.R;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.core.ImageTranscoderType;
import com.facebook.imagepipeline.core.MemoryChunkType;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.FirebaseApp;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.perfectcorp.perfectlib.SkuHandler;

import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

import javax.inject.Inject;

import dagger.hilt.android.HiltAndroidApp;
import kotlinx.coroutines.CoroutineScope;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import za.co.absa.openbankingapi.Cryptography;
import za.co.absa.openbankingapi.KeyGenerationFailureException;
import za.co.wigroup.androidutils.Util;
import za.co.woolworths.financial.services.android.geolocation.network.model.ValidatePlace;
import za.co.woolworths.financial.services.android.models.dto.UpdateBankDetail;
import za.co.woolworths.financial.services.android.models.dto.WGlobalState;
import za.co.woolworths.financial.services.android.models.service.RxBus;
import za.co.woolworths.financial.services.android.onecartgetstream.common.constant.OCConstant;
import za.co.woolworths.financial.services.android.recommendations.analytics.CoroutineScopeProvider;
import za.co.woolworths.financial.services.android.recommendations.analytics.RecommendationAnalytics;
import za.co.woolworths.financial.services.android.recommendations.analytics.RecommendationEvents;
import za.co.woolworths.financial.services.android.recommendations.analytics.RecommendationUseCaseProvider;
import za.co.woolworths.financial.services.android.recommendations.analytics.RecommendationUseCases;
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity;
import za.co.woolworths.financial.services.android.ui.activities.onboarding.OnBoardingActivity;
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatAWSAmplify;
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.helper.LiveChatService;
import za.co.woolworths.financial.services.android.ui.vto.ui.PfSDKInitialCallback;
import za.co.woolworths.financial.services.android.ui.vto.utils.SdkUtility;
import za.co.woolworths.financial.services.android.util.ConnectivityLiveData;
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager;
import za.co.woolworths.financial.services.android.util.analytics.HuaweiManager;

@HiltAndroidApp
public class WoolworthsApplication extends Application implements Application.ActivityLifecycleCallbacks, LifecycleObserver {

    private static Context mContextApplication;
    private UserManager mUserManager;
    private Tracker mTracker;

    private static String cartCheckoutLinkWithParams;

    private Set<Class<Activity>> visibleActivities = new HashSet<>();

    private WGlobalState mWGlobalState;

    private static String creditCardType;
    private boolean isOther = false;
    private static int productOfferingId;
    private boolean shouldDisplayServerMessage = true;
    public UpdateBankDetail updateBankDetail;

    private RxBus bus;
    private static boolean isApplicationInForeground = false;
    private Activity mCurrentActivity = null;

    private static ValidatePlace validatePlace;
    private static ValidatePlace dashValidatePlace;
    private static ValidatePlace cncValidatePlace;

   @Inject ConnectivityLiveData connectivityLiveData;

    private RecommendationEvents recommendationAnalytics;

    private RecommendationAnalytics initRecommendationAnalytics() {
        final RecommendationUseCases recommendationUseCases = new RecommendationUseCaseProvider();
        final CoroutineScope coroutineScope = CoroutineScopeProvider.INSTANCE.getExternalScope();
        return RecommendationAnalytics.Companion.getInstance(recommendationUseCases, coroutineScope);
    }


    public static String getApiId() {
        PackageInfo packageInfo = null;
        try {

            packageInfo = WoolworthsApplication.getInstance().getPackageManager().getPackageInfo(WoolworthsApplication.getInstance().getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        String prefix = "ANDROID_V";
        String majorMinorVersion = packageInfo.versionName.substring(0, packageInfo.versionName.lastIndexOf('.'));
        return prefix.concat(majorMinorVersion);
    }

    public static String getAppVersionName() {
        PackageInfo packageInfo = null;
        try {

            packageInfo = WoolworthsApplication.getInstance().getPackageManager().getPackageInfo(WoolworthsApplication.getInstance().getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return packageInfo.versionName;
    }

    public static String getCreditCardType() {
        return creditCardType;
    }

    public static void setCreditCardType(String creditCardType) {
        WoolworthsApplication.creditCardType = creditCardType;
    }

    public static final String TAG = WoolworthsApplication.class.getSimpleName();

    private static WoolworthsApplication mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        this.registerActivityLifecycleCallbacks(this);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        FirebaseApp.initializeApp(getApplicationContext());
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);
        if (FirebaseCrashlytics.getInstance().didCrashOnPreviousExecution()) {
            FirebaseCrashlytics.getInstance().sendUnsentReports();
        }

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        TimeZone.setDefault(TimeZone.getTimeZone("Africa/Johannesburg"));

        ImagePipelineConfig config = ImagePipelineConfig.newBuilder(this)
                        .setDownsampleEnabled(true)
                        .setMemoryChunkType(MemoryChunkType.BUFFER_MEMORY)
                        .setImageTranscoderType(ImageTranscoderType.JAVA_TRANSCODER)
                        .experiment().setNativeCodeDisabled(true)
                        .build();
        Fresco.initialize(this, config);

        // Initialise Firebase and Huawei Analytics (if this is a Huawei variant)
        initializeAnalytics();

        mWGlobalState = new WGlobalState();
        updateBankDetail = new UpdateBankDetail();
        // set app context
        mContextApplication = getApplicationContext();
        //Crittercism.initialize(getApplicationContext(), getResources().getString(R.string.crittercism_app_id));
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/WFutura-medium.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
        getTracker();
        bus = new RxBus();
        vtoSyncServer();

    }

    private void initializeAnalytics() {
        FirebaseManager.Companion.getInstance();
        HuaweiManager.Companion.getInstance();
    }


    //#region ShowServerMessage
    public void showServerMessageOrProceed(Activity activity) {
        String passphrase = BuildConfig.VERSION_NAME + ", " + BuildConfig.SHA1;
        byte[] hash = null;
        try {
            hash = Cryptography.PasswordBasedKeyDerivationFunction2(passphrase, Integer.toString(BuildConfig.VERSION_CODE), 1007, 256);
        } catch (KeyGenerationFailureException | UnsupportedEncodingException e) {

        }
        String hashB64 = Base64.encodeToString(hash, Base64.NO_WRAP);
        if (!AppConfigSingleton.INSTANCE.getAuthenticVersionStamp().isEmpty() && !hashB64.equals(AppConfigSingleton.INSTANCE.getAuthenticVersionStamp())) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle(getString(R.string.update_title));
            builder.setCancelable(false);
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }
    //#endregion

    //#region ActivityLifeCycleCallBack
    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        setCurrentActivity(activity);
        if (activity.getClass().equals(OnBoardingActivity.class) || activity.getClass().equals(BottomNavigationActivity.class) && shouldDisplayServerMessage) {
            showServerMessageOrProceed(activity);
            shouldDisplayServerMessage = false;
        }
    }

    @Override
    public void onActivityStarted(Activity activity) {
        setCurrentActivity(activity);
    }

    @Override
    public void onActivityResumed(Activity activity) {
        setCurrentActivity(activity);
        if (activity != null) {
            Class<Activity> activityClass = (Class<Activity>) activity.getClass();
            visibleActivities.add(activityClass);
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
    }

    @Override
    public void onActivityStopped(Activity activity) {
        visibleActivities.remove(activity.getClass());
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        if (!isAnyActivityVisible() && ChatAWSAmplify.INSTANCE.isLiveChatBackgroundServiceRunning()) {
            Intent intentDismissService = new Intent(LiveChatService.CHANNEL_ID);
            sendBroadcast(intentDismissService);
        }

        if (!isAnyActivityVisible() && OCConstant.Companion.isOCChatBackgroundServiceRunning()) {
            OCConstant.Companion.stopOCChatService(this);
        }

    }
    //#endregion

    public UserManager getUserManager() {
        if (mUserManager == null) {
            mUserManager = new UserManager(this);
        }
        return mUserManager;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mUserManager = null;
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

    public boolean isOther() {
        return isOther;
    }

    public void setOther(boolean other) {
        isOther = other;
    }

    public static int getProductOfferingId() {
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

    public WGlobalState getWGlobalState() {
        return mWGlobalState;
    }

    public RecommendationEvents getRecommendationAnalytics() {
        if (recommendationAnalytics == null) {
            recommendationAnalytics = initRecommendationAnalytics();
        }
        return recommendationAnalytics;
    }

    public RxBus bus() {
        return bus;
    }

    public static synchronized WoolworthsApplication getInstance() {
        return mInstance;
    }

    public static boolean isApplicationInForeground() {
        return isApplicationInForeground;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    void onAppForegrounded() {
        isApplicationInForeground = true;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    void onAppBackgrounded() {
        isApplicationInForeground = false;
    }

    public Activity getCurrentActivity() {
        return mCurrentActivity;
    }

    public void setCurrentActivity(Activity mCurrentActivity) {
        this.mCurrentActivity = mCurrentActivity;
    }

    public static ValidatePlace getValidatePlaceDetails() {
        return validatePlace;
    }

    public static void setValidatedSuburbProducts(ValidatePlace validatePlace) {
        WoolworthsApplication.validatePlace = validatePlace;
    }
    public static ValidatePlace getDashBrowsingValidatePlaceDetails() {
        return dashValidatePlace;
    }

    public static void setDashBrowsingValidatePlaceDetails(ValidatePlace validatePlace) {
        WoolworthsApplication.dashValidatePlace = validatePlace;
    }

    public static ValidatePlace getCncBrowsingValidatePlaceDetails() {
        return cncValidatePlace;
    }

    public static void setCncBrowsingValidatePlaceDetails(ValidatePlace validatePlace) {
        WoolworthsApplication.cncValidatePlace = validatePlace;
    }

    public boolean isAnyActivityVisible() {
        return !visibleActivities.isEmpty();
    }

    @VisibleForTesting
    public static void testSetInstance(WoolworthsApplication application) {
        mInstance = application;
    }

    @VisibleForTesting
    public static void testSetContext(Context context) {
        mContextApplication = context;
    }

    public static void setCartCheckoutLinkWithParams(String cartCheckoutLinkWithParams) {
        WoolworthsApplication.cartCheckoutLinkWithParams = cartCheckoutLinkWithParams;
    }

    public static String getCartCheckoutLinkWithParams() {
        return cartCheckoutLinkWithParams;
    }

    /**
     *  This method used for check PF crop SDK for (VTO) sync server
     *  When user come first time or when update available
     */
    private void vtoSyncServer() {
        SdkUtility.initSdk(this, new PfSDKInitialCallback() {
            @Override
            public void onInitialized() {
                checkVtoUpdate();
            }

            @Override
            public void onFailure(Throwable throwable) {
                FirebaseManager.logException(throwable);
            }
        });
    }

    /**
     * This method check if SDK update update available or not for syncServer
     */
    private void checkVtoUpdate() {
        SkuHandler skuHandler = SkuHandler.getInstance();
        if (skuHandler == null) {
            return;
        }
        skuHandler.checkNeedToUpdate(new SkuHandler.CheckNeedToUpdateCallback() {
            @Override
            public void onSuccess(boolean needUpdate) {
                if (needUpdate) {
                    callVtoSyncServer(skuHandler);
                }
            }
            @Override
            public void onFailure(Throwable throwable) {
                FirebaseManager.logException(throwable);
            }
        });
    }

    /**
     * This method call when update available/getting true
     * @param skuHandler
     */
    private void callVtoSyncServer(SkuHandler skuHandler) {

        skuHandler.syncServer(new SkuHandler.SyncServerCallback() {
            @Override
            public void progress(double progress) {
                //sync SDK in background. when update needed.
                // later may be required show on UI
            }

            @Override
            public void onSuccess() {
                //Do Nothing
                // required later update UI.
            }

            @Override
            public void onFailure(Throwable throwable) {
                FirebaseManager.logException(throwable);
            }
        });

    }
}
