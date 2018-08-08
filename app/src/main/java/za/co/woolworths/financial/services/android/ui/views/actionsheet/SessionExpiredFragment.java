package za.co.woolworths.financial.services.android.ui.views.actionsheet;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.util.ScreenManager;

public class SessionExpiredFragment extends ActionSheetDialogFragment implements View.OnClickListener {

	public static SessionExpiredFragment newInstance(String stsParams) {
		SessionExpiredFragment sessionExpiredFragment = new SessionExpiredFragment();
		Bundle bundle = new Bundle();
		bundle.putString("stsParams", stsParams);
		sessionExpiredFragment.setArguments(bundle);
		return sessionExpiredFragment;
	}

	private String mStsParams;
	private WButton btnSECancel;
	private WButton btnSESignIn;

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		View sessionExpiredView = getLayoutInflater().inflate(R.layout.session_expired_fragment, null);
		mFrameAlertContainer.addView(sessionExpiredView);

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
				onDialogBackPressed(false);
				break;
			case R.id.btnSESignIn:
				changeTappedButtonColor(btnSESignIn);
				onDialogBackPressed(true);
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
		activity.setResult(ACTION_SHEET_WAS_DISMISSED_RESULT_CODE);
		dismissDialog();
	}
}
