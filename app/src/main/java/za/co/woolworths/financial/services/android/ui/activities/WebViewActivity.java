package za.co.woolworths.financial.services.android.ui.activities;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.views.WTextView;

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
		webView.clearCache(true);
		webView.clearHistory();
		clearCookies(this);
		webView.loadUrl(url);
		webView.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onReceivedTitle(WebView view, String title) {
				super.onReceivedTitle(view, title);
				if (title.equalsIgnoreCase("about:blank")) {
					toolbarTextView.setText("");
				} else if (title.equalsIgnoreCase(getString(R.string
						.sso_title_text_submit_this_form)))
					toolbarTextView.setText("");
				else
					toolbarTextView.setText(title);
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				if (canGoBack()) {
					enableBackButton();
					this.webView.goBack();
				} else {
					finishActivity();
					disableBackButton();
				}
				break;
		}
		return true;
	}

	protected class WebViewController extends WebViewClient {

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
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
		finish();
		overridePendingTransition(R.anim.slide_down_anim, R.anim.stay);
	}


	public boolean canGoBack() {
		return (this.webView.canGoBack() && mGoBack());
	}

	public void enableBackButton() {
		Drawable upArrow = ContextCompat.getDrawable(WebViewActivity.this, R.drawable.back24);
		upArrow.setColorFilter(ContextCompat.getColor(WebViewActivity.this, R.color.transparent),
				PorterDuff.Mode.SRC_ATOP);
		getSupportActionBar().setHomeAsUpIndicator(upArrow);
	}

	public void disableBackButton() {
		Drawable upArrow = ContextCompat.getDrawable(WebViewActivity.this, R.drawable.back24);
		upArrow.setColorFilter(ContextCompat.getColor(WebViewActivity.this, R.color.greyish), PorterDuff.Mode.SRC_ATOP);
		getSupportActionBar().setHomeAsUpIndicator(upArrow);
	}

	public boolean mGoBack() {
		WebBackForwardList mWebBackForwardList = webView.copyBackForwardList();
		if (mWebBackForwardList.getCurrentIndex() >= 1) {
			if (getHistoryUrl(mWebBackForwardList, 0).equalsIgnoreCase(getHistoryUrl(mWebBackForwardList, 1))) {
				disableBackButton();
				return false;
			} else {
				enableBackButton();
				return true;
			}
		} else {
			disableBackButton();
			return false;
		}
	}

	public String getHistoryUrl(WebBackForwardList item, int position) {
		return item.getItemAtIndex(item.getCurrentIndex() - position).getUrl().toString();
	}
}