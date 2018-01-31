package za.co.woolworths.financial.services.android.ui.fragments.wtoday;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.awfs.coordination.BR;
import com.awfs.coordination.R;
import com.awfs.coordination.databinding.WtodayFragmentBinding;

import za.co.woolworths.financial.services.android.models.WoolworthsApplication;
import za.co.woolworths.financial.services.android.ui.base.BaseFragment;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.Utils;

public class WTodayFragment extends BaseFragment<WtodayFragmentBinding, WTodayViewModel> implements WTodayNavigator {

	private WTodayViewModel wTodayViewModel;
	WebView webView;
	ErrorHandlerView mErrorHandlerView;

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

	@SuppressLint("SetJavaScriptEnabled")
	private void initWebView(View view) {
		webView = view.findViewById(R.id.wtoday_fragment_webview);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
		webView.addJavascriptInterface(new WebAppInterface(getActivity()), "Android");
		webView.loadUrl(WoolworthsApplication.getWwTodayURI());
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
}
