package za.co.woolworths.financial.services.android.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.util.ScreenManager;

/**
 * Created by W7099877 on 05/01/2017.
 */

public class WRewardsLoggedOutFragment extends Fragment implements View.OnClickListener {
    public WButton login;
    public WButton register;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.wrewards_loggedout_loggedin_notlinked, container, false);
        view.findViewById(R.id.wRewars_linkaccounts).setVisibility(View.GONE);
        login=(WButton)view.findViewById(R.id.wRewars_login);
        register=(WButton)view.findViewById(R.id.wRewars_register);
        login.setOnClickListener(this);
        register.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.wRewars_login:
                ScreenManager.presentSSOSignin(getActivity());
                break;
            case R.id.wRewars_register:
                ScreenManager.presentSSORegister(getActivity());
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
