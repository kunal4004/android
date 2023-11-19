package za.co.woolworths.financial.services.android.ui.activities.write_a_review.view

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.awfs.coordination.databinding.WriteAReviewTermsAndConditionsBinding
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding

class WriteAReviewTnCFragment : BaseFragmentBinding<WriteAReviewTermsAndConditionsBinding>(
    WriteAReviewTermsAndConditionsBinding::inflate
) {
    private val writeAReviewTnCLink = AppConfigSingleton.enableWriteReviews?.tncLink

    companion object {
        fun newInstance() = WriteAReviewTnCFragment()
        const val ACTION_ITEMS_TnC = AppConstant.actionItemTnC
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.configureUI()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun WriteAReviewTermsAndConditionsBinding.configureUI() {
        writeAReviewTncWebview.apply {
            settings.apply {
                javaScriptEnabled = true
                mediaPlaybackRequiresUserGesture = true
                domStorageEnabled = true
                cacheMode = WebSettings.LOAD_NO_CACHE
                webViewClient = MyWebViewClient()
                binding.writeAReviewProgressbar.visibility = View.VISIBLE
                if (writeAReviewTnCLink != null) {
                    loadUrl(writeAReviewTnCLink)
                }
            }
        }
    }

    private inner class MyWebViewClient : WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
        }

        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            if (writeAReviewTnCLink != null) {
                view?.loadUrl(writeAReviewTnCLink)
            }
            return true
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            binding.writeAReviewProgressbar.visibility = View.GONE
        }
    }
}


