package za.co.woolworths.financial.services.android.util;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.webkit.JavascriptInterface;


import com.google.gson.Gson;


import za.co.woolworths.financial.services.android.models.dto.Ingredient;
import za.co.woolworths.financial.services.android.models.dto.ShoppingList;
import za.co.woolworths.financial.services.android.ui.activities.ProductViewActivity;
import za.co.woolworths.financial.services.android.ui.activities.TransientActivity;

public class WebAppInterface {
    Context mContext;

    /**
     * Instantiate the interface and set the context
     * must be added for API 17 or higher
     */
    public WebAppInterface(Context c) {
        mContext = c;
    }

    @JavascriptInterface
    public void showProducts(String id, String productName) {

        Intent openProductName = new Intent(mContext, ProductViewActivity.class);
        openProductName.putExtra("searchProduct", "");
        openProductName.putExtra("title", id);
        openProductName.putExtra("titleNav", productName);
        mContext.startActivity(openProductName);
        ((AppCompatActivity) mContext).overridePendingTransition(0, 0);
    }

    @JavascriptInterface
    public void addToShoppingList(String ingredients) {
        if (!TextUtils.isEmpty(ingredients)) {
            Gson gson = new Gson();
            Ingredient ingredient[] = gson.fromJson(ingredients, Ingredient[].class);
            if (ingredient.length > 0) {
                for (Ingredient i : ingredient) {
                    Utils.addToShoppingCart(mContext, new ShoppingList(
                            i.id, i.displayName, false));
                }
            }

            ((AppCompatActivity) mContext).runOnUiThread(new Runnable() {
                public void run() {
                    Utils.displayValidationMessage(mContext,
                            TransientActivity.VALIDATION_MESSAGE_LIST.SHOPPING_LIST_INFO,
                            "viewShoppingList");

                }
            });

        }

    }
}