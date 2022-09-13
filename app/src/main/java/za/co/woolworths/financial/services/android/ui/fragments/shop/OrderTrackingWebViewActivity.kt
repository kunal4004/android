package za.co.woolworths.financial.services.android.ui.fragments.shop

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Bundle
import android.view.View
import android.webkit.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.awfs.coordination.R
import com.awfs.coordination.databinding.TrackOrderWebviewActivityBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import za.co.woolworths.financial.services.android.common.ClickOnDialogButton
import za.co.woolworths.financial.services.android.common.CommonConnectivityStatus
import za.co.woolworths.financial.services.android.common.CommonErrorBottomSheetDialog
import za.co.woolworths.financial.services.android.util.Utils
import javax.inject.Inject


@AndroidEntryPoint
class OrderTrackingWebViewActivity : AppCompatActivity() {

    private var _binding: TrackOrderWebviewActivityBinding? = null
    private val binding get() = _binding
    private var trackingURL: String? = ""

    @Inject
    lateinit var commonConnectivityStatus: CommonConnectivityStatus

    @Inject
    lateinit var errorBottomSheetDialog: CommonErrorBottomSheetDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = TrackOrderWebviewActivityBinding.inflate(layoutInflater)
        val view = binding?.root
        setContentView(view)
        Utils.updateStatusBarBackground(this)
        trackingURL = checkNotNull(intent.getStringExtra(TRACKING_URL))
        checkNetworkStatus()
        closeTrackingScreen()

    }

    private fun closeTrackingScreen() {
        binding?.backImage?.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right)
        }
    }

    private fun checkNetworkStatus() {

        commonConnectivityStatus.connectivityObserve().onEach {
            when (it) {
                CommonConnectivityStatus.Available -> setupWebView()
                CommonConnectivityStatus.Lost -> showNoNetworkConnection()
                CommonConnectivityStatus.Unavailable -> showNoNetworkConnection()
                CommonConnectivityStatus.Losing -> showNoNetworkConnection()
            }
        }.launchIn(lifecycleScope)
    }

    private fun showNoNetworkConnection() {
        binding?.apply {
            trackingOrderWebView.visibility = View.GONE
            trackingDriverProgressBar.visibility = View.GONE
            trackOrderConnectionLayout.noConnectionLayout.visibility = View.VISIBLE
        }

    }

    private fun setupWebView() {
        binding?.apply {
            trackOrderConnectionLayout.noConnectionLayout.visibility = View.GONE
            trackingOrderWebView.webViewClient = object : WebViewClient() {

                override fun onLoadResource(view: WebView?, url: String?) {
                    // removing header and chat,calling icon from loading url (WebView) with class name i.e nav1
                    trackingOrderWebView.loadUrl("javascript:(function() { " +
                            "var head = document.getElementsByClassName('nav1')[0].style.display='none'; " +
                            "var head = document.getElementsByClassName('hippo-chat-icon')[0].style.display='none'; " +
                            "})()")

                    super.onLoadResource(view, url)
                }

                override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                    trackingDriverProgressBar.visibility = View.VISIBLE
                }

                override fun onPageFinished(view: WebView, url: String) {
                    trackingDriverProgressBar.visibility = View.GONE
                    trackingOrderWebView.visibility = View.VISIBLE
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?,
                ) {
                    super.onReceivedError(view, request, error)
                    showErrorDialog()
                }

                override fun onReceivedSslError(
                    view: WebView?,
                    handler: SslErrorHandler?,
                    error: SslError?,
                ) {
                    val builder = AlertDialog.Builder(this@OrderTrackingWebViewActivity)
                    builder.setMessage(R.string.ssl_error)
                    builder.setPositiveButton(
                        getString(R.string.continueLabel),
                    ) { _: DialogInterface?, _: Int ->
                        handler?.proceed()
                    }
                    builder.setNegativeButton(getString(R.string.link_cancel)
                    ) { _: DialogInterface?, _: Int ->
                        handler?.cancel()
                    }
                    val dialog = builder.create()
                    dialog.show()

                }

            }

            trackingOrderWebView.apply {
                settings.apply {
                    builtInZoomControls = false
                    setSupportZoom(false)
                    loadWithOverviewMode = true
                    javaScriptCanOpenWindowsAutomatically = true
                    domStorageEnabled = true
                    javaScriptEnabled = true
                }
                trackingURL?.let { loadUrl(it) }
            }


        }

    }

    companion object {
        private const val TRACKING_URL = "key:url"
        fun newIntent(context: Context, trackingURL: String): Intent =
            Intent(context, OrderTrackingWebViewActivity::class.java).putExtra(TRACKING_URL,
                trackingURL)

    }

    private fun showErrorDialog() {
        errorBottomSheetDialog.showCommonErrorBottomDialog(
            object : ClickOnDialogButton {
                override fun onClick() {
                    finish()
                }
            },
            this,
            getString(R.string.generic_error_something_wrong_newline),
            getString(R.string.please_try_again),
            getString(R.string.got_it)
        )
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right)

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}