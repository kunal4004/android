package za.co.woolworths.financial.services.android.ui.views;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;

import com.awfs.coordination.R;


public class WProgressDialogFragment extends DialogFragment {


    public WProgressDialogFragment() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }

    public static WProgressDialogFragment newInstance(String title) {
        WProgressDialogFragment frag = new WProgressDialogFragment();
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

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            // Marshmallow+
        } else {
            //below Marshmallow
            // remove dialog divider
            int divierId = getDialog().getContext().getResources().getIdentifier("android:id/titleDivider", null, null);
            View divider = getDialog().findViewById(divierId);
            if (divider != null) {
                divider.setBackgroundColor(ContextCompat.getColor(getActivity(), android.R.color.transparent));
            }
        }

        ProgressBar mProgressBar = (ProgressBar) view.findViewById(R.id.mWoolworthsProgressBar);
        mProgressBar.getIndeterminateDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
    }
}