package za.co.woolworths.financial.services.android.ui.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.awfs.coordination.R;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.Collections;

import io.fabric.sdk.android.services.common.CommonUtils;
import za.co.woolworths.financial.services.android.contracts.ConfigResponseListener;
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.contracts.IStartupViewModel;
import za.co.woolworths.financial.services.android.models.StartupViewModel;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.ConfigResponse;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.ui.views.WVideoView;
import za.co.woolworths.financial.services.android.ui.views.actionsheet.RootedDeviceInfoFragment;
import za.co.woolworths.financial.services.android.util.AuthenticateUtils;
import za.co.woolworths.financial.services.android.util.NetworkManager;
import za.co.woolworths.financial.services.android.util.NotificationUtils;
import za.co.woolworths.financial.services.android.util.Utils;

public class StartupActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener {

    private FirebaseAnalytics mFirebaseAnalytics = null;

    private String appVersion;
    private String environment;

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
    private LinearLayout errorLayout;
    private View noVideoView;
    private View serverMessageView;
    private WTextView serverMessageLabel;
    private RelativeLayout videoViewLayout;
    private ProgressBar pBar;

    private IStartupViewModel startupViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_startup);
        Toolbar toolbar = findViewById(R.id.mToolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.hide();

        startupViewModel = new StartupViewModel(this);
        startupViewModel.setIntent(getIntent());
        Bundle bundle = startupViewModel.getIntent().getExtras();
        if (bundle != null)
            startupViewModel.setPushNotificationUpdate(bundle.getString(NotificationUtils.PUSH_NOTIFICATION_INTENT));

        try {
            this.appVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            this.environment = com.awfs.coordination.BuildConfig.FLAVOR;
        } catch (PackageManager.NameNotFoundException e) {
            this.appVersion = "6.1.0";
            this.environment = "QA";
            e.printStackTrace();
        }

        if (mFirebaseAnalytics == null)
            mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

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
            mFirebaseAnalytics.setUserProperty(APP_SERVER_ENVIRONMENT_KEY, StartupActivity.this.environment.isEmpty() ? "prod" : StartupActivity.this.environment.toLowerCase());
            mFirebaseAnalytics.setUserProperty(APP_VERSION_KEY, StartupActivity.this.appVersion);

            setUpScreen();
        } else {
            showNonVideoViewWithErrorLayout();
        }

        findViewById(R.id.retry).setOnClickListener(onRetryButtonTapped);

        //Remove old usage of SharedPreferences data.
        Utils.clearSharedPreferences(StartupActivity.this);
        AuthenticateUtils.getInstance(StartupActivity.this).enableBiometricForCurrentSession(true);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        startupViewModel.setIntent(intent);
        initialize();
    }

    private void initialize() {

        startupViewModel.queryServiceGetConfig(new ConfigResponseListener() {
            @Override
            public void onSuccess(ConfigResponse response) {
                mVideoPlayerShouldPlay = false;

                if (response.configs.enviroment.stsURI == null || response.configs.enviroment.stsURI.isEmpty()) {
                    showNonVideoViewWithErrorLayout();
                    return;
                }

                splashScreenText = response.configs.enviroment.splashScreenText;
                splashScreenDisplay = response.configs.enviroment.splashScreenDisplay;
                splashScreenPersist = response.configs.enviroment.splashScreenPersist;

                if (!isVideoPlaying) {
                    presentNextScreenOrServerMessage();
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                showNonVideoViewWithErrorLayout();
            }
        });
    }

    //video player on completion
    @Override
    public void onCompletion(MediaPlayer mp) {

        isVideoPlaying = false;

        if (!mVideoPlayerShouldPlay) {

            presentNextScreenOrServerMessage();
            mp.stop();

        } else {
            showNonVideoViewWithoutErrorLayout();
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
        if (Utils.checkForBinarySu() && CommonUtils.isRooted(this) && getSupportFragmentManager() != null) {
            if (pBar != null)
                pBar.setVisibility(View.GONE);
            Utils.setScreenName(this, FirebaseManagerAnalyticsProperties.ScreenNames.DEVICE_ROOTED_AT_STARTUP);
            RootedDeviceInfoFragment rootedDeviceInfoFragment = RootedDeviceInfoFragment.Companion.newInstance(getString(R.string.rooted_phone_desc));
            rootedDeviceInfoFragment.show(getSupportFragmentManager(), RootedDeviceInfoFragment.class.getSimpleName());
            return;
        }

        if (isMinimized) {
            isMinimized = false;
            if (isServerMessageShown) {
                showNonVideoViewWithoutErrorLayout();
                initialize();
            } else {
                startActivity(new Intent(this, StartupActivity.class));
                finish();
            }
        } else {
            initialize();
        }
    }

    private View.OnClickListener onRetryButtonTapped = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (NetworkManager.getInstance().isConnectedToNetwork(StartupActivity.this)) {
                mFirebaseAnalytics.setUserProperty(APP_SERVER_ENVIRONMENT_KEY, StartupActivity.this.environment.isEmpty() ? "prod" : StartupActivity.this.environment.toLowerCase());
                mFirebaseAnalytics.setUserProperty(APP_VERSION_KEY, StartupActivity.this.appVersion);

                setUpScreen();
                initialize();
            } else {
                showNonVideoViewWithErrorLayout();
            }
        }
    };

    private void showVideoView() {
        noVideoView.setVisibility(View.GONE);
        serverMessageView.setVisibility(View.GONE);
        videoViewLayout.setVisibility(View.VISIBLE);
        String randomVideo = startupViewModel.getRandomVideoPath();
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

    private void showNonVideoViewWithoutErrorLayout() {
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
                    showNonVideoViewWithoutErrorLayout();
                    startupViewModel.presentNextScreen();
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
            showNonVideoViewWithoutErrorLayout();
        }
    }

    private void presentNextScreenOrServerMessage() {
        if (splashScreenDisplay) {
            showServerMessage(splashScreenText, splashScreenPersist);
        } else {
            showNonVideoViewWithoutErrorLayout();
            startupViewModel.presentNextScreen();
        }
    }

    protected void onResume() {
        super.onResume();
        Utils.setScreenName(this, FirebaseManagerAnalyticsProperties.ScreenNames.STARTUP);
        NotificationUtils.clearNotifications(StartupActivity.this);
    }

    @VisibleForTesting
    public boolean testIsFirstTime() {
        return this.isFirstTime();
    }

    @VisibleForTesting
    public String testGetRandomVideos() {
        return startupViewModel.getRandomVideoPath();
    }

//    private void openDeepLinkBackgroundActivity(String isFirstTime) {
//        if (isFirstTime == null || Utils.isAppUpdated(this)) {
//            ScreenManager.presentOnboarding(StartupActivity.this);
//        } else {
//            Intent openBottomActivity = new Intent(this, BottomNavigationActivity.class);
//            openBottomActivity.putExtra(NotificationUtils.PUSH_NOTIFICATION_INTENT, "");
//            startActivity(openBottomActivity);
//            overridePendingTransition(0, 0);
//        }
//    }
}