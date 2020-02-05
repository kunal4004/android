package za.co.woolworths.financial.services.android.ui.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
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

import com.awfs.coordination.R;
import com.google.firebase.analytics.FirebaseAnalytics;

import io.fabric.sdk.android.services.common.CommonUtils;
import za.co.woolworths.financial.services.android.contracts.ConfigResponseListener;
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.viewmodels.StartupViewModel;
import za.co.woolworths.financial.services.android.viewmodels.StartupViewModelImpl;
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

    private StartupViewModel startupViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);
        setSupportActionBar(findViewById(R.id.mToolbar));
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.hide();

        startupViewModel = new StartupViewModelImpl(this);
        startupViewModel.setIntent(getIntent());
        Bundle bundle = startupViewModel.getIntent().getExtras();
        if (bundle != null)
            startupViewModel.setPushNotificationUpdate(bundle.getString(NotificationUtils.PUSH_NOTIFICATION_INTENT));

        try {
            startupViewModel.setAppVersion(getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
            startupViewModel.setEnvironment(com.awfs.coordination.BuildConfig.FLAVOR);
        } catch (PackageManager.NameNotFoundException e) {
            startupViewModel.setAppVersion("6.1.0");
            startupViewModel.setEnvironment("QA");
            e.printStackTrace();
        }

        if (startupViewModel.getFirebaseAnalytics() == null)
            startupViewModel.setFirebaseAnalytics(FirebaseAnalytics.getInstance(this));

        if (NetworkManager.getInstance().isConnectedToNetwork(this)) {
            startupViewModel.getFirebaseAnalytics().setUserProperty(StartupViewModelImpl.Companion.getAPP_SERVER_ENVIRONMENT_KEY(), startupViewModel.getEnvironment().isEmpty() ? "prod" : startupViewModel.getEnvironment().toLowerCase());
            startupViewModel.getFirebaseAnalytics().setUserProperty(StartupViewModelImpl.Companion.getAPP_VERSION_KEY(), startupViewModel.getAppVersion());

            setupScreen();
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
                startupViewModel.setVideoPlayerShouldPlay(false);

                if (response.configs.enviroment.stsURI == null || response.configs.enviroment.stsURI.isEmpty()) {
                    showNonVideoViewWithErrorLayout();
                    return;
                }

                if (!startupViewModel.isVideoPlaying()) {
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

        startupViewModel.setVideoPlaying(false);

        if (!startupViewModel.videoPlayerShouldPlay()) {

            presentNextScreenOrServerMessage();
            mp.stop();

        } else {
            showNonVideoViewWithoutErrorLayout();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        startupViewModel.setAppMinimized(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Utils.checkForBinarySu() && CommonUtils.isRooted(this) && getSupportFragmentManager() != null) {
            ProgressBar pBar = (ProgressBar) findViewById(R.id.progressBar);

            if (pBar != null)
                pBar.setVisibility(View.GONE);

            Utils.setScreenName(this, FirebaseManagerAnalyticsProperties.ScreenNames.DEVICE_ROOTED_AT_STARTUP);
            RootedDeviceInfoFragment rootedDeviceInfoFragment = RootedDeviceInfoFragment.Companion.newInstance(getString(R.string.rooted_phone_desc));
            rootedDeviceInfoFragment.show(getSupportFragmentManager(), RootedDeviceInfoFragment.class.getSimpleName());
            return;
        }

        if (startupViewModel.isAppMinimized()) {
            startupViewModel.setAppMinimized(false);

            if (startupViewModel.isServerMessageShown()) {
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
                startupViewModel.getFirebaseAnalytics().setUserProperty(StartupViewModelImpl.Companion.getAPP_SERVER_ENVIRONMENT_KEY(), startupViewModel.getEnvironment().isEmpty() ? "prod" : startupViewModel.getEnvironment().toLowerCase());
                startupViewModel.getFirebaseAnalytics().setUserProperty(StartupViewModelImpl.Companion.getAPP_VERSION_KEY(), startupViewModel.getAppVersion());

                setupScreen();
                initialize();
            } else {
                showNonVideoViewWithErrorLayout();
            }
        }
    };

    private void showVideoView() {
        WVideoView videoView = (WVideoView) findViewById(R.id.activity_wsplash_screen_videoview);
        View noVideoView = (View) findViewById(R.id.splashNoVideoView);
        View serverMessageView = (View) findViewById(R.id.splashServerMessageView);
        RelativeLayout videoViewLayout = (RelativeLayout) findViewById(R.id.videoViewLayout);

        noVideoView.setVisibility(View.GONE);
        serverMessageView.setVisibility(View.GONE);
        videoViewLayout.setVisibility(View.VISIBLE);
        String randomVideo = startupViewModel.getRandomVideoPath();
        Uri videoUri = Uri.parse(randomVideo);

        videoView.setVideoURI(videoUri);
        videoView.start();
        videoView.setOnCompletionListener(this);

        startupViewModel.setVideoPlaying(true);
    }

    private void showNonVideoViewWithErrorLayout() {
        LinearLayout errorLayout = (LinearLayout) findViewById(R.id.errorLayout);
        View noVideoView = (View) findViewById(R.id.splashNoVideoView);
        View serverMessageView = (View) findViewById(R.id.splashServerMessageView);
        RelativeLayout videoViewLayout = (RelativeLayout) findViewById(R.id.videoViewLayout);
        ProgressBar pBar = (ProgressBar) findViewById(R.id.progressBar);

        runOnUiThread(() -> {
            pBar.setVisibility(View.GONE);
            videoViewLayout.setVisibility(View.GONE);
            noVideoView.setVisibility(View.VISIBLE);
            serverMessageView.setVisibility(View.GONE);
            errorLayout.setVisibility(View.VISIBLE);
        });

    }

    private void showNonVideoViewWithoutErrorLayout() {
        LinearLayout errorLayout = (LinearLayout) findViewById(R.id.errorLayout);
        View noVideoView = (View) findViewById(R.id.splashNoVideoView);
        View serverMessageView = (View) findViewById(R.id.splashServerMessageView);
        RelativeLayout videoViewLayout = (RelativeLayout) findViewById(R.id.videoViewLayout);
        ProgressBar pBar = (ProgressBar) findViewById(R.id.progressBar);

        pBar.setVisibility(View.VISIBLE);
        videoViewLayout.setVisibility(View.GONE);
        errorLayout.setVisibility(View.GONE);
        noVideoView.setVisibility(View.VISIBLE);
        serverMessageView.setVisibility(View.GONE);
    }

    private void showServerMessage() {
        LinearLayout errorLayout = (LinearLayout) findViewById(R.id.errorLayout);
        View noVideoView = (View) findViewById(R.id.splashNoVideoView);
        View serverMessageView = (View) findViewById(R.id.splashServerMessageView);
        WTextView serverMessageLabel = (WTextView) findViewById(R.id.messageLabel);
        RelativeLayout videoViewLayout = (RelativeLayout) findViewById(R.id.videoViewLayout);
        ProgressBar pBar = (ProgressBar) findViewById(R.id.progressBar);

        pBar.setVisibility(View.GONE);
        videoViewLayout.setVisibility(View.GONE);
        errorLayout.setVisibility(View.GONE);
        noVideoView.setVisibility(View.GONE);

        serverMessageLabel.setText(startupViewModel.getSplashScreenText());
        WButton proceedButton = findViewById(R.id.proceedButton);
        if (startupViewModel.isSplashScreenPersist()) {
            proceedButton.setVisibility(View.GONE);
        } else {
            proceedButton.setVisibility(View.VISIBLE);
            proceedButton.setOnClickListener(v -> {
                showNonVideoViewWithoutErrorLayout();
                startupViewModel.presentNextScreen();
            });
        }

        serverMessageView.setVisibility(View.VISIBLE);

        startupViewModel.setServerMessageShown(true);
    }

    private boolean isFirstTime() {
        if (Utils.getSessionDaoValue(StartupActivity.this, SessionDao.KEY.SPLASH_VIDEO) == null) {
            return true;
        } else
            return false;
    }

    private void setupScreen() {
        if (isFirstTime()) {
            showVideoView();
        } else {
            showNonVideoViewWithoutErrorLayout();
        }
    }

    private void presentNextScreenOrServerMessage() {
        if (startupViewModel.isSplashScreenDisplay()) {
            showServerMessage();
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