package za.co.woolworths.financial.services.android.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.awfs.coordination.R;
import com.google.gson.Gson;

import java.text.ParseException;
import java.util.List;

import za.co.wigroup.logger.lib.WiGroupLogger;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.Account;
import za.co.woolworths.financial.services.android.models.dto.AccountsResponse;
import za.co.woolworths.financial.services.android.models.dto.OfferActive;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.ui.activities.CLIActivity;
import za.co.woolworths.financial.services.android.ui.activities.LoanWithdrawalActivity;
import za.co.woolworths.financial.services.android.ui.activities.MyAccountCardsActivity;
import za.co.woolworths.financial.services.android.ui.activities.WTransactionsActivity;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.FontHyperTextParser;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.PersonalLoanAmount;
import za.co.woolworths.financial.services.android.util.SharePreferenceHelper;
import za.co.woolworths.financial.services.android.util.PopWindowValidationMessage;
import za.co.woolworths.financial.services.android.util.WFormatter;

import static com.google.android.gms.plus.PlusOneDummyView.TAG;


/**
 * Created by W7099877 on 22/11/2016.
 */

public class WPersonalLoanFragment extends MyAccountCardsActivity.MyAccountCardsFragment implements View.OnClickListener {


    private PersonalLoanAmount personalLoanInfo;
    public WTextView availableBalance;
    public WTextView creditLimit;
    public WTextView dueDate;
    public WTextView minAmountDue;
    public WTextView currentBalance;
    public WTextView withdrawCashNow;
    public WTextView transactions;
    public WTextView txtIncreseLimit;

