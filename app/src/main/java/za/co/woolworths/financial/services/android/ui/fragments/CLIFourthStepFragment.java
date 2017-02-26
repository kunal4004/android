package za.co.woolworths.financial.services.android.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.awfs.coordination.R;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.util.SharePreferenceHelper;

public class CLIFourthStepFragment extends Fragment implements View.OnClickListener {

    private View view;
    private WButton mBtnBackToAccounts;

    public CLIFourthStepFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.cli_fragment_step_four, container, false);
        setHasOptionsMenu(true);
        SharePreferenceHelper mSharePreference = SharePreferenceHelper.getInstance(getContext());
        Intent i = new Intent(mSharePreference.getValue(mSharePreference.getValue("acc_card_activity")));
        getActivity().sendBroadcast(i);
        initUI();
        setListener();
        setContent();

        return view;
    }

    private void initUI() {
        mBtnBackToAccounts = (WButton) view.findViewById(R.id.btnContinue);
    }

    private void setListener() {
        mBtnBackToAccounts.setOnClickListener(this);
    }

    private void setContent() {
        mBtnBackToAccounts.setText(getActivity().getResources().getString(R.string.cli_back_to_acc));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnContinue:
                closeActivity();
                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Do something that differs the Activity's menu here
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        closeActivity();
        return false;
    }

    public void closeActivity() {
        getActivity().finish();
        getActivity().overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }
}
