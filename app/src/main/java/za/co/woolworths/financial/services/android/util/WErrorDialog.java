package za.co.woolworths.financial.services.android.util;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v4.content.IntentCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.activities.SSOActivity;
import za.co.woolworths.financial.services.android.ui.activities.WOneAppBaseActivity;

public class WErrorDialog {

	public static AlertDialog getSimplyErrorDialog(Context c) {
		return new AlertDialog.Builder(c, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
				.setTitle(FontHyperTextParser.getSpannable(c.getString(R.string.error), 2, c))
				.setPositiveButton(FontHyperTextParser.getSpannable(c.getString(R.string.ok), 1, c), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				}).create();
	}

	public static AlertDialog.Builder expiredTokenDialog(final Context c) {
		Resources res = c.getResources();
		return new AlertDialog.Builder(c, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
				.setMessage(dialogFont(res.getString(R.string.token_timeout_message), 1, c)).setNegativeButton(WErrorDialog.dialogFont(res.getString(R.string.cancel_button), 1, c), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Activity activity = ((AppCompatActivity) c);
						Intent i = new Intent(activity, WOneAppBaseActivity.class);
						i.putExtra("tokenState", "expired");
						i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
						i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//						i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
//								Intent.FLAG_ACTIVITY_SINGLE_TOP |
//								IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
						activity.setResult(SSOActivity.SSOActivityResult.EXPIRED.rawValue(), i);
						activity.overridePendingTransition(0, 0);
						activity.finish();
//
//
//						Intent intent = new Intent(activity, WOneAppBaseActivity.class);
//						ComponentName cn = intent.getComponent();
//						Intent mainIntent = IntentCompat.makeRestartActivityTask(cn);
//						activity.setResult(SSOActivity.SSOActivityResult.EXPIRED.rawValue(), mainIntent);

					}
				});
	}

	public static SpannableString dialogFont(String description, int fontType, Context context) {
		return FontHyperTextParser.getSpannable(description, fontType, context);
	}
}
