package za.co.woolworths.financial.services.android.models;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.multidex.MultiDex;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;
import android.util.Base64;
import android.util.Log;

import com.awfs.coordination.BuildConfig;
import com.awfs.coordination.R;
import com.crashlytics.android.Crashlytics;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import java.io.UnsupportedEncodingException;

import io.fabric.sdk.android.Fabric;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import za.co.absa.openbankingapi.Cryptography;
import za.co.absa.openbankingapi.KeyGenerationFailureException;
import za.co.wigroup.androidutils.Util;
import za.co.woolworths.financial.services.android.models.dto.UpdateBankDetail;
import za.co.woolworths.financial.services.android.models.dto.WGlobalState;
import za.co.woolworths.financial.services.android.models.service.RxBus;
import za.co.woolworths.financial.services.android.ui.activities.OnBoardingActivity;
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity;
import za.co.woolworths.financial.services.android.util.FirebaseManager;


public class WoolworthsApplication extends Application implements Application.ActivityLifecycleCallbacks {

	private static Context context;
	private static Context mContextApplication;
	private UserManager mUserManager;
	private WfsApi mWfsApi;
	private RetrofitAsyncClient mRetrofitClient;
	private Tracker mTracker;
	private static String applyNowLink;
	private static String registrationTCLink;
	private static String faqLink;
	private static String wrewardsLink;
	private static String rewardingLink;
	private static String howToSaveLink;
	private static String wrewardsTCLink;
	private static String cartCheckoutLink;


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
		StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
		StrictMode.setVmPolicy(builder.build());
		Fabric.with(WoolworthsApplication.this, new Crashlytics());
		AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

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
			builder.setMessage(getString(R.string.update_desc));
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
		if(activity.getClass().equals(OnBoardingActivity.class) || activity.getClass().equals(BottomNavigationActivity.class) && shouldDisplayServerMessage){
			showServerMessageOrProceed(activity);
			shouldDisplayServerMessage = false;
		}
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
	//#endregion

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

	public RetrofitAsyncClient getAsyncApi() {
		if (mRetrofitClient == null) {
			mRetrofitClient = new RetrofitAsyncClient(this);
		}
		return mRetrofitClient;
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		mUserManager = null;
		mWfsApi = null;
		mRetrofitClient = null;
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

	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		MultiDex.install(this);
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

}
