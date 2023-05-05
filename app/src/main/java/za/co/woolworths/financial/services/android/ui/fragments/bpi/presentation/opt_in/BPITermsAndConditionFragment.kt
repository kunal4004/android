package za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.opt_in

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.util.Base64
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.BpiTermsConditionsFragmentBinding
import com.google.gson.JsonParser
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.dto.account.AccountsProductGroupCode
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.GenericResponse
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.extension.onClick
import za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.BalanceProtectionInsuranceActivity
import za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.BalanceProtectionInsuranceActivity.Companion.BPI_PRODUCT_GROUP_CODE
import za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.carousel.BPIOptInCarouselFragment
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding

class BPITermsAndConditionFragment : BaseFragmentBinding<BpiTermsConditionsFragmentBinding>(BpiTermsConditionsFragmentBinding::inflate)  {

    private var productGroupCode: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let { Utils.updateStatusBarBackground(it, R.color.white) }

        binding.apply {
            setUserEmail()

            hideAllViews()
            showProcessingView()

            productGroupCode = arguments?.getString(BPI_PRODUCT_GROUP_CODE)

            setUpWebView()

            Handler().postDelayed({
                showTermsAndConditionView()
            }, AppConstant.DELAY_3000_MS)


            bpiEmailCopyButton.onClick {
                emailTermsAndConditions()
            }

            bpiEmailSuccessInclude.bpiGotItButton.onClick {
                (activity as? BalanceProtectionInsuranceActivity)?.onBackPressed()
            }

            bpiEmailFailureInclude.bpiRetryButton.onClick {
                emailTermsAndConditions()
            }
        }
    }

    private fun BpiTermsConditionsFragmentBinding.hideAllViews() {
        bpiScrollView.visibility = GONE
        bpiEmailCopyButton.visibility = GONE
        bpiProcessingInclude.root.visibility = GONE
        bpiEmailSuccessInclude.root.visibility = GONE
        bpiEmailFailureInclude.root.visibility = GONE
    }

    private fun BpiTermsConditionsFragmentBinding.showProcessingView() {
        (activity as? BalanceProtectionInsuranceActivity)?.setToolbarTitle("")
        hideAllViews()
        bpiProcessingInclude.root.visibility = VISIBLE
    }

    private fun BpiTermsConditionsFragmentBinding.showTermsAndConditionView() {
        (activity as? BalanceProtectionInsuranceActivity)?.setToolbarTitle(R.string.bpi_terms_conditions_title)
        hideAllViews()
        bpiScrollView?.visibility = VISIBLE
        bpiEmailCopyButton?.visibility = VISIBLE
    }

    private fun BpiTermsConditionsFragmentBinding.showEmailSuccessView() {
        (activity as? BalanceProtectionInsuranceActivity)?.let{ bpiActivity ->
            bpiActivity.binding.btnClose?.visibility = GONE
            bpiActivity.setToolbarTitle("")
        }
        hideAllViews()
        bpiEmailSuccessInclude.root.visibility = VISIBLE
    }

    private fun BpiTermsConditionsFragmentBinding.showEmailFailureView() {
        (activity as? BalanceProtectionInsuranceActivity)?.setToolbarTitle("")
        hideAllViews()
        bpiEmailFailureInclude.root.visibility = VISIBLE
    }

    private fun BpiTermsConditionsFragmentBinding.emailTermsAndConditions() {
        productGroupCode?.let { productGroupCode ->
            var bpiTaggingEventCode: String? = null
            val arguments: MutableMap<String, String> = HashMap()

            when (productGroupCode) {
                AccountsProductGroupCode.CREDIT_CARD.groupCode -> {
                    bpiTaggingEventCode = FirebaseManagerAnalyticsProperties.CC_BPI_OPT_IN_SEND_EMAIL
                    arguments[FirebaseManagerAnalyticsProperties.PropertyNames.ACTION] =
                        FirebaseManagerAnalyticsProperties.PropertyValues.CC_BPI_OPT_IN_SEND_EMAIL_VALUE
                }
                AccountsProductGroupCode.STORE_CARD.groupCode -> {
                    bpiTaggingEventCode = FirebaseManagerAnalyticsProperties.SC_BPI_OPT_IN_SEND_EMAIL
                    arguments[FirebaseManagerAnalyticsProperties.PropertyNames.ACTION] =
                        FirebaseManagerAnalyticsProperties.PropertyValues.SC_BPI_OPT_IN_SEND_EMAIL_VALUE
                }
                AccountsProductGroupCode.PERSONAL_LOAN.groupCode -> {
                    bpiTaggingEventCode = FirebaseManagerAnalyticsProperties.PL_BPI_OPT_IN_SEND_EMAIL
                    arguments[FirebaseManagerAnalyticsProperties.PropertyNames.ACTION] =
                        FirebaseManagerAnalyticsProperties.PropertyValues.PL_BPI_OPT_IN_SEND_EMAIL_VALUE
                }
            }

            bpiTaggingEventCode?.let { Utils.triggerFireBaseEvents(it, arguments, activity) }

            showProcessingView()
            OneAppService().emailBPITermsAndConditions(productGroupCode).enqueue(
                CompletionHandler(object : IResponseListener<GenericResponse> {
                    override fun onSuccess(response: GenericResponse?) {
                        when(response?.httpCode){
                            AppConstant.HTTP_OK -> {
                                showEmailSuccessView()
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
        (activity as? BalanceProtectionInsuranceActivity)?.let { bpiActivity ->
            bpiActivity.changeActionBarUIForBPITermsConditions()
            if(binding.bpiScrollView?.visibility == VISIBLE){
                bpiActivity.setToolbarTitle(R.string.bpi_terms_conditions_title)
            }
            else{
                bpiActivity.setToolbarTitle("")
            }
        }
        super.onResume()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun BpiTermsConditionsFragmentBinding.setUpWebView(){
        bpiTermsConditionsWebView?.apply {
            with(settings) {
                javaScriptEnabled = true
                allowContentAccess = true
                domStorageEnabled = true
                cacheMode = WebSettings.LOAD_NO_CACHE
                mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            }

            webViewClient = object : android.webkit.WebViewClient() {

                override fun onPageFinished(view: WebView?, url: String?) {
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

                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?): Boolean {
                    var url = request?.url.toString()
                    if (url.contains("mailto:")) {
                        url.split(":").let {
                            if(it.size>1){
                                url = it[1]
                            }
                        }
                        KotlinUtils.sendEmail(activity, url, "BPI Terms and Conditions" )
                    }
                    return true
                }
            }
        }

        BPIOptInCarouselFragment.htmlContent?.termsAndConditionsHtml?.let {
            bpiTermsConditionsWebView?.loadData(it,"text/html; charset=utf-8", null)
            bpiTermsConditionsWebView?.refreshDrawableState()
        }
    }

    private fun BpiTermsConditionsFragmentBinding.setUserEmail() {
        var email = ""
        val splitToken = OneAppService().getSessionToken().split(".")
        if(splitToken.size > 1){
            val decodedBytes = Base64.decode(splitToken[1], Base64.DEFAULT)
            email = JsonParser.parseString(String(decodedBytes)).asJsonObject["email"].asString
        }
        bpiEmailSuccessInclude.mainDescriptionSuccessTextview?.text = bindString(R.string.bpi_sent_email_success, email)
        bpiEmailFailureInclude.mainDescriptionFailureTextview?.text = bindString(R.string.bpi_sent_email_failure, email)
    }
}