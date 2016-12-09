package za.co.woolworths.financial.services.android.ui.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import com.awfs.coordination.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import retrofit.RestAdapter;
import za.co.wigroup.androidutils.Util;
import za.co.wigroup.logger.lib.WiGroupLogger;
import za.co.woolworths.financial.services.android.models.ApiInterface;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.ConfigResponse;
import za.co.woolworths.financial.services.android.models.dto.Expiry;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.models.dto.VoucherResponse;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.WErrorDialog;

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
                                .setLogLevel(Util.isDebug(SplashActivity.this) ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE)
                                .build()
                                .create(ApiInterface.class);

                        //return mApiInterface.getConfig(getString(R.string.app_token),getDeviceID(),"wfs-"+appVersion);
                        return mApiInterface.getConfig(getString(R.string.app_token),getDeviceID(), mcsAppVersion);
                    }

                    @Override
                    public ConfigResponse httpError(final String errorMessage, final HttpErrorCode httpErrorCode) {
                        if (httpErrorCode == HttpErrorCode.NETWORK_UNREACHABLE){

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

                        return null;
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
                        //JSONObject enviroment = WoolworthsApplication.config().getJSONObject("enviroment");
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
