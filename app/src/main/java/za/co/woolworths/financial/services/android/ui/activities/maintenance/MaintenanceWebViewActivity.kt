package za.co.woolworths.financial.services.android.ui.activities.maintenance

import android.content.DialogInterface
import android.net.http.SslError
import android.os.Bundle
import android.webkit.SslErrorHandler
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.maintenance_web_view_layout.*
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.BUNDLE
import za.co.woolworths.financial.services.android.util.SessionUtilities

/**
 * Created by Kunal Uttarwar on 11/10/22.
 */
class MaintenanceWebViewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.maintenance_web_view_layout)
        val b = intent.getBundleExtra(BUNDLE)
        val url = b!!.getString("link")
        maintenanceWebView.settings.javaScriptEnabled = true
        maintenanceWebView.settings.domStorageEnabled = true
        maintenanceWebView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        maintenanceWebView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        maintenanceWebView.webViewClient = WebViewController()
        try {
            val m =
                WebSettings::class.java.getMethod("setMixedContentMode",
                    Int::class.javaPrimitiveType)
            m.invoke(maintenanceWebView.settings, 2) // 2 = MIXED_CONTENT_COMPATIBILITY_MODE
        } catch (ex: Exception) {
        }
        maintenanceWebView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        maintenanceWebView.loadUrl(url!!, getExtraHeader()!!)
    }

    protected class WebViewController : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            view.loadUrl(url)
            return true
        }

        override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
            //super.onReceivedSslError(view, handler, error);
            val builder = AlertDialog.Builder(WoolworthsApplication.getInstance())
            builder.setMessage(R.string.ssl_error)
            builder.setPositiveButton("continue"
            ) { dialog: DialogInterface?, which: Int -> handler.proceed() }
            builder.setNegativeButton("cancel"
            ) { dialog: DialogInterface?, which: Int -> handler.cancel() }
            val dialog = builder.create()
            dialog.show()
        }

        override fun onPageFinished(view: WebView, url: String) {
            // do your stuff here
        }
    }

    private fun getExtraHeader(): Map<String, String>? {
        val extraHeaders: MutableMap<String, String> = HashMap()
        extraHeaders["bearer"] = SessionUtilities.getInstance().sessionToken
        return extraHeaders
    }

    override fun onBackPressed() {
    }
}