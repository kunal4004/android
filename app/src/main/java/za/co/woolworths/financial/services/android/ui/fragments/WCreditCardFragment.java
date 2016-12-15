package za.co.woolworths.financial.services.android.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;
import com.google.gson.Gson;

import java.text.ParseException;
import java.util.List;

import za.co.wigroup.logger.lib.WiGroupLogger;
import za.co.woolworths.financial.services.android.models.dto.Account;
import za.co.woolworths.financial.services.android.models.dto.AccountsResponse;
import za.co.woolworths.financial.services.android.ui.activities.TransactionHistoryActivity;
import za.co.woolworths.financial.services.android.ui.activities.WTransactionsActivity;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.FontHyperTextParser;
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
    String productOfferingId;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.cards_common_fragment, container, false);

        availableBalance=(WTextView)view.findViewById(R.id.available_funds);
        creditLimit=(WTextView)view.findViewById(R.id.creditLimit);
        dueDate=(WTextView)view.findViewById(R.id.dueDate);
        minAmountDue=(WTextView)view.findViewById(R.id.minAmountDue);
        currentBalance=(WTextView)view.findViewById(R.id.currentBalance);
        transactions=(WTextView)view.findViewById(R.id.txtTransactions);
        transactions.setOnClickListener(this);
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
       }
    }
}
