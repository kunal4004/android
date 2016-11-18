package za.co.woolworths.financial.services.android.ui.fragments;

import android.support.v4.app.Fragment;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;

public abstract class BaseAccountFragment extends Fragment {

    public abstract int getTitle();
    public abstract int getTabColor();

    WoolworthsApplication getApplication() {
        return ((WoolworthsApplication)getActivity().getApplication());
    }
}
