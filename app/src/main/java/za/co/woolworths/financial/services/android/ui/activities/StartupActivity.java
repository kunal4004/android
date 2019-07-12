package za.co.woolworths.financial.services.android.ui.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.Collections;

import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.contracts.OnResultListener;
import za.co.woolworths.financial.services.android.contracts.RootActivityInterface;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dao.MobileConfigServerDao;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.ConfigResponse;
import za.co.woolworths.financial.services.android.models.dto.WGlobalState;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.ui.views.WVideoView;
import za.co.woolworths.financial.services.android.util.AuthenticateUtils;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.NetworkManager;
import za.co.woolworths.financial.services.android.util.NotificationUtils;
import za.co.woolworths.financial.services.android.util.ScreenManager;
import za.co.woolworths.financial.services.android.util.Utils;

public class StartupActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener, RootActivityInterface {

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
			notifyIfNeeded();
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
					notifyIfNeeded();
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

		MobileConfigServerDao.Companion.getConfig(WoolworthsApplication.getInstance(), new OnResultListener<ConfigResponse>() {
			@Override
			public void success(ConfigResponse configResponse) {
				switch (configResponse.httpCode) {
					case 200:
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
							WoolworthsApplication.setAuthenticVersionStamp(configResponse.configs.enviroment.getAuthenticVersionStamp());
							WoolworthsApplication.setApplyNowLink(configResponse.configs.defaults.getApplyNowLink());
							WoolworthsApplication.setRegistrationTCLink(configResponse.configs.defaults.getRegisterTCLink());
							WoolworthsApplication.setFaqLink(configResponse.configs.defaults.getFaqLink());
							WoolworthsApplication.setWrewardsLink(configResponse.configs.defaults.getWrewardsLink());
							WoolworthsApplication.setRewardingLink(configResponse.configs.defaults.getRewardingLink());
							WoolworthsApplication.setHowToSaveLink(configResponse.configs.defaults.getHowtosaveLink());
							WoolworthsApplication.setWrewardsTCLink(configResponse.configs.defaults.getWrewardsTCLink());
							WoolworthsApplication.setCartCheckoutLink(configResponse.configs.defaults.getCartCheckoutLink());
							WoolworthsApplication.setAbsaBankingOpenApiServices(configResponse.configs.absaBankingOpenApiServices);

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
						break;
					default:
						break;
				}
			}

			@Override
			public void failure(String errorMessage, HttpAsyncTask.HttpErrorCode httpErrorCode) {
				showNonVideoViewWithErrorLayout();
			}

			@Override
			public void complete() {

			}
		});
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

	private void presentNextScreen() {
		try {
			String isFirstTime = Utils.getSessionDaoValue(StartupActivity.this, SessionDao.KEY.ON_BOARDING_SCREEN);
			if (isFirstTime == null || isAppUpdated())
				ScreenManager.presentOnboarding(StartupActivity.this);
			else {
				ScreenManager.presentMain(StartupActivity.this, mPushNotificationUpdate);
			}
		} catch (NullPointerException ignored) {
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

	@Override
	public void notifyIfNeeded() {
	}

	@VisibleForTesting
	public boolean testIsFirstTime(){
		return this.isFirstTime();
	}

	@VisibleForTesting
	public String testGetRandomVideos(){
		return getRandomVideos();
	}
}
