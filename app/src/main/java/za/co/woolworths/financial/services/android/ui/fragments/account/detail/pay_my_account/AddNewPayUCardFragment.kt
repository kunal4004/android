package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.fragment.app.Fragment
import com.awfs.coordination.BuildConfig
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.add_new_payu_card_fragment.*
import za.co.woolworths.financial.services.android.contracts.IPayUInterface
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountActivity
import java.net.URLEncoder
import java.util.*

class AddNewPayUCardFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.add_new_payu_card_fragment, container, false)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? PayMyAccountActivity)?.apply {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            displayToolbarDivider(false)
        }


        addNewUserPayUWebView?.apply {
            val webSettings: WebSettings = settings
            webSettings.javaScriptEnabled = true
            webSettings.domStorageEnabled = true

            addJavascriptInterface(PayUCardFormJavascriptBridge(object : IPayUInterface {
                override fun onAddNewCardSuccess(token: String) {
                    super.onAddNewCardSuccess(token)
                    Log.e("onAddNewCardSuccess", token)
                }
            }), "JSBridge")

            webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(cm: ConsoleMessage): Boolean {
                    Log.d("onConsoleMessage", String.format("%s @ %d: %s", cm.message(), cm.lineNumber(), cm.sourceId()))
                    return super.onConsoleMessage(cm)
                }
            }

            webViewClient = object : WebViewClient() {
                override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest): WebResourceResponse? {
                    CookieManager.getInstance().removeAllCookies(null)
                    return super.shouldInterceptRequest(view, request)
                }
            }

            val postData = "?api_id=" + URLEncoder.encode(WoolworthsApplication.getApiId()?.toLowerCase(Locale.ROOT), "UTF-8").toString() + "&sha1=" + URLEncoder.encode(BuildConfig.SHA1, "UTF-8")
            loadUrl("https://payu-qa.wfs.wigroup.io/$postData")
        }
    }
}