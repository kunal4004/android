package za.co.woolworths.financial.services.android.ui.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;

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
import za.co.woolworths.financial.services.android.ui.activities.WTransactionsActivity;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.FontHyperTextParser;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.WErrorDialog;
import za.co.woolworths.financial.services.android.util.WFormatter;

import static com.google.android.gms.plus.PlusOneDummyView.TAG;

/**
 * Created by W7099877 on 22/11/2016.
 */

public class WPersonalLoanFragment extends Fragment implements View.OnClickListener {

    public WTextView availableBalance;
    public WTextView creditLimit;
    public WTextView dueDate;
    public WTextView minAmountDue;
    public WTextView currentBalance;
    public WTextView withdrawCashNow;
    public WTextView transactions;
    public WTextView txtIncreseLimit;

    String productOfferingId;
    private ProgressDialog mGetActiveOfferProgressDialog;
    private WoolworthsApplication woolworthsApplication;
    private ConnectionDetector connectionDetector;
    private WebView mProgressCreditLimit;
    private boolean isOfferActive = true;
    private ImageView mImageArrow;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.cards_common_fragment, container, false);
        woolworthsApplication = (WoolworthsApplication) getActivity().getApplication();
        connectionDetector = new ConnectionDetector();
        availableBalance = (WTextView) view.findViewById(R.id.available_funds);
        creditLimit = (WTextView) view.findViewById(R.id.creditLimit);
        dueDate = (WTextView) view.findViewById(R.id.dueDate);
        minAmountDue = (WTextView) view.findViewById(R.id.minAmountDue);
        currentBalance = (WTextView) view.findViewById(R.id.currentBalance);
        withdrawCashNow = (WTextView) view.findViewById(R.id.withdrawCashNow);
        transactions = (WTextView) view.findViewById(R.id.txtTransactions);
        txtIncreseLimit = (WTextView) view.findViewById(R.id.txtIncreseLimit);
        mProgressCreditLimit = (WebView)view.findViewById(R.id.progressCreditLimit);
        mImageArrow = (ImageView)view.findViewById(R.id.imgArrow);
        mProgressCreditLimit.loadUrl("file:///android_asset/web/pulse.html");
        withdrawCashNow.setVisibility(View.VISIBLE);
        withdrawCashNow.setOnClickListener(this);
        txtIncreseLimit.setOnClickListener(this);
        transactions.setOnClickListener(this);
        txtIncreseLimit.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ripple_effect_purple));
        AccountsResponse accountsResponse = new Gson().fromJson(getArguments().getString("accounts"), AccountsResponse.class);
        bindData(accountsResponse);
        disableIncreaseLimit();
        hideProgressBar();
        getActiveOffer();
        return view;
    }

    public void bindData(AccountsResponse response) {
        List<Account> accountList = response.accountList;
        if (accountList != null) {
            for (Account p : accountList) {
                if ("PL".equals(p.productGroupCode)) {
                    productOfferingId = String.valueOf(p.productOfferingId);
                    woolworthsApplication.setProductOfferingId(p.productOfferingId);
                    availableBalance.setText(FontHyperTextParser.getSpannable(WFormatter.formatAmount(p.availableFunds), 1, getActivity()));
                    creditLimit.setText(FontHyperTextParser.getSpannable(WFormatter.formatAmount(p.creditLimit), 1, getActivity()));
                    minAmountDue.setText(FontHyperTextParser.getSpannable(WFormatter.formatAmount(p.minimumAmountDue), 1, getActivity()));
                    currentBalance.setText(FontHyperTextParser.getSpannable(WFormatter.formatAmount(p.currentBalance), 1, getActivity()));
                    try {
                        dueDate.setText(FontHyperTextParser.getSpannable(WFormatter.formatDate(p.paymentDueDate), 1, getActivity()));
                    } catch (ParseException e) {
                        dueDate.setText(p.paymentDueDate);
                        WiGroupLogger.e(getActivity(), TAG, e.getMessage(), e);
                    }

                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.withdrawCashNow:
                break;
            case R.id.txtTransactions:
                Intent intent = new Intent(getActivity(), WTransactionsActivity.class);
                intent.putExtra("productOfferingId", productOfferingId);
                startActivity(intent);
                break;


            case R.id.txtIncreseLimit:
                if (!isOfferActive) {
                    Intent openCLIIncrease = new Intent(getActivity(), CLIActivity.class);
                    startActivity(openCLIIncrease);
                    getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
                break;

        }
    }

    private void getActiveOffer() {
        if (connectionDetector.isOnline(getActivity())) {
            new HttpAsyncTask<String, String, OfferActive>() {
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
                        if(isOfferActive){
                            disableIncreaseLimit();
                        }else {
                            enableIncreaseLimit();
                        }
                    } else {
                        disableIncreaseLimit();
                        WErrorDialog.setErrorMessage(getActivity(), httpDesc);
                    }
                    hideProgressBar();
                }

                @Override
                protected Class<OfferActive> httpDoInBackgroundReturnType() {
                    return OfferActive.class;
                }
            }.execute();
        } else {
            hideProgressBar();
            WErrorDialog.getErrConnectToServer(getActivity());
        }
    }

    public void hideProgressBar() {
        mProgressCreditLimit.setVisibility(View.GONE);
        mImageArrow.setVisibility(View.VISIBLE);
        txtIncreseLimit.setVisibility(View.VISIBLE);
    }

    public void enableIncreaseLimit(){
        txtIncreseLimit.setEnabled(true);
        txtIncreseLimit.setTextColor(Color.BLACK);
    }

    public void disableIncreaseLimit(){
        txtIncreseLimit.setEnabled(false);
        txtIncreseLimit.setTextColor(Color.GRAY);
    }
}