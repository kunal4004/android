package za.co.woolworths.financial.services.android.ui.activities.webview.usercase

import android.annotation.TargetApi
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Handler
import android.webkit.*
import kotlinx.coroutines.flow.MutableStateFlow
import za.co.woolworths.financial.services.android.ui.activities.webview.data.WebViewActions
import za.co.woolworths.financial.services.android.ui.activities.webview.data.WebViewData
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.KotlinUtils
import javax.inject.Inject

interface IWebViewClientHandler {
    var _webViewActions: MutableStateFlow<WebViewActions>
    var webViewData: WebViewData?
    var webViewClient: WebViewClient
}
class WebViewClientHandler @Inject constructor():IWebViewClientHandler {
    override var _webViewActions = MutableStateFlow<WebViewActions>(WebViewActions.HideAppBar)
    override var webViewData: WebViewData? = null
    override var webViewClient = object : WebViewClient() {
        @TargetApi(Build.VERSION_CODES.M)
        override fun onReceivedError(
            view: WebView,
            request: WebResourceRequest,
            error: WebResourceError
        ) {
            super.onReceivedError(view, request, error)
            _webViewActions.value = WebViewActions.ShowAppBar
            _webViewActions.value = WebViewActions.WebViewBlankPage(view)
            _webViewActions.value = WebViewActions.NetworkFailureHandler(error.toString())
        }

        override fun onReceivedError(
            webView: WebView,
            errorCode: Int,
            description: String,
            failingUrl: String
        ) {
            _webViewActions.value = WebViewActions.WebViewBlankPage(webView)
            _webViewActions.value = WebViewActions.ShowAppBar
            _webViewActions.value = WebViewActions.NetworkFailureHandler(description)
            _webViewActions.value = WebViewActions.HideProgressBar
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            _webViewActions.value = WebViewActions.HideProgressBar
        }

        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            val uri = Uri.parse(url)
            return handleUri(view, uri)
        }

        @TargetApi(Build.VERSION_CODES.N)
        override fun shouldOverrideUrlLoading(
            view: WebView,
            request: WebResourceRequest
        ): Boolean {
            val uri = request.url
            return handleUri(view, uri)
        }

        override fun onReceivedSslError(
            view: WebView,
            handler: SslErrorHandler,
            error: SslError
        ) {
            _webViewActions.value = WebViewActions.ShowSslErrorDialogDialog(handler)


        }

        override fun doUpdateVisitedHistory(view: WebView, url: String, isReload: Boolean) {
            super.doUpdateVisitedHistory(view, url, isReload)
            webViewData?.apply {
                if (treatmentPlan!!) {
                    val parameters = getQueryString(url)
                    if (parameters.containsKey("Scope")) {
                        if (parameters["Scope"] == "paynow") {
                            _webViewActions.value =
                                WebViewActions.FinishActivityForElite(parameters)
                        } else if (parameters["Scope"] == "back" &&
                            url.contains(collectionsExitUrl!!)
                        ) {
                            _webViewActions.value = WebViewActions.FinishActivity
                        }
                    } else if (url.contains(collectionsExitUrl!!)) {
                        val uri = Uri.parse(url)
                        val urlToOpen = uri.getQueryParameter("nburl")
                        if (urlToOpen != null) {
                            _webViewActions.value = WebViewActions.OpenUrlInPhoneBrowser(urlToOpen)
                            val handler = Handler()
                            handler.postDelayed(
                                { _webViewActions.value = WebViewActions.LoadURL(mExternalLink) },
                                AppConstant.DELAY_900_MS
                            )
                        } else {
                            _webViewActions.value = WebViewActions.FinishActivity
                        }
                    }
                }
            }
            ficaHandling(url)
            petInsuranceHandling(url)
        }
    }


    private fun petInsuranceHandling(url: String) {
        webViewData?.apply {
            if (isPetInsurance && url.contains(collectionsExitUrl!!)) {
                _webViewActions.value = WebViewActions.DestroyWebView
                _webViewActions.value = WebViewActions.FinishActivity
            }
        }
    }

    fun ficaHandling(url: String?) {
        webViewData?.apply {
            url?.let {
                collectionsExitUrl?.apply {
                    if (it.contains(this)) {
                        _webViewActions.value = WebViewActions.DestroyWebView
                        val parameters = getQueryString(url)
                        if (parameters.containsKey("IsCompleted") && parameters["IsCompleted"] == "false") {
                            ficaCanceled = true
                        }
                        _webViewActions.value = WebViewActions.FinishActivity
                    }
                }
            }
        }
    }

    private fun handleUri(view: WebView, uri: Uri): Boolean {
        val url = uri.toString()
        if (url.contains("mailto:")) {
            _webViewActions.value = WebViewActions.SendEmail(url)
        } else if (url.startsWith("tel:")) {
            _webViewActions.value = WebViewActions.MakeCall(url)
        } else if (!privacyUrlForFica().isEmpty() && url.contains(privacyUrlForFica()) && KotlinUtils.isFicaEnabled()) {
            _webViewActions.value = WebViewActions.OpenUrlInPhoneBrowser(url)

        } else if (webViewData?.mustRedirectBlankTargetLinkToExternal == true) {
            // Open hyperlink on external browser if it contains target="_blank", else open on the WebView itself
            val selector =
                "(function() { var elements = document.querySelectorAll('a[href*=\\'" + url.replace(
                    "/$".toRegex(),
                    ""
                ) + "\\']'); if (elements.length > 0) { return elements[0].target == '_blank'; } else { return false; }})();"
            view.evaluateJavascript(selector) { value: String ->
                if (value.equals("true", ignoreCase = true)) {
                    _webViewActions.value = WebViewActions.OpenUrlInPhoneBrowser(url)
                } else {
                    view.loadUrl(url)
                }
            }
        } else {
            view.loadUrl(url)
        }
        return true
    }
    fun getQueryString(url: String?): HashMap<String, String> {
        val map = HashMap<String, String>()
        if (!url.isNullOrBlank()) {
            val uri = Uri.parse(url)
            if (uri.isHierarchical) {
                for (paramName in uri.queryParameterNames) {
                    if (paramName != null) {
                        val paramValue = uri.getQueryParameter(paramName)
                        if (paramValue != null) {
                            map[paramName] = paramValue
                        }
                    }
                }
            }
        }
        return map
    }
    private fun privacyUrlForFica(): String {
        return webViewData?.fica?.privacyPolicyUrl ?: ""
    }
}