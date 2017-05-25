package za.co.woolworths.financial.services.android.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
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
import za.co.woolworths.financial.services.android.ui.views.WVideoView;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.ScreenManager;

public class WSplashScreenActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener {

    private boolean mVideoPlayerShouldPlay = false;
    private boolean isMinimized = false;
    private ErrorHandlerView mErrorHandlerView;
    private WVideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_wsplash_screen);
        Toolbar toolbar = (Toolbar) findViewById(R.id.mToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().hide();
        videoView = (WVideoView) findViewById(R.id.activity_wsplash_screen_videoview);
        Uri videoUri = Uri.parse(getRandomVideos());
        videoView.setVideoURI(videoUri);
        videoView.start();
        videoView.setOnCompletionListener(this);
        //Mobile Config Server
        mErrorHandlerView = new ErrorHandlerView(this
                , (RelativeLayout) findViewById(R.id.no_connection_layout));
        executeConfigServer();
        findViewById(R.id.btnRetry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (new ConnectionDetector().isOnline()) {
                    executeConfigServer();
                } else {
                    mErrorHandlerView.showToast();
                }
            }

        });
    }

    private void executeConfigServer() {
        mobileConfigServer().execute();
    }

    private HttpAsyncTask<String, String, ConfigResponse> mobileConfigServer() {
        return new HttpAsyncTask<String, String, ConfigResponse>() {

            @Override
            protected void onPreExecute() {
                mErrorHandlerView.hideErrorHandlerLayout();
                videoView.start();
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
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        videoView.pause();
                    }
                });
                mErrorHandlerView.networkFailureHandler(errorMessage);
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
                    WoolworthsApplication.setWwTodayURI(configResponse.enviroment.getWwTodayURI());
                    WoolworthsApplication.setApplyNowLink(configResponse.defaults.getApplyNowLink());
                    WoolworthsApplication.setRegistrationTCLink(configResponse.defaults.getRegisterTCLink());
                    WoolworthsApplication.setFaqLink(configResponse.defaults.getFaqLink());
                    WoolworthsApplication.setWrewardsLink(configResponse.defaults.getWrewardsLink());
                    WoolworthsApplication.setRewardingLink(configResponse.defaults.getRewardingLink());
                    WoolworthsApplication.setHowToSaveLink(configResponse.defaults.getHowtosaveLink());
                    WoolworthsApplication.setWrewardsTCLink(configResponse.defaults.getWrewardsTCLink());
                } catch (NullPointerException ignored) {
                }
            }
        };
    }

    //video player on completion
    @Override
    public void onCompletion(MediaPlayer mp) {

        if (!WSplashScreenActivity.this.mVideoPlayerShouldPlay) {
            /*
            * When creating a SessionDao with a key where the entry doesn't exist
            * in SQL lite, return a new SessionDao where the key is equal to the
            * key that's passed in the constructor e.g
            *
            * SessionDoa sessionDao = SessionDao(SessionDao.USER_TOKEN) //and the record doesn't exist
            * print(sessionDao.value) //null or empty
            * print(sessionDao.key) //SessionDao.USER_TOKEN
            *
            * sessionDoa.key = SessionDao.USER_TOKEN
            * sessionDao.save()
            *
            *
            * */
            try {
                SessionDao sessionDao = new SessionDao(WSplashScreenActivity.this, SessionDao.KEY.USER_TOKEN).get();
                if (sessionDao.value != null && !sessionDao.value.equals("")) {
                    ScreenManager.presentMain(WSplashScreenActivity.this);
                    return;
                }
            } catch (Exception e) {
                Log.e("WSplashScreen", e.getMessage());
            }
            ScreenManager.presentOnboarding(WSplashScreenActivity.this);
            mp.stop();

        } else {
            mp.start();
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
        listOfVideo.add(rawFolderPath + R.raw.fashion_studiow_men);
        listOfVideo.add(rawFolderPath + R.raw.fashion_summertime);
        listOfVideo.add(rawFolderPath + R.raw.food_broccoli);
        listOfVideo.add(rawFolderPath + R.raw.food_chocolate);
        Collections.shuffle(listOfVideo);
        return listOfVideo.get(0);
    }
}
