package za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.opt_in

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.bpi_terms_conditions_fragment.*
import za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.BalanceProtectionInsuranceActivity
import za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.BalanceProtectionInsuranceActivity.Companion.BPI_TERMS_CONDITIONS_HTML
import za.co.woolworths.financial.services.android.util.Utils

class BPITermsAndConditionFragment : Fragment()  {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.bpi_terms_conditions_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let { Utils.updateStatusBarBackground(it, R.color.white) }

        setUpWebView()

        val htmlContent = arguments?.getString(BPI_TERMS_CONDITIONS_HTML)
        if(htmlContent != null){
            bpiTermsConditionsWebView?.loadData(htmlContent, "text/html; charset=utf-8", null)
        }
    }

    override fun onResume() {
        (activity as? BalanceProtectionInsuranceActivity)?.apply {
            changeActionBarUIForBPITermsConditions()
            setToolbarTitle(R.string.bpi_terms_conditions_title)
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