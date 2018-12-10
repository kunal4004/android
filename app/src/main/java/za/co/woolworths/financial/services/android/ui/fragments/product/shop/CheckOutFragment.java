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

import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.NetworkManager;
import za.co.woolworths.financial.services.android.util.SessionUtilities;
import za.co.woolworths.financial.services.android.util.Utils;

public class CheckOutFragment extends Fragment {

	public static int REQUEST_CART_REFRESH_ON_DESTROY = 9;

	private enum QueryString {
		COMPLETE("goto=complete"),
		ABANDON("goto=abandon");

		private String value;

		QueryString(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}

	private WebView mWebCheckOut;
	private String TAG = this.getClass().getSimpleName();
	private ProgressBar mProgressLayout;
	private ErrorHandlerView mErrorHandlerView;
	private QueryString closeOnNextPage;

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
			mWebCheckOut.loadUrl(getUrl(), getExtraHeader());
			retryConnect(view);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		Utils.setScreenName(getActivity(), FirebaseManagerAnalyticsProperties.ScreenNames.CART_CHECKOUT);
	}

	@NonNull
	private String getUrl() {
		return WoolworthsApplication.getCartCheckoutLink();
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

				if (url.contains(QueryString.COMPLETE.getValue())) {
					closeOnNextPage = QueryString.COMPLETE;
				} else if (url.contains(QueryString.ABANDON.getValue())) {
					closeOnNextPage = QueryString.ABANDON;
				}

				// close cart activity if current url equals next url
				if (closeOnNextPage != null && !url.contains(closeOnNextPage.getValue())) {
					mWebCheckOut.stopLoading();
					finishCartActivity();
				}
			}

			public void onPageFinished(WebView view, String url) {
				mProgressLayout.setVisibility(View.GONE);
				if (closeOnNextPage != null && !url.contains(closeOnNextPage.getValue())) {
					finishCartActivity();
				}
			}
		});
	}

	private void finishCartActivity() {
		Activity activity = getActivity();
		if (activity != null) {
			Intent returnIntent = new Intent();
			if (closeOnNextPage == QueryString.COMPLETE) {
				activity.setResult(Activity.RESULT_OK, returnIntent);
			} else if (closeOnNextPage == QueryString.ABANDON) {
				activity.setResult(Activity.RESULT_CANCELED, returnIntent);
			}
			activity.finish();
			activity.overridePendingTransition(R.anim.slide_down_anim, R.anim.stay);
		}
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


	public static void clearWebViewCachesCustom(Context context) {
		try {
			String dataDir = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).applicationInfo.dataDir;
			new File(dataDir + "/app_webview/").delete();
		} catch (Exception e) {
			Log.e("clearWebViewCaches", e.getMessage());
		}
	}

	private void retryConnect(View view) {
		view.findViewById(R.id.btnRetry).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (NetworkManager.getInstance().isConnectedToNetwork(getActivity())) {
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
			extraHeaders.put("token", SessionUtilities.getInstance().getSessionToken());
		}
		return extraHeaders;
	}
}
