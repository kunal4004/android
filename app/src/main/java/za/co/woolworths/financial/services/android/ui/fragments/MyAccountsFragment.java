package za.co.woolworths.financial.services.android.ui.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class MyAccountsFragment extends Fragment {


    public MyAccountsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.my_accounts_fragment, container, false);

        return view;
    }

}
