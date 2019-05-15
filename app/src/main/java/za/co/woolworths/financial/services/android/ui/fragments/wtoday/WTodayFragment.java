package za.co.woolworths.financial.services.android.ui.fragments.wtoday;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.awfs.coordination.BR;
import com.awfs.coordination.R;
import com.awfs.coordination.databinding.WtodayFragmentBinding;

import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties;
import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.ui.base.BaseFragment;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.Utils;

public class WTodayFragment extends BaseFragment<WtodayFragmentBinding, WTodayViewModel> implements WTodayNavigator {

	private WTodayViewModel wTodayViewModel;
	private WebView webView;
	private ErrorHandlerView mErrorHandlerView;
	private String TAG = this.getClass().getSimpleName();

	@Override
	public WTodayViewModel getViewModel() {
		return wTodayViewModel;
	}

	@Override
	public int getBindingVariable() {
		return BR.viewModel;
	}

	@Override
	public int getLayoutId() {
		return R.layout.wtoday_fragment;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		wTodayViewModel = ViewModelProviders.of(this).get(WTodayViewModel.class);
		wTodayViewModel.setNavigator(this);
		try {
			hideToolbar();
		} catch (NullPointerException ex) {
			Log.e(TAG, ex.toString());
		}
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		Utils.updateStatusBarBackground(getActivity());
		Activity activity = getActivity();
		if (activity != null) {
			hideToolbar();
			mErrorHandlerView = new ErrorHandlerView(activity, getViewDataBinding().incNoConnectionHandler.noConnectionLayout);
			mErrorHandlerView.setMargin(getViewDataBinding().incNoConnectionHandler.noConnectionLayout, 0, 0, 0, 0);
			initWebView(view);
			retryConnect(view);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		Utils.setScreenName(getActivity(), FirebaseManagerAnalyticsProperties.ScreenNames.WTODAY);
	}

	@SuppressLint("SetJavaScriptEnabled")
	private void initWebView(View view) {
		webView = view.findViewById(R.id.wtoday_fragment_webview);
		webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
		webView.getSettings().setDomStorageEnabled(true);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
		webView.addJavascriptInterface(new WebAppInterface(getActivity()), "Android");
		webView.loadUrl(WoolworthsApplication.getWwTodayURI());
		webView.getSettings().setSupportMultipleWindows(true);
		webView.setWebViewClient(new WebViewClient() {
			@TargetApi(android.os.Build.VERSION_CODES.M)
			@Override
			public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
				super.onReceivedError(view, request, error);
				mErrorHandlerView.webViewBlankPage(view);
				mErrorHandlerView.networkFailureHandler(error.toString());
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				return super.shouldOverrideUrlLoading(view, url);
			}

			@SuppressWarnings("deprecation")
			@Override
			public void onReceivedError(WebView webView, int errorCode, String description, String failingUrl) {
				mErrorHandlerView.webViewBlankPage(webView);
				mErrorHandlerView.networkFailureHandler(description);
			}
		});

		webView.setWebChromeClient(new WebChromeClient(){
			@Override
			public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
				String data = view.getHitTestResult().getExtra();
				try {
					if (!TextUtils.isEmpty(data)) {
						Uri uri = Uri.parse(data);
						Intent intent = new Intent(Intent.ACTION_VIEW, uri);
						if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
							startActivity(intent);
						}
					}
				} catch (Exception e) {
					Log.e(TAG, e.getMessage());
				}
				return false;
			}
		});
		
	}

	private void retryConnect(View view) {
		view.findViewById(R.id.btnRetry).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isNetworkConnected()) {
					mErrorHandlerView.hideErrorHandler();
					webView.loadUrl(WoolworthsApplication.getWwTodayURI());
				}
			}
		});
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (!hidden) {
			//do when hidden
			setStatusBarColor(R.color.white);
			hideToolbar();
		}
	}

	public void scrollToTop() {
		ObjectAnimator anim = ObjectAnimator.ofInt(webView, "scrollY", webView.getScrollY(), 0);
		anim.setDuration(500).start();
	}
}
