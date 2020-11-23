package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import com.awfs.coordination.R

import kotlinx.android.synthetic.main.secure_3d_webview_fragment.*
import za.co.woolworths.financial.services.android.util.FirebaseManager

class Secure3DPMAFragment : PMAFragment() {

    val payMyAccountViewModel: PayMyAccountViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.secure_3d_webview_fragment, container, false)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureToolbar(true, R.string.secure_3d_title)
        setupWebView()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {

        secureWebView?.apply {
            with(settings) {
                javaScriptEnabled = true
                webViewClient = object : WebViewClient() {

                    override fun onPageFinished(view: WebView?, url: String?) {
                        if (!isAdded) return
                        activity?.runOnUiThread {
                            try {
                                payMyAccountViewModel.constructPayUPayResultCallback(url, { stopLoading() }, {
                                    view?.let { v -> Navigation.findNavController(v).navigate(R.id.action_secure3DPMAFragment_to_PMA3DSecureProcessRequestFragment) }
                                })
                            } catch (iex: IllegalArgumentException) {
                                FirebaseManager.logException(iex.toString())
                            }
                        }
                        super.onPageFinished(view, url)
                    }
                }
            }
            payMyAccountViewModel.getMerchantSiteAndMerchantUrl().second?.let { merchantUrl -> loadUrl(merchantUrl) }
        }
    }
}