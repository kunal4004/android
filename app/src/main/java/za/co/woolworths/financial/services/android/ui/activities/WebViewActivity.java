package za.co.woolworths.financial.services.android.ui.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.awfs.coordination.R;

import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.FontHyperTextParser;

public class WebViewActivity extends AppCompatActivity {

    WebView webView;
    ContentLoadingProgressBar progressBar;
    public Toolbar toolbar;
    public WTextView toolbarTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        webView = (WebView)findViewById(R.id.webview);
        progressBar = (ContentLoadingProgressBar)findViewById(R.id.load);
        progressBar.show();
        Bundle b = new Bundle();
        b= getIntent().getBundleExtra("Bundle");
        // getActionBar().setTitle(FontHyperTextParser.getSpannable(b.getString("title"), 1, this));

        //getActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbarTextView = (WTextView) findViewById(R.id.toolbar_title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(null);

        String url = b.getString("link");
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewController());
        webView.loadUrl(url);
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                toolbarTextView.setText(title);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
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
            if(url.contains("Login")){
                finish();
            }
            progressBar.hide();
        }
    }

}