package za.co.woolworths.financial.services.android.ui.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.activities.ContactUsActivity;
import za.co.woolworths.financial.services.android.ui.activities.MessagesActivity;
import za.co.woolworths.financial.services.android.ui.activities.MyAccountCardsActivity;
import za.co.woolworths.financial.services.android.ui.activities.WContactUsActivity;
import za.co.woolworths.financial.services.android.ui.views.WTextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class MyAccountsFragment extends Fragment implements View.OnClickListener {

    ImageView openMessageActivity;
    WTextView applyNowStoreCard;
    RelativeLayout contactUs;

    public MyAccountsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.my_accounts_fragment, container, false);
        openMessageActivity=(ImageView)view.findViewById(R.id.openMessageActivity);
        applyNowStoreCard=(WTextView) view.findViewById(R.id.applyNowStoreCard);
        contactUs=(RelativeLayout)view.findViewById(R.id.contactUs);
        openMessageActivity.setOnClickListener(this);
        applyNowStoreCard.setOnClickListener(this);
        contactUs.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.openMessageActivity:
                startActivity(new Intent(getActivity(), MessagesActivity.class).putExtra("fromNotification", false));
                getActivity().overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
                break;
            case R.id.applyNowStoreCard:
                startActivity(new Intent(getActivity(), MyAccountCardsActivity.class));
                getActivity().overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
                break;
            case R.id.contactUs:
                startActivity(new Intent(getActivity(), WContactUsActivity.class));
                getActivity().overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
                break;


        }
    }
}
