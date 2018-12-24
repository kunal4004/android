package za.co.woolworths.financial.services.android.ui.views.actionsheet;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WTextView;

public class SingleButtonDialogFragment extends ActionSheetDialogFragment implements View.OnClickListener {

    public interface DialogListener {
        void onDismissListener();
    }

    private DialogListener dialogListener;

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
        if (activity instanceof DialogListener) {
            dialogListener = (DialogListener) activity;
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
        Activity activity = getActivity();
        if (activity == null) return;
        switch (view.getId()) {
            case R.id.rootActionSheetConstraint:
            case R.id.btnCancel:
                onDialogBackPressed(false);

                if (dialogListener != null)
                    dialogListener.onDismissListener();
                break;
            default:
                break;
        }
    }
}
