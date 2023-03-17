package za.co.woolworths.financial.services.android.ui.activities.webview.usercase

import android.app.DownloadManager
import android.net.Uri
import android.os.Environment
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.webkit.WebSettings
import android.webkit.WebView
import za.co.woolworths.financial.services.android.ui.activities.webview.data.WebViewActions
import za.co.woolworths.financial.services.android.util.ErrorHandlerView
import javax.inject.Inject

interface IWebViewHandler {
    fun webSettings(internalWebView: WebView, isStoragePermissionGranted: Boolean)
}

class WebViewHandler @Inject constructor(webViewClientHandler:WebViewClientHandler) : IWebViewHandler,IWebViewClientHandler by webViewClientHandler  {
    lateinit var mErrorHandlerView: ErrorHandlerView

    override fun webSettings(internalWebView: WebView, isStoragePermissionGranted: Boolean) {
        with(internalWebView) {
            webViewData?.apply {
                settings.javaScriptEnabled = true
                if (treatmentPlan) {
                    settings.cacheMode = android.webkit.WebSettings.LOAD_NO_CACHE
                    clearCache(true)
                }
                settings.domStorageEnabled = true
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
                    settings.mixedContentMode =
                        WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE

                loadUrl(mExternalLink)
                setDownloadListener { url: String?, userAgent: String?, contentDisposition: String?, mimeType: String?, contentLength: Long ->
                    downLoadUrl = url
                    downLoadMimeType = mimeType
                    downLoadUserAgent = userAgent
                    downLoadConntentDisposition = contentDisposition
                    if (isStoragePermissionGranted) {
                        downloadFile(url, mimeType, userAgent, contentDisposition)
                    } else {
                        _webViewActions.value = WebViewActions.RequestStoragePermission
                    }
                }
            }

        }
    }

    fun downloadFile(
        url: String?,
        mimeType: String?,
        userAgent: String?,
        contentDisposition: String?
    ) {
        val request = DownloadManager.Request(Uri.parse(url))
        request.setMimeType(mimeType)
        val cookies = CookieManager.getInstance().getCookie(url)
        request.addRequestHeader("cookie", cookies)
        request.addRequestHeader("User-Agent", userAgent)
        request.setDescription("Downloading file...")
        request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimeType))
        request.allowScanningByMediaScanner()
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE) // Visibility of the download Notification
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(
            Environment.DIRECTORY_DOWNLOADS,
            URLUtil.guessFileName(url, contentDisposition, mimeType)
        )
        _webViewActions.value = WebViewActions.DownloadManager(request)
    }

    companion object {
        const val REQUEST_CODE = 123
        const val ARG_REDIRECT_BLANK_TARGET_LINK_EXTERNAL = "redirect_blank_target_link_external"
    }
}
