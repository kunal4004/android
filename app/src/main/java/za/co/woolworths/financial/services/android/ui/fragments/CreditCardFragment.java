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
import za.co.woolworths.financial.services.android.ui.activities.TransactionHistoryActivity;
import za.co.woolworths.financial.services.android.ui.views.WBalanceView;
import za.co.woolworths.financial.services.android.util.FontHyperTextParser;
import za.co.woolworths.financial.services.android.util.WErrorDialog;
import za.co.woolworths.financial.services.android.util.WFormatter;

@SuppressLint("ValidFragment")
public class CreditCardFragment extends BaseAccountFragment {
    private static final String TAG = "CreditCardFragment";
    private int mCreditCardId;
    private AlertDialog mError;

    @Override
    public int getTitle() {
        return R.string.credit_card;
    }

    @Override
    public int getTabColor() {
        return R.color.credit_card;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.credit_card_fragment, null);

        mError = WErrorDialog.getSimplyErrorDialog(getActivity());
        view.findViewById(R.id.credit_card_transaction_action).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), TransactionHistoryActivity.class);
                intent.putExtra(TransactionHistoryActivity.ACCOUNT_TYPE, TransactionHistoryActivity.Accounts.CREDIT_CARD);
                intent.putExtra(TransactionHistoryActivity.PRODUCT_ID, mCreditCardId);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.slide_up_in, R.anim.slide_up_out);
            }
        });

        view.findViewById(R.id.buttonLayoutCredit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), TransactionHistoryActivity.class);
                intent.putExtra(TransactionHistoryActivity.ACCOUNT_TYPE, TransactionHistoryActivity.Accounts.CREDIT_CARD);
                intent.putExtra(TransactionHistoryActivity.PRODUCT_ID, mCreditCardId);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.slide_up_in, R.anim.slide_up_out);
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Account account = new Gson().fromJson(getApplication().getUserManager().getAccount(UserManager.CREDIT_CARD), Account.class);
        if (account.retrievalError) {
            new AsyncTask<Integer, String, AccountResponse>() {
                @Override
                protected AccountResponse doInBackground(Integer... params) {
                    try {
                        if (getActivity() != null && !getActivity().isFinishing()) {
                            return getApplication().getApi().getAccount(String.valueOf(params[0]));
                        } else {
                            return null;
                        }
                    } catch (RetrofitError e) {
                        try {
                            retrofit.client.Response response = e.getResponse();
                            if (response != null && response.getBody().mimeType().equals("application/json")) {
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
                    if (getActivity() != null && !getActivity().isFinishing()) {
                        handleAccountResponse(accountResponse);
                    }
                }
            }.execute(account.productOfferingId);
            return;
        } else {
            handleAccountResponse(account);
        }
    }

    private void handleAccountResponse(Account account) {
        mCreditCardId = account.productOfferingId;
        View view = getView();
        WBalanceView balanceView = (WBalanceView) view.findViewById(R.id.credit_card_balance);
        balanceView.setAmountInCents(account.currentBalance);
        balanceView.setPercentSpent(100 - ((float) account.availableFunds / (float) account.creditLimit * 100f));
//        if (account.creditLimit < 0) {
//            account.creditLimit = 0;
//        }
//        if (account.availableFunds < 0) {
//            account.availableFunds = 0;
//        }
        ((TextView) view.findViewById(R.id.credit_card_limit)).setText(FontHyperTextParser.getSpannable(WFormatter.formatAmount(account.creditLimit), 1, getActivity()));
        ((TextView) view.findViewById(R.id.credit_card_available_funds)).setText(FontHyperTextParser.getSpannable(WFormatter.formatAmount(account.availableFunds), 1, getActivity()));
        ((TextView) view.findViewById(R.id.credit_card_minimum_payment)).setText(FontHyperTextParser.getSpannable(WFormatter.formatAmount(account.minimumAmountDue), 1, getActivity()));
        try {
            ((TextView) view.findViewById(R.id.credit_card_payment_date)).setText(FontHyperTextParser.getSpannable(WFormatter.formatDate(account.paymentDueDate), 1, getActivity()));
        } catch (ParseException e) {
            ((TextView) view.findViewById(R.id.credit_card_payment_date)).setText(account.paymentDueDate);
            WiGroupLogger.e(getActivity(), TAG, e.getMessage(), e);
        }
        view.findViewById(R.id.credit_card_loading).setVisibility(View.GONE);
    }

    private void handleAccountResponse(AccountResponse accountResponse) {
        switch (accountResponse.httpCode) {
            case 200:
                getApplication().getUserManager().setAccount(UserManager.CREDIT_CARD, accountResponse.account);
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