    String productOfferingId;
    private WoolworthsApplication woolworthsApplication;
    private ConnectionDetector connectionDetector;
    private ProgressBar mProgressCreditLimit;
    private boolean isOfferActive = true;
    private ImageView mImageArrow;
    private PopWindowValidationMessage mPopWindowValidationMessage;
    private SharePreferenceHelper mSharePreferenceHelper;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.cards_common_fragment, container, false);
        woolworthsApplication = (WoolworthsApplication) getActivity().getApplication();
        connectionDetector = new ConnectionDetector();
        mSharePreferenceHelper = SharePreferenceHelper.getInstance(getActivity());
        availableBalance = (WTextView) view.findViewById(R.id.available_funds);
        mPopWindowValidationMessage = new PopWindowValidationMessage(getActivity());
        creditLimit = (WTextView) view.findViewById(R.id.creditLimit);
        dueDate = (WTextView) view.findViewById(R.id.dueDate);
        minAmountDue = (WTextView) view.findViewById(R.id.minAmountDue);
        currentBalance = (WTextView) view.findViewById(R.id.currentBalance);
        withdrawCashNow = (WTextView) view.findViewById(R.id.withdrawCashNow);
        transactions = (WTextView) view.findViewById(R.id.txtTransactions);
        txtIncreseLimit = (WTextView) view.findViewById(R.id.txtIncreseLimit);
        mProgressCreditLimit = (ProgressBar) view.findViewById(R.id.progressCreditLimit);
        mProgressCreditLimit.getIndeterminateDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);

        mImageArrow = (ImageView) view.findViewById(R.id.imgArrow);
        withdrawCashNow.setVisibility(View.VISIBLE);
        withdrawCashNow.setOnClickListener(this);
        txtIncreseLimit.setOnClickListener(this);
        transactions.setOnClickListener(this);
        AccountsResponse accountsResponse = new Gson().fromJson(getArguments().getString("accounts"), AccountsResponse.class);
        bindData(accountsResponse);
        disableIncreaseLimit();
        hideProgressBar();
        getActiveOffer();
        setTextSize();
        return view;
    }

    //To remove negative signs from negative balance and add "CR" after the negative balance
    public String removeNegativeSymbol(SpannableString amount) {
        String currentAmount = amount.toString();
        if (currentAmount.contains("-")) {
            currentAmount = currentAmount.replace("-", "") + " CR";
        }
        return currentAmount;
    }

    //To remove negative signs from negative balance and add "CR" after the negative balance
    public String removeNegativeSymbol(String amount) {
        String currentAmount = amount.toString();
        if (currentAmount.contains("-")) {
            currentAmount = currentAmount.replace("-", "") + " CR";
        }
        return currentAmount;
    }

    public void bindData(AccountsResponse response) {
        List<Account> accountList = response.accountList;
        if (accountList != null) {
            for (Account p : accountList) {
                if ("PL".equals(p.productGroupCode)) {
                    productOfferingId = String.valueOf(p.productOfferingId);
                    woolworthsApplication.setProductOfferingId(p.productOfferingId);
                    mSharePreferenceHelper.save(String.valueOf(p.productOfferingId),"lw_product_offering_id");
                    int minDrawnAmount = p.minDrawDownAmount;
                    if (TextUtils.isEmpty(String.valueOf(minDrawnAmount))){
                        minDrawnAmount=0;
                    }
                    personalLoanInfo.minDrawnAmount(minDrawnAmount);
                    availableBalance.setText(removeNegativeSymbol(FontHyperTextParser.getSpannable(WFormatter.formatAmount(p.availableFunds), 1, getActivity())));
                    mSharePreferenceHelper.save(availableBalance.getText().toString(), "lw_available_fund");
                    creditLimit.setText(removeNegativeSymbol(FontHyperTextParser.getSpannable(WFormatter.formatAmount(p.creditLimit), 1, getActivity())));
                    mSharePreferenceHelper.save(creditLimit.getText().toString(), "lw_credit_limit");
//                    try {
//                        minAmountDue.setText(removeNegativeSymbol(FontHyperTextParser.getSpannable(WFormatter.formatAmount(p.minimumAmountDue), 1, getActivity())));
//                        currentBalance.setText(removeNegativeSymbol(FontHyperTextParser.getSpannable(WFormatter.formatAmount(p.currentBalance), 1, getActivity())));
//                        dueDate.setText(FontHyperTextParser.getSpannable(WFormatter.formatDate(p.paymentDueDate), 1, getActivity()));
//                    } catch (ParseException e) {
//                        Log.e("dueDateErr", e.toString());
//                        dueDate.setText(p.paymentDueDate);
//                        WiGroupLogger.e(getActivity(), TAG, e.getMessage(), e);
//                    }
                    minAmountDue.setText(removeNegativeSymbol(WFormatter.formatAmount(p.minimumAmountDue)));
                    currentBalance.setText(removeNegativeSymbol(WFormatter.formatAmount(p.currentBalance)));
                    try {
                        dueDate.setText(WFormatter.formatDate(p.paymentDueDate));
                    } catch (ParseException ex) {
                        dueDate.setText(p.paymentDueDate);
                    }
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.withdrawCashNow:
                mSharePreferenceHelper.save("", "lw_amount_drawn_cent");
                Intent openWithdrawCashNow = new Intent(getActivity(), LoanWithdrawalActivity.class);
                startActivity(openWithdrawCashNow);
                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                break;
            case R.id.txtTransactions:
                Intent intent = new Intent(getActivity(), WTransactionsActivity.class);
                intent.putExtra("productOfferingId", productOfferingId);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                break;
            case R.id.txtIncreseLimit:
                if (!isOfferActive) {
                    Intent openCLIIncrease = new Intent(getActivity(), CLIActivity.class);
                    startActivity(openCLIIncrease);
                    getActivity().overridePendingTransition(0, 0);
                }
                break;
        }
    }

    private void getActiveOffer() {
        if (connectionDetector.isOnline(getActivity())) {
            AsyncTask<String, String, OfferActive> asyncActiveOfferRequestLoan = new HttpAsyncTask<String, String, OfferActive>() {
                @Override
                protected OfferActive httpDoInBackground(String... params) {
                    return (woolworthsApplication.getApi().getActiveOfferRequest(productOfferingId));
                }

                @Override
                protected OfferActive httpError(String errorMessage, HttpErrorCode httpErrorCode) {
                    OfferActive offerActive = new OfferActive();
                    offerActive.response = new Response();
                    isOfferActive = false;
                    hideProgressBar();
                    return offerActive;
                }

                @Override
                protected void onPreExecute() {
                    mProgressCreditLimit.setVisibility(View.VISIBLE);
                    mImageArrow.setVisibility(View.GONE);
                    txtIncreseLimit.setVisibility(View.GONE);
                    super.onPreExecute();
                }

                @Override
                protected void onPostExecute(OfferActive offerActive) {
                    super.onPostExecute(offerActive);
                    int httpCode = offerActive.httpCode;
                    String httpDesc = offerActive.response.desc;
                    if (httpCode == 200) {
                        isOfferActive = offerActive.offerActive;
                        if (isOfferActive) {
                            disableIncreaseLimit();
                        } else {
                            enableIncreaseLimit();
                        }
                    } else {
                        disableIncreaseLimit();
                        mPopWindowValidationMessage.displayValidationMessage(httpDesc,
                                PopWindowValidationMessage.OVERLAY_TYPE.ERROR);
                    }
                    hideProgressBar();
                }

                @Override
                protected Class<OfferActive> httpDoInBackgroundReturnType() {
                    return OfferActive.class;
                }
            };
            asyncActiveOfferRequestLoan.execute();
        } else {
            hideProgressBar();
            mPopWindowValidationMessage.displayValidationMessage(getString(R.string.connect_to_server),
                    PopWindowValidationMessage.OVERLAY_TYPE.ERROR);
        }
    }

    public void hideProgressBar() {
        mProgressCreditLimit.setVisibility(View.GONE);
        mImageArrow.setVisibility(View.VISIBLE);
        txtIncreseLimit.setVisibility(View.VISIBLE);
    }

    public void enableIncreaseLimit() {
        txtIncreseLimit.setEnabled(true);
        txtIncreseLimit.setTextColor(Color.BLACK);
        txtIncreseLimit.setAlpha(1);
        mImageArrow.setImageAlpha(255);
    }

    public void disableIncreaseLimit() {
        txtIncreseLimit.setEnabled(false);
        txtIncreseLimit.setTextColor(Color.GRAY);
        mImageArrow.setImageAlpha(75);
    }

    private void setTextSize() {
        dueDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        minAmountDue.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        currentBalance.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);

        Typeface mMyriaProFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/MyriadPro-Regular.otf");
        dueDate.setTypeface(mMyriaProFont);
        minAmountDue.setTypeface(mMyriaProFont);
        currentBalance.setTypeface(mMyriaProFont);
    }

    @Override
    public void onResume() {
        super.onResume();
        setTextSize();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        personalLoanInfo = (PersonalLoanAmount) context;

    }
}

