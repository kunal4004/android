package za.co.woolworths.financial.services.android.ui.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.WebBackForwardList;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.awfs.coordination.R;
import com.google.android.material.appbar.AppBarLayout;

import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.NetworkManager;
import za.co.woolworths.financial.services.android.util.Utils;

public class WInternalWebPageActivity extends AppCompatActivity implements View.OnClickListener {

	private WebView webInternalPage;
	private ErrorHandlerView mErrorHandlerView;
	private String mExternalLink;
	private ProgressBar mWoolworthsProgressBar;
	private AppBarLayout mAppbar;
	private static final int REQUEST_CODE=123;
	private String downLoadUrl;
	private String downLoadMimeType;
	private String downLoadUserAgent;
	private String downLoadConntentDisposition;

	@Override
	protected void onStart() {
		overridePendingTransition(R.anim.slide_up_anim, R.anim.stay);
		super.onStart();
	}

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utils.updateStatusBarBackground(this, R.color.black);
		setContentView(R.layout.internal_webview_activity);
		mToolbar();
		bundle();
		init();
		hideAppBar();
		webSetting();
		retryConnect();
	}

	private void mToolbar() {
		mAppbar = (AppBarLayout) findViewById(R.id.appbar);
		ImageView imBackButton = (ImageView) findViewById(R.id.imBackButton);
		imBackButton.setOnClickListener(this);
	}

	private void init() {
		webInternalPage = (WebView) findViewById(R.id.internalWebView);
		mWoolworthsProgressBar = (ProgressBar) findViewById(R.id.mWoolworthsProgressBar);
		mWoolworthsProgressBar.getIndeterminateDrawable().setColorFilter(null);
		mWoolworthsProgressBar.getIndeterminateDrawable().setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
		RelativeLayout rlConnectLayout = (RelativeLayout) findViewById(R.id.no_connection_layout);
		mErrorHandlerView = new ErrorHandlerView(WInternalWebPageActivity.this, rlConnectLayout);
	}

	@SuppressLint("SetJavaScriptEnabled")
	private void webSetting() {
		showProgressBar();
		webInternalPage.getSettings().setJavaScriptEnabled(true);
		webInternalPage.getSettings().setDomStorageEnabled(true);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			webInternalPage.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
		webInternalPage.setWebViewClient(new WebViewClient() {
			@TargetApi(android.os.Build.VERSION_CODES.M)
			@Override
			public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
				super.onReceivedError(view, request, error);
				showAppBar();
				mErrorHandlerView.webViewBlankPage(view);
				mErrorHandlerView.networkFailureHandler(error.toString());
			}

			@SuppressWarnings("deprecation")
			@Override
			public void onReceivedError(WebView webView, int errorCode, String description, String failingUrl) {
				mErrorHandlerView.webViewBlankPage(webView);
				showAppBar();
				mErrorHandlerView.networkFailureHandler(description);
				hideProgressBar();
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				// TODO Auto-generated method stub
				super.onPageFinished(view, url);
				hideProgressBar();
			}

			@SuppressWarnings("deprecation")
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				final Uri uri = Uri.parse(url);
				handleUri(view, uri);
				return true;
			}

			@TargetApi(Build.VERSION_CODES.N)
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
				final Uri uri = request.getUrl();
				handleUri(view, uri);
				return true;
			}
		});
		webInternalPage.loadUrl(mExternalLink);

		webInternalPage.setDownloadListener(new DownloadListener() {
			@Override
			public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {
				downLoadUrl=url;
				downLoadMimeType=mimeType;
				downLoadUserAgent=userAgent;
				downLoadConntentDisposition=contentDisposition;

				if (isStoragePermissionGranted()) {
					downloadFile(url,mimeType,userAgent,contentDisposition);
				}
			}
		});
	}

	private void handleUri(WebView view, Uri uri) {
		String url = uri.toString();
		if (url.contains("mailto:")) {
			Utils.sendEmail(url, "",getApplicationContext());
		}else {
			view.loadUrl(url);
		}
	}

	private void retryConnect() {
		findViewById(R.id.btnRetry).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (NetworkManager.getInstance().isConnectedToNetwork(WInternalWebPageActivity.this)) {
					hideAppBar();
					showProgressBar();
					WebBackForwardList history = webInternalPage.copyBackForwardList();
					int index = -1;
					String url;
					while (webInternalPage.canGoBackOrForward(index)) {
						if (!history.getItemAtIndex(history.getCurrentIndex() + index).getUrl().equals("about:blank")) {
							webInternalPage.goBackOrForward(index);
							url = history.getItemAtIndex(-index).getUrl();
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
		if (NetworkManager.getInstance().isConnectedToNetwork(WInternalWebPageActivity.this)) {
			WebBackForwardList history = webInternalPage.copyBackForwardList();
			int index = -1;
			String url = null;

			while (webInternalPage.canGoBackOrForward(index)) {
				if (!history.getItemAtIndex(history.getCurrentIndex() + index).getUrl().equals("about:blank")) {
					mErrorHandlerView.hideErrorHandlerLayout();
					webInternalPage.goBackOrForward(index);
					url = history.getItemAtIndex(-index).getUrl();
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
		overridePendingTransition(R.anim.stay, R.anim.slide_down_anim);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.imBackButton:
				goBackInWebView();
				break;
		}
	}

	private void showAppBar() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mAppbar.setVisibility(View.VISIBLE);
			}
		});
	}

	private void hideAppBar() {
		mAppbar.setVisibility(View.GONE);
	}

	@Override
	public void onBackPressed() {
		finishActivity();
	}

	public void downloadFile(String url,String mimeType,String userAgent,String contentDisposition)
	{
		DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

		request.setMimeType(mimeType);

		String cookies = CookieManager.getInstance().getCookie(url);
		request.addRequestHeader("cookie", cookies);
		request.addRequestHeader("User-Agent", userAgent);
		request.setDescription("Downloading file...");
		request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimeType));
		request.allowScanningByMediaScanner();
		request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);// Visibility of the download Notification
		request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
		request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url, contentDisposition, mimeType));
		DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
		dm.enqueue(request);
		Toast.makeText(getApplicationContext(), R.string.downloaing_text, Toast.LENGTH_LONG).show();
	}

	public  boolean isStoragePermissionGranted() {
		if (Build.VERSION.SDK_INT >= 23) {
			if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
					== PackageManager.PERMISSION_GRANTED) {
				return true;
			} else {

				ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
				return false;
			}
		}
		else {
			return true;
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if(grantResults.length > 0
				&& grantResults[0] == PackageManager.PERMISSION_GRANTED){
			switch (requestCode){
				case REQUEST_CODE:
					downloadFile(downLoadUrl,downLoadMimeType,downLoadUserAgent,downLoadConntentDisposition);
					break;
			}
		}
	}
}
