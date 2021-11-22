package za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.opt_in

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.balance_protection_insurance_activity.*
import kotlinx.android.synthetic.main.bpi_email_sent_failure_layout.*
import kotlinx.android.synthetic.main.bpi_email_sent_success_layout.*
import kotlinx.android.synthetic.main.bpi_terms_conditions_fragment.*
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.GenericResponse
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.BalanceProtectionInsuranceActivity
import za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.BalanceProtectionInsuranceActivity.Companion.BPI_PRODUCT_GROUP_CODE
import za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.BalanceProtectionInsuranceActivity.Companion.BPI_TERMS_CONDITIONS_HTML
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.Utils

class BPITermsAndConditionFragment : Fragment()  {

    private var productGroupCode: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.bpi_terms_conditions_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let { Utils.updateStatusBarBackground(it, R.color.white) }

        productGroupCode = arguments?.getString(BPI_PRODUCT_GROUP_CODE)

        setUpWebView()

        val htmlContent = arguments?.getString(BPI_TERMS_CONDITIONS_HTML)
        if(htmlContent != null){
            bpiTermsConditionsWebView?.loadData(htmlContent, "text/html; charset=utf-8", null)
        }

        bpiEmailCopyButton?.setOnClickListener{
            emailTermsAndConditions()
        }

        bpiGotItButton?.setOnClickListener {
            (activity as? BalanceProtectionInsuranceActivity)?.onBackPressed()
        }

        bpiRetryButton?.setOnClickListener {
            emailTermsAndConditions()
        }

    }

    private fun hideAllViews() {
        bpiScrollView?.visibility = GONE
        bpiEmailCopyButton?.visibility = GONE
        bpiEmailProcessingInclude?.visibility = GONE
        bpiEmailSuccessInclude?.visibility = GONE
        bpiEmailFailureInclude?.visibility = GONE
    }

    private fun showEmailProcessingView() {
        (activity as? BalanceProtectionInsuranceActivity)?.setToolbarTitle("")
        hideAllViews()
        bpiEmailProcessingInclude?.visibility = VISIBLE
    }

    private fun showEmailSuccessView() {
        (activity as? BalanceProtectionInsuranceActivity)?.apply{
            btnClose?.visibility = GONE
            setToolbarTitle("")
        }
        hideAllViews()
        bpiEmailSuccessInclude?.visibility = VISIBLE
    }

    private fun showEmailFailureView() {
        (activity as? BalanceProtectionInsuranceActivity)?.setToolbarTitle("")
        hideAllViews()
        bpiEmailFailureInclude?.visibility = VISIBLE
    }

    private fun emailTermsAndConditions() {
        productGroupCode?.let { productGroupCode ->
            showEmailProcessingView()
            OneAppService.emailBPITermsAndConditions(productGroupCode).enqueue(
                CompletionHandler(object : IResponseListener<GenericResponse> {
                    override fun onSuccess(response: GenericResponse?) {
                        when(response?.httpCode){
                            AppConstant.HTTP_OK -> {
                                showEmailSuccessView()
                                //TODO: handle response when api starts working
                            }
                            else -> {
                                showEmailFailureView()
                            }
                        }
                    }

                    override fun onFailure(error: Throwable?) {
                        showEmailFailureView()
                    }
                }, GenericResponse::class.java))
        }
    }

    override fun onResume() {
        (activity as? BalanceProtectionInsuranceActivity)?.apply {
            changeActionBarUIForBPITermsConditions()
            if(bpiScrollView?.visibility == VISIBLE){
                setToolbarTitle(R.string.bpi_terms_conditions_title)
            }
            else{
                setToolbarTitle("")
            }
        }
        super.onResume()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setUpWebView(){
        bpiTermsConditionsWebView?.apply {
            with(settings) {
                javaScriptEnabled = true
            }

            webViewClient = object : android.webkit.WebViewClient() {

                override fun onPageFinished(view: android.webkit.WebView?, url: kotlin.String?) {
                    val inputStream: java.io.InputStream = resources.assets.open("fonts/WebViewFontFaceStyle.css")
                    var inputAsString = inputStream.bufferedReader().use { it.readText() }

                    inputAsString = inputAsString.replace("\\s".toRegex(),"")

                    val js = "var style = document.createElement('style'); " +
                            "style.innerHTML = \"$inputAsString\";" +
                            " document.head.appendChild(style);" +
                            "document.querySelectorAll('body').forEach((element) => { element.classList.add('bpi-terms-conditions'); });"
                    bpiTermsConditionsWebView.evaluateJavascript(js,null)

                    super.onPageFinished(view, url)
                }
            }
        }
    }
}