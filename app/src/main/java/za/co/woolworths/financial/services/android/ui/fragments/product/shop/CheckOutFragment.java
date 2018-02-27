package za.co.woolworths.financial.services.android.ui.fragments.product.shop;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.awfs.coordination.R;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import za.co.woolworths.financial.services.android.ui.fragments.product.shop.communicator.MyJavaScriptInterface;
import za.co.woolworths.financial.services.android.util.Utils;

public class CheckOutFragment extends Fragment {

	private WebView mWebCheckOut;
	private String TAG = this.getClass().getSimpleName();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.checkout_fragment, container, false);
	}

	@SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mWebCheckOut = view.findViewById(R.id.webCheckout);
		setWebSetting(mWebCheckOut);
		Map<String, String> extraHeaders = new HashMap<>();
		Activity activity = getActivity();
		if (activity != null) {
			extraHeaders.put("token", Utils.getSessionToken(activity));

			setWebViewClient();
			setWebChromeClient();
			mWebCheckOut.addJavascriptInterface(new MyJavaScriptInterface(activity), "javascript");
			mWebCheckOut.loadUrl("http://www-win-qa.woolworths.co.za/mcommerce/jsp/checkout-summary.jsp", extraHeaders);
		}
	}

	@SuppressLint("SetJavaScriptEnabled")
	private void setWebSetting(WebView webCheckout) {
		WebSettings ws = webCheckout.getSettings();
		ws.setJavaScriptEnabled(true);
		ws.setUseWideViewPort(true);
		ws.setLoadWithOverviewMode(true);
		ws.setDomStorageEnabled(true);
		clearWebViewCachesCustom(getActivity());
		clearCookie();
		if (Build.VERSION.SDK_INT >= 21) {
			ws.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
		}
	}

	private void clearCookie() {
		CookieManager cookieManager = CookieManager.getInstance();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			cookieManager.removeAllCookies(new ValueCallback<Boolean>() {
				// a callback which is executed when the cookies have been removed
				@Override
				public void onReceiveValue(Boolean aBoolean) {
					Log.d(TAG, "Cookie removed: " + aBoolean);
				}
			});
		} else cookieManager.removeAllCookie();
	}

	private void setWebViewClient() {
		mWebCheckOut.setWebViewClient(new WebViewClient() {

			@Override
			public void onLoadResource(WebView view, String url) {
				super.onLoadResource(view, url);
				try {
//					view.loadUrl("javascript:(function() { " +
//							"var head = document.getElementsByTagName('header')[0];"
//							+ "head.parentNode.removeChild(head);" +
//							"})()");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				return true;
			}

			@Override
			public void onPageStarted(WebView view, String url,
									  Bitmap favicon) {
			}

			public void onPageFinished(WebView view, String url) {
			}
		});
	}

	private void setWebChromeClient() {
		mWebCheckOut.setWebChromeClient(new WebChromeClient() {

			@Override
			public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
				Log.e("onJsPrompt", String.valueOf(result) + " message " + message + " url " + url);
				return super.onJsPrompt(view, url, message, defaultValue, result);
			}

			@Override
			public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
				Log.e("onJsAlert", String.valueOf(result) + " message " + message + " url " + url);
				return super.onJsAlert(view, url, message, result);
			}

			@Override
			public boolean onConsoleMessage(ConsoleMessage cm) {
				Log.e(TAG, String.format("%s @ %d: %s", cm.message(),
						cm.lineNumber(), cm.sourceId()));
				return true;
			}

			@Override
			public void onReceivedTitle(WebView view, String title) {
				super.onReceivedTitle(view, title);
				Log.e("currentTitle", title);
			}
		});
	}

	public WebView getWebCheckOut() {
		return mWebCheckOut;
	}

	public void callJavaScript(WebView view, String methodName, Object... params) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("javascript:try{");
		stringBuilder.append(methodName);
		stringBuilder.append("(");
		String separator = "";
		for (Object param : params) {
			stringBuilder.append(separator);
			separator = ",";
			if (param instanceof String) {
				stringBuilder.append("'");
			}
			stringBuilder.append(param);
			if (param instanceof String) {
				stringBuilder.append("'");
			}

		}
		stringBuilder.append(")}catch(error){console.error(error.message);}");
		final String call = stringBuilder.toString();
		Log.i(TAG, "callJavaScript: call=" + call);


		view.loadUrl(call);
	}

	public static void clearWebViewCachesCustom(Context context) {
		try {
			String dataDir = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).applicationInfo.dataDir;
			new File(dataDir + "/app_webview/").delete();
		} catch (Exception e) {
			Log.e("clearWebViewCaches", e.getMessage());
			e.printStackTrace();
			e.getSuppressed();
		}
	}

}
