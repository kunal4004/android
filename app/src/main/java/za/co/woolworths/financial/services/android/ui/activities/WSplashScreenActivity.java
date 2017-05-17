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

import com.awfs.coordination.R;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;

import retrofit.RestAdapter;
import za.co.wigroup.androidutils.Util;
import za.co.woolworths.financial.services.android.models.ApiInterface;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dao.SessionDao;
import za.co.woolworths.financial.services.android.models.dto.ConfigResponse;
import za.co.woolworths.financial.services.android.ui.views.WVideoView;
import za.co.woolworths.financial.services.android.util.PersistenceLayer;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.ScreenManager;

import static com.google.android.gms.plus.PlusOneDummyView.TAG;

public class WSplashScreenActivity extends Activity implements MediaPlayer.OnCompletionListener {

    private boolean mVideoPlayerShouldPlay = false;
    private WVideoView videoView;
    private boolean isMinimized = false;
    PersistenceLayer dbHelper = null;
    public final String TAG="WSplashScreenActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wsplash_screen);
        this.videoView = (WVideoView) findViewById(R.id.activity_wsplash_screen_videoview);

        Uri videoUri = Uri.parse(getRandomVideos());
        this.videoView.setVideoURI(videoUri);
        this.videoView.start();

        this.videoView.setOnCompletionListener(this);
        String configResponseJSON=readConfigJSONFromRaw();
        if(configResponseJSON!=null)
        {
            try {
                ConfigResponse configResponse=new Gson().fromJson(configResponseJSON,ConfigResponse.class);
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
            }catch (Exception e)
            {
                Log.i(TAG,e.getMessage());
            }

        }
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
                Log.e(TAG, e.getMessage());
            }
            ScreenManager.presentOnboarding(WSplashScreenActivity.this);
            mp.stop();

        } else {
            mp.start();
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
        listOfVideo.add(rawFolderPath+ R.raw.fashion_summertime);
        listOfVideo.add(rawFolderPath+ R.raw.food_broccoli);
        listOfVideo.add(rawFolderPath+ R.raw.food_chocolate);
        Collections.shuffle(listOfVideo);
        return listOfVideo.get(0);
    }

    private String readConfigJSONFromRaw()
    {
        String json = null;
        try {
            InputStream is = getResources().openRawResource(R.raw.config);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }
}
