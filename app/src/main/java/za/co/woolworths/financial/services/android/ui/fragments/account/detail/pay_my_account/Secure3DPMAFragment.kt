package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.secure_3d_webview_fragment.*

class Secure3DPMAFragment : Fragment() {

    private var navController: NavController? = null

    val args: Secure3DPMAFragmentArgs by navArgs()

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

        val redirection = args.pmaRedirection
        navController = Navigation.findNavController(view)

        secureWebView?.apply {

            with(settings) {
                javaScriptEnabled = true
                domStorageEnabled = true
            }

            loadUrl(redirection?.merchantSiteUrl)
        }

        val response = "{\n" +
                "    \"redirection\": {\n" +
                "        \"created\": \"1596640255430\",\n" +
                "        \"merchantSiteUrl\": \"http://localhost:8080/wfs/app/v4/payments/payu/result?customer=13897641\",\n" +
                "        \"url\": \"https://staging.payu.co.za/merchant/secure3DRedirect.do?PayUReference=17893934651488\"\n" +
                "    },\n" +
                "    \"response\": {\n" +
                "        \"code\": \"-1\",\n" +
                "        \"desc\": \"Success\"\n" +
                "    },\n" +
                "    \"httpCode\": 200\n" +
                "}"


    }
}