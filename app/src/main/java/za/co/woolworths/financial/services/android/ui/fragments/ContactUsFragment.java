package za.co.woolworths.financial.services.android.ui.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.util.binder.ContactUsFragmentChange;

public class ContactUsFragment extends Fragment implements View.OnClickListener {

    public RelativeLayout fsLayout;
    public RelativeLayout csLayout;
    public ContactUsFragmentChange contactUsFragmentChange;
    public ContactUsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_contact_us, container, false);
        fsLayout=(RelativeLayout)view.findViewById(R.id.financialService);
        csLayout=(RelativeLayout)view.findViewById(R.id.customerService);
        fsLayout.setOnClickListener(this);
        csLayout.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.financialService:
                openFragment(new ContactUsFinancialServiceFragment());
                break;
            case R.id.customerService:
                 openFragment(new ContactUsCustomerServiceFragment());
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
        contactUsFragmentChange.onFragmentChanged(getActivity().getResources().getString(R.string.contact_us));
    }
}
