package za.co.woolworths.financial.services.android.ui.activities.webview.activities

import android.Manifest
import android.app.DownloadManager
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.KeyEvent
import android.view.View
import android.webkit.SslErrorHandler
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.awfs.coordination.R
import com.awfs.coordination.databinding.InternalWebviewActivityBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInPresenterImpl
import za.co.woolworths.financial.services.android.ui.activities.webview.data.EliteWebViewArgs.*
import za.co.woolworths.financial.services.android.ui.activities.webview.usercase.WebViewHandler.Companion.REQUEST_CODE
import za.co.woolworths.financial.services.android.ui.activities.webview.data.WebViewActions
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.BindingBaseActivity
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.Constants
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.eliteplan.ElitePlanModel

@AndroidEntryPoint
class WInternalWebPageActivity : BindingBaseActivity<InternalWebviewActivityBinding>(InternalWebviewActivityBinding::inflate), View.OnClickListener {

    private val viewModel: WebViewModel by viewModels()

    private var isPetInsurance: Boolean = false

    override fun onStart() {
        if (viewModel.webViewClientHandler.webViewData?.treatmentPlan == true) {
            overridePendingTransition(R.anim.slide_from_right, R.anim.stay)
        } else {
            overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
        }
        super.onStart()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intentExtras =  intent.extras
        isPetInsurance = intentExtras?.getBoolean(Constants.IS_PET_INSURANCE, false) == true
        when (isPetInsurance) {
            true -> {
                setPetInsurancePawGifImage()
                viewModel.navigateToPetInsuranceStatusCOVERED()
                handleAppGuidResponse()
            }
            false -> {
                intentExtras?.let { viewModel.bundle(it, chromeClient = chromeClient()) }
                initWebView()
            }
        }
    }

    private fun handleAppGuidResponse() {
        lifecycleScope.launch {
            with(viewModel) {
                fetchAppGuidState.collect { result ->
                    result.data?.let { response ->
                        val breakoutParams = breakoutToWebViewParams(response = response)
                        setBreakoutParams(params = breakoutParams,chromeClient = chromeClient())
                        initWebView()
                    }
                    showProgressBar(result.isLoading)
                }
            }
        }
    }

    private fun setPetInsurancePawGifImage() {
        showProgressBar(true)
    }

    private fun initWebView() {
        init()
        webSetting()
        retryConnect()
    }

    private fun chromeClient(): ChromeClient {
        return ChromeClient(this@WInternalWebPageActivity).apply {
            viewModel.webViewClientHandler.webViewData?.chromeClient?.setUpWebViewDefaults(
                binding.internalWebView
            )
            binding.internalWebView.webChromeClient = this
        }
    }

