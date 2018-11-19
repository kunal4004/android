package za.co.woolworths.financial.services.android.ui.views.actionsheet;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WTextView;

public class SingleButtonDialogFragment extends ActionSheetDialogFragment implements View.OnClickListener {

	public static SingleButtonDialogFragment newInstance(String responseDesc) {
		SingleButtonDialogFragment singleButtonDialogFragment = new SingleButtonDialogFragment();
		Bundle bundle = new Bundle();
		bundle.putString("responseDesc", responseDesc);
		singleButtonDialogFragment.setArguments(bundle);
		return singleButtonDialogFragment;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		String mResponseDesc = getArguments().getString("responseDesc");
		addContentView(R.layout.single_button_dialog_fragment);

		WTextView tvResponseDesc = view.findViewById(R.id.tvResponseDesc);
		if (!TextUtils.isEmpty(mResponseDesc))
			tvResponseDesc.setText(mResponseDesc);

		WButton btnCancel = view.findViewById(R.id.btnCancel);
		btnCancel.setOnClickListener(this);

		mRootActionSheetConstraint.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.rootActionSheetConstraint:
			case R.id.btnCancel:
				onDialogBackPressed(false);
				break;
			default:
				break;
		}

	}
}
