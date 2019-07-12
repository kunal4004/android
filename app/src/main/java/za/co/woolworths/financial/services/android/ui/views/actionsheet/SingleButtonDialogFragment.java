package za.co.woolworths.financial.services.android.ui.views.actionsheet;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.contracts.IDialogListener;
import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WTextView;

public class SingleButtonDialogFragment extends ActionSheetDialogFragment implements View.OnClickListener {

    private IDialogListener mDialogListener;

    public static SingleButtonDialogFragment newInstance(String responseDesc) {
        SingleButtonDialogFragment singleButtonDialogFragment = new SingleButtonDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString("responseDesc", responseDesc);
        singleButtonDialogFragment.setArguments(bundle);
        return singleButtonDialogFragment;
    }

    public static SingleButtonDialogFragment newInstance(String responseDesc, String buttonText) {
        SingleButtonDialogFragment singleButtonDialogFragment = new SingleButtonDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString("responseDesc", responseDesc);
        bundle.putString("okButtonText", buttonText);
        singleButtonDialogFragment.setArguments(bundle);
        return singleButtonDialogFragment;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        addContentView(R.layout.single_button_dialog_fragment);

        Activity activity = getActivity();
        if (activity instanceof IDialogListener) {
            mDialogListener = (IDialogListener) activity;
        }

        Bundle bundleArguments = getArguments();
        String mResponseDesc = bundleArguments.getString("responseDesc");

        WTextView tvResponseDesc = view.findViewById(R.id.tvResponseDesc);
        if (!TextUtils.isEmpty(mResponseDesc))
            tvResponseDesc.setText(mResponseDesc);

        WButton btnCancel = view.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(this);

        // Update ok button text if okButton text is not empty
        if (!TextUtils.isEmpty(bundleArguments.getString("okButtonText"))) {
            btnCancel.setText(bundleArguments.getString("okButtonText"));
        }

        mRootActionSheetConstraint.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rootActionSheetConstraint:
            case R.id.btnCancel:
                navigateBackToActivity();
                break;
            default:
                break;
        }
    }

    private void navigateBackToActivity() {
        shouldAnimateViewOnCancel(true);
        if (mDialogListener != null)
            mDialogListener.onDialogDismissed();
    }


    //Override hardware back button from popup Dialog
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        return new Dialog(getActivity(), getTheme()) {
            @Override
            public void onBackPressed() {
                navigateBackToActivity();
            }
        };
    }
}
