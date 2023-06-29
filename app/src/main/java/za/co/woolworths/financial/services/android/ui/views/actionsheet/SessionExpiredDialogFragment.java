package za.co.woolworths.financial.services.android.ui.views.actionsheet;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.views.WButton;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.ScreenManager;
import za.co.woolworths.financial.services.android.util.Utils;

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
	private WTextView tvSessionExpiredTitle;
	private TextView tvSessionExpiredDesc;

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		addContentView(R.layout.session_expired_fragment);

		mStsParams = getArguments().getString("stsParams");

		btnSECancel = view.findViewById(R.id.btnSECancel);
		btnSESignIn = view.findViewById(R.id.btnSESignIn);
		tvSessionExpiredTitle = view.findViewById(R.id.tvSessionExpiredTitle);
		tvSessionExpiredDesc = view.findViewById(R.id.tvSessionExpiredDesc);

		tvSessionExpiredTitle.setText(Utils.getUserKMSIState() ? getString(R.string.kmsi_session_expired_title) : getString(R.string.session_expired_title));
		tvSessionExpiredDesc.setText(Utils.getUserKMSIState() ? getString(R.string.kmsi_session_expired_desc) : getString(R.string.session_expired_desc));
		btnSECancel.setText(Utils.getUserKMSIState() ? getString(R.string.cancel_no_thanks) : getString(R.string.cancel));

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
		if (!(activity instanceof AppCompatActivity)) {
			dismissDialog();
			return;
		}
		FragmentManager fm = ((AppCompatActivity) activity).getSupportFragmentManager();
		fm.setFragmentResult("" + DIALOG_REQUEST_CODE, null);
		dismissDialog();
	}
}
