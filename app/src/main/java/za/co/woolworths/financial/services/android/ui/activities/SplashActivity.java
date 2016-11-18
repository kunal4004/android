package za.co.woolworths.financial.services.android.ui.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;


import com.awfs.coordination.R;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import retrofit.RestAdapter;
import retrofit.RetrofitError;

import za.co.wigroup.androidutils.Util;
import za.co.wigroup.logger.lib.WiGroupLogger;
import za.co.woolworths.financial.services.android.models.ApiInterface;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.ConfigResponse;
import za.co.woolworths.financial.services.android.models.dto.Expiry;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.models.dto.VoucherResponse;
import za.co.woolworths.financial.services.android.ui.adapters.MesssagesListAdapter;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.Utils;
import za.co.woolworths.financial.services.android.util.WErrorDialog;

import static com.google.android.gms.internal.zzsp.Me;

public class SplashActivity extends Activity  {

    private static final String TAG = "SplashActivity";
    private AlertDialog mError;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mError = WErrorDialog.getSingleActionActivityErrorDialog(this, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        final boolean[] hasExpiry = {false};
        new AsyncTask<String, String, LoadingResult>() {

            private long mStart;

            @Override
            protected void onPreExecute() {
                mStart = System.currentTimeMillis();
            }

            @Override
            protected LoadingResult doInBackground(String... params) {
                LoadingResult result = LoadingResult.LOGIN;
                WoolworthsApplication application = (WoolworthsApplication) getApplication();
                if (!application.getUserManager().getSession().isEmpty() && !application.getUserManager().getLandingScreen().isEmpty()) {
                    result = LoadingResult.SUCCESS;
                }
                while ((System.currentTimeMillis() - mStart) < 100) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {

                    }
                }
                return result;
            }

            @Override
            protected void onPostExecute(final LoadingResult loadingResult) {
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
                        String appVersion = "4.7-staging";
                        String versionName="";
                        try {

                            versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
                        final String[] versionNames = versionName.split("\\.");
                         String major = versionNames[0];
                         String minor = (versionNames[1]);
                        String patch = (versionNames[2]);

                        String[] temp2 = patch.split("-");
                        if(temp2.length==2){
                            appVersion = major+"."+minor+"-"+temp2[1];
                        }else{
                            appVersion = major+"."+minor;
                        }
                        ApiInterface mApiInterface = new RestAdapter.Builder()
                                .setEndpoint(getString(R.string.config_endpoint))
                                .setLogLevel(Util.isDebug(SplashActivity.this) ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE)
                                .build()
                                .create(ApiInterface.class);

                        //return mApiInterface.getConfig(getString(R.string.app_token),getDeviceID(),"wfs-"+appVersion);
                        return mApiInterface.getConfig(getString(R.string.app_token),getDeviceID(),"wfs-3.3");
                    }

                    @Override
                    public ConfigResponse httpError(final String errorMessage, final HttpErrorCode httpErrorCode) {
                        ConfigResponse configResponse = null;

                        if (httpErrorCode == HttpErrorCode.NETWORK_UNREACHABLE){

                            //while network is unreachable,
                            //check if previous user config request has been made.
                            //use cached config if found, else display no internet connection found dialog
                            configResponse = new Gson().fromJson(WoolworthsApplication.config().toString(), ConfigResponse.class);

                            if(configResponse == null){
                                SplashActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    AlertDialog alertDialog = new AlertDialog.Builder(SplashActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
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
                            }
                        }

                        return configResponse;
                    }

                    @Override
                    protected void onPostExecute(ConfigResponse configResponse) {
                        Long CurrentTime = System.currentTimeMillis();
                        Long reminderInterval = Long.valueOf("0");
                        boolean expired = false;
                        Expiry expires= configResponse.expiry;
                        if( expires != null) {
                            long str_date = expires.getExpiry_date();
                            Date date = new Date(str_date * 1000L); // *1000 is to convert seconds to milliseconds
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z"); // the format of your date
                            sdf.setTimeZone(TimeZone.getTimeZone("GMT+2")); // give a timezone reference for formating (see comment at the bottom
                            String formattedDate = sdf.format(date);
                            String expiredMSG = "";
                            String reminderMSG = "";
                            String update_url = "";

                            Long lastExpiryNotify = Long.valueOf("0");
                            try {
                                lastExpiryNotify = WoolworthsApplication.getConfig_expireLastNotify();
                            } catch (Exception e) {

                            }
                            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                            // Date date = (Date)formatter.parse(GMTTime);
                            update_url = expires.getUpdate_url();
                            expiredMSG = expires.getExpiry_msg();
                            reminderMSG = expires.getReminder_msg();

                            reminderInterval = expires.getReminder_interval();
                            if (date.getTime() < CurrentTime) {
                                expired = true;

                            }
                            hasExpiry[0] = true;
                            if (expired) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this);

                                final String finalUpdate_url = update_url;
                                builder.setMessage(expiredMSG)
                                        .setTitle("ALERT")
                                        .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                                intent.setData(Uri.parse(finalUpdate_url));
                                                startActivity(intent);
                                                finish();
                                            }
                                        })
                                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                finish();
                                            }
                                        });
                                builder.create();
                                builder.show();
                            } else {

                                if (lastExpiryNotify + reminderInterval < CurrentTime && hasExpiry[0]) {
                                    WoolworthsApplication.setConfig_expireLastNotify();
                                    final String finalUpdate_url = update_url;
                                    AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this);
                                    builder.setMessage(reminderMSG)
                                            .setTitle("REMINDER")
                                            .setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                                    intent.setData(Uri.parse(finalUpdate_url));
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            })
                                            .setNegativeButton("IGNORE", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    openPage(loadingResult);
                                                }
                                            });
                                    builder.create();
                                    builder.show();
                                } else {
                                    openPage(loadingResult);
                                }

                            }
                        }else{
                            openPage(loadingResult);
                        }


                        String configJson = new Gson().toJson(configResponse);
                        try{
                            WoolworthsApplication.setConfig(new JSONObject(configJson));
                        }
                        catch (JSONException e){

                        }

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
        }.execute();



    }

    private String getDeviceID(){
        try{

            return  Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        }catch (Exception e){
            return null;
        }
    }



