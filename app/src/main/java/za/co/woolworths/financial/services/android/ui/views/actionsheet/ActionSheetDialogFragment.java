package za.co.woolworths.financial.services.android.ui.views.actionsheet;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.views.WButton;

public class ActionSheetDialogFragment extends DialogFragment implements AnimateLayout.ResultAnimator {

	public LinearLayout mFrameAlertContainer;
	private AnimateLayout mAnimationUtils;

	public static int DIALOG_REQUEST_CODE = 1211;
	public RelativeLayout mRootActionSheetConstraint;

	public ActionSheetDialogFragment() {
	}

	@Override
	public void onStart() {
		super.onStart();
		Dialog dialog = getDialog();
		if (dialog == null) return;
		Window window = dialog.getWindow();
		if (window == null) return;
		window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(DialogFragment.STYLE_NO_TITLE, R.style.AppDialogTheme);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		return inflater.inflate(R.layout.action_sheet_dialog_fragment, container);
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		return new Dialog(getActivity(), getTheme()) {
			@Override
			public void onBackPressed() {
				shouldAnimateViewOnCancel(false);
			}
		};
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mFrameAlertContainer = view.findViewById(R.id.frameAlertContainer);
		mRootActionSheetConstraint = view.findViewById(R.id.rootActionSheetConstraint);
		setAnimation();
	}

	public void addContentView(int layoutResourceId) {
		View sessionExpiredView = getLayoutInflater().inflate(layoutResourceId, null);
		mFrameAlertContainer.addView(sessionExpiredView);
	}

	/****
	 * Slide up and down animation for custom popup container
	 * ACTION_SHEET_WAS_DISMISSED_RESULT_CODE represents the default callback
	 * value of the dismissed dialog
	 */
	public void setAnimation() {
		mAnimationUtils = new AnimateLayout();
		mAnimationUtils.setAnimation(mFrameAlertContainer, this);
	}

	public void shouldAnimateViewOnCancel(boolean animateAndDismiss) {
		mAnimationUtils.animateDismissView(mFrameAlertContainer, animateAndDismiss);
	}

	public void changeTappedButtonColor(WButton button) {
		button.setBackgroundColor(Color.BLACK);
		button.setTextColor(Color.WHITE);
	}

	@Override
	public void onAnimationCompleted(boolean positiveResultSelected) {
		Activity activity = getActivity();
		if (activity == null) return;
		activity.setResult(DIALOG_REQUEST_CODE);
		dismissDialog();
	}

	public void dismissDialog() {
		Dialog dialog = getDialog();
		if (dialog != null)
			dialog.dismiss();
	}
}
