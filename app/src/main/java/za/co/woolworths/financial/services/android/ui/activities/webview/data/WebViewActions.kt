package za.co.woolworths.financial.services.android.ui.activities.webview.data

import android.webkit.SslErrorHandler
import android.webkit.WebView

sealed class WebViewActions {
    object FinishActivity : WebViewActions()
    object ShowAppBar : WebViewActions()
    object HideProgressBar : WebViewActions()
    object HideAppBar : WebViewActions()
    object DestroyWebView : WebViewActions()
    object RequestStoragePermission : WebViewActions()
    data class OpenUrlInPhoneBrowser(var url: String) : WebViewActions()
    data class SendEmail(var url: String) : WebViewActions()
    data class WebViewBlankPage(var webView: WebView) : WebViewActions()
    data class NetworkFailureHandler(var errorMessage: String) : WebViewActions()
    data class MakeCall(var url: String) : WebViewActions()
    data class FinishActivityForElite(var params: HashMap<String, String>) : WebViewActions()
    data class ShowSslErrorDialogDialog(var handler: SslErrorHandler) : WebViewActions()
    data class DownloadManager(var request: android.app.DownloadManager.Request) : WebViewActions()
    data class LoadURL(var mExternalLink: String) : WebViewActions()
}
