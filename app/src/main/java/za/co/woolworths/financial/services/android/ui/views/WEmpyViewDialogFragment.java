package za.co.woolworths.financial.services.android.ui.views;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;

import com.awfs.coordination.R;


public class WEmpyViewDialogFragment extends DialogFragment {


    public WEmpyViewDialogFragment() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static WEmpyViewDialogFragment newInstance(String title) {
        WEmpyViewDialogFragment frag = new WEmpyViewDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Get field from view
        getDialog().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        getDialog().getWindow().setBackgroundDrawableResource(
                android.R.color.transparent);
        ProgressBar mProgressBar = (ProgressBar) view.findViewById(R.id.mWoolworthsProgressBar);
        mProgressBar.getIndeterminateDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
        mProgressBar.setVisibility(View.GONE);
    }

}