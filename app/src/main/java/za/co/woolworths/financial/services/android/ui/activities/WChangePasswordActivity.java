package za.co.woolworths.financial.services.android.ui.activities;

import android.annotation.TargetApi;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.util.BaseActivity;
import za.co.woolworths.financial.services.android.util.ConnectionDetector;
import za.co.woolworths.financial.services.android.util.ErrorHandlerView;
import za.co.woolworths.financial.services.android.util.Utils;

public class WChangePasswordActivity extends BaseActivity {

    private Toolbar mToolbar;
    private WebView webView;
    private ErrorHandlerView mErrorHandlerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.updateStatusBarBackground(this);
        setContentView(R.layout.change_password_activity);
        init();
        setActionBar();
        bindDateWithUI();
        retryConnect();
    }
    private void setActionBar() {
        setSupportActionBar(mToolbar);
        ActionBar mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setDisplayShowTitleEnabled(false);
            mActionBar.setDisplayUseLogoEnabled(false);
            mActionBar.setDefaultDisplayHomeAsUpEnabled(false);
            mActionBar.setHomeAsUpIndicator(R.drawable.back24);
        }
    }
    private void init() {
        webView = (WebView) findViewById(R.id.changePasswordWeb);
        mToolbar = (Toolbar) findViewById(R.id.mToolbar);
        RelativeLayout rlConnectLayout = (RelativeLayout) findViewById(R.id.no_connection_layout);
        mErrorHandlerView = new ErrorHandlerView(WChangePasswordActivity.this, rlConnectLayout);
        mErrorHandlerView.setMargin(rlConnectLayout, 0, 0, 0, 0);
    }
    public void bindDateWithUI()
    {

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
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
            public void onPageFinished(WebView view, String url) {
                // TODO Auto-generated method stub
                super.onPageFinished(view, url);
            }
        });
        webView.loadUrl(getString(R.string.change_password_url));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        else
        {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
    private void retryConnect() {
        findViewById(R.id.btnRetry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (new ConnectionDetector().isOnline(WChangePasswordActivity.this)) {
                    webView.goBack();
                    mErrorHandlerView.hideErrorHandlerLayout();
                }
            }
        });
    }
}