    private fun init() {
        Utils.updateStatusBarBackground(this, R.color.black)
        binding.imBackButton.setOnClickListener(this)
        lifecycleScope.launch {
            viewModel.webViewActions.collect { action ->
                when (action) {
                    WebViewActions.HideAppBar -> hideAppBar()
                    WebViewActions.FinishActivity -> finishActivity()
                    WebViewActions.ShowAppBar -> {runOnUiThread { binding.appbar.visibility = View.VISIBLE } }
                    WebViewActions.HideProgressBar -> hideProgressBar()
                    WebViewActions.DestroyWebView -> binding.internalWebView.destroy()
                    WebViewActions.RequestStoragePermission -> {ActivityCompat.requestPermissions(this@WInternalWebPageActivity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CODE)}
                    is WebViewActions.NetworkFailureHandler -> viewModel.webViewClientHandler.mErrorHandlerView.networkFailureHandler(action.errorMessage)
                    is WebViewActions.WebViewBlankPage -> viewModel.webViewClientHandler.mErrorHandlerView.webViewBlankPage(action.webView)
                    is WebViewActions.MakeCall -> {makeCall(action.url) }
                    is WebViewActions.FinishActivityForElite -> finishActivityForElite(action.params)
                    is WebViewActions.ShowSslErrorDialogDialog -> sslErrorDialog(action.handler)
                    is WebViewActions.OpenUrlInPhoneBrowser -> openUrlInPhoneBrowser(action.url)
                    is WebViewActions.SendEmail -> Utils.sendEmail(action.url, "", this@WInternalWebPageActivity)
                    is WebViewActions.DownloadManager -> {downloadFile(action.request) }
                    is WebViewActions.LoadURL -> binding.internalWebView.loadUrl(action.mExternalLink)
                }
            }
        }
        binding.mWoolworthsProgressBar.indeterminateDrawable.apply {
            colorFilter = null
            setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY)
        }
        viewModel.webViewClientHandler.mErrorHandlerView = ErrorHandlerView(this@WInternalWebPageActivity, binding.noConnectionHandler.noConnectionLayout)
    }

    private fun openUrlInPhoneBrowser(url: String ) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    private fun downloadFile(request: DownloadManager.Request) {
        val dm = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        dm.enqueue(request)
        Toast.makeText(this@WInternalWebPageActivity, R.string.downloaing_text, Toast.LENGTH_LONG).show()
    }

    private fun sslErrorDialog(handler: SslErrorHandler) {
        val builder = AlertDialog.Builder(this@WInternalWebPageActivity)
        builder.setMessage(R.string.ssl_error)
        builder.setPositiveButton("continue") { dialog: DialogInterface?, which: Int -> handler.proceed() }
        builder.setNegativeButton("cancel") { dialog: DialogInterface?, which: Int -> handler.cancel() }
        val dialog = builder.create()
        dialog.show()
    }

    private fun webSetting() {
        viewModel.webViewClientHandler.webSettings(
            binding.internalWebView,
            isStoragePermissionGranted
        )
        binding.internalWebView.webViewClient = viewModel.webViewClientHandler.webViewClient
    }

    fun makeCall(url: String) {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse(url))
        startActivity(intent)
    }

    private fun retryConnect() {
        binding.noConnectionHandler.btnRetry.setOnClickListener { v: View? ->
            if (NetworkManager.getInstance().isConnectedToNetwork(this@WInternalWebPageActivity)) {
                hideAppBar()
                showProgressBar(true)
                val history = binding.internalWebView.copyBackForwardList()
                var index = -1
                while (binding.internalWebView.canGoBackOrForward(index)) {
                    if (history.getItemAtIndex(history.currentIndex + index).url != "about:blank") {
                        binding.internalWebView.goBackOrForward(index)
                        break
                    }
                    index--
                }
                viewModel.webViewClientHandler.mErrorHandlerView.hideErrorHandlerLayout()
            }
        }
    }

    private fun showProgressBar(isShown : Boolean = false) {
        binding.mWoolworthsProgressBar.visibility = if (isShown) View.VISIBLE else View.GONE
    }

    private fun hideProgressBar() {
        if (viewModel.webViewClientHandler.webViewData?.treatmentPlan == true) {
            val handler = Handler()
            handler.postDelayed(
                {  showProgressBar(false) },
                AppConstant.DELAY_1000_MS
            )
        } else {
            runOnUiThread { showProgressBar(false) }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            when (keyCode) {
                KeyEvent.KEYCODE_BACK -> {
                    goBackInWebView()
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    fun goBackInWebView() {
        if (NetworkManager.getInstance().isConnectedToNetwork(this@WInternalWebPageActivity)) {
            val history = binding.internalWebView.copyBackForwardList()
            var index = -1
            var url: String? = null
            while (binding.internalWebView.canGoBackOrForward(index)) {
                if (history.getItemAtIndex(history.currentIndex + index).url != "about:blank") {
                    viewModel.webViewClientHandler.mErrorHandlerView.hideErrorHandlerLayout()
                    binding.internalWebView.goBackOrForward(index)
                    url = history.getItemAtIndex(-index).url
                    break
                }
                index--
            }
            // no history found that is not empty
            if (url == null) {
                if (binding.internalWebView.canGoBack()) {
                    binding.internalWebView.goBack()
                } else {
                    finishActivity()
                }
            }
            if (viewModel.webViewClientHandler.webViewData?.treatmentPlan == true && binding.internalWebView.url?.contains(
                    KotlinUtils.collectionsIdUrl
                ) == true
            ) {
                finishActivity()
            }
        } else {
            finishActivity()
        }
    }

    fun finishActivity() {
        viewModel.webViewClientHandler.webViewData?.apply {
            if (!ficaCanceled) {
                setResult(RESULT_OK)
            }
        }
        finish()
        overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
    }

    private fun finishActivityForElite(params: HashMap<String, String>) {
        val resultIntent = Intent()
        val elitePlanModel =
            ElitePlanModel(params[SCOPE.type], params[DISCOUNT_AMOUNT.type], params[SETTLEMENT_AMOUNT.type])
        resultIntent.putExtra(AccountSignedInPresenterImpl.ELITE_PLAN_MODEL, elitePlanModel)
        setResult(RESULT_OK, resultIntent)
        finish()
        overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.imBackButton -> goBackInWebView()
        }
    }
    private fun hideAppBar() {
        binding.appbar.visibility = View.GONE
    }

    override fun onBackPressed() {
        finishActivity()
    }

    private val isStoragePermissionGranted: Boolean
        get() = if (Build.VERSION.SDK_INT >= 23) {
            (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        } else {
            true
        }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            viewModel.handleRequestPermission(requestCode)
        }
    }
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!viewModel.handleFicaFilesCallBack(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}