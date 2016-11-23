package za.co.woolworths.financial.services.android.ui.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.activities.MessagesActivity;


/**
 * A simple {@link Fragment} subclass.
 */
public class MyAccountsFragment extends Fragment implements View.OnClickListener {

    ImageView openMessageActivity;


    public MyAccountsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.my_accounts_fragment, container, false);
        openMessageActivity=(ImageView)view.findViewById(R.id.openMessageActivity);
        openMessageActivity.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.openMessageActivity:
                startActivity(new Intent(getActivity(), MessagesActivity.class));
                getActivity().overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
                break;

        }
    }
}
