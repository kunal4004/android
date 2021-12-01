package za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.opt_in

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import com.google.gson.JsonParser
import com.huawei.hms.support.log.common.Base64
import kotlinx.android.synthetic.main.balance_protection_insurance_activity.*
import kotlinx.android.synthetic.main.bpi_email_sent_failure_layout.*
import kotlinx.android.synthetic.main.bpi_email_sent_success_layout.*
import kotlinx.android.synthetic.main.bpi_more_info_fragment.*
import kotlinx.android.synthetic.main.bpi_terms_conditions_fragment.*
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
import java.util.HashMap

class BPITermsAndConditionFragment : Fragment()  {

    private var productGroupCode: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.bpi_terms_conditions_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let { Utils.updateStatusBarBackground(it, R.color.white) }

        setUserEmail()

        hideAllViews()
        showProcessingView()

        productGroupCode = arguments?.getString(BPI_PRODUCT_GROUP_CODE)

        setUpWebView()

        Handler().postDelayed({
            showTermsAndConditionView()
        }, AppConstant.DELAY_3000_MS)


        bpiEmailCopyButton?.onClick {
                emailTermsAndConditions()
        }

        bpiGotItButton?.onClick {
            (activity as? BalanceProtectionInsuranceActivity)?.onBackPressed()
        }

        bpiRetryButton?.onClick {
            emailTermsAndConditions()
        }

    }

    private fun hideAllViews() {
        bpiScrollView?.visibility = GONE
        bpiEmailCopyButton?.visibility = GONE
        bpiProcessingInclude?.visibility = GONE
        bpiEmailSuccessInclude?.visibility = GONE
        bpiEmailFailureInclude?.visibility = GONE
    }

    private fun showProcessingView() {
        (activity as? BalanceProtectionInsuranceActivity)?.setToolbarTitle("")
        hideAllViews()
        bpiProcessingInclude?.visibility = VISIBLE
    }

    private fun showTermsAndConditionView() {
        (activity as? BalanceProtectionInsuranceActivity)?.setToolbarTitle(R.string.bpi_terms_conditions_title)
        hideAllViews()
        bpiScrollView?.visibility = VISIBLE
        bpiEmailCopyButton?.visibility = VISIBLE
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
            OneAppService.emailBPITermsAndConditions(productGroupCode).enqueue(
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

    private fun setUserEmail() {
        var email = ""
        val splitToken = OneAppService.getSessionToken().split(".")
        if(splitToken.size > 1){
            val decodedBytes = Base64.decode(splitToken[1])
            email = JsonParser.parseString(String(decodedBytes)).asJsonObject["email"].asString
        }
        mainDescriptionSuccessTextview?.text = bindString(R.string.bpi_sent_email_success, email)
        mainDescriptionFailureTextview?.text = bindString(R.string.bpi_sent_email_failure, email)
    }
}