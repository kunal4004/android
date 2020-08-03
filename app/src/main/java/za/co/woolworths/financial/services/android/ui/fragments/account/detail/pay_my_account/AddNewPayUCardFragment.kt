package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.add_new_payu_card_fragment.*


class AddNewPayUCardFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.add_new_payu_card_fragment, container, false)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addNewUserPayUWebView?.apply {
            val webSettings: WebSettings = settings
            webSettings.javaScriptCanOpenWindowsAutomatically = true
            webSettings.javaScriptEnabled = true
            loadUrl("https://qa.d1nnludhatueui.amplifyapp.com/")

        }
    }
}