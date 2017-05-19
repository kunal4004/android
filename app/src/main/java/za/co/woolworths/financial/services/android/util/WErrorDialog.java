package za.co.woolworths.financial.services.android.util;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;

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

    public static AlertDialog getErrConnectToServer(final Activity c){
        return  new AlertDialog.Builder(c)
                .setTitle(FontHyperTextParser.getSpannable(c.getString(R.string.error), 2, c))
                .setCancelable(false)
                .setMessage(FontHyperTextParser.getSpannable(c.getString(R.string.connect_to_server), 0, c))
                .setPositiveButton(FontHyperTextParser.getSpannable(c.getString(R.string.ok), 1, c), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

}
