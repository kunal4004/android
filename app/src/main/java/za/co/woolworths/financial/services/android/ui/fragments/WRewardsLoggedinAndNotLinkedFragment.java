package za.co.woolworths.financial.services.android.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.awfs.coordination.R;

/**
 * Created by W7099877 on 05/01/2017.
 */

public class WRewardsLoggedinAndNotLinkedFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.wrewards_loggedout_loggedin_notlinked, container, false);
        view.findViewById(R.id.layoutLoginLogout).setVisibility(View.GONE);

        return view;
    }
}
