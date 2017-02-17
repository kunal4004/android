package za.co.woolworths.financial.services.android.util;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.JavascriptInterface;

import org.w3c.dom.Text;

import za.co.woolworths.financial.services.android.ui.activities.ProductViewActivity;

public class WebAppInterface {
    Context mContext;

    /**
     * Instantiate the interface and set the context
     */
    public WebAppInterface(Context c) {
        mContext = c;
    }

    @JavascriptInterface   // must be added for API 17 or higher
    public void showProducts(String id, String productName) {

        Intent openProductName = new Intent(mContext, ProductViewActivity.class);
        openProductName.putExtra("searchProduct", "");
        openProductName.putExtra("title", id);
        openProductName.putExtra("titleNav", productName);
        mContext.startActivity(openProductName);
        ((AppCompatActivity) mContext).overridePendingTransition(0, 0);
    }
}