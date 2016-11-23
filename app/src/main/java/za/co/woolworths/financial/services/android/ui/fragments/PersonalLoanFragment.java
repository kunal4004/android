package za.co.woolworths.financial.services.android.ui.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.awfs.coordination.R;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;

import retrofit.RetrofitError;
import za.co.wigroup.logger.lib.WiGroupLogger;
import za.co.woolworths.financial.services.android.models.UserManager;
import za.co.woolworths.financial.services.android.models.dto.Account;
import za.co.woolworths.financial.services.android.models.dto.AccountResponse;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.ui.activities.PersonalLoanWithdrawalActivity;
import za.co.woolworths.financial.services.android.ui.activities.TransactionHistoryActivity;
import za.co.woolworths.financial.services.android.ui.views.WBalanceView;
import za.co.woolworths.financial.services.android.util.FontHyperTextParser;
import za.co.woolworths.financial.services.android.util.WErrorDialog;
import za.co.woolworths.financial.services.android.util.WFormatter;

@SuppressLint("ValidFragment")
public class PersonalLoanFragment extends BaseAccountFragment {
    private static final String TAG = "PersonalLoanFragment";
    private int mPersonalLoanId;
    private int mAvailableFunds;
    private int mMinDrawDownAmount;
    private int mCurrentBalance;
    private int mCreditLimit;
    private int mCreditThreshold;
    private AlertDialog mError;

    @Override
    public int getTitle() {
        return R.string.personal_loan;
    }

    @Override
    public int getTabColor() {
        return R.color.personal_loan;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.personal_loan_fragment, null);
        mError = WErrorDialog.getSimplyErrorDialog(getActivity());
        view.findViewById(R.id.personal_loan_transaction_action).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), TransactionHistoryActivity.class);
                intent.putExtra(TransactionHistoryActivity.ACCOUNT_TYPE, TransactionHistoryActivity.Accounts.PERSONAL_LOAN);
                intent.putExtra(TransactionHistoryActivity.PRODUCT_ID, mPersonalLoanId);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.slide_up_in, R.anim.slide_up_out);
            }
        });
        view.findViewById(R.id.personal_loan_withdrawal_action).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PersonalLoanWithdrawalActivity.class);
                intent.putExtra(PersonalLoanWithdrawalActivity.CREDIT_LIMIT, mCreditLimit);
                intent.putExtra(PersonalLoanWithdrawalActivity.LOAN_MAX, mAvailableFunds);
                intent.putExtra(PersonalLoanWithdrawalActivity.LOAN_MIN, mMinDrawDownAmount);
                intent.putExtra(PersonalLoanWithdrawalActivity.CURRENT_BALANCE, mCurrentBalance);
                intent.putExtra(PersonalLoanWithdrawalActivity.CREDIT_THRESHOLD, mCreditThreshold);
                intent.putExtra(PersonalLoanWithdrawalActivity.PRODUCT_ID, mPersonalLoanId);
                startActivity(intent);
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Account account = new Gson().fromJson(getApplication().getUserManager().getAccount(UserManager.PERSONAL_LOAN), Account.class);
        if (account.retrievalError) {
            new AsyncTask<Integer, String, AccountResponse>() {
                @Override
                protected AccountResponse doInBackground(Integer... params) {
                    try {
                        return getApplication().getApi().getAccount(String.valueOf(params[0]));
                    } catch (RetrofitError e) {
                        try {
                            retrofit.client.Response response = e.getResponse();
                            if (response != null) {
                                return new Gson().fromJson(new InputStreamReader(response.getBody().in()), AccountResponse.class);
                            } else {
                                AccountResponse accountResponse = new AccountResponse();
                                accountResponse.httpCode = 408;
                                accountResponse.response = new Response();
                                accountResponse.response.desc = getString(R.string.err_002);
                                return accountResponse;
                            }
                        } catch (IOException e1) {
                            WiGroupLogger.e(getActivity(), TAG, e.getMessage(), e);
                            return null;
                        }
                    }
                }

                @Override
                protected void onPostExecute(AccountResponse accountResponse) {
                    handleAccountResponse(accountResponse);
                }
            }.execute(account.productOfferingId);
            return;
        } else {
            handleAccountResponse(account);
        }
    }

    private void handleAccountResponse(Account account) {
        mPersonalLoanId = account.productOfferingId;
        View view = getView();
        if (view == null){
            return;
        }
        WBalanceView balanceView = (WBalanceView) view.findViewById(R.id.personal_loan_balance);
        mCurrentBalance = account.currentBalance;
        mCreditThreshold = account.rpCreditLimitThreshold;
        balanceView.setAmountInCents(mCurrentBalance);
        balanceView.setPercentSpent(100 - ((float) account.availableFunds / (float) account.creditLimit * 100f));
        mCreditLimit = account.creditLimit;
        mAvailableFunds = account.availableFunds;
        mMinDrawDownAmount = account.minDrawDownAmount;
        if (mAvailableFunds < mMinDrawDownAmount) {
            view.findViewById(R.id.personal_loan_withdrawal_action).setEnabled(false);
        }
//        if (account.creditLimit < 0){
//            account.creditLimit = 0;
//        }
//        if (account.availableFunds < 0){
//            account.availableFunds = 0;
//        }
        ((TextView) view.findViewById(R.id.personal_loan_credit_limit)).setText(FontHyperTextParser.getSpannable(WFormatter.formatAmount(account.creditLimit), 1, getActivity()));
        ((TextView) view.findViewById(R.id.personal_loan_available_funds)).setText(FontHyperTextParser.getSpannable(WFormatter.formatAmount(account.availableFunds), 1, getActivity()));
        ((TextView) view.findViewById(R.id.personal_loan_minimum_payment)).setText(FontHyperTextParser.getSpannable(WFormatter.formatAmount(account.minimumAmountDue), 1, getActivity()));
        try {
            ((TextView) view.findViewById(R.id.personal_loan_payment_date)).setText(FontHyperTextParser.getSpannable(WFormatter.formatDate(account.paymentDueDate), 1, getActivity()));
        } catch (ParseException e) {
            ((TextView) view.findViewById(R.id.personal_loan_payment_date)).setText(account.paymentDueDate);
            WiGroupLogger.e(getActivity(), TAG, e.getMessage(), e);
        }
        view.findViewById(R.id.personal_loan_loading).setVisibility(View.GONE);
    }

    private void handleAccountResponse(AccountResponse accountResponse) {
        switch (accountResponse.httpCode) {
            case 200:
                getApplication().getUserManager().setAccount(UserManager.PERSONAL_LOAN, accountResponse.account);
                handleAccountResponse(accountResponse.account);

                break;
            default:
                mError.setMessage(FontHyperTextParser.getSpannable(accountResponse.response.desc, 0, getActivity()));
                mError.show();
                break;
        }
    }

    @Override
    public void onPause() {
        mError.dismiss();
        super.onPause();
    }
}
