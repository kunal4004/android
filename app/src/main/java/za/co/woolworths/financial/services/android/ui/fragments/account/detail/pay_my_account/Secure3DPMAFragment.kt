package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import com.awfs.coordination.R
import com.crashlytics.android.Crashlytics

import kotlinx.android.synthetic.main.secure_3d_webview_fragment.*
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountActivity
import za.co.woolworths.financial.services.android.ui.extension.bindString

class Secure3DPMAFragment : Fragment() {

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
        configureToolbar()
        setupWebView()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {

        val urls = payMyAccountViewModel.getMerchantSiteAndMerchantUrl()
        val merchantUrl = urls.second

        secureWebView?.apply {
            with(settings) {
                javaScriptEnabled = true
                domStorageEnabled = true
                webViewClient = object : WebViewClient() {

                    override fun onPageFinished(view: WebView?, url: String?) {
                        if (!isAdded) return
                        activity?.runOnUiThread {
                            try {
                                payMyAccountViewModel.constructPayUPayResultCallback(url, { stopLoading() }, { result ->
                                    view?.let { v -> Navigation.findNavController(v).navigate(Secure3DPMAFragmentDirections.actionSecure3DPMAFragmentToPMA3DSecureProcessRequestFragment(result)) }
                                })
                            } catch (iex: IllegalArgumentException) {
                                Crashlytics.log(iex.toString())
                            }
                        }
                        super.onPageFinished(view, url)
                    }

                }
            }
            loadUrl(merchantUrl)
        }
    }

    private fun configureToolbar() {
        (activity as? PayMyAccountActivity)?.apply {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            displayToolbarDivider(true)
            configureToolbar(bindString(R.string.secure_3d_title))
        }
    }
}