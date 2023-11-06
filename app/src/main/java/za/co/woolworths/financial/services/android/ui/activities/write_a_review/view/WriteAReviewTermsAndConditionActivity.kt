package za.co.woolworths.financial.services.android.ui.activities.write_a_review.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.PersistableBundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.awfs.coordination.R

class WriteAReviewTermsAndConditionActivity: AppCompatActivity (){
    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write_a_review_terms_and_condition)
        webView = findViewById(R.id.termsAndConditionWebView)
        webView.webViewClient = WebViewClient()
        webView.loadUrl("https://www.woolworths.co.za/corporate/cmp212633")
        webView.settings.javaScriptEnabled = true
        webView.settings.setSupportZoom(true)
    }

    override fun onBackPressed() {
        // if your webview can go back it will go back
        if (webView.canGoBack())
            webView.goBack()
        // if your webview cannot go back
        // it will exit the application
        else
            super.onBackPressed()
    }
}