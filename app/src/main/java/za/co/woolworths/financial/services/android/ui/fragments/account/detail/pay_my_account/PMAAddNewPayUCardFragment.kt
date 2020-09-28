package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.add_new_payu_card_fragment.*
import kotlinx.coroutines.GlobalScope
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountActivity
import za.co.woolworths.financial.services.android.ui.extension.doAfterDelay
import za.co.woolworths.financial.services.android.ui.fragments.account.PayMyAccountViewModel

class PMAAddNewPayUCardFragment : Fragment() {

    private var navController: NavController? = null

    val payMyAccountViewModel : PayMyAccountViewModel by activityViewModels()

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

        navController = Navigation.findNavController(view)

        configureToolbar()
        configureWebView()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configureWebView() {
        with(addNewUserPayUWebView) {

            with(settings) {
                javaScriptEnabled = true
                domStorageEnabled = true
            }

            addJavascriptInterface(PayUCardFormJavascriptBridge({
                // showProgress
                GlobalScope.doAfterDelay(100) {
                    (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
                    addNewUserPayUWebView?.visibility = GONE
                    processCardNavHostLinearLayout?.visibility = VISIBLE
                }
            }, { addCardResponse ->
                // onSuccess
                GlobalScope.doAfterDelay(100) {
                    (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
                    addNewUserPayUWebView?.visibility = GONE
                    processCardNavHostLinearLayout?.visibility = VISIBLE
                    val navigateToSaveCardAndPayNow = PMAAddNewPayUCardFragmentDirections.actionAddNewPayUCardFragmentToSaveCardAndPayNowFragment(addCardResponse)
                    navController?.navigate(navigateToSaveCardAndPayNow)
                }
            }, {
                // on failure
                GlobalScope.doAfterDelay(100) {
                    (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
                    addNewUserPayUWebView?.visibility = VISIBLE
                    processCardNavHostLinearLayout?.visibility = GONE
                }

            }), "JSBridge")

            val productGroupCode = payMyAccountViewModel.getProductGroupCode()
            WoolworthsApplication.getPayMyAccountOption()?.addCardUrl(productGroupCode)?.let { cardUrl -> loadUrl(cardUrl) }
        }
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