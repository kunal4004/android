package za.co.woolworths.financial.services.android.ui.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.activities.ContactUsActivity;
import za.co.woolworths.financial.services.android.ui.activities.MessagesActivity;
import za.co.woolworths.financial.services.android.ui.activities.MyAccountCardsActivity;
import za.co.woolworths.financial.services.android.ui.activities.WContactUsActivity;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WTextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class MyAccountsFragment extends Fragment implements View.OnClickListener {

    ImageView openMessageActivity;
    WTextView applyNowStoreCard;
    RelativeLayout contactUs;
    boolean isLoggedIn = false;
    boolean isCreditCard = false;
    boolean isStoreCard = true;
    boolean isPersonalCard = true;
    CardView applyCreditCardView;
    CardView applyStoreCardView;
    CardView applyPersonalCardView;
    CardView linkedCreditCardView;
    CardView linkedStoreCardView;
    CardView linkedPersonalCardView;
    LinearLayout linkedAccountsLayout;
    LinearLayout applyNowAccountsLayout;
    LinearLayout loggedOutHeaderLayout;
    LinearLayout loggedInHeaderLayout;
    WButton linkAccountsBtn;

    public MyAccountsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.my_accounts_fragment, container, false);
        // ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("ACCOUNTS");
        openMessageActivity = (ImageView) view.findViewById(R.id.openMessageActivity);
        applyNowStoreCard = (WTextView) view.findViewById(R.id.applyNowStoreCardTxt);
        contactUs = (RelativeLayout) view.findViewById(R.id.contactUs);
        applyStoreCardView = (CardView) view.findViewById(R.id.applyStoreCard);
        applyCreditCardView = (CardView) view.findViewById(R.id.applyCrediCard);
        applyPersonalCardView = (CardView) view.findViewById(R.id.applyPersonalLoan);
        linkedCreditCardView = (CardView) view.findViewById(R.id.linkedCrediCard);
        linkedStoreCardView = (CardView) view.findViewById(R.id.linkedStoreCard);
        linkedPersonalCardView = (CardView) view.findViewById(R.id.linkedPersonalLoan);
        linkedAccountsLayout = (LinearLayout) view.findViewById(R.id.linkedLayout);
        applyNowAccountsLayout = (LinearLayout) view.findViewById(R.id.applyNowLayout);
        loggedOutHeaderLayout = (LinearLayout) view.findViewById(R.id.loggedOutHeaderLayout);
        loggedInHeaderLayout = (LinearLayout) view.findViewById(R.id.loggedInHeaderLayout);
        linkAccountsBtn=(WButton) view.findViewById(R.id.linkAccountsBtn);
        openMessageActivity.setOnClickListener(this);
        applyNowStoreCard.setOnClickListener(this);
        contactUs.setOnClickListener(this);
        setUpView();
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.openMessageActivity:
                startActivity(new Intent(getActivity(), MessagesActivity.class).putExtra("fromNotification", false));
                getActivity().overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
                break;
            case R.id.applyNowStoreCardTxt:
                startActivity(new Intent(getActivity(), MyAccountCardsActivity.class));
                getActivity().overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
                break;
            case R.id.contactUs:
                startActivity(new Intent(getActivity(), WContactUsActivity.class));
                getActivity().overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
                break;


        }
    }

    public void setUpView() {
        if (isLoggedIn) {
            loggedInHeaderLayout.setVisibility(View.VISIBLE);
            loggedOutHeaderLayout.setVisibility(View.GONE);


            if (isCreditCard == false && isStoreCard == false && isPersonalCard == false) {
               linkedAccountsLayout.setVisibility(View.GONE);
            } else if (isCreditCard == true && isStoreCard == true && isPersonalCard == true) {
                applyNowAccountsLayout.setVisibility(View.GONE);
            }

            if(isCreditCard)
                linkedCreditCardView.setVisibility(View.VISIBLE);
            else
                applyCreditCardView.setVisibility(View.VISIBLE);

            if(isStoreCard)
                linkedStoreCardView.setVisibility(View.VISIBLE);
            else
                applyStoreCardView.setVisibility(View.VISIBLE);

            if(isPersonalCard)
                linkedPersonalCardView.setVisibility(View.VISIBLE);
            else
                applyPersonalCardView.setVisibility(View.VISIBLE);

        }
        else {
            loggedInHeaderLayout.setVisibility(View.GONE);
            linkedAccountsLayout.setVisibility(View.GONE);
            applyCreditCardView.setVisibility(View.VISIBLE);
            applyStoreCardView.setVisibility(View.VISIBLE);
            applyPersonalCardView.setVisibility(View.VISIBLE);

        }
    }
}
