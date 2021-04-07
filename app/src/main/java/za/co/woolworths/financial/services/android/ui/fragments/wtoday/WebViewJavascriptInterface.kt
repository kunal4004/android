package za.co.woolworths.financial.services.android.ui.fragments.wtoday

import android.webkit.JavascriptInterface
import za.co.woolworths.financial.services.android.contracts.IWTodayInterface

class WebViewJavascriptInterface(private val jsListener: IWTodayInterface) {

    @JavascriptInterface
    fun showProducts(categoryId: String, categoryName: String) {
        jsListener.onShowProductListing(categoryId, categoryName)
    }

    @JavascriptInterface
    fun addToShoppingList(ingredients: String) {
        jsListener.onAddIngredientsToShoppingList(ingredients)
    }

    @JavascriptInterface
    fun showProduct(productId: String, skuId: String) {
        jsListener.onShowProductDetail(productId, skuId)
    }

    @JavascriptInterface
    fun sendEventToFirebase(eventName: String, parameter: String) {
        jsListener.sendEventToFirebase(eventName, parameter)
    }
}