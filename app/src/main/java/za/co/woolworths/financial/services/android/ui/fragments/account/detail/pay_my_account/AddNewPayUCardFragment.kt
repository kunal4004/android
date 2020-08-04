package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.util.JsonToken
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.awfs.coordination.BuildConfig
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.add_new_payu_card_fragment.*
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.network.NetworkConfig
import java.io.IOException


class AddNewPayUCardFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.add_new_payu_card_fragment, container, false)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val builder = Uri.Builder()
        builder.scheme("https://qa.d1nnludhatueui.amplifyapp.com/") // moved host.rawValue() from authority to schema as MCS returns host with " https:// "
                .appendQueryParameter("client_id", "WWOneApp")
                .appendQueryParameter("username", WoolworthsApplication.getApiId())
                .appendQueryParameter("password", BuildConfig.SHA1)
                .appendQueryParameter("token", NetworkConfig().getSessionToken())

        addNewUserPayUWebView?.apply {
            val webSettings: WebSettings = settings
            webSettings.javaScriptCanOpenWindowsAutomatically = true
            webSettings.javaScriptEnabled = true
            val map = HashMap<String, String>()
            map["publicKey"] = "dfba7039-53ed-4dfd-b6d2-5bb3daaed0eb"
            map["environment"] = "test"
            map["username"] = WoolworthsApplication.getApiId()
            map["password"] = BuildConfig.SHA1
            webSettings.userAgentString = "userAgent"
            webSettings.domStorageEnabled = true
            clearHistory();
            clearCache(true)
            webSettings.javaScriptEnabled = true
            webSettings.setSupportZoom(true)
            webSettings.useWideViewPort = false
            webSettings.loadWithOverviewMode = false
            WebView.setWebContentsDebuggingEnabled(true)

        val documentEventListener = "document.addEventListener(\"message\", function(data) {" +
                "      console.log(data)" +
                "      if(data.length) {" +
                "        this.setState({" +
                "          username: ${WoolworthsApplication.getApiId()}," +
                "          password: ${BuildConfig.SHA1}" +
                "        })" +
                "      }"

            evaluateJavascript(documentEventListener) { s -> Log.e("evaluateJav", s) }

            webViewClient = object : WebViewClient() {
                override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest): WebResourceResponse? {
                    CookieManager.getInstance().removeAllCookies(null)
                    return super.shouldInterceptRequest(view, request)
                }
            }
            loadUrl(builder.toString(), map)
        }
    }
}