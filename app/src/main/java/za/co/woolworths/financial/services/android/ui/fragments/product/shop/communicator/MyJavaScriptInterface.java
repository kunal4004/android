package za.co.woolworths.financial.services.android.ui.fragments.product.shop.communicator;

import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

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

	@JavascriptInterface
	public void printAddress(String address, int total) {
		Toast.makeText(ctx, "Address: " + address + " " + total, Toast.LENGTH_LONG).show();
	}

	@JavascriptInterface
	public void performClick() throws Exception {
		Toast.makeText(ctx, "Login clicked", Toast.LENGTH_LONG).show();
	}
}