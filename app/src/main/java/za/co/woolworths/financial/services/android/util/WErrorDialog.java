package za.co.woolworths.financial.services.android.util;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.ui.activities.LoginActivity;

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

    public static AlertDialog getSingleActionActivityErrorDialog(Context c, DialogInterface.OnClickListener onClickListener) {
        return new AlertDialog.Builder(c, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                .setTitle(FontHyperTextParser.getSpannable(c.getString(R.string.error), 2, c))
                .setPositiveButton(FontHyperTextParser.getSpannable(c.getString(R.string.ok), 1, c), onClickListener).create();
    }

    public static AlertDialog getLoginErrorDialog(final Activity c) {
        return new AlertDialog.Builder(c, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                .setTitle(FontHyperTextParser.getSpannable(c.getString(R.string.error), 2, c))
                .setMessage(FontHyperTextParser.getSpannable(c.getString(R.string.session_expired), 0, c))
                .setPositiveButton(FontHyperTextParser.getSpannable(c.getString(R.string.ok), 1, c), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((WoolworthsApplication) c.getApplication()).getUserManager().setSession("");
                        Intent intent = new Intent(c, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        c.startActivity(intent);
                        c.finish();
                    }
                }).create();
    }
}
