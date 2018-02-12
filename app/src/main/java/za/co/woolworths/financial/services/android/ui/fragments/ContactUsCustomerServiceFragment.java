package za.co.woolworths.financial.services.android.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.util.ContactUsFragmentChange;

/**
 * Created by W7099877 on 01/12/2016.
 */

public class ContactUsCustomerServiceFragment extends Fragment implements View.OnClickListener {

    public RelativeLayout generalEng;
    public RelativeLayout woolworthsOnline;
    public RelativeLayout wRewards;
    public RelativeLayout mySchoolEnq;
    public ContactUsFragmentChange contactUsFragmentChange;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.contact_us_customer_service, container, false);
        generalEng=(RelativeLayout)view.findViewById(R.id.generalEnq);
        woolworthsOnline=(RelativeLayout)view.findViewById(R.id.woolworthsOnline);
        wRewards=(RelativeLayout)view.findViewById(R.id.wRewards);
        mySchoolEnq=(RelativeLayout)view.findViewById(R.id.mySchoolEnq);
        generalEng.setOnClickListener(this);
        woolworthsOnline.setOnClickListener(this);
        wRewards.setOnClickListener(this);
        mySchoolEnq.setOnClickListener(this);

        return view;    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.generalEnq:
                openFragment(new ContactUsGeneralEnquiriesFragment());
                break;
            case R.id.woolworthsOnline:
                openFragment(new ContactUsOnlineFragment());
                break;
            case R.id.wRewards:
                openFragment(new ContactUsWRewardsFragment());
                break;
            case R.id.mySchoolEnq:
                openFragment(new ContactUsMySchoolFragment());
                break;
        }
    }

    public void openFragment(Fragment fragment)
    {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

        fragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.slide_from_right, R.anim.slide_to_left,R.anim.slide_from_left,R.anim.slide_to_right)
                .replace(R.id.content_frame, fragment).addToBackStack(null).commit();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            contactUsFragmentChange = (ContactUsFragmentChange) getActivity();
        } catch (ClassCastException ex) {
            Log.e("Interface", ex.toString());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        contactUsFragmentChange.onFragmentChanged(getActivity().getResources().getString(R.string.contact_us_customer_service));
    }
}
