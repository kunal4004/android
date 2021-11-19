package za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.opt_in

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.bpi_more_info_fragment.*
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.navigation.findNavController
import za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.BalanceProtectionInsuranceActivity.Companion.BPI_MORE_INFO_HTML
import za.co.woolworths.financial.services.android.util.Utils
import java.io.InputStream


class BPIMoreInfoFragment : Fragment()  {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.bpi_more_info_fragment, container, false)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.let { Utils.updateStatusBarBackground(it, R.color.white) }
        bpiMoreInfoWebView?.apply {
            with(settings) {
                javaScriptEnabled = true
            }

            webViewClient = object : WebViewClient() {

                override fun onPageFinished(view: WebView?, url: String?) {
                    val inputStream: InputStream = resources.assets.open("fonts/WebViewFontFaceStyle.css")
                    var inputAsString = inputStream.bufferedReader().use { it.readText() }

                    inputAsString = inputAsString.replace("\\s".toRegex(),"")

                    val js = "var style = document.createElement('style'); " +
                            "style.innerHTML = \"$inputAsString\";" +
                            " document.head.appendChild(style);" +
                            "document.querySelectorAll('body').forEach((element) => { element.classList.add('bpi-notes'); });"
                    bpiMoreInfoWebView.evaluateJavascript(js,null)

                    super.onPageFinished(view, url)
                }
            }
        }

        val htmlContent = arguments?.getString(BPI_MORE_INFO_HTML)
        if(htmlContent != null){
            bpiMoreInfoWebView?.loadData(htmlContent, "text/html; charset=utf-8", null)
        }

        bpiCheckBox?.setOnClickListener {
            it.isSelected = !it.isSelected
            optInBpiButton.isEnabled = it.isSelected
        }

        optInBpiButton?.setOnClickListener{
            view.findNavController().navigate(R.id.action_BPIMoreInfoFragment_to_BPIOptInConfirmationFragment)
        }

        bpiCheckBoxDescriptionTextView?.setOnClickListener{
            //TODO : WOP-12811 - Android: Implement BPI Terms & Conditions flow
            // load arguments?.getString(BPI_TERMS_CONDITIONS_HTML)
        }
    }

    override fun onResume() {
        bpiCheckBox?.isChecked = optInBpiButton.isEnabled
        bpiCheckBox?.isSelected = optInBpiButton.isEnabled
        super.onResume()
    }
}