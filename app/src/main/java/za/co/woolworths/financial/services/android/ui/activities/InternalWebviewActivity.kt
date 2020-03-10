package za.co.woolworths.financial.services.android.ui.activities

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View.GONE
import android.view.View.VISIBLE
import android.webkit.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.internal_webview_layout.*
import kotlinx.android.synthetic.main.no_connection_handler.*
import za.co.woolworths.financial.services.android.util.NetworkManager
import za.co.woolworths.financial.services.android.util.Utils

class InternalWebViewActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE = 123
    }

    private var mLink: String? = ""
    private var downLoadUrl: String? = null
    private var downLoadMimeType: String? = null
    private var downLoadUserAgent: String? = null
    private var downLoadConntentDisposition: String? = null

    override fun onStart() {
        overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
        super.onStart()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.internal_webview_layout)
        Utils.updateStatusBarBackground(this, R.color.black)

        mLink = intent?.extras?.getString("externalLink", "") ?: ""

        loadingInProgressWebView?.indeterminateDrawable?.setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY)

        when (hasNetworkConnection()) {
            true -> {
                showProgressBar()
                configureWebSettings()
                linkLoaderWebView?.loadUrl(mLink)
            }
            false -> includeNoConnectionLayout?.visibility = VISIBLE
        }

        btnRetry?.setOnClickListener {
            if (hasNetworkConnection()) {
                showProgressBar()
                hideConnectionLayout()
                linkLoaderWebView?.loadUrl(mLink)
            }
        }

        imBackButton?.setOnClickListener { closeActivity() }
    }

    private fun hasNetworkConnection() = NetworkManager.getInstance().isConnectedToNetwork(this@InternalWebViewActivity)

    @SuppressLint("SetJavaScriptEnabled")
    private fun configureWebSettings() {
        linkLoaderWebView?.apply {
            with(settings) {
                javaScriptEnabled = true
                domStorageEnabled = true
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                }
            }

            webViewClient = object : WebViewClient() {

                override fun onLoadResource(view: WebView?, url: String?) {
                    super.onLoadResource(view, url)
                }

                @RequiresApi(Build.VERSION_CODES.M)
                override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                    super.onReceivedError(view, request, error)
                    handleError(error.errorCode)

                }

                override fun onReceivedError(webView: WebView, errorCode: Int, description: String, failingUrl: String) {
                    handleError(errorCode)
                }

                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    val uri = Uri.parse(url)
                    handleUri(view, uri)
                    return true
                }

                override fun onPageFinished(view: WebView, url: String) {
                    super.onPageFinished(view, url)
                    hideProgressBar()
                }

                @TargetApi(Build.VERSION_CODES.N)
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                    val uri = request.url
                    handleUri(view, uri)
                    return true
                }
            }

            setDownloadListener { url, userAgent, contentDisposition, mimeType, contentLength ->
                downLoadUrl = url
                downLoadMimeType = mimeType
                downLoadUserAgent = userAgent
                downLoadConntentDisposition = contentDisposition
                if (isStoragePermissionGranted()) {
                    downloadFile(url, mimeType, userAgent, contentDisposition)
                }
            }
        }

    }

    private fun showConnectionLayout() {
        includeNoConnectionLayout?.visibility = VISIBLE
    }

    private fun hideConnectionLayout() {
        includeNoConnectionLayout?.visibility = GONE
    }

    private fun hideProgressBar() {
        loadingInProgressWebView?.visibility = GONE
    }

    private fun showProgressBar() {
        loadingInProgressWebView?.visibility = VISIBLE
    }

    private fun handleUri(view: WebView, uri: Uri) {
        val url = uri.toString()
        when (url.contains("mailto:")) {
            true -> Utils.sendEmail(url, "", applicationContext)
            false -> view.loadUrl(url)
        }
    }


    override fun onBackPressed() {
        goBackInWebView()
    }


    private fun downloadFile(url: String?, mimeType: String?, userAgent: String?, contentDisposition: String?) {
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
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url, contentDisposition, mimeType))
        val dm =
                getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        dm.enqueue(request)
        Toast.makeText(applicationContext, R.string.downloaing_text, Toast.LENGTH_LONG).show()
    }

    private fun isStoragePermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                true
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CODE)
                false
            }
        } else {
            true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            when (requestCode) {
                REQUEST_CODE -> downloadFile(downLoadUrl, downLoadMimeType, downLoadUserAgent, downLoadConntentDisposition)
            }
        }
    }


    fun goBackInWebView() {
        if (NetworkManager.getInstance().isConnectedToNetwork(this@InternalWebViewActivity)) {
            val history: WebBackForwardList = linkLoaderWebView.copyBackForwardList()
            var index = -1
            var url: String? = null
            while (linkLoaderWebView.canGoBackOrForward(index)) {
                if (history.getItemAtIndex(history.currentIndex + index).url != "about:blank") {
                    hideConnectionLayout()
                    linkLoaderWebView.goBackOrForward(index)
                    url = history.getItemAtIndex(-index).url
                    break
                }
                index--
            }
            // no history found that is not empty
            if (url == null) {
                if (linkLoaderWebView.canGoBack()) {
                    linkLoaderWebView.goBack()
                } else {
                    closeActivity()
                }
            }
        } else {
            closeActivity()
        }
    }

    private fun closeActivity() {
        finish()
        overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
    }

    private fun handleError(errorCode: Int) = when (errorCode) {
        WebViewClient.ERROR_CONNECT, WebViewClient.ERROR_TIMEOUT ->{
            hideProgressBar()
            aboutBlank()
            showConnectionLayout()
        }
        else -> {}
    }

    private fun aboutBlank() {
        linkLoaderWebView?.loadUrl("about:blank")
    }

}