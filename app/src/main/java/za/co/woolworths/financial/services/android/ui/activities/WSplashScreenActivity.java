package za.co.woolworths.financial.services.android.ui.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
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

import java.util.ArrayList;
import java.util.Collections;

import retrofit.RestAdapter;
import za.co.wigroup.androidutils.Util;
import za.co.woolworths.financial.services.android.models.ApiInterface;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.ConfigResponse;
import za.co.woolworths.financial.services.android.models.dto.WGlobalState;
import za.co.woolworths.financial.services.android.ui.views.WVideoView;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.NotificationUtils;
import za.co.woolworths.financial.services.android.util.ScreenManager;
import za.co.woolworths.financial.services.android.util.Utils;

public class WSplashScreenActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener {

	private boolean mVideoPlayerShouldPlay = true;
	private boolean isMinimized = false;
	private WVideoView videoView;
	private String TAG = this.getClass().getSimpleName();
	private LinearLayout errorLayout;
	private View noVideoView;
	private RelativeLayout videoViewLayout;
	private ProgressBar pBar;
	private WGlobalState mWGlobalState;
	private String mPushNotificationUpdate;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_wsplash_screen);
		Toolbar toolbar = (Toolbar) findViewById(R.id.mToolbar);
		setSupportActionBar(toolbar);
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.hide();
		}

		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			mPushNotificationUpdate = bundle.getString(NotificationUtils.PUSH_NOTIFICATION_INTENT);
		}

		WoolworthsApplication woolworthsApplication = (WoolworthsApplication) WSplashScreenActivity.this.getApplication();
		mWGlobalState = woolworthsApplication.getWGlobalState();

		videoView = (WVideoView) findViewById(R.id.activity_wsplash_screen_videoview);
		errorLayout = (LinearLayout) findViewById(R.id.errorLayout);
		noVideoView = (View) findViewById(R.id.splashNoVideoView);
		videoViewLayout = (RelativeLayout) findViewById(R.id.videoViewLayout);
		pBar = (ProgressBar) findViewById(R.id.progressBar);

		pBar.getIndeterminateDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
		//Mobile Config Server
		if (new ConnectionDetector().isOnline(WSplashScreenActivity.this)) {
			setUpScreen();
			executeConfigServer();
		} else {
			showNonVideoViewWithErrorLayout();
		}
		findViewById(R.id.retry).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (new ConnectionDetector().isOnline(WSplashScreenActivity.this)) {
					setUpScreen();
					executeConfigServer();
				} else {
					showNonVideoViewWithErrorLayout();
				}
			}

		});
		//Remove old usage of SharedPreferences data.
		Utils.clearSharedPreferences(WSplashScreenActivity.this);
	}

	private void executeConfigServer() {
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
				final String appName = "woneapp";
				String appVersion = "5.0.0";//default to 5.0.0
				String environment = "";//default to PROD
				try {
					appVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
					environment = com.awfs.coordination.BuildConfig.FLAVOR;
				} catch (PackageManager.NameNotFoundException e) {
					e.printStackTrace();
				}

				//MCS expects empty value for PROD
				//woneapp-5.0 = PROD
				//woneapp-5.0-qa = QA
				//woneapp-5.0-dev = DEV
				String majorMinorVersion = appVersion.substring(0, 3);
				final String mcsAppVersion = (appName + "-" + majorMinorVersion + (environment.equals("production") ? "" : ("-" + environment)));
				Log.d("MCS", mcsAppVersion);

				ApiInterface mApiInterface = new RestAdapter.Builder()
						.setEndpoint(getString(R.string.config_endpoint))
						.setLogLevel(Util.isDebug(WSplashScreenActivity.this) ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE)
						.build()
						.create(ApiInterface.class);

				return mApiInterface.getConfig(getString(R.string.app_token), getDeviceID(), mcsAppVersion);
			}

			@Override
			public ConfigResponse httpError(final String errorMessage, final HttpErrorCode httpErrorCode) {
				showNonVideoViewWithErrorLayout();
				return new ConfigResponse();
			}

			@Override
			protected void onPostExecute(ConfigResponse configResponse) {
				try {
					WSplashScreenActivity.this.mVideoPlayerShouldPlay = false;

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

					mWGlobalState.setStartRadius(configResponse.enviroment.getStoreStockLocatorConfigStartRadius());
					mWGlobalState.setEndRadius(configResponse.enviroment.getStoreStockLocatorConfigEndRadius());
					mWGlobalState.setClothingProducts(configResponse.enviroment.storeStockLocatorConfigClothingProducts());
					mWGlobalState.setFoodProducts(configResponse.enviroment.storeStockLocatorConfigFoodProducts());

					if (!isFirstTime())
						presentNextScreen();
				} catch (NullPointerException ignored) {
				}
			}
		};
	}

	//video player on completion
	@Override
	public void onCompletion(MediaPlayer mp) {

		if (!WSplashScreenActivity.this.mVideoPlayerShouldPlay) {

			presentNextScreen();
			mp.stop();

		} else {
			showNonVideoViewWithOutErrorLayout();
		}
	}

	private String getDeviceID() {
		try {
			return Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
		} catch (Exception e) {
			return null;
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
		if (isMinimized) {
			startActivity(new Intent(this, WSplashScreenActivity.class));
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
				errorLayout.setVisibility(View.VISIBLE);
			}
		});

	}

	private void showNonVideoViewWithOutErrorLayout() {
		pBar.setVisibility(View.VISIBLE);
		videoViewLayout.setVisibility(View.GONE);
		errorLayout.setVisibility(View.GONE);
		noVideoView.setVisibility(View.VISIBLE);
	}

	private boolean isFirstTime() {
		if (Utils.getSessionDaoValue(WSplashScreenActivity.this, SessionDao.KEY.SPLASH_VIDEO) == null)
			return true;
		else
			return false;
	}

	private void setUpScreen() {
		if (isFirstTime()) {
			showVideoView();
		} else {
			showNonVideoViewWithOutErrorLayout();
		}
	}

	private void presentNextScreen() {
		try {
			String isFirstTime = Utils.getSessionDaoValue(WSplashScreenActivity.this, SessionDao.KEY.ON_BOARDING_SCREEN);
			if (isFirstTime == null || isAppUpdated())
				ScreenManager.presentOnboarding(WSplashScreenActivity.this);
			else {
				ScreenManager.presentMain(WSplashScreenActivity.this, mPushNotificationUpdate);
			}
		} catch (NullPointerException ignored) {
		}
	}

	private boolean isAppUpdated() {
		String appVersionFromDB = Utils.getSessionDaoValue(WSplashScreenActivity.this, SessionDao.KEY.APP_VERSION);
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
		NotificationUtils.clearNotifications(WSplashScreenActivity.this);
	}
}
