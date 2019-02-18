package za.co.woolworths.financial.services.android.ui.views.actionsheet;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.util.ScreenManager;

public class SessionExpiredDialogFragment extends ActionSheetDialogFragment implements View.OnClickListener {

	public static SessionExpiredDialogFragment newInstance(String stsParams) {
		SessionExpiredDialogFragment sessionExpiredDialogFragment = new SessionExpiredDialogFragment();
		Bundle bundle = new Bundle();
		bundle.putString("stsParams", stsParams);
		sessionExpiredDialogFragment.setArguments(bundle);
		return sessionExpiredDialogFragment;
	}

	private String mStsParams;
	private WButton btnSECancel;
	private WButton btnSESignIn;

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		addContentView(R.layout.session_expired_fragment);

		mStsParams = getArguments().getString("stsParams");

		btnSECancel = view.findViewById(R.id.btnSECancel);
		btnSESignIn = view.findViewById(R.id.btnSESignIn);

		btnSECancel.setOnClickListener(this);
		btnSESignIn.setOnClickListener(this);

	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.btnSECancel:
				changeTappedButtonColor(btnSECancel);
				shouldAnimateViewOnCancel(false);
				break;
			case R.id.btnSESignIn:
				changeTappedButtonColor(btnSESignIn);
				shouldAnimateViewOnCancel(true);
				break;
			default:
				break;
		}
	}

	/***
	 * Override onAnimationCompleted to enable dismiss animation first
	 */

	@Override
	public void onAnimationCompleted(boolean positiveResultSelected) {
		if (positiveResultSelected) {
			ScreenManager.presentExpiredTokenSSOSignIn(getActivity(), mStsParams);
			dismissDialog();
			return;
		}

		Activity activity = getActivity();
		if (activity == null) return;
		Fragment targetFragment = getTargetFragment();
		if (targetFragment != null)
			targetFragment.onActivityResult(getTargetRequestCode(), getTargetRequestCode(), null);
		dismissDialog();
	}
}
