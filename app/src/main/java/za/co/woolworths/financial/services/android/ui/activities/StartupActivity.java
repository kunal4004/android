package za.co.woolworths.financial.services.android.ui.activities;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.awfs.coordination.BuildConfig;
import com.awfs.coordination.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;

import java.util.ArrayList;
import java.util.Collections;

import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import retrofit2.Call;
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.contracts.RequestListener;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.AbsaBankingOpenApiServices;
import za.co.woolworths.financial.services.android.models.dto.ConfigResponse;
import za.co.woolworths.financial.services.android.models.dto.WGlobalState;
import za.co.woolworths.financial.services.android.models.network.CompletionHandler;
import za.co.woolworths.financial.services.android.models.network.OneAppService;
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity;
import za.co.woolworths.financial.services.android.ui.activities.deep_link.RetrieveProductDetail;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.ui.views.WVideoView;
import za.co.woolworths.financial.services.android.util.AuthenticateUtils;
import za.co.woolworths.financial.services.android.util.NetworkManager;
import za.co.woolworths.financial.services.android.util.NotificationUtils;
import za.co.woolworths.financial.services.android.util.ScreenManager;
import za.co.woolworths.financial.services.android.util.Utils;

public class StartupActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener {

	private FirebaseAnalytics mFirebaseAnalytics = null;

	private String appVersion = "";
	private String environment = "";

	private static final String APP_IS_EXPIRED_KEY = "app_isExpired";
	private static final String APP_SERVER_ENVIRONMENT_KEY = "app_server_environment";
	private static final String APP_VERSION_KEY = "app_version";

	private boolean mVideoPlayerShouldPlay = true;
	private boolean isVideoPlaying = false;
	private boolean isMinimized = false;
	private boolean isServerMessageShown = false;

	private boolean splashScreenDisplay = false;
	private boolean splashScreenPersist = false;
	private String splashScreenText = "";

