package za.co.woolworths.financial.services.android.models;

import android.app.Activity;
import android.app.Application;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.StrictMode;

import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.awfs.coordination.BuildConfig;
import com.awfs.coordination.R;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.JsonElement;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import io.fabric.sdk.android.Fabric;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import za.co.absa.openbankingapi.Cryptography;
import za.co.absa.openbankingapi.KeyGenerationFailureException;
import za.co.wigroup.androidutils.Util;
import za.co.woolworths.financial.services.android.models.dto.AbsaBankingOpenApiServices;
import za.co.woolworths.financial.services.android.models.dto.ClickAndCollect;
import za.co.woolworths.financial.services.android.models.dto.CreditCardActivation;
import za.co.woolworths.financial.services.android.models.dto.ApplyNowLinks;
import za.co.woolworths.financial.services.android.models.dto.InstantCardReplacement;
import za.co.woolworths.financial.services.android.models.dto.ProductDetailsPage;
import za.co.woolworths.financial.services.android.models.dto.Sts;
import za.co.woolworths.financial.services.android.models.dto.UpdateBankDetail;
import za.co.woolworths.financial.services.android.models.dto.ValidatedSuburbProducts;
import za.co.woolworths.financial.services.android.models.dto.VirtualTempCard;
import za.co.woolworths.financial.services.android.models.dto.WGlobalState;
import za.co.woolworths.financial.services.android.models.dto.chat.amplify.InAppChat;
import za.co.woolworths.financial.services.android.models.dto.contact_us.ContactUs;
import za.co.woolworths.financial.services.android.models.dto.quick_shop.QuickShopDefaultValues;
import za.co.woolworths.financial.services.android.models.dto.whatsapp.WhatsApp;
import za.co.woolworths.financial.services.android.models.service.RxBus;
import za.co.woolworths.financial.services.android.ui.activities.onboarding.OnBoardingActivity;
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity;
import za.co.woolworths.financial.services.android.util.FirebaseManager;


public class WoolworthsApplication extends Application implements Application.ActivityLifecycleCallbacks, LifecycleObserver {

    private static Context context;
    private static Context mContextApplication;
    private static WhatsApp whatsApp;
    private static List<ContactUs> mContactUs;
    private static InAppChat inAppChat;
    private UserManager mUserManager;
    private Tracker mTracker;
    private static ApplyNowLinks applyNowLink;
    private static String registrationTCLink;
    private static String faqLink;
    private static String wrewardsLink;
    private static String rewardingLink;
    private static String howToSaveLink;
    private static String wrewardsTCLink;
    private static String cartCheckoutLink;
    private static JsonElement storeCardBlockReasons;
    private static String authenticVersionReleaseNote;


    private WGlobalState mWGlobalState;

    private static String ssoRedirectURI;
    private static String stsURI;
    private static String ssoRedirectURILogout;
    private static String ssoUpdateDetailsRedirectUri;
    private static String wwTodayURI;
    private static String creditCardType;
    private boolean isOther = false;
    private static int productOfferingId;
    private static String authenticVersionStamp = "";

    private boolean shouldDisplayServerMessage = true;
    public UpdateBankDetail updateBankDetail;

    private RxBus bus;
    private static boolean isApplicationInForeground = false;
    private static AbsaBankingOpenApiServices absaBankingOpenApiServices;
    private static QuickShopDefaultValues quickShopDefaultValues;
    private static InstantCardReplacement instantCardReplacement;
    private static VirtualTempCard virtualTempCard;
    private static ArrayList<String> whitelistedDomainsForQRScanner;
    private static Sts stsValues;
    private static CreditCardActivation creditCardActivation;
    private static ClickAndCollect clickAndCollect;
    private static String transUnionLink;

    private Activity mCurrentActivity = null;

    private static ValidatedSuburbProducts validatedSuburbProducts;

    private static ProductDetailsPage productDetailsPage;

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

    public static ApplyNowLinks getApplyNowLink() {
        return applyNowLink;
    }

    public static void setApplyNowLink(ApplyNowLinks applyNowLink) {
        WoolworthsApplication.applyNowLink = applyNowLink;
    }

    public static String getSsoRedirectURI() {
        return ssoRedirectURI;
    }

    public static void setSsoRedirectURI(String ssoRedirectURI) {
        WoolworthsApplication.ssoRedirectURI = ssoRedirectURI;
    }

    public static String getSsoRedirectURILogout() {
        return ssoRedirectURILogout;
    }

    public static void setSsoRedirectURILogout(String ssoRedirectURILogout) {
        WoolworthsApplication.ssoRedirectURILogout = ssoRedirectURILogout;
    }

    public static String getWwTodayURI() {
        return wwTodayURI;
    }

    public static void setWwTodayURI(String wwTodayURI) {
        WoolworthsApplication.wwTodayURI = wwTodayURI;
    }

    public static String getCreditCardType() {
        return creditCardType;
    }

    public static void setCreditCardType(String creditCardType) {
        WoolworthsApplication.creditCardType = creditCardType;
    }

    public static String getStsURI() {
        return stsURI;
    }

    public static void setStsURI(String stsURI) {
        WoolworthsApplication.stsURI = stsURI;
    }

    public static final String TAG = WoolworthsApplication.class.getSimpleName();

    private static WoolworthsApplication mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        WoolworthsApplication.context = this.getApplicationContext();
        this.registerActivityLifecycleCallbacks(this);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        Fabric.with(this,new Crashlytics.Builder().core(new CrashlyticsCore()).build());
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        TimeZone.setDefault(TimeZone.getTimeZone("Africa/Johannesburg"));

        ImagePipelineConfig config = ImagePipelineConfig.newBuilder(context)
                .setDownsampleEnabled(true)
                .build();
        Fresco.initialize(this, config);
        //wake up FirebaseManager that will instantiate
        //FirebaseApp
        FirebaseManager.Companion.getInstance();
        FacebookSdk.sdkInitialize(WoolworthsApplication.this);
        AppEventsLogger.activateApp(WoolworthsApplication.this);
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
    }


    //#region ShowServerMessage
    public void showServerMessageOrProceed(Activity activity){
        String passphrase = BuildConfig.VERSION_NAME+", "+BuildConfig.SHA1;
        byte[] hash = null;
        try {
            hash = Cryptography.PasswordBasedKeyDerivationFunction2(passphrase,Integer.toString(BuildConfig.VERSION_CODE),1007,256);
        } catch (KeyGenerationFailureException | UnsupportedEncodingException e) {
            Log.e(TAG,e.getMessage());
        }
        String hashB64 = Base64.encodeToString(hash,Base64.NO_WRAP);
        if(!authenticVersionStamp.isEmpty() && !hashB64.equals(authenticVersionStamp)){
            final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle(getString(R.string.update_title));
            builder.setMessage(TextUtils.isEmpty(getAuthenticVersionReleaseNote()) ? getString(R.string.update_desc) : getAuthenticVersionReleaseNote());
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
        if(activity.getClass().equals(OnBoardingActivity.class) || activity.getClass().equals(BottomNavigationActivity.class) && shouldDisplayServerMessage){
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
    }

    @Override
    public void onActivityPaused(Activity activity) {
        setCurrentActivity(activity);
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

    public static String getSsoUpdateDetailsRedirectUri() {
        return ssoUpdateDetailsRedirectUri;
    }

    public static void setSsoUpdateDetailsRedirectUri(String pSsoUpdateDetailsRedirectUri) {
        ssoUpdateDetailsRedirectUri = pSsoUpdateDetailsRedirectUri;
    }

    public RxBus bus() {
        return bus;
    }

    public static synchronized WoolworthsApplication getInstance() {
        return mInstance;
    }

    public static void setCartCheckoutLink(String link) {
        cartCheckoutLink = link;
    }

    public static String getCartCheckoutLink() {
        return cartCheckoutLink;
    }
    public static String getAuthenticVersionStamp() {
        return authenticVersionStamp;
    }

    public static void setAuthenticVersionStamp(String authenticVersionStamp) {
        WoolworthsApplication.authenticVersionStamp = authenticVersionStamp;
    }

    public static boolean isApplicationInForeground(){
        return isApplicationInForeground;
    }


    public static void setStoreCardBlockReasons(JsonElement storeCardBlockReason) {
        WoolworthsApplication.storeCardBlockReasons = storeCardBlockReason;
    }

    public JsonElement getStoreCardBlockReasons() {
        return storeCardBlockReasons;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    void onAppForegrounded() {
        isApplicationInForeground = true;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    void onAppBackgrounded() {
        isApplicationInForeground = false;
    }

    public static AbsaBankingOpenApiServices getAbsaBankingOpenApiServices() {
        return absaBankingOpenApiServices;
    }

    public static void setAbsaBankingOpenApiServices(AbsaBankingOpenApiServices absaBankingOpenApiServices) {
        WoolworthsApplication.absaBankingOpenApiServices = absaBankingOpenApiServices;
    }

    public static void setAuthenticVersionReleaseNote(String authenticVersionReleaseNote) {
        WoolworthsApplication.authenticVersionReleaseNote = authenticVersionReleaseNote;
    }

    public static String getAuthenticVersionReleaseNote() {
        return authenticVersionReleaseNote;
    }

    public static void setQuickShopDefaultValues(QuickShopDefaultValues quickShopDefaultValues) {
        WoolworthsApplication.quickShopDefaultValues = quickShopDefaultValues;
    }

    public static QuickShopDefaultValues getQuickShopDefaultValues() {
        return quickShopDefaultValues;
    }

    public Activity getCurrentActivity(){
        return mCurrentActivity;
    }
    public void setCurrentActivity(Activity mCurrentActivity){
        this.mCurrentActivity = mCurrentActivity;
    }

    public static void setInstantCardReplacement(InstantCardReplacement instantCardReplacement) {
        WoolworthsApplication.instantCardReplacement = instantCardReplacement;
    }

    public static InstantCardReplacement getInstantCardReplacement() {
        return instantCardReplacement;
    }

    public static VirtualTempCard getVirtualTempCard() {
        return virtualTempCard != null ? virtualTempCard : new VirtualTempCard();
    }

    public static void setVirtualTempCard(VirtualTempCard virtualTempCard) {
        WoolworthsApplication.virtualTempCard = virtualTempCard;
    }

    public static ArrayList<String> getWhitelistedDomainsForQRScanner() {
        return whitelistedDomainsForQRScanner;
    }

    public static void setWhitelistedDomainsForQRScanner(ArrayList<String> whitelistedDomainsForQRScanner) {
        WoolworthsApplication.whitelistedDomainsForQRScanner = whitelistedDomainsForQRScanner;
    }

    public static Sts getStsValues() {
        return stsValues != null ? stsValues : new Sts();
    }

    public static void setStsValues(Sts stsValues) {
        WoolworthsApplication.stsValues = stsValues;
    }

    public static CreditCardActivation getCreditCardActivation() {
        return creditCardActivation;
    }

    public static void setCreditCardActivation(CreditCardActivation creditCardActivation) {
        WoolworthsApplication.creditCardActivation = creditCardActivation;
    }

    public static void setWhatsAppConfig(@Nullable WhatsApp whatsApp) {
        WoolworthsApplication.whatsApp = whatsApp;
    }

    public static WhatsApp getWhatsAppConfig() {
        return whatsApp;
    }

    public static void setContactUsDetails(@NotNull List<ContactUs> contactUs) {
        mContactUs = contactUs;
    }

    public static List<ContactUs> getContactUs() {
        return mContactUs;
    }

    public static ClickAndCollect getClickAndCollect() {
        return clickAndCollect;
    }

    public static void setClickAndCollect(ClickAndCollect clickAndCollect) {
        WoolworthsApplication.clickAndCollect = clickAndCollect;
    }

    public static String getTransUnionLink() {
        return transUnionLink;
    }

    public static void setTransUnionLink(String transUnionLink) {
        WoolworthsApplication.transUnionLink = transUnionLink;
    }

    public static void setInAppChat(@Nullable InAppChat inAppChat) {
        WoolworthsApplication.inAppChat = inAppChat;
    }

    public static InAppChat getInAppChat() {
        return inAppChat;
    }

    public static ValidatedSuburbProducts getValidatedSuburbProducts() {
        return validatedSuburbProducts;
    }

    public static void setValidatedSuburbProducts(ValidatedSuburbProducts validatedSuburbProducts) {
        WoolworthsApplication.validatedSuburbProducts = validatedSuburbProducts;
    }

    public static ProductDetailsPage getProductDetailsPage() {
        return productDetailsPage;
    }

    public static void setProductDetailsPage(ProductDetailsPage productDetailsPage) {
        WoolworthsApplication.productDetailsPage = productDetailsPage;
    }
}
