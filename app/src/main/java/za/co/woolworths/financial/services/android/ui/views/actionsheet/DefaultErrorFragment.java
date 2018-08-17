package za.co.woolworths.financial.services.android.ui.views.actionsheet;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WTextView;

public class DefaultErrorFragment extends ActionSheetDialogFragment implements View.OnClickListener {

	private WButton btnCancel;
	private String mResponseDesc;
	private WTextView tvResponseDesc;

	public static DefaultErrorFragment newInstance(String responseDesc) {
		DefaultErrorFragment defaultErrorFragment = new DefaultErrorFragment();
		Bundle bundle = new Bundle();
		bundle.putString("responseDesc", responseDesc);
		defaultErrorFragment.setArguments(bundle);
		return defaultErrorFragment;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mResponseDesc = getArguments().getString("responseDesc");
		addContentView(R.layout.default_error_fragment);

		tvResponseDesc = view.findViewById(R.id.tvResponseDesc);
		if (!TextUtils.isEmpty(mResponseDesc))
			tvResponseDesc.setText(mResponseDesc);

		btnCancel = view.findViewById(R.id.btnCancel);
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
