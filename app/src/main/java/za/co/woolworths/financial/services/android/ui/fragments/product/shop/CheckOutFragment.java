package za.co.woolworths.financial.services.android.ui.fragments.product.shop;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow;
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.communicator.MyJavaScriptInterface;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.Utils;

public class CheckOutFragment extends Fragment implements View.OnTouchListener {

	public static int REQUESTCODE_CHECKOUT = 9;

	private WebView mWebCheckOut;
	private String TAG = this.getClass().getSimpleName();
	private ProgressBar mProgressLayout;
	private ErrorHandlerView mErrorHandlerView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.checkout_fragment, container, false);
	}

	@SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mWebCheckOut = view.findViewById(R.id.webCheckout);
		mProgressLayout = view.findViewById(R.id.progressCreditLimit);
		mErrorHandlerView = new ErrorHandlerView(getActivity(), (RelativeLayout) view.findViewById(R.id.no_connection_layout));
		mErrorHandlerView.setMargin(view.findViewById(R.id.no_connection_layout), 0, 0, 0, 0);
		setWebSetting(mWebCheckOut);
		Activity activity = getActivity();
		if (activity != null) {
			setWebViewClient();
			setWebChromeClient();
			mWebCheckOut.setOnTouchListener(this);
			mWebCheckOut.addJavascriptInterface(new MyJavaScriptInterface(activity), "JSInterface");
			mWebCheckOut.loadUrl(getUrl(), getExtraHeader());
			retryConnect(view);
		}
	}

	@NonNull
	private String getUrl() {
		return "http://www-win-qa.woolworths.co.za/mcommerce/jsp/checkout-summary.jsp";
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
		mProgressLayout.setVisibility(View.VISIBLE);
		mWebCheckOut.setWebViewClient(new WebViewClient() {

			@TargetApi(android.os.Build.VERSION_CODES.M)
			@Override
			public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
				super.onReceivedError(view, request, error);
				mErrorHandlerView.webViewBlankPage(view);
				mErrorHandlerView.networkFailureHandler(error.toString());
			}

			@SuppressWarnings("deprecation")
			@Override
			public void onReceivedError(WebView webView, int errorCode, String description, String failingUrl) {
				mErrorHandlerView.webViewBlankPage(webView);
				mErrorHandlerView.networkFailureHandler(description);
			}

			@Override
			public void onLoadResource(WebView view, String url) {
				super.onLoadResource(view, url);
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				return super.shouldOverrideUrlLoading(view, url);
			}

			@Override
			public void onPageStarted(WebView view, String url,
									  Bitmap favicon) {
				if(url.contains("goto=complete")) {
					Intent returnIntent = new Intent();
					getActivity().setResult(Activity.RESULT_OK, returnIntent);
					getActivity().finish();
				} else if(url.contains("goto=abandon")) {
					Intent returnIntent = new Intent();
					getActivity().setResult(Activity.RESULT_CANCELED, returnIntent);
					getActivity().finish();
				}
			}

			public void onPageFinished(WebView view, String url) {
				mProgressLayout.setVisibility(View.GONE);
//				mWebCheckOut.loadUrl("javascript:(function() { " +
//						"var x = document.getElementsByClassName('heading--1').length;" +
//						"var content = document.getElementsByTagName('h1')[0].innerHTML; " +
//						"window.JSInterface.printAddress(content, x);" +
//						"})()");
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
				Log.e("onJsAlert", message);
				result.confirm();
				return true;
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

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (v.getId() == R.id.webCheckout && event.getAction() == MotionEvent.ACTION_DOWN) {
//			mWebCheckOut.loadUrl("javascript:(function() { " +
//					"var title = document.getElementsByTagName('h1')[0];" +
//					"var type = title.getAttribute('heading--1');" +
//					"window.JSInterface.printAddress(title, 1);" +
//					"})()");
		}
		return false;
	}

	private void retryConnect(View view) {
		view.findViewById(R.id.btnRetry).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (new ConnectionDetector().isOnline(getActivity())) {
					mErrorHandlerView.hideErrorHandler();
					mProgressLayout.setVisibility(View.VISIBLE);
					mWebCheckOut.loadUrl(getUrl(), getExtraHeader());
				}
			}
		});
	}

	private Map<String, String> getExtraHeader() {
		Map<String, String> extraHeaders = new HashMap<>();
		Activity activity = getActivity();
		if (activity != null) {
			extraHeaders.put("token", Utils.getSessionToken(activity));
		}
		return extraHeaders;
	}
}
