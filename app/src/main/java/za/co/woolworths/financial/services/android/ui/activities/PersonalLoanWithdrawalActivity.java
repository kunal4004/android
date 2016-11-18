package za.co.woolworths.financial.services.android.ui.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.awfs.coordination.R;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStreamReader;

import retrofit.RetrofitError;
import za.co.wigroup.logger.lib.WiGroupLogger;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.IssueLoanRequest;
import za.co.woolworths.financial.services.android.models.dto.IssueLoanResponse;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.FontHyperTextParser;
import za.co.woolworths.financial.services.android.util.WErrorDialog;
import za.co.woolworths.financial.services.android.util.WFormatter;

public class PersonalLoanWithdrawalActivity extends Activity {

    public static final String LOAN_MAX = "loan_max";
    public static final String LOAN_MIN = "loan_min";
    public static final String CURRENT_BALANCE = "current_balance";
    public static final String CREDIT_LIMIT = "credit_limit";
    public static final String CREDIT_THRESHOLD = "credit_threshold";
    public static final String PRODUCT_ID = "product_id";
    private static final String TAG = "PersonalLoanWithdrawalActivity";
    long mMaxAmountCents, mMinAmountCents, mIncrementAmount = 100l, mDefaultAmount;
    int mDefaultMonths = 0;
    int[] mMonths;
    private int mCurrentBalance, mCreditLimit;
    private int mCreditThreshold;
    private Thread mLoop;
    private Runnable mRunnable;
    private int mFPS = 4;
    private boolean mRun = false;
    private int mMultiplier = 1;
    private boolean mIncrement;
    private long mStartTime;
    private long mMultiplierTime = 1000;
    private int mProductId;
    private ProgressDialog mProgress;
    private AlertDialog mError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.personal_loan_withdrawal_activity);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setTitle(FontHyperTextParser.getSpannable(getString(R.string.personal_loan_withdrawal), 0, this));
        mProgress = new ProgressDialog(this);
        mProgress.setMessage(FontHyperTextParser.getSpannable(getString(R.string.issue_loan), 1, this));
        mProgress.setCancelable(false);
        mMaxAmountCents = mDefaultAmount = getIntent().getExtras().getInt(LOAN_MAX);
        mMinAmountCents = getIntent().getExtras().getInt(LOAN_MIN);
        mCurrentBalance = getIntent().getExtras().getInt(CURRENT_BALANCE);
        mCreditLimit = getIntent().getExtras().getInt(CREDIT_LIMIT);
        mCreditThreshold = getIntent().getExtras().getInt(CREDIT_THRESHOLD);
        mProductId = getIntent().getExtras().getInt(PRODUCT_ID);
        mError = WErrorDialog.getSimplyErrorDialog(this);
        LinearLayout monthView = (LinearLayout) findViewById(R.id.personal_loan_withdrawal_activity_months);
        monthView.removeAllViews();
        if (mCreditLimit > mCreditThreshold) {
            mMonths = getResources().getIntArray(R.array.months_max);
            monthView.addView(getLayoutInflater().inflate(R.layout.months_max, null));
        } else {
            mMonths = getResources().getIntArray(R.array.months_default);
            monthView.addView(getLayoutInflater().inflate(R.layout.months_default, null));
        }

        if (mMaxAmountCents < 0) {
            mMaxAmountCents = 0;
        }

        if (mCurrentBalance < 0) {
            ((TextView) findViewById(R.id.personal_loan_withdrawal_activity_current_balance)).setText(WFormatter.formatAmount(mCurrentBalance *-1)+" CR");
        }
        else{
            ((TextView) findViewById(R.id.personal_loan_withdrawal_activity_current_balance)).setText(WFormatter.formatAmount(mCurrentBalance));
        }

        ((TextView) findViewById(R.id.personal_loan_withdrawal_activity_available_funds)).setText(WFormatter.formatAmount((int) mMaxAmountCents));
        SeekBar seekBarAmount = (SeekBar) findViewById(R.id.personal_loan_withdrawal_activity_amount_seek);
        seekBarAmount.setMax(getNumberOfIncrements());
        seekBarAmount.setProgress((int) ((mDefaultAmount - mMinAmountCents) / mIncrementAmount));
        long amountInCents = mMinAmountCents + (seekBarAmount.getProgress() * mIncrementAmount);
        ((TextView) findViewById(R.id.personal_loan_withdrawal_activity_amount_text)).setText(String.format("R%d.%02d", (int) (amountInCents / 100), (int) (amountInCents % 100)));
        seekBarAmount.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                long amountInCents = mMinAmountCents + (progress * mIncrementAmount);
                ((TextView) findViewById(R.id.personal_loan_withdrawal_activity_amount_text)).setText(String.format("R%d.%02d", (int) (amountInCents / 100), (int) (amountInCents % 100)));
                if (progress == 0) {
                    findViewById(R.id.personal_loan_withdrawal_activity_amount_minus).setEnabled(false);
                } else {
                    findViewById(R.id.personal_loan_withdrawal_activity_amount_minus).setEnabled(true);
                }
                if (progress == ((SeekBar) findViewById(R.id.personal_loan_withdrawal_activity_amount_seek)).getMax()) {
                    findViewById(R.id.personal_loan_withdrawal_activity_amount_plus).setEnabled(false);
                } else {
                    findViewById(R.id.personal_loan_withdrawal_activity_amount_plus).setEnabled(true);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        findViewById(R.id.personal_loan_withdrawal_activity_amount_minus).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mIncrement = false;
                        mStartTime = System.currentTimeMillis();
                        mMultiplier = 1;
                        mFPS = 6;
                        mRun = true;
                        break;
                    case MotionEvent.ACTION_UP:
                        mRun = false;
                        break;
                }
                return true;
            }
        });
        findViewById(R.id.personal_loan_withdrawal_activity_amount_plus).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mIncrement = true;
                        mStartTime = System.currentTimeMillis();
                        mMultiplier = 1;
                        mFPS = 6;
                        mRun = true;
                        break;
                    case MotionEvent.ACTION_UP:
                        mRun = false;
                        break;
                }
                return true;
            }
        });

        SeekBar seekBarMonths = (SeekBar) findViewById(R.id.personal_loan_withdrawal_activity_months_seek);
        seekBarMonths.setMax(mMonths.length - 1);
        seekBarMonths.setProgress(mDefaultMonths);
        ((TextView) findViewById(R.id.personal_loan_withdrawal_activity_months_text)).setText(getString(R.string.withdraw_months, mMonths[mDefaultMonths]));
        seekBarMonths.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ((TextView) findViewById(R.id.personal_loan_withdrawal_activity_months_text)).setText(getString(R.string.withdraw_months, mMonths[progress]));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        findViewById(R.id.personal_loan_withdrawal_activity_action).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AsyncTask<Integer, String, IssueLoanResponse>() {
                    @Override
                    protected IssueLoanResponse doInBackground(Integer... params) {
                        try {
                            return ((WoolworthsApplication) getApplication()).getApi().issueLoan(new IssueLoanRequest(params[0], params[1], params[2], params[3]));
                        } catch (RetrofitError e) {
                            try {
                                retrofit.client.Response response = e.getResponse();
                                if (response != null) {
                                    return new Gson().fromJson(new InputStreamReader(response.getBody().in()), IssueLoanResponse.class);
                                } else {
                                    IssueLoanResponse issueLoanResponse = new IssueLoanResponse();
                                    issueLoanResponse.httpCode = 408;
                                    issueLoanResponse.response = new Response();
                                    issueLoanResponse.response.desc = getString(R.string.err_002);
                                    return issueLoanResponse;
                                }
                            } catch (IOException e1) {
                                WiGroupLogger.e(PersonalLoanWithdrawalActivity.this, TAG, e.getMessage(), e);
                            }
                        }
                        return null;
                    }

                    @Override
                    protected void onPreExecute() {
                        mProgress.show();
                    }

                    @Override
                    protected void onPostExecute(IssueLoanResponse s) {
                        if (!isFinishing()) {
                            mProgress.dismiss();
                            switch (s.httpCode) {
                                case 200:
                                    Intent intent = new Intent(PersonalLoanWithdrawalActivity.this, PersonalLoanWithdrawalConfirmationActivity.class);
                                    intent.putExtra(PersonalLoanWithdrawalConfirmationActivity.ISSUE_LOAN_RESPONSE, new Gson().toJson(s));
                                    intent.putExtra(PersonalLoanWithdrawalConfirmationActivity.PRODUCT_ID, mProductId);
                                    intent.putExtra(PersonalLoanWithdrawalConfirmationActivity.CREDIT_LIMIT, mCreditLimit);
                                    startActivity(intent);
                                    finish();
                                    break;
                                default:
                                    mError.setMessage(FontHyperTextParser.getSpannable(s.response.desc, 0, PersonalLoanWithdrawalActivity.this));
                                    mError.show();

                            }
                        }
                    }
                }.execute(mProductId,
                        (int) (mMinAmountCents + ((SeekBar) findViewById(R.id.personal_loan_withdrawal_activity_amount_seek)).getProgress() * mIncrementAmount),
                        mMonths[((SeekBar) findViewById(R.id.personal_loan_withdrawal_activity_months_seek)).getProgress()],
                        mCreditLimit);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRunnable = new Runnable() {

            @Override
            public void run() {
                while (true) {
                    if (mRun == true) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                SeekBar seekBar = (SeekBar) findViewById(R.id.personal_loan_withdrawal_activity_amount_seek);
                                if (mIncrement) {
                                    int newProgress = seekBar.getProgress() + (1 * mMultiplier);
                                    int max = seekBar.getMax();
                                    if (max > newProgress) {
                                        seekBar.setProgress(newProgress);
                                    } else {
                                        seekBar.setProgress(max);
                                        mRun = false;
                                    }
                                } else {
                                    int newProgress = seekBar.getProgress() - (1 * mMultiplier);
                                    if (0 < newProgress) {
                                        seekBar.setProgress(newProgress);
                                    } else {
                                        seekBar.setProgress(0);
                                        mRun = false;
                                    }
                                }
                            }
                        });
                        if (mMultiplier < 10000) {
                            if (mStartTime + mMultiplierTime < System.currentTimeMillis()) {
                                mStartTime = System.currentTimeMillis();
                                mMultiplier += 1;
                            }
                        }
                    }
                    try {
                        Thread.sleep(1000 / mFPS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        mLoop = new Thread(mRunnable);
        mLoop.start();
    }

    @Override
    protected void onPause() {
        mLoop.interrupt();
        mProgress.dismiss();
        mError.dismiss();
        super.onPause();
    }

    private int getNumberOfIncrements() {
        return (int) ((mMaxAmountCents - mMinAmountCents) / mIncrementAmount);
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
