package za.co.woolworths.financial.services.android.ui.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.awfs.coordination.R;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStreamReader;

import retrofit.RetrofitError;
import za.co.wigroup.logger.lib.WiGroupLogger;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.AuthoriseLoanRequest;
import za.co.woolworths.financial.services.android.models.dto.AuthoriseLoanResponse;
import za.co.woolworths.financial.services.android.models.dto.IssueLoanResponse;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.util.FontHyperTextParser;
import za.co.woolworths.financial.services.android.util.WErrorDialog;
import za.co.woolworths.financial.services.android.util.WFormatter;

public class PersonalLoanWithdrawalConfirmationActivity extends Activity {

    public static final String ISSUE_LOAN_RESPONSE = "ISSUE_LOAN_RESPONSE";
    public static final String PRODUCT_ID = "PRODUCT_ID";
    public static final String CREDIT_LIMIT = "CREDIT_LIMIT";
    private static final String TAG = "PersonalLoanWithdrawalConfirmationActivity";
    private IssueLoanResponse mIssueLoanResponse;
    private int mProductId;
    private int mCreditLimit;
    private ProgressDialog mProgressDialog;
    private AlertDialog mAlertDialog;
    private AlertDialog mErrorDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.personal_loan_withdrawal_confrimation_activity);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(FontHyperTextParser.getSpannable(getString(R.string.authorising_loan), 0, this));
        mProgressDialog.setCancelable(false);
        getActionBar().setTitle(FontHyperTextParser.getSpannable(getString(R.string.personal_loan_withdrawal), 0, this));
        mIssueLoanResponse = new Gson().fromJson(getIntent().getExtras().getString(ISSUE_LOAN_RESPONSE, ""), IssueLoanResponse.class);
        mProductId = getIntent().getExtras().getInt(PRODUCT_ID, 0);
        mCreditLimit = getIntent().getExtras().getInt(CREDIT_LIMIT, 0);
        mAlertDialog = new AlertDialog.Builder(this)
                .setTitle(FontHyperTextParser.getSpannable(getString(R.string.success), 1, this))
                .setPositiveButton(FontHyperTextParser.getSpannable(getString(R.string.ok), 1, this), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((WoolworthsApplication) getApplication()).getUserManager().invalidateCache();
                        finish();
                    }
                }).create();
        mErrorDialog = WErrorDialog.getSimplyErrorDialog(this);
        IssueLoanResponse issueLoanResponse = new Gson().fromJson(getIntent().getExtras().getString(ISSUE_LOAN_RESPONSE, ""), IssueLoanResponse.class);
        ((TextView) findViewById(R.id.personal_loan_withdrawal_confirmation_amount)).setText(WFormatter.formatAmount(issueLoanResponse.drawDownAmount));
        ((TextView) findViewById(R.id.personal_loan_withdrawal_confirmation_repayment)).setText(WFormatter.formatAmount(issueLoanResponse.installmentAmount));
        ((TextView) findViewById(R.id.personal_loan_withdrawal_confirmation_months)).setText(getString(R.string.issue_loan_confirmation_months, issueLoanResponse.repaymentPeriod));
        findViewById(R.id.personal_loan_withdrawal_confirmation_cancel_action).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        findViewById(R.id.personal_loan_withdrawal_confirmation_submit_action).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AsyncTask<AuthoriseLoanRequest, String, AuthoriseLoanResponse>() {

                    @Override
                    protected void onPreExecute() {
                        mProgressDialog.show();
                    }

                    @Override
                    protected void onPostExecute(AuthoriseLoanResponse authoriseLoanResponse) {
                        if (!isFinishing()) {
                            mProgressDialog.dismiss();
                            switch (authoriseLoanResponse.httpCode) {
                                case 200:
                                    mAlertDialog.setMessage(FontHyperTextParser.getSpannable(getString(R.string.success_person_loan_draw_down), 0, PersonalLoanWithdrawalConfirmationActivity.this));
                                    mAlertDialog.show();
                                    break;
                                default:
                                    mErrorDialog.setMessage(FontHyperTextParser.getSpannable(authoriseLoanResponse.response.desc, 0, PersonalLoanWithdrawalConfirmationActivity.this));
                                    mErrorDialog.show();
                                    break;
                            }
                        }
                    }

                    @Override
                    protected AuthoriseLoanResponse doInBackground(AuthoriseLoanRequest... params) {
                        try {
                            return ((WoolworthsApplication) getApplication()).getApi().authoriseLoan(params[0]);
                        } catch (RetrofitError e) {
                            try {
                                retrofit.client.Response response = e.getResponse();
                                if (response != null) {
                                    return new Gson().fromJson(new InputStreamReader(response.getBody().in()), AuthoriseLoanResponse.class);
                                } else {
                                    AuthoriseLoanResponse loginResponse = new AuthoriseLoanResponse();
                                    loginResponse.httpCode = 408;
                                    loginResponse.response = new Response();
                                    loginResponse.response.desc = getString(R.string.err_002);
                                    return loginResponse;
                                }
                            } catch (IOException e1) {
                                WiGroupLogger.e(PersonalLoanWithdrawalConfirmationActivity.this, TAG, e.getMessage(), e);
                            }
                        }
                        return null;
                    }
                }.execute(new AuthoriseLoanRequest(mProductId, mIssueLoanResponse.drawDownAmount, mIssueLoanResponse.repaymentPeriod, mIssueLoanResponse.installmentAmount, mCreditLimit));
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        mProgressDialog.dismiss();
        mAlertDialog.dismiss();
        mErrorDialog.dismiss();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
