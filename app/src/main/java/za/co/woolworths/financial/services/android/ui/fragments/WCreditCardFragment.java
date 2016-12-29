package za.co.woolworths.financial.services.android.ui.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import za.co.woolworths.financial.services.android.ui.activities.TransactionHistoryActivity;
import za.co.woolworths.financial.services.android.ui.activities.WTransactionsActivity;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.FontHyperTextParser;
import za.co.woolworths.financial.services.android.util.HttpAsyncTask;
import za.co.woolworths.financial.services.android.util.WErrorDialog;
import za.co.woolworths.financial.services.android.util.WFormatter;

import static com.awfs.coordination.R.id.swipe;
import static com.awfs.coordination.R.id.txtTransactions;
import static com.google.android.gms.plus.PlusOneDummyView.TAG;

/**
 * Created by W7099877 on 22/11/2016.
 */

public class WCreditCardFragment extends Fragment implements View.OnClickListener {
    public WTextView availableBalance;
    public WTextView creditLimit;
    public WTextView dueDate;
    public WTextView minAmountDue;
    public WTextView currentBalance;
    public WTextView transactions;
    public WTextView txtIncreseLimit;
    String productOfferingId;
    private ProgressDialog mGetActiveOfferProgressDialog;
    private ConnectionDetector connectionDetector;
    private WoolworthsApplication woolworthsApplication;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.cards_common_fragment, container, false);
        woolworthsApplication = (WoolworthsApplication)getActivity().getApplication();
        connectionDetector = new ConnectionDetector();
        availableBalance=(WTextView)view.findViewById(R.id.available_funds);
        creditLimit=(WTextView)view.findViewById(R.id.creditLimit);
        dueDate=(WTextView)view.findViewById(R.id.dueDate);
        minAmountDue=(WTextView)view.findViewById(R.id.minAmountDue);
        currentBalance=(WTextView)view.findViewById(R.id.currentBalance);
        transactions=(WTextView)view.findViewById(R.id.txtTransactions);
        txtIncreseLimit = (WTextView)view.findViewById(R.id.txtIncreseLimit);

        transactions.setOnClickListener(this);
        txtIncreseLimit.setOnClickListener(this);
        AccountsResponse accountsResponse=new Gson().fromJson(getArguments().getString("accounts"),AccountsResponse.class);
        bindData(accountsResponse);
        return view;
    }

    public void bindData(AccountsResponse response)
    {
        List<Account> accountList = response.accountList;
        if (accountList != null) {
            for (Account p : accountList) {
                if ("CC".equals(p.productGroupCode)) {
                    productOfferingId=String.valueOf(p.productOfferingId);
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
       switch (v.getId())
       {
           case R.id.txtTransactions:
               Intent intent =new Intent(getActivity(), WTransactionsActivity.class);
               intent.putExtra("productOfferingId",productOfferingId);
               startActivity(intent);
               break;

           case R.id.txtIncreseLimit:
               getActiveOffer();
               break;
       }
    }

    private void getActiveOffer() {
        if (connectionDetector.isOnline()) {
            new HttpAsyncTask<String, String, OfferActive>() {
                @Override
                protected OfferActive httpDoInBackground(String... params) {
                    return (woolworthsApplication.getApi().getActiveOfferRequest(productOfferingId));
                }

                @Override
                protected OfferActive httpError(String errorMessage, HttpErrorCode httpErrorCode) {
                    OfferActive offerActive = new OfferActive();
                    offerActive.response = new Response();
                    stopProgressDialog();
                    return offerActive;
                }

                @Override
                protected void onPreExecute() {
                    mGetActiveOfferProgressDialog = new ProgressDialog(getActivity());
                    mGetActiveOfferProgressDialog.setMessage(FontHyperTextParser.getSpannable(getString(R.string.cli_loading_active_offer), 1, getActivity()));
                    mGetActiveOfferProgressDialog.setCancelable(false);
                    mGetActiveOfferProgressDialog.show();
                    super.onPreExecute();
                }

                @Override
                protected void onPostExecute(OfferActive offerActive) {
                    super.onPostExecute(offerActive);
                    int httpCode = offerActive.httpCode;
                    String httpDesc = offerActive.response.desc;
                    if (httpCode == 200) {
                        if (offerActive.offerActive) {
                            Intent openCLIIncrease = new Intent(getActivity(), CLIActivity.class);
                            startActivity(openCLIIncrease);
                            getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        } else {
                            WErrorDialog.setErrorMessage(getActivity(), getString(R.string.cli_cannot_proceed_error));
                        }
                    } else {
                        WErrorDialog.setErrorMessage(getActivity(), httpDesc);
                    }
                    stopProgressDialog();
                }

                @Override
                protected Class<OfferActive> httpDoInBackgroundReturnType() {
                    return OfferActive.class;
                }
            }.execute();
        }else {
            WErrorDialog.getErrConnectToServer(getActivity());
        }
    }

    public void stopProgressDialog(){
        if(mGetActiveOfferProgressDialog != null && mGetActiveOfferProgressDialog.isShowing()){
            mGetActiveOfferProgressDialog.dismiss();
        }
    }

}
