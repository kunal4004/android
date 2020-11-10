package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.add_new_payu_card_fragment.*
import kotlinx.coroutines.GlobalScope
import za.co.woolworths.financial.services.android.models.dto.AddCardResponse
import za.co.woolworths.financial.services.android.ui.extension.doAfterDelay
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.ConnectionBroadcastReceiver
import za.co.woolworths.financial.services.android.util.ErrorHandlerView
import za.co.woolworths.financial.services.android.util.NetworkManager

class PMAAddNewPayUCardFragment : PMAFragment() {

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

        noTitleBarToolbar()
        configureWebView(Navigation.findNavController(view))
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configureWebView(navController: NavController?) {

        with(addNewUserPayUWebView) {

            settings.javaScriptEnabled = true

            addJavascriptInterface(PayUCardFormJavascriptBridge({
                // showProgress
                processYourCard()
                GlobalScope.doAfterDelay(AppConstant.DELAY_100_MS) {
                    displayToolbarBackIcon(false)
                    showWebView(false)
                }
            }, { addCardResponse ->
                // onSuccess
                payMyAccountViewModel.isAddNewCardFormLoaded = false
                GlobalScope.doAfterDelay(AppConstant.DELAY_100_MS) {
                    displayToolbarBackIcon(true)
                    showWebView(false)
                    navigateToSavePayNowFragment(addCardResponse, navController)
                }
            }, {
                // on failure
                payMyAccountViewModel.isAddNewCardFormLoaded = false
                GlobalScope.doAfterDelay(AppConstant.DELAY_100_MS) {
                    displayToolbarBackIcon(true)
                    showWebView(true)
                }

            }, {
                // onPayUFormLoaded
                payMyAccountViewModel.isAddNewCardFormLoaded = NetworkManager.getInstance().isConnectedToNetwork(activity)
                GlobalScope.doAfterDelay(AppConstant.DELAY_100_MS) {
                    displayToolbarBackIcon(true)
                    showWebView(true)
                }
            }), "JSBridge")

            autoConnectionListener()
        }
    }

    private fun processYourCard() {
        processingYourCardFragment?.visibility = VISIBLE
        processYourRequestFragment?.visibility = GONE
    }

    private fun autoConnectionListener() {
        activity?.let { act ->
            ConnectionBroadcastReceiver.registerToFragmentAndAutoUnregister(act, this, object : ConnectionBroadcastReceiver() {
                override fun onConnectionChanged(hasConnection: Boolean) {
                    when (hasConnection && !payMyAccountViewModel.isAddNewCardFormLoaded) {
                        true -> {
                            loadAddPayUForm()
                            addNewUserPayUWebView?.loadUrl(payMyAccountViewModel.getAddNewCardUrl())
                        }
                        else -> {
                            if (!hasConnection)
                                ErrorHandlerView(act).showToast()
                        }
                    }
                }
            })
        }
    }

    private fun loadAddPayUForm() {
        processingYourCardFragment?.visibility = GONE
        processYourRequestFragment?.visibility = VISIBLE
        showWebView(false)
        displayToolbarBackIcon(true)
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
        noTitleBarToolbar()
    }

    override fun onDestroy() {
        super.onDestroy()
        payMyAccountViewModel.isAddNewCardFormLoaded = false
    }
}