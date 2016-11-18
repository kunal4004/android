package za.co.woolworths.financial.services.android.ui.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.awfs.coordination.R;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStreamReader;

import retrofit.RetrofitError;
import za.co.wigroup.androidutils.Util;
import za.co.wigroup.logger.lib.WiGroupLogger;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.LoginRequest;
import za.co.woolworths.financial.services.android.models.dto.LoginResponse;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.models.dto.VoucherResponse;
import za.co.woolworths.financial.services.android.ui.views.WEditTextView;
import za.co.woolworths.financial.services.android.util.FontHyperTextParser;
import za.co.woolworths.financial.services.android.util.FourDigitCardFormatWatcher;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.WCreditCardUtil;
import za.co.woolworths.financial.services.android.util.WErrorDialog;

public class LoginActivity extends Activity {

    private static final String TAG = "LoginActivity";
    private AlertDialog mError;
    private AlertDialog mAccountDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        mError = WErrorDialog.getSimplyErrorDialog(this);
        findViewById(R.id.login_action_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean valid = true;
                if (!idValid()) {
                    valid = false;
                }
                if (!accountValid()) {
                    valid = false;
                }
                if (valid) {
                    String s = ((EditText) findViewById(R.id.login_account_number)).getText().toString().replaceAll(" ", "");
                    if (WCreditCardUtil.getCardBucket(s) == WCreditCardUtil.CardTypes.CREDIT_CARD) {
                        s = s.substring(0, 6);
                    }
                    new HttpAsyncTask<String, String, LoginResponse>() {

                        private String accountNumber;


                        @Override
                        protected void onPreExecute() {
                            findViewById(R.id.login_progressBar).setVisibility(View.VISIBLE);
                        }

                        @Override
                        protected Class<LoginResponse> httpDoInBackgroundReturnType() {
                            return LoginResponse.class;
                        }

                        @Override
                        protected LoginResponse httpDoInBackground(String... params) {
                            accountNumber = params[1];
                            return ((WoolworthsApplication) getApplication()).getApi().login(new LoginRequest(params[0], accountNumber));
                        }

                        @Override
                        protected LoginResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {
                            WiGroupLogger.e(LoginActivity.this, TAG, errorMessage);
                            LoginResponse loginResponse = new LoginResponse();
                            loginResponse.httpCode = 408;
                            loginResponse.response = new Response();
                            if(httpErrorCode == HttpErrorCode.UNKOWN_ERROR || errorMessage.isEmpty())
                                loginResponse.response.desc = getString(R.string.err_002);
                            else
                                loginResponse.response.desc = errorMessage;
                            return loginResponse;
                        }

                        @Override
                        protected void onPostExecute(LoginResponse s) {
                            findViewById(R.id.login_progressBar).setVisibility(View.GONE);
                            if (s != null) {
                                switch (s.httpCode) {
                                    case 200:
                                        WoolworthsApplication application = (WoolworthsApplication) getApplication();
                                        application.getUserManager().setSession(s.sessionToken);
                                        application.getUserManager().invalidateCache();
                                        if (accountNumber.startsWith("60078501") ||
                                                accountNumber.startsWith("60078502") ||
                                                accountNumber.startsWith("60078503") ||
                                                accountNumber.startsWith("60078504") ||
                                                accountNumber.startsWith("60078505") ||
                                                accountNumber.startsWith("60078506") ||
                                                accountNumber.startsWith("60078507")) {
                                            application.getUserManager().setLandingScreen(WoolworthsApplication.LANDING_STORE_CARD);
                                            loadWRewards(0);
                                           /* Intent intent = new Intent(LoginActivity.this, AccountsActivity.class);
                                            intent.putExtra(AccountsActivity.LANDING_SCREEN, 0);
                                            startActivity(intent);*/
                                        } else if (accountNumber.startsWith("486725") ||
                                                accountNumber.startsWith("400154") ||
                                                accountNumber.startsWith("410374") ||
                                                accountNumber.startsWith("410375") ) {
                                            application.getUserManager().setLandingScreen(WoolworthsApplication.LANDING_CREDIT_CARD);
                                            loadWRewards(1);
                                            /*Intent intent = new Intent(LoginActivity.this, AccountsActivity.class);
                                            intent.putExtra(AccountsActivity.LANDING_SCREEN, 1);
                                            startActivity(intent);*/
                                        } else if (accountNumber.startsWith("60078511") ||
                                                accountNumber.startsWith("60078515") ||
                                                accountNumber.startsWith("60078516")) {
                                            application.getUserManager().setLandingScreen(WoolworthsApplication.LANDING_LOAN_CARD);
                                            loadWRewards(2);
                                            /*Intent intent = new Intent(LoginActivity.this, AccountsActivity.class);
                                            intent.putExtra(AccountsActivity.LANDING_SCREEN, 2);
                                            startActivity(intent);*/
                                        } else {
                                            application.getUserManager().setLandingScreen(WoolworthsApplication.LANDING_REWARDS_CARD);
                                            loadWRewards(3);
                                            //startActivity(new Intent(LoginActivity.this, WRewardsActivity.class));
                                        }
                                        //finish();
                                        break;
                                    default:
                                        mError.setMessage(FontHyperTextParser.getSpannable(s.response.desc, 1, LoginActivity.this));
                                        mError.show();
                                }
                            }
                        }
                    }.execute(((EditText) findViewById(R.id.login_id_number)).getText().toString(), s);
                }
            }
        });
        findViewById(R.id.login_terms_and_conditions).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i =new Intent(LoginActivity.this, WebViewActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("title","Terms and Conditions");
                bundle.putString("link", WoolworthsApplication.getRegistrationTCLink());
                i.putExtra("Bundle",bundle);
                startActivity(i);
               // Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.login_terms_and_conditions_url)));
             //   startActivity(browserIntent);
            }
        });

        mAccountDialog = new AlertDialog.Builder(LoginActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                .setTitle(FontHyperTextParser.getSpannable(getString(R.string.acc_number), 2, LoginActivity.this))
                .setMessage(FontHyperTextParser.getSpannable(getString(R.string.acc_number_info), 1, LoginActivity.this))
                .setPositiveButton(FontHyperTextParser.getSpannable(getString(R.string.ok), 1, LoginActivity.this), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create();

        findViewById(R.id.login_info).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAccountDialog.show();
            }
        });

        ((WEditTextView) findViewById(R.id.login_account_number)).addTextChangedListener(new FourDigitCardFormatWatcher());
    }

    private boolean accountValid() {
        EditText account = (EditText) findViewById(R.id.login_account_number);
        account.setError(null);
        String s = account.getText().toString().replaceAll(" ", "");
        if (s.isEmpty()
                || s.length() != 16
                || s.matches("[^0-9]")) {
            account.setError(getString(R.string.err_004));
            return false;
        }
        return true;
    }

    private boolean idValid() {
        EditText id = (EditText) findViewById(R.id.login_id_number);
        id.setError(null);
        String s = id.getText().toString();
        if (s.isEmpty() ||
                s.length() != 13 ||
                s.matches("[^0-9]") ||
                !Util.isIdValid(s)
                ) {
            id.setError(getString(R.string.err_003));
            return false;
        }
        return true;
    }

    @Override
    protected void onPause() {
        findViewById(R.id.login_progressBar).setVisibility(View.GONE);
        mError.dismiss();
        mAccountDialog.dismiss();
        super.onPause();
    }
    private WoolworthsApplication getWoolworthsApplication() {
        return (WoolworthsApplication) getApplication();
    }
    private void loadWRewards(final int landingScreen) {
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
                protected VoucherResponse httpError(String errorMessage, HttpErrorCode httpErrorCode) {

                    WiGroupLogger.e(LoginActivity.this, TAG, errorMessage);
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
                    findViewById(R.id.login_progressBar).setVisibility(View.GONE);
                    switch (voucherResponse.httpCode) {
                        case 200:

                            getWoolworthsApplication().getUserManager().setWRewards(voucherResponse);
                            if(voucherResponse.voucherCollection.vouchers != null)
                                WoolworthsApplication.setNumVouchers(voucherResponse.voucherCollection.vouchers.size());

                            if (landingScreen==0) {
                                Intent intent = new Intent(LoginActivity.this, AccountsActivity.class);
                                intent.putExtra(AccountsActivity.LANDING_SCREEN, 0);
                                startActivity(intent);
                                finish();
                            } else if (landingScreen==1) {
                                Intent intent = new Intent(LoginActivity.this, AccountsActivity.class);
                                intent.putExtra(AccountsActivity.LANDING_SCREEN, 1);
                                startActivity(intent);
                                finish();
                            } else if (landingScreen==2) {
                                Intent intent = new Intent(LoginActivity.this, AccountsActivity.class);
                                intent.putExtra(AccountsActivity.LANDING_SCREEN, 2);
                                startActivity(intent);
                                finish();
                            } else {
                                startActivity(new Intent(LoginActivity.this, WRewardsActivity.class));
                                finish();
                            }

                            break;

                        default:
                            if (landingScreen==0) {
                                Intent intent = new Intent(LoginActivity.this, AccountsActivity.class);
                                intent.putExtra(AccountsActivity.LANDING_SCREEN, 0);
                                startActivity(intent);
                                finish();
                            } else if (landingScreen==1) {
                                Intent intent = new Intent(LoginActivity.this, AccountsActivity.class);
                                intent.putExtra(AccountsActivity.LANDING_SCREEN, 1);
                                startActivity(intent);
                                finish();
                            } else if (landingScreen==2) {
                                Intent intent = new Intent(LoginActivity.this, AccountsActivity.class);
                                intent.putExtra(AccountsActivity.LANDING_SCREEN, 2);
                                startActivity(intent);
                                finish();
                            } else {
                                startActivity(new Intent(LoginActivity.this, WRewardsActivity.class));
                                finish();
                            }
                    }
                }
            }.execute();
        } else {
            if (landingScreen==0) {
                Intent intent = new Intent(LoginActivity.this, AccountsActivity.class);
                intent.putExtra(AccountsActivity.LANDING_SCREEN, 0);
                startActivity(intent);
                finish();
            } else if (landingScreen==1) {
                Intent intent = new Intent(LoginActivity.this, AccountsActivity.class);
                intent.putExtra(AccountsActivity.LANDING_SCREEN, 1);
                startActivity(intent);
                finish();
            } else if (landingScreen==2) {
                Intent intent = new Intent(LoginActivity.this, AccountsActivity.class);
                intent.putExtra(AccountsActivity.LANDING_SCREEN, 2);
                startActivity(intent);
                finish();
            } else {
                startActivity(new Intent(LoginActivity.this, WRewardsActivity.class));
                finish();
            }

        }
    }
}