public void openPage(LoadingResult loadingResult){
    switch (loadingResult) {
        case ERROR:
            mError.show();
            break;
        case LOGIN:
            startActivity(new Intent(SplashActivity.this, WOneAppBaseActivity.class));
            finish();
            break;
        case SUCCESS:
            String landingScreen = ((WoolworthsApplication) getApplication()).getUserManager().getLandingScreen();
            loadWRewards(landingScreen);
           /* if (landingScreen.equals(WoolworthsApplication.LANDING_STORE_CARD)) {
                Intent intent = new Intent(SplashActivity.this, AccountsActivity.class);
                intent.putExtra(AccountsActivity.LANDING_SCREEN, 0);
                startActivity(intent);
                finish();
            } else if (landingScreen.equals(WoolworthsApplication.LANDING_CREDIT_CARD)) {
                Intent intent = new Intent(SplashActivity.this, AccountsActivity.class);
                intent.putExtra(AccountsActivity.LANDING_SCREEN, 1);
                startActivity(intent);
                finish();
            } else if (landingScreen.equals(WoolworthsApplication.LANDING_LOAN_CARD)) {
                Intent intent = new Intent(SplashActivity.this, AccountsActivity.class);
                intent.putExtra(AccountsActivity.LANDING_SCREEN, 2);
                startActivity(intent);
                finish();
            } else {
                startActivity(new Intent(SplashActivity.this, WRewardsActivity.class));
                finish();
            }*/

            break;
    }
}
    private WoolworthsApplication getWoolworthsApplication() {
        return (WoolworthsApplication) getApplication();
    }
    private void loadWRewards(final String landingScreen) {
        String wRewards = getWoolworthsApplication().getUserManager().getWRewards();
        if (wRewards.isEmpty()) {










            new HttpAsyncTask<String, String, VoucherResponse>() {

                @Override
                protected Class<VoucherResponse> httpDoInBackgroundReturnType() {
                    return VoucherResponse.class;
                }

                @Override
                protected VoucherResponse httpDoInBackground(String... params) {
                    return getWoolworthsApplication().getApi().getVouchers();
                }

                @Override
                public VoucherResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {

                    WiGroupLogger.e(SplashActivity.this, TAG, errorMessage);
                    VoucherResponse voucherResponse = new VoucherResponse();
                    voucherResponse.httpCode = 408;
                    voucherResponse.response = new Response();
                    voucherResponse.response.desc = getString(R.string.err_002);

                    return voucherResponse;
                }

                @Override
                protected void onPreExecute() {
                   // findViewById(R.id.w_rewards_loading).setVisibility(View.VISIBLE);
                }

                @Override
                protected void onPostExecute(VoucherResponse voucherResponse) {
                   // findViewById(R.id.w_rewards_loading).setVisibility(View.GONE);
                    switch (voucherResponse.httpCode) {
                        case 200:
                            getWoolworthsApplication().getUserManager().setWRewards(voucherResponse);
                            WoolworthsApplication.setNumVouchers(voucherResponse.voucherCollection.vouchers.size());
                            if (landingScreen.equals(WoolworthsApplication.LANDING_STORE_CARD)) {
                                Intent intent = new Intent(SplashActivity.this, AccountsActivity.class);
                                intent.putExtra(AccountsActivity.LANDING_SCREEN, 0);
                                startActivity(intent);
                                finish();
                            } else if (landingScreen.equals(WoolworthsApplication.LANDING_CREDIT_CARD)) {
                                Intent intent = new Intent(SplashActivity.this, AccountsActivity.class);
                                intent.putExtra(AccountsActivity.LANDING_SCREEN, 1);
                                startActivity(intent);
                                finish();
                            } else if (landingScreen.equals(WoolworthsApplication.LANDING_LOAN_CARD)) {
                                Intent intent = new Intent(SplashActivity.this, AccountsActivity.class);
                                intent.putExtra(AccountsActivity.LANDING_SCREEN, 2);
                                startActivity(intent);
                                finish();
                            } else {
                                startActivity(new Intent(SplashActivity.this, WRewardsActivity.class));
                                finish();
                            }

                            break;

                        default:
                            if (landingScreen.equals(WoolworthsApplication.LANDING_STORE_CARD)) {
                                Intent intent = new Intent(SplashActivity.this, AccountsActivity.class);
                                intent.putExtra(AccountsActivity.LANDING_SCREEN, 0);
                                startActivity(intent);
                                finish();
                            } else if (landingScreen.equals(WoolworthsApplication.LANDING_CREDIT_CARD)) {
                                Intent intent = new Intent(SplashActivity.this, AccountsActivity.class);
                                intent.putExtra(AccountsActivity.LANDING_SCREEN, 1);
                                startActivity(intent);
                                finish();
                            } else if (landingScreen.equals(WoolworthsApplication.LANDING_LOAN_CARD)) {
                                Intent intent = new Intent(SplashActivity.this, AccountsActivity.class);
                                intent.putExtra(AccountsActivity.LANDING_SCREEN, 2);
                                startActivity(intent);
                                finish();
                            } else {
                                startActivity(new Intent(SplashActivity.this, WRewardsActivity.class));
                                finish();
                            }

                    }
                }
            }.execute();
        } else {
            if (landingScreen.equals(WoolworthsApplication.LANDING_STORE_CARD)) {
                Intent intent = new Intent(SplashActivity.this, AccountsActivity.class);
                intent.putExtra(AccountsActivity.LANDING_SCREEN, 0);
                startActivity(intent);
                finish();
            } else if (landingScreen.equals(WoolworthsApplication.LANDING_CREDIT_CARD)) {
                Intent intent = new Intent(SplashActivity.this, AccountsActivity.class);
                intent.putExtra(AccountsActivity.LANDING_SCREEN, 1);
                startActivity(intent);
                finish();
            } else if (landingScreen.equals(WoolworthsApplication.LANDING_LOAN_CARD)) {
                Intent intent = new Intent(SplashActivity.this, AccountsActivity.class);
                intent.putExtra(AccountsActivity.LANDING_SCREEN, 2);
                startActivity(intent);
                finish();
            } else {
                startActivity(new Intent(SplashActivity.this, WRewardsActivity.class));
                finish();
            }

        }
    }
    private enum LoadingResult {
        LOGIN,
        ERROR,
        SUCCESS
    }


}
