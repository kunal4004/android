package za.co.woolworths.financial.services.android.ui.activities;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebBackForwardList;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.Utils;

public class WInternalWebPageActivity extends AppCompatActivity {

	private WebView webInternalPage;
	private ErrorHandlerView mErrorHandlerView;
	private String mExternalLink;
	private ProgressBar mWoolworthsProgressBar;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utils.updateStatusBarBackground(this, R.color.black);
		setContentView(R.layout.internal_webview_activity);
		bundle();
		init();
		webSetting();
		retryConnect();
	}

	private void init() {
		webInternalPage = (WebView) findViewById(R.id.internalWebView);
		mWoolworthsProgressBar = (ProgressBar) findViewById(R.id.mWoolworthsProgressBar);
		mWoolworthsProgressBar.getIndeterminateDrawable().setColorFilter(null);
		mWoolworthsProgressBar.getIndeterminateDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
		RelativeLayout rlConnectLayout = (RelativeLayout) findViewById(R.id.no_connection_layout);
		mErrorHandlerView = new ErrorHandlerView(WInternalWebPageActivity.this, rlConnectLayout);
		mErrorHandlerView.setMargin(rlConnectLayout, 0, 0, 0, 0);
	}

	@SuppressLint("SetJavaScriptEnabled")
	private void webSetting() {
		showProgressBar();
		webInternalPage.getSettings().setJavaScriptEnabled(true);
		webInternalPage.setWebViewClient(new WebViewClient() {
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
				hideProgressBar();
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				// TODO Auto-generated method stub
				super.onPageFinished(view, url);
				hideProgressBar();
			}
		});
		webInternalPage.loadUrl(mExternalLink);
	}

	private void retryConnect() {
		findViewById(R.id.btnRetry).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (new ConnectionDetector().isOnline(WInternalWebPageActivity.this)) {
					showProgressBar();
					WebBackForwardList history = webInternalPage.copyBackForwardList();
					int index = -1;
					String url;
					while (webInternalPage.canGoBackOrForward(index)) {
						if (!history.getItemAtIndex(history.getCurrentIndex() + index).getUrl().equals("about:blank")) {
							webInternalPage.goBackOrForward(index);
							url = history.getItemAtIndex(-index).getUrl();
							Log.e("tag", "first non empty" + url);
							break;
						}
						index--;
					}
					mErrorHandlerView.hideErrorHandlerLayout();
				}
			}
		});
	}

	private void showProgressBar() {
		mWoolworthsProgressBar.setVisibility(View.VISIBLE);
	}

	private void hideProgressBar() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mWoolworthsProgressBar.setVisibility(View.GONE);
			}
		});
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			switch (keyCode) {
				case KeyEvent.KEYCODE_BACK:
					goBackInWebView();
					return true;
			}

		}
		return super.onKeyDown(keyCode, event);
	}

	private void bundle() {
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			mExternalLink = bundle.getString("externalLink");
		}
	}

	public void goBackInWebView() {
		if (new ConnectionDetector().isOnline(WInternalWebPageActivity.this)) {
			WebBackForwardList history = webInternalPage.copyBackForwardList();
			int index = -1;
			String url = null;

			while (webInternalPage.canGoBackOrForward(index)) {
				if (!history.getItemAtIndex(history.getCurrentIndex() + index).getUrl().equals("about:blank")) {
					mErrorHandlerView.hideErrorHandlerLayout();
					webInternalPage.goBackOrForward(index);
					url = history.getItemAtIndex(-index).getUrl();
					Log.e("tag", "first non empty" + url);
					break;
				}
				index--;
			}
			// no history found that is not empty
			if (url == null) {
				if (webInternalPage.canGoBack()) {
					webInternalPage.goBack();
				} else {
					finishActivity();
				}
			}
		} else {
			finishActivity();
		}
	}

	public void finishActivity() {
		finish();
		overridePendingTransition(R.anim.slide_down_anim, R.anim.stay);
	}
}
