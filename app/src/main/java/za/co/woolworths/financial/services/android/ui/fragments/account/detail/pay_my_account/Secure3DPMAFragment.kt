package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.awfs.coordination.R
import com.crashlytics.android.Crashlytics

import kotlinx.android.synthetic.main.secure_3d_webview_fragment.*
import za.co.woolworths.financial.services.android.models.dto.PayUPayResultRequest
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountActivity
import za.co.woolworths.financial.services.android.ui.extension.bindString


class Secure3DPMAFragment : Fragment() {

    private var root: View? = null
    private var merchantUrl: String? = null
    private var merchantSiteUrl: String? = null
    private var navController: NavController? = null

    val args: Secure3DPMAFragmentArgs by navArgs()
    val payMyAccountViewModel: PayMyAccountViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (root == null)
            root = inflater.inflate(R.layout.secure_3d_webview_fragment, container, false)
        return root
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initArgument()
        configureToolbar()

        navController = Navigation.findNavController(view)
        setupWebView()
    }

    private fun initArgument() {
        val redirection = args.pmaRedirection
        merchantSiteUrl = redirection?.merchantSiteUrl?.replace("[\\u003d]".toRegex(), "=") ?: ""
        merchantUrl = redirection?.url?.replace("[\\u003d]".toRegex(), "=") ?: ""
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        secureWebView?.apply {
            with(settings) {
                javaScriptEnabled = true
                domStorageEnabled = true

                webViewClient = object : WebViewClient() {

                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        if (!isAdded) return
                        try {
                            if (merchantSiteUrl?.let { url?.contains(it) }!!) {
                                stopLoading()
                                val siteUrl = url?.substring(url.indexOf("?"), url.length)

                                val urlParams = siteUrl?.split("&")
                                val customer = urlParams?.get(0)
                                val paymentId = urlParams?.get(1)
                                val chargeId = urlParams?.get(2)
                                val status = urlParams?.get(3)

                                val payUPayResultRequest = PayUPayResultRequest(
                                        customer?.substring(customer.indexOf("=").plus(1), customer.length)
                                                ?: "",
                                        paymentId?.substring(paymentId.indexOf("=").plus(1), paymentId.length)
                                                ?: "",
                                        chargeId?.substring(chargeId.indexOf("=").plus(1), chargeId.length)
                                                ?: "",
                                        status?.substring(status.indexOf("=").plus(1), status.length)
                                                ?: "", payMyAccountViewModel.getProductOfferingId()?.toString()
                                        ?: "")

                                val secure3DPMAFragmentDirections = Secure3DPMAFragmentDirections.actionSecure3DPMAFragmentToPMA3DSecureProcessRequestFragment(payUPayResultRequest)
                                navController?.navigate(secure3DPMAFragmentDirections)
                            }
                            } catch (iex: IllegalArgumentException) {
                                Crashlytics.log(iex.toString())
                            }
                        super.onPageStarted(view, url, favicon)
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