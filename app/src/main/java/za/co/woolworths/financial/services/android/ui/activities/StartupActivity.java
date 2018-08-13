package za.co.woolworths.financial.services.android.ui.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.awfs.coordination.BuildConfig;
import com.awfs.coordination.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.ArrayList;
import java.util.Collections;

import retrofit.RestAdapter;
import za.co.wigroup.androidutils.Util;
import za.co.woolworths.financial.services.android.contracts.RootActivityInterface;
import za.co.woolworths.financial.services.android.models.ApiInterface;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.ConfigResponse;
import za.co.woolworths.financial.services.android.models.dto.WGlobalState;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.ui.views.WVideoView;
import za.co.woolworths.financial.services.android.util.AuthenticateUtils;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.NotificationUtils;
import za.co.woolworths.financial.services.android.util.ScreenManager;
import za.co.woolworths.financial.services.android.util.Utils;

public class StartupActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener, RootActivityInterface {

	private FirebaseRemoteConfig mFirebaseRemoteConfig = null;
	private FirebaseAnalytics mFirebaseAnalytics = null;

	private String appVersion = "";
	private String environment = "";

	private static final String APP_IS_EXPIRED_KEY = "app_isExpired";
	private static final String APP_SERVER_ENVIRONMENT_KEY = "app_server_environment";
	private static final String APP_VERSION_KEY = "app_version";

	private boolean mVideoPlayerShouldPlay = true;
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
		if (new ConnectionDetector().isOnline(StartupActivity.this)) {
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
				if (new ConnectionDetector().isOnline(StartupActivity.this)) {
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
		if(mFirebaseRemoteConfig.getBoolean(APP_IS_EXPIRED_KEY)){
			this.notifyIfNeeded();
			return;
		}

		mobileConfigServer().execute();
	}

	private HttpAsyncTask<String, String, ConfigResponse> mobileConfigServer() {
		return new HttpAsyncTask<String, String, ConfigResponse>() {

			@Override
			protected void onPreExecute() {

			}

			@Override
			protected Class<ConfigResponse> httpDoInBackgroundReturnType() {
				return ConfigResponse.class;
			}

			@Override
			protected ConfigResponse httpDoInBackground(String... params) {
				final String appName = mFirebaseRemoteConfig.getString("mcs_appName");

				//MCS expects empty value for PROD
				//woneapp-5.0 = PROD
				//woneapp-5.0-qa = QA
				//woneapp-5.0-dev = DEV
				String majorMinorVersion = appVersion.substring(0, 3);
				final String mcsAppVersion = (appName + "-" + majorMinorVersion + (environment.equals("production") ? "" : ("-" + environment)));
				Log.d("MCS", mcsAppVersion);

				ApiInterface mApiInterface = new RestAdapter.Builder()
						.setEndpoint(getString(R.string.config_endpoint))
						.setLogLevel(Util.isDebug(StartupActivity.this) ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE)
						.build()
						.create(ApiInterface.class);

				return mApiInterface.getConfig(mFirebaseRemoteConfig.getString("mcs_appApiKey"), getDeviceID(), mcsAppVersion);
			}

			@Override
			public ConfigResponse httpError(final String errorMessage, final HttpErrorCode httpErrorCode) {
				showNonVideoViewWithErrorLayout();
				return new ConfigResponse();
			}

			@Override
			protected void onPostExecute(ConfigResponse configResponse) {
				try {
					StartupActivity.this.mVideoPlayerShouldPlay = false;

					if (configResponse.enviroment.stsURI == null || configResponse.enviroment.stsURI.isEmpty()) {
						showNonVideoViewWithErrorLayout();
						return;
					}

					WoolworthsApplication.setBaseURL(configResponse.enviroment.getBase_url());
					WoolworthsApplication.setApiKey(configResponse.enviroment.getApiId());
					WoolworthsApplication.setSha1Password(configResponse.enviroment.getApiPassword());
					WoolworthsApplication.setSsoRedirectURI(configResponse.enviroment.getSsoRedirectURI());
					WoolworthsApplication.setStsURI(configResponse.enviroment.getStsURI());
					WoolworthsApplication.setSsoRedirectURILogout(configResponse.enviroment.getSsoRedirectURILogout());
					WoolworthsApplication.setSsoUpdateDetailsRedirectUri(configResponse.enviroment.getSsoUpdateDetailsRedirectUri());
					WoolworthsApplication.setWwTodayURI(configResponse.enviroment.getWwTodayURI());
					WoolworthsApplication.setApplyNowLink(configResponse.defaults.getApplyNowLink());
					WoolworthsApplication.setRegistrationTCLink(configResponse.defaults.getRegisterTCLink());
					WoolworthsApplication.setFaqLink(configResponse.defaults.getFaqLink());
					WoolworthsApplication.setWrewardsLink(configResponse.defaults.getWrewardsLink());
					WoolworthsApplication.setRewardingLink(configResponse.defaults.getRewardingLink());
					WoolworthsApplication.setHowToSaveLink(configResponse.defaults.getHowtosaveLink());
					WoolworthsApplication.setWrewardsTCLink(configResponse.defaults.getWrewardsTCLink());
					WoolworthsApplication.setCartCheckoutLink(configResponse.defaults.getCartCheckoutLink());
					mWGlobalState.setStartRadius(configResponse.enviroment.getStoreStockLocatorConfigStartRadius());
					mWGlobalState.setEndRadius(configResponse.enviroment.getStoreStockLocatorConfigEndRadius());

					splashScreenText = configResponse.enviroment.splashScreenText;
					splashScreenDisplay = configResponse.enviroment.splashScreenDisplay;
					splashScreenPersist = configResponse.enviroment.splashScreenPersist;

					if (!isFirstTime()) {
						presentNextScreenOrServerMessage();
					}

				} catch (NullPointerException ignored) {
				}
			}
		};
	}

	//video player on completion
	@Override
	public void onCompletion(MediaPlayer mp) {

		if (!StartupActivity.this.mVideoPlayerShouldPlay) {

			presentNextScreenOrServerMessage();
			mp.stop();

		} else {
			showNonVideoViewWithOutErrorLayout();
		}
	}

	@SuppressLint("HardwareIds")
	private String getDeviceID() {
		return Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
	}

	@Override
	protected void onStop() {
		super.onStop();
		isMinimized = true;
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (isMinimized && !isServerMessageShown) {
			startActivity(new Intent(this, StartupActivity.class));
			isMinimized = false;
			finish();
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
		NotificationUtils.clearNotifications(StartupActivity.this);
	}

	@Override
	public void notifyIfNeeded() {

		if (mFirebaseRemoteConfig == null){
			mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
			FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
					.setDeveloperModeEnabled(BuildConfig.DEBUG)
					.build();
			mFirebaseRemoteConfig.setConfigSettings(configSettings);

			mFirebaseRemoteConfig.fetch().addOnCompleteListener(new OnCompleteListener<Void>() {
				@Override
				public void onComplete(@NonNull Task<Void> task) {
					if (task.isSuccessful())
						mFirebaseRemoteConfig.activateFetched();
					else
						mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);

					executeConfigServer();
				}
			});
		}
		else if (mFirebaseRemoteConfig.getBoolean(APP_IS_EXPIRED_KEY)){
			throw new RuntimeException("Something awful happened...");
		}
	}
}
