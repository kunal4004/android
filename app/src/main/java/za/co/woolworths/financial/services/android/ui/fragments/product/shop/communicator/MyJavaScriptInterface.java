package za.co.woolworths.financial.services.android.ui.fragments.product.shop.communicator;

import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;

public class MyJavaScriptInterface {

	private Context ctx;

	public MyJavaScriptInterface(Context ctx) {
		this.ctx = ctx;
	}

	public void showHTML(String html) {
		Log.e("showHTML", html);
	}

	@JavascriptInterface
	public void onError(String error) {
		throw new Error(error);
	}
}