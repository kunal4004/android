package za.co.woolworths.financial.services.android.ui.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;

import java.lang.reflect.Method;

import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.SessionUtilities;

public class WebViewActivity extends AppCompatActivity {

	WebView webView;
	public Toolbar toolbar;
	public WTextView toolbarTextView;
	private ProgressBar loadingProgressBar;
	private ErrorHandlerView mErrorView;
	public ProgressBar loadingProgressBarKMSI;
	public LinearLayout ssoLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_web_view);
		webView = (WebView) findViewById(R.id.webview);
		Bundle b = new Bundle();
		b = getIntent().getBundleExtra("Bundle");
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		toolbarTextView = (WTextView) findViewById(R.id.toolbar_title);
		loadingProgressBar = findViewById(R.id.loadingProgressBar);
		mErrorView = new ErrorHandlerView(WebViewActivity.this, (RelativeLayout) findViewById
				(R.id.no_connection_layout));
		loadingProgressBarKMSI = findViewById(R.id.kmsiProgressBar);
		ssoLayout = findViewById(R.id.ssoLayout);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(null);

		String url = b.getString("link");
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setUserAgentString("iphone");
		webView.getSettings().setDomStorageEnabled(true);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
		webView.setWebViewClient(new WebViewController());
		try {
			Method m = WebSettings.class.getMethod("setMixedContentMode", int.class);
			if (m == null) {
				Log.d("WebSettings", "Error getting setMixedContentMode method");
			} else {
				m.invoke(webView.getSettings(), 2); // 2 = MIXED_CONTENT_COMPATIBILITY_MODE
				Log.d("WebSettings", "Successfully set MIXED_CONTENT_COMPATIBILITY_MODE");
			}
		} catch (Exception ex) {
			Log.e("WebSettings", "Error calling setMixedContentMode: " + ex.getMessage(), ex);
		}
		//webView.clearCache(true);
		//webView.clearHistory();
		if (Build.VERSION.SDK_INT >= 21) {
			webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
		}
		//clearCookies(this);
		if (TextUtils.isEmpty(url)) mErrorView.networkFailureHandler("");
		webView.loadUrl(url);
	}

	public void toggleLoading(boolean show) {
		if (show) {
			// show progress
			loadingProgressBar.getIndeterminateDrawable().setColorFilter(null);
			loadingProgressBar.getIndeterminateDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
			loadingProgressBar.setVisibility(View.VISIBLE);
		} else {
			// hide progress
			loadingProgressBar.setVisibility(View.GONE);
			loadingProgressBar.getIndeterminateDrawable().setColorFilter(null);
		}
	}

	protected class WebViewController extends WebViewClient {

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
		}

		@Override
		public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
			//super.onReceivedSslError(view, handler, error);
			final AlertDialog.Builder builder = new AlertDialog.Builder(WebViewActivity.this);
			builder.setMessage(R.string.ssl_error);
			builder.setPositiveButton("continue", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					handler.proceed();
				}
			});
			builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					handler.cancel();
				}
			});
			final AlertDialog dialog = builder.create();
			dialog.show();
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			// do your stuff here
			if (url.contains("Login")) {
				finish();
			}
		}

	}

	@SuppressWarnings("deprecation")
	public static void clearCookies(Context context) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
			// Log.d(TAG, "Using clearCookies code for API >=" + String.valueOf(Build.VERSION_CODES.LOLLIPOP_MR1));
			CookieManager.getInstance().removeAllCookies(null);
			CookieManager.getInstance().flush();
		} else {
			//  Log.d(C.TAG, "Using clearCookies code for API <" + String.valueOf(Build.VERSION_CODES.LOLLIPOP_MR1));
			CookieSyncManager cookieSyncMngr = CookieSyncManager.createInstance(context);
			cookieSyncMngr.startSync();
			CookieManager cookieManager = CookieManager.getInstance();
			cookieManager.removeAllCookie();
			cookieManager.removeSessionCookie();
			cookieSyncMngr.stopSync();
			cookieSyncMngr.sync();
		}
	}



}