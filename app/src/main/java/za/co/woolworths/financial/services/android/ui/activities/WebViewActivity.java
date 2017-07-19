package za.co.woolworths.financial.services.android.ui.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.awfs.coordination.R;

import java.lang.reflect.Method;

import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.SessionExpiredUtilities;

public class WebViewActivity extends AppCompatActivity {

	WebView webView;
	public Toolbar toolbar;
	public WTextView toolbarTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_web_view);
		webView = (WebView) findViewById(R.id.webview);
		Bundle b = new Bundle();
		b = getIntent().getBundleExtra("Bundle");
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		toolbarTextView = (WTextView) findViewById(R.id.toolbar_title);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(null);

		String url = b.getString("link");
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setWebViewClient(new WebViewController());
		try {
			Method m = WebSettings.class.getMethod("setMixedContentMode", int.class);
			if ( m == null ) {
				Log.e("WebSettings", "Error getting setMixedContentMode method");
			}
			else {
				m.invoke(webView.getSettings(), 2); // 2 = MIXED_CONTENT_COMPATIBILITY_MODE
				Log.e("WebSettings", "Successfully set MIXED_CONTENT_COMPATIBILITY_MODE");
			}
		}
		catch (Exception ex) {
			Log.e("WebSettings", "Error calling setMixedContentMode: " + ex.getMessage(), ex);
		}
		webView.clearCache(true);
		webView.clearHistory();
		if (Build.VERSION.SDK_INT >= 21) {
			webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
		}
		clearCookies(this);
		webView.loadUrl(url);
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

	public void finishActivity() {
		SessionExpiredUtilities.INSTANCE
				.getGlobalState(WebViewActivity.this).setNewSTSParams("");
		finish();
		overridePendingTransition(R.anim.slide_down_anim, R.anim.stay);
	}

}