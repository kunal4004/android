package za.co.woolworths.financial.services.android.ui.views;

import android.app.Activity;
import android.content.Intent;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.ui.activities.WConnectionHandlerActivity;

public class ErrorHandlerView {

    private WoolworthsApplication mWoolworthApp;

    public ErrorHandlerView(WoolworthsApplication woolworthsApplication) {
        this.mWoolworthApp = woolworthsApplication;
    }

    public void hideErrorHandlerLayout() {
        mWoolworthApp.setTriggerErrorHandler(false);
    }

    public void startActivity(Activity currentActivity) {
        Intent currentIntent = new Intent(currentActivity, WConnectionHandlerActivity.class);
        currentActivity.startActivity(currentIntent);
        currentActivity.overridePendingTransition(0, 0);
    }
}
