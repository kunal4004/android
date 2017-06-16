package za.co.woolworths.financial.services.android.util;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.text.SpannableString;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.models.dto.Response;
import za.co.woolworths.financial.services.android.models.dto.WGlobalState;
import za.co.woolworths.financial.services.android.ui.activities.SSOActivity;
import za.co.woolworths.financial.services.android.ui.activities.WOneAppBaseActivity;

public class WErrorDialog {

	private Activity mActivity;
	private WGlobalState mWGlobalState;
	private Context mContext;
	private AlertDialogInterface mAction;
	private String mSTSParams;

	public WErrorDialog(Context context, WoolworthsApplication mOneApp, AlertDialogInterface
			alertDialogInterface) {
		this.mContext = context;
		this.mAction = alertDialogInterface;
		this.mActivity = ((Activity) mContext);
		this.mWGlobalState = mOneApp.getWGlobalState();
	}

	public void showExpiredTokenDialog(String stsParams) {
		this.mSTSParams = stsParams;
		mWGlobalState.setAccountSignInState(false);
		try {
			final Resources res = mContext.getResources();
			AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT).setMessage(dialogFont(res.getString(R.string.token_timeout_message), 1));

			dialogBuilder.setNegativeButton(dialogFont(res.getString(R.string.cancel_button), 1), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					mAction.onExpiredTokenCancel();
				}
			});

			dialogBuilder.setPositiveButton(dialogFont(res.getString(R.string.token_timeout_authenticate), 1), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					mAction.onExpiredTokenAuthentication();
				}
			});
			AlertDialog alertDialog = dialogBuilder.create();
			alertDialog.setCancelable(false);
			alertDialog.setCanceledOnTouchOutside(false);
			alertDialog.show();
		} catch (Exception ignored) {
		}
	}

	private SpannableString dialogFont(String description, int fontType) {
		return FontHyperTextParser.getSpannable(description, fontType, mContext);
	}

	public boolean getAccountSignInState() {
		return mWGlobalState.getAccountSignInState();
	}

	public boolean getOnBackPressState() {
		return mWGlobalState.getOnBackPressed();
	}

	public void onCancel() {
		mWGlobalState.setOnBackPressed(false);
		Intent i = new Intent(mActivity, WOneAppBaseActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		mActivity.startActivity(i);
		mActivity.overridePendingTransition(0, 0);
		mActivity.finish();
	}

	public void reAuthenticate() {
		ScreenManager.presentExpiredTokenSSOSignIn(mActivity, mSTSParams);
	}

	public void onCancelResult() {
		mWGlobalState.setOnBackPressed(false);
		Intent i = new Intent(mActivity, WOneAppBaseActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		mActivity.startActivityForResult(i, SSOActivity.SSOActivityResult.EXPIRED.rawValue());
		mActivity.overridePendingTransition(0, 0);
		mActivity.finish();
	}
}