	private WVideoView videoView;
	private String TAG = this.getClass().getSimpleName();
	private LinearLayout errorLayout;
	private View noVideoView;
	private View serverMessageView;
	private WTextView serverMessageLabel;
	private RelativeLayout videoViewLayout;
	private ProgressBar pBar;
	private WGlobalState mWGlobalState;
	private String mPushNotificationUpdate;
	private String mDeepLinkUrl = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_startup);
		Toolbar toolbar = findViewById(R.id.mToolbar);
		setSupportActionBar(toolbar);
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.hide();
		}

		try {
			this.appVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
			this.environment = com.awfs.coordination.BuildConfig.FLAVOR;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}

		if (mFirebaseAnalytics == null)
			mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);


		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			mPushNotificationUpdate = bundle.getString(NotificationUtils.PUSH_NOTIFICATION_INTENT);
		}

		WoolworthsApplication woolworthsApplication = (WoolworthsApplication) StartupActivity.this.getApplication();
		mWGlobalState = woolworthsApplication.getWGlobalState();

		videoView = (WVideoView) findViewById(R.id.activity_wsplash_screen_videoview);
		errorLayout = (LinearLayout) findViewById(R.id.errorLayout);
		noVideoView = (View) findViewById(R.id.splashNoVideoView);
		serverMessageView = (View) findViewById(R.id.splashServerMessageView);
		serverMessageLabel = (WTextView) findViewById(R.id.messageLabel);
		videoViewLayout = (RelativeLayout) findViewById(R.id.videoViewLayout);
		pBar = (ProgressBar) findViewById(R.id.progressBar);

		pBar.getIndeterminateDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
		//Mobile Config Server
		if (NetworkManager.getInstance().isConnectedToNetwork(this)) {
			mFirebaseAnalytics.setUserProperty(APP_SERVER_ENVIRONMENT_KEY, StartupActivity.this.environment.isEmpty() ? "prod": StartupActivity.this.environment.toLowerCase());
			mFirebaseAnalytics.setUserProperty(APP_VERSION_KEY, StartupActivity.this.appVersion);

			setUpScreen();
		} else {
			showNonVideoViewWithErrorLayout();
		}
		findViewById(R.id.retry).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (NetworkManager.getInstance().isConnectedToNetwork(StartupActivity.this)) {
					mFirebaseAnalytics.setUserProperty(APP_SERVER_ENVIRONMENT_KEY, StartupActivity.this.environment.isEmpty() ? "prod": StartupActivity.this.environment.toLowerCase());
					mFirebaseAnalytics.setUserProperty(APP_VERSION_KEY, StartupActivity.this.appVersion);

					setUpScreen();
					executeConfigServer();
				} else {
					showNonVideoViewWithErrorLayout();
				}
			}

		});

		//Remove old usage of SharedPreferences data.
		Utils.clearSharedPreferences(StartupActivity.this);
		AuthenticateUtils.getInstance(StartupActivity.this).enableBiometricForCurrentSession(true);
	}


	private void executeConfigServer() {
		//if app is expired, don't execute MCS.

		Call<ConfigResponse> configResponseCall = OneAppService.INSTANCE.getConfig();
		configResponseCall.enqueue(new CompletionHandler<>(new RequestListener<ConfigResponse>(){

			@Override
			public void onSuccess(ConfigResponse configResponse) {
				if (configResponse.httpCode == 200) {
					try {
						StartupActivity.this.mVideoPlayerShouldPlay = false;

						if (configResponse.configs.enviroment.stsURI == null || configResponse.configs.enviroment.stsURI.isEmpty()) {
							showNonVideoViewWithErrorLayout();
							return;
						}

						WoolworthsApplication.setStoreCardBlockReasons(configResponse.configs.enviroment.storeCardBlockReasons);
						WoolworthsApplication.setSsoRedirectURI(configResponse.configs.enviroment.getSsoRedirectURI());
						WoolworthsApplication.setStsURI(configResponse.configs.enviroment.getStsURI());
						WoolworthsApplication.setSsoRedirectURILogout(configResponse.configs.enviroment.getSsoRedirectURILogout());
						WoolworthsApplication.setSsoUpdateDetailsRedirectUri(configResponse.configs.enviroment.getSsoUpdateDetailsRedirectUri());
						WoolworthsApplication.setWwTodayURI(configResponse.configs.enviroment.getWwTodayURI());
						WoolworthsApplication.setAuthenticVersionReleaseNote(configResponse.configs.enviroment.getAuthenticVersionReleaseNote());
						WoolworthsApplication.setAuthenticVersionStamp(configResponse.configs.enviroment.getAuthenticVersionStamp());
						WoolworthsApplication.setApplyNowLink(configResponse.configs.defaults.getApplyNowLink());
						WoolworthsApplication.setRegistrationTCLink(configResponse.configs.defaults.getRegisterTCLink());
						WoolworthsApplication.setFaqLink(configResponse.configs.defaults.getFaqLink());
						WoolworthsApplication.setWrewardsLink(configResponse.configs.defaults.getWrewardsLink());
						WoolworthsApplication.setRewardingLink(configResponse.configs.defaults.getRewardingLink());
						WoolworthsApplication.setHowToSaveLink(configResponse.configs.defaults.getHowtosaveLink());
						WoolworthsApplication.setWrewardsTCLink(configResponse.configs.defaults.getWrewardsTCLink());
						WoolworthsApplication.setCartCheckoutLink(configResponse.configs.defaults.getCartCheckoutLink());

						AbsaBankingOpenApiServices absaBankingOpenApiServices = configResponse.configs.absaBankingOpenApiServices;
						Integer appMinorMajorBuildVersion = Integer.valueOf((BuildConfig.VERSION_NAME + BuildConfig.VERSION_CODE).replace(".", ""));
						int minimumSupportedAppVersion = TextUtils.isEmpty(absaBankingOpenApiServices.getMinSupportedAppVersion()) ? 0 : Integer.valueOf(absaBankingOpenApiServices.getMinSupportedAppVersion().replace(".", ""));
						absaBankingOpenApiServices.setEnabled(appMinorMajorBuildVersion >= minimumSupportedAppVersion);
						WoolworthsApplication.setAbsaBankingOpenApiServices(absaBankingOpenApiServices);
						WoolworthsApplication.setPresenceInAppChat(configResponse.configs.presenceInAppChat);

						mWGlobalState.setStartRadius(configResponse.configs.enviroment.getStoreStockLocatorConfigStartRadius());
						mWGlobalState.setEndRadius(configResponse.configs.enviroment.getStoreStockLocatorConfigEndRadius());

						splashScreenText = configResponse.configs.enviroment.splashScreenText;
						splashScreenDisplay = configResponse.configs.enviroment.splashScreenDisplay;
						splashScreenPersist = configResponse.configs.enviroment.splashScreenPersist;

						if (!isVideoPlaying) {
							presentNextScreenOrServerMessage();
						}

					} catch (NullPointerException ex) {
						showNonVideoViewWithErrorLayout();
					}
				}
			}

			@Override
			public void onFailure(Throwable error) {
				showNonVideoViewWithErrorLayout();

			}
		},ConfigResponse.class));
	}



	//video player on completion
	@Override
	public void onCompletion(MediaPlayer mp) {

		isVideoPlaying = false;

		if (!StartupActivity.this.mVideoPlayerShouldPlay) {

			presentNextScreenOrServerMessage();
			mp.stop();

		} else {
			showNonVideoViewWithOutErrorLayout();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		isMinimized = true;
	}

	@Override
	protected void onStart() {
		super.onStart();

//		if (CommonUtils.isRooted(this) && getSupportFragmentManager() != null) {
//			if (pBar != null)
//				pBar.setVisibility(View.GONE);
//			RootedDeviceInfoFragment rootedDeviceInfoFragment = RootedDeviceInfoFragment.Companion.newInstance(getString(R.string.rooted_phone_desc));
//			rootedDeviceInfoFragment.show(getSupportFragmentManager(), RootedDeviceInfoFragment.class.getSimpleName());
//			return;
//		}

		FirebaseDynamicLinks.getInstance()
				.getDynamicLink(getIntent())
				.addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
					@Override
					public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
						// Get deep link from result (may be null if no link is found)
						if (pendingDynamicLinkData!=null && pendingDynamicLinkData.getLink() !=null) {
							mDeepLinkUrl = pendingDynamicLinkData.getLink().toString();
						}
					}
				})
				.addOnFailureListener(this, new OnFailureListener() {
					@Override
					public void onFailure(Exception e) {
						Log.w(TAG, "getDynamicLink:onFailure", e);
					}
				});
		if (isMinimized) {
			isMinimized = false;
			if (isServerMessageShown) {
				showNonVideoViewWithOutErrorLayout();
				executeConfigServer();
			} else{
				startActivity(new Intent(this, StartupActivity.class));
				finish();
			}
		}else{
			executeConfigServer();
		}
	}

	private String getRandomVideos() {
		ArrayList<String> listOfVideo = new ArrayList<>();
		String rawFolderPath = "android.resource://" + getPackageName() + "/";
		listOfVideo.add(rawFolderPath + R.raw.food_broccoli);
		listOfVideo.add(rawFolderPath + R.raw.food_chocolate);
		Collections.shuffle(listOfVideo);
		return listOfVideo.get(0);
	}

	private void showVideoView() {
		noVideoView.setVisibility(View.GONE);
		serverMessageView.setVisibility(View.GONE);
		videoViewLayout.setVisibility(View.VISIBLE);
		String randomVideo = getRandomVideos();
		Log.d("randomVideo", randomVideo);
		Uri videoUri = Uri.parse(randomVideo);

		videoView.setVideoURI(videoUri);
		videoView.start();
		videoView.setOnCompletionListener(this);

		isVideoPlaying = true;
	}

	private void showNonVideoViewWithErrorLayout() {
		runOnUiThread(new Runnable() {
			public void run() {
				pBar.setVisibility(View.GONE);
				videoViewLayout.setVisibility(View.GONE);
				noVideoView.setVisibility(View.VISIBLE);
				serverMessageView.setVisibility(View.GONE);
				errorLayout.setVisibility(View.VISIBLE);
			}
		});

	}

	private void showNonVideoViewWithOutErrorLayout() {
		pBar.setVisibility(View.VISIBLE);
		videoViewLayout.setVisibility(View.GONE);
		errorLayout.setVisibility(View.GONE);
		noVideoView.setVisibility(View.VISIBLE);
		serverMessageView.setVisibility(View.GONE);
	}

	private void showServerMessage(String label, boolean persist) {

		pBar.setVisibility(View.GONE);
		videoViewLayout.setVisibility(View.GONE);
		errorLayout.setVisibility(View.GONE);
		noVideoView.setVisibility(View.GONE);

		serverMessageLabel.setText(label);
		WButton proceedButton = findViewById(R.id.proceedButton);
		if (persist) {
			proceedButton.setVisibility(View.GONE);
		} else {
			proceedButton.setVisibility(View.VISIBLE);
			proceedButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					presentNextScreen();
				}

			});
		}

		serverMessageView.setVisibility(View.VISIBLE);

		isServerMessageShown = true;
	}

	private boolean isFirstTime() {
		if (Utils.getSessionDaoValue(StartupActivity.this, SessionDao.KEY.SPLASH_VIDEO) == null) {
			return true;
		} else
			return false;
	}

	private void setUpScreen() {
		if (isFirstTime()) {
			showVideoView();
		} else {
			showNonVideoViewWithOutErrorLayout();
		}
	}

	private void presentNextScreenOrServerMessage() {
		if (splashScreenDisplay) {
			showServerMessage(splashScreenText, splashScreenPersist);
		} else {
			presentNextScreen();
		}
	}

	private boolean isAppUpdated() {
		String appVersionFromDB = Utils.getSessionDaoValue(StartupActivity.this, SessionDao.KEY.APP_VERSION);
		String appLatestVersion = null;
		try {
			appLatestVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		if (appVersionFromDB == null || !appVersionFromDB.equalsIgnoreCase(appLatestVersion)) {
			return true;
		} else {
			return false;
		}
	}

	protected void onResume() {
		super.onResume();
		Utils.setScreenName(this, FirebaseManagerAnalyticsProperties.ScreenNames.STARTUP);
		NotificationUtils.clearNotifications(StartupActivity.this);
	}

	@VisibleForTesting
	public boolean testIsFirstTime(){
		return this.isFirstTime();
	}

	@VisibleForTesting
	public String testGetRandomVideos(){
		return getRandomVideos();
	}

	private void presentNextScreen() {
		try {
			showNonVideoViewWithOutErrorLayout();
			String isFirstTime = Utils.getSessionDaoValue(StartupActivity.this, SessionDao.KEY.ON_BOARDING_SCREEN);
			// DeepLinking redirection
			if (!TextUtils.isEmpty(mDeepLinkUrl)) {
				if (mDeepLinkUrl.endsWith("/barcode/")) {// land on barcode activity
					openDeepLinkBackgroundActivity(isFirstTime);
					Intent openBarcodeActivity = new Intent(this, BarcodeScanActivity.class);
					startActivity(openBarcodeActivity);
					overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);
				} else if (mDeepLinkUrl.endsWith("/")) { // land on wToday
					ScreenManager.presentMain(StartupActivity.this, mPushNotificationUpdate);
					finish();
				} else if ((mDeepLinkUrl.contains("/help/"))) { // land on tips and trick activity
					openDeepLinkBackgroundActivity(isFirstTime);
					Intent openTipsAndTrickActivity = new Intent(this, TipsAndTricksViewPagerActivity.class);
					startActivity(openTipsAndTrickActivity);
					overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);
					finish();
				} else if ((mDeepLinkUrl.contains("/products/"))) { // land on product detail activity
					String productIdAndSkuId = mDeepLinkUrl.substring(mDeepLinkUrl.lastIndexOf('/') + 1);
					String[] arrayOfProductAndSKuId = productIdAndSkuId.split("&sku=");
					new RetrieveProductDetail(this, arrayOfProductAndSKuId[0], arrayOfProductAndSKuId[1], isFirstTime == null || isAppUpdated()).retrieveProduct();
				}
			}else if (isFirstTime == null || isAppUpdated())
				ScreenManager.presentOnboarding(StartupActivity.this);
			else {
				ScreenManager.presentMain(StartupActivity.this, mPushNotificationUpdate);
			}
		} catch (NullPointerException ex) {
			if (ex.getMessage() != null)
				Log.e(TAG, ex.getMessage());
		}
	}

	private void openDeepLinkBackgroundActivity(String isFirstTime) {
		if (isFirstTime == null || isAppUpdated()) {
			ScreenManager.presentOnboarding(StartupActivity.this);
		} else {
			Intent openBottomActivity = new Intent(this, BottomNavigationActivity.class);
			openBottomActivity.putExtra(NotificationUtils.PUSH_NOTIFICATION_INTENT, "");
			startActivity(openBottomActivity);
			overridePendingTransition(0, 0);
		}
	}
}