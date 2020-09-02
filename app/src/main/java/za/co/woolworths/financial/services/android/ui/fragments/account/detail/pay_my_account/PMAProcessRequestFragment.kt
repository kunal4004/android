package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account

import android.graphics.Paint
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.awfs.coordination.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.pma_process_detail_layout.*
import kotlinx.android.synthetic.main.processing_request_failure_fragment.*
import za.co.absa.openbankingapi.woolworths.integration.dto.PayUResponse
import za.co.woolworths.financial.services.android.contracts.IGenericAPILoaderView
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountPresenterImpl
import za.co.woolworths.financial.services.android.ui.extension.request
import za.co.woolworths.financial.services.android.ui.fragments.account.PayMyAccountViewModel
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension
import java.lang.Exception
import java.net.ConnectException

class PMAProcessRequestFragment : ProcessYourRequestFragment(), View.OnClickListener {

    private lateinit var cardResponse: String
    private lateinit var accountInfo: String
    private lateinit var paymentMethod: String

    private var menuItem: MenuItem? = null
    private var paymentMethodArgs: MutableList<GetPaymentMethod>? = null
    private var cardDetailArgs: AddCardResponse? = null
    private var accountArgs: Account? = null
    private var navController: NavController? = null
    private var hasPMAPostPayUPayCompleted: Boolean = false
    private var callUsNumber: String? = "0861 50 20 20"

    val payMyAccountViewModel: PayMyAccountViewModel by activityViewModels()
    val args: PMAProcessRequestFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.apply {
            accountInfo = getString(PayMyAccountPresenterImpl.GET_ACCOUNT_INFO, "")
            paymentMethod = getString(PayMyAccountPresenterImpl.GET_PAYMENT_METHOD, "")
            cardResponse = getString(PayMyAccountPresenterImpl.GET_CARD_RESPONSE, "")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setArguments()

        navController = Navigation.findNavController(view)

        circularProgressListener({}, {}) // onSuccess(), onFailure()

        btnRetryProcessPayment?.apply {
            setOnClickListener(this@PMAProcessRequestFragment)
            AnimationUtilExtension.animateViewPushDown(this)
        }

        callCenterNumberTextView?.apply {
            setOnClickListener(this@PMAProcessRequestFragment)
            AnimationUtilExtension.animateViewPushDown(this)
            paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
        }

        autoConnection()
    }

    private fun updateUIOnFailure(desc: String?) {
        menuItem?.isVisible = true
        desc?.apply {
            // keep numeric characters only
            val number = this.replace("[^\\d.]", "")
            if (number.isNotEmpty()) {
                callCenterNumberTextView?.visibility = VISIBLE
                callUsNumber = number
            } else {
                callUsNumber = "0861 50 20 20"
                callCenterNumberTextView?.visibility = GONE
            }
        }
        stopSpinning(false)
        processResultFailureTextView?.text = desc
        includePMAProcessingSuccess?.visibility = GONE
        includePMAProcessingFailure?.visibility = VISIBLE
        includePMAProcessing?.visibility = GONE
    }

    private fun showPMAProcess() {
        menuItem?.isVisible = false
        includePMAProcessingSuccess?.visibility = GONE
        includePMAProcessingFailure?.visibility = GONE
        includePMAProcessing?.visibility = VISIBLE
    }

    private fun autoConnection() {
        activity?.let { activity ->
            ConnectionBroadcastReceiver.registerToFragmentAndAutoUnregister(activity, this, object : ConnectionBroadcastReceiver() {
                override fun onConnectionChanged(hasConnection: Boolean) {
                    when (hasConnection && !hasPMAPostPayUPayCompleted) {
                        true -> postPayUMethod()
                        else -> return
                    }
                }
            })
        }
    }

    private fun setArguments() {
        try {
            accountArgs = args.account
            cardDetailArgs = args.tokenReceivedFromAddCard
        } catch (e: Exception) {
            accountArgs = Gson().fromJson(accountInfo, Account::class.java)
            paymentMethodArgs = Gson().fromJson<MutableList<GetPaymentMethod>>(paymentMethod, object : TypeToken<MutableList<GetPaymentMethod>>() {}.type)
            cardDetailArgs = Gson().fromJson(cardResponse, AddCardResponse::class.java)
        }
    }

    private fun setupToolbar() {
        (activity as? PayMyAccountActivity)?.apply {
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            showPMAProcess()
            configureToolbar("")
            displayToolbarDivider(false)
        }
    }

    private fun postPayUMethod() {
        val payURequestBody = payURequestBody(cardDetailArgs, accountArgs)
        startSpinning()
        request(OneAppService.queryServicePostPayU(payURequestBody), object : IGenericAPILoaderView<Any> {

            override fun onSuccess(response: Any?) {
                if (!isAdded) return
                isAPICallSuccessFul = true
                hasPMAPostPayUPayCompleted = true
                (response as? PayUResponse)?.apply {
                    when (httpCode) {
                        200 -> {
                            stopSpinning(true)
                            navController?.navigate(PMAProcessRequestFragmentDirections.actionPMAProcessRequestFragmentToSecure3DPMAFragment(accountArgs, redirection))
                        }
                        440 -> SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, response.response.stsParams, activity)
                        502 -> {
                            if (response.response.code.startsWith("P0"))
                                response.response.desc?.let { desc -> updateUIOnFailure(desc) }
                            else
                                response.response.desc?.let { desc -> updateUIOnFailure(desc) }
                        }
                        else -> {
                            response.response.desc?.let { desc -> updateUIOnFailure(desc) }
                        }
                    }
                }
            }

            override fun onFailure(error: Throwable?) {
                hasPMAPostPayUPayCompleted = false
                activity?.apply {
                    if (!isAdded) return
                    stopSpinning(false)
                    if (error is ConnectException) {
                        ErrorHandlerView(this).showToast()
                    }
                }
            }
        })
    }

    private fun payURequestBody(cardDetailArgs: AddCardResponse?, accountArgs: Account?): PayUPay {

        val amountEntered = payMyAccountViewModel.getCardDetail()?.amountEnteredInInt() ?: 0

        val creditCardCVV = cardDetailArgs?.card?.cvv ?: ""
        val token = cardDetailArgs?.token ?: "0"
        val type = cardDetailArgs?.card?.type ?: ""
        val isSaveCardChecked = cardDetailArgs?.saveChecked ?: false
        val currency = "ZAR"

        val accountNumber = accountArgs?.accountNumber ?: "0"
        val productOfferingId = accountArgs?.productOfferingId ?: 0
        val paymentMethod = PayUPaymentMethod(token, creditCardCVV, type)

        return PayUPay(amountEntered, currency, productOfferingId, isSaveCardChecked, paymentMethod, accountNumber)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnRetryProcessPayment -> {
                if (NetworkManager.getInstance().isConnectedToNetwork(activity)) {
                    showPMAProcess()
                    postPayUMethod()
                } else {
                    ErrorHandlerView(activity).showToast()
                }
            }
            R.id.callCenterNumberTextView -> {
                Utils.makeCall(callUsNumber)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.close_menu_item, menu)
        menuItem = menu.findItem(R.id.closeIcon)
        menuItem?.isVisible = false
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.closeIcon -> {
                activity?.finish()
                activity?.overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
