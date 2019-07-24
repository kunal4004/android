package za.co.woolworths.financial.services.android.ui.views.actionsheet;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WTextView;

public class OkButtonErrorMessageFragment extends ActionSheetDialogFragment implements View.OnClickListener {

    public static OkButtonErrorMessageFragment newInstance(String responseDesc) {
        OkButtonErrorMessageFragment singleButtonDialogFragment = new OkButtonErrorMessageFragment();
        Bundle bundle = new Bundle();
        bundle.putString("responseDesc", responseDesc);
        singleButtonDialogFragment.setArguments(bundle);
        return singleButtonDialogFragment;
    }

    public static OkButtonErrorMessageFragment newInstance(String responseDesc, String buttonText) {
        OkButtonErrorMessageFragment singleButtonDialogFragment = new OkButtonErrorMessageFragment();
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
