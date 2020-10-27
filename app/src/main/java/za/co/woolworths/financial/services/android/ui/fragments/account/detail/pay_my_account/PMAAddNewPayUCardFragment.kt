package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.add_new_payu_card_fragment.*
import kotlinx.coroutines.GlobalScope
import za.co.woolworths.financial.services.android.models.dto.AddCardResponse
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountActivity
import za.co.woolworths.financial.services.android.ui.extension.doAfterDelay
import za.co.woolworths.financial.services.android.util.AppConstant

class PMAAddNewPayUCardFragment : Fragment() {

    val payMyAccountViewModel: PayMyAccountViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.add_new_payu_card_fragment, container, false)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configureToolbar()
        configureWebView(Navigation.findNavController(view))
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configureWebView(navController: NavController?) {
        with(addNewUserPayUWebView) {

            with(settings) {
                javaScriptEnabled = true
                domStorageEnabled = true
            }

            webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                    return true
                }
            }

            addJavascriptInterface(PayUCardFormJavascriptBridge({
                // showProgress
                GlobalScope.doAfterDelay(AppConstant.DELAY_100_MS) {
                    displayToolbarBackIcon(false)
                    showWebView(false)
                }
            }, { addCardResponse ->
                // onSuccess
                GlobalScope.doAfterDelay(AppConstant.DELAY_100_MS) {
                    displayToolbarBackIcon(true)
                    showWebView(false)
                    navigateToSavePayNowFragment(addCardResponse, navController)
                }
            }, {
                // on failure
                GlobalScope.doAfterDelay(AppConstant.DELAY_100_MS) {
                    displayToolbarBackIcon(true)
                    showWebView(true)
                }

            }), "JSBridge")

            loadUrl(payMyAccountViewModel.getAddNewCardUrl())
        }
    }

    private fun navigateToSavePayNowFragment(addCardResponse: AddCardResponse, navController: NavController?) {
        payMyAccountViewModel.setAddCardResponse(addCardResponse)
        navController?.navigate(PMAAddNewPayUCardFragmentDirections.actionAddNewPayUCardFragmentToSaveCardAndPayNowFragment())
    }


    private fun showWebView(isVisible: Boolean) {
        addNewUserPayUWebView?.visibility = if (isVisible) VISIBLE else GONE
        processCardNavHostLinearLayout?.visibility = if (isVisible) GONE else VISIBLE
    }

    private fun displayToolbarBackIcon(isVisible: Boolean) {
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(isVisible)
    }

    override fun onResume() {
        super.onResume()
        configureToolbar()
    }

    private fun configureToolbar() {
        (activity as? PayMyAccountActivity)?.apply {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            displayToolbarDivider(false)
            configureToolbar("")
        }
    }
}