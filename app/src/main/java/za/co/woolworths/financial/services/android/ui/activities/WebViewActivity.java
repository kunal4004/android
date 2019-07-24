package za.co.woolworths.financial.services.android.ui.activities;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;

public class WebViewActivity extends AppCompatActivity {

	WebView webView;
	public Toolbar toolbar;
	public WTextView toolbarTextView;
	private ProgressBar loadingProgressBar;
	private ErrorHandlerView mErrorView;
	public LinearLayout loadingProgressBarKMSI;
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
		loadingProgressBarKMSI = findViewById(R.id.kmsiProgressLayout);
		ssoLayout = findViewById(R.id.ssoLayout);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(null);

		String url = b.getString("link");
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setDomStorageEnabled(true);
		webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
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
		webView.loadUrl(url, getExtraHeader());
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

	private Map<String, String> getExtraHeader() {
		Map<String, String> extraHeaders = new HashMap<>();
		//extraHeaders.put("bearer", SessionUtilities.getInstance().getSessionToken());
		return extraHeaders;
	}
}