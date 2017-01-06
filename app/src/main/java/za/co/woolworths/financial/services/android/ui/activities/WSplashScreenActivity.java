package za.co.woolworths.financial.services.android.ui.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.VideoView;

import com.awfs.coordination.R;

import retrofit.RestAdapter;
import za.co.wigroup.androidutils.Util;
import za.co.woolworths.financial.services.android.models.ApiInterface;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.ConfigResponse;
import za.co.woolworths.financial.services.android.util.PersistenceLayer;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.ScreenManager;

import static com.google.android.gms.plus.PlusOneDummyView.TAG;

public class WSplashScreenActivity extends Activity implements MediaPlayer.OnCompletionListener {

    private boolean mVideoPlayerShouldPlay = true;
    private VideoView videoView;
    private boolean isMinimized = false;
    PersistenceLayer dbHelper= null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wsplash_screen);

        this.videoView = (VideoView) findViewById(R.id.activity_wsplash_screen_videoview);

        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.wsplash_screen_video);
        this.videoView.setVideoURI(videoUri);
        this.videoView.start();

        this.videoView.setOnCompletionListener(this);

        //Mobile Config Server
        new HttpAsyncTask<String, String, ConfigResponse>() {

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

                return mApiInterface.getConfig(getString(R.string.app_token),getDeviceID(), mcsAppVersion);
            }

            @Override
            public ConfigResponse httpError(final String errorMessage, final HttpErrorCode httpErrorCode) {
                if (httpErrorCode == HttpErrorCode.NETWORK_UNREACHABLE){

                    WSplashScreenActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog alertDialog = new AlertDialog.Builder(WSplashScreenActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                                    .setTitle("Connection Error")
                                    .setMessage(errorMessage)
                                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                        }
                                    }).show();
                        }
                    });
                } else if (httpErrorCode == HttpErrorCode.UNKOWN_ERROR){

                    WSplashScreenActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog alertDialog = new AlertDialog.Builder(WSplashScreenActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                                    .setTitle("Service Error")
                                    .setMessage(errorMessage)
                                    .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                        }
                                    }).show();
                        }
                    });
                }

                return null;
            }

            @Override
            protected void onPostExecute(ConfigResponse configResponse) {
                WSplashScreenActivity.this.mVideoPlayerShouldPlay = false;

                WoolworthsApplication.setBaseURL(configResponse.enviroment.getBase_url());
                WoolworthsApplication.setApiKey(configResponse.enviroment.getApiId());
                WoolworthsApplication.setSha1Password(configResponse.enviroment.getApiPassword());
                WoolworthsApplication.setApplyNowLink(configResponse.defaults.getApplyNowLink());
                WoolworthsApplication.setRegistrationTCLink(configResponse.defaults.getRegisterTCLink());
                WoolworthsApplication.setFaqLink(configResponse.defaults.getFaqLink());
                WoolworthsApplication.setWrewardsLink(configResponse.defaults.getWrewardsLink());
                WoolworthsApplication.setRewardingLink(configResponse.defaults.getRewardingLink());
                WoolworthsApplication.setHowToSaveLink(configResponse.defaults.getHowtosaveLink());
                WoolworthsApplication.setWrewardsTCLink(configResponse.defaults.getWrewardsTCLink());
            }
        }.execute();
    }

    //video player on completion
    @Override
    public void onCompletion(MediaPlayer mp) {

        if(!WSplashScreenActivity.this.mVideoPlayerShouldPlay){
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
            try{
                SessionDao sessionDao = new SessionDao(WSplashScreenActivity.this, SessionDao.KEY.USER_TOKEN).get();
                if (sessionDao.value != null && !sessionDao.value.equals("")){
                    ScreenManager.presentMain(WSplashScreenActivity.this);
                    return;
                }
            }catch(Exception e){
                Log.e(TAG, e.getMessage());
            }
            ScreenManager.presentOnboarding(WSplashScreenActivity.this);
            mp.stop();

        }else{
            mp.start();
        }
    }

    private enum LoadingResult {
        LOGIN,
        ERROR,
        SUCCESS
    }

    private String getDeviceID(){
        try{

            return  Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        }catch (Exception e){
            return null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        isMinimized=true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(isMinimized)
        {
            startActivity(new Intent(this , WSplashScreenActivity.class));
            isMinimized = false;
            finish();
        }
    }
}
