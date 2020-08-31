package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.awfs.coordination.R
import com.facebook.shimmer.Shimmer
import com.google.gson.Gson
import kotlinx.android.synthetic.main.card_payment_option_header_item.*
import kotlinx.android.synthetic.main.credit_and_debit_card_payments_fragment.*
import kotlinx.android.synthetic.main.debit_or_credit_card_item.*
import kotlinx.android.synthetic.main.payment_options_activity.*
import kotlinx.android.synthetic.main.payment_options_activity.whatsAppIconImageView
import kotlinx.android.synthetic.main.pma_at_your_nearest_woolies_store_item.*
import kotlinx.android.synthetic.main.pma_by_electronic_fund_transfer_eft_item.*
import kotlinx.android.synthetic.main.pma_pay_at_any_atm.*
import kotlinx.android.synthetic.main.pma_whatsapp_chat_with_us.*
import kotlinx.coroutines.GlobalScope
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.PayMyAccount
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountPresenterImpl
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.whatsapp.WhatsAppChatToUs
import za.co.woolworths.financial.services.android.ui.extension.bindDrawable
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.extension.doAfterDelay
import za.co.woolworths.financial.services.android.ui.fragments.account.PayMyAccountViewModel
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WhatsAppUnavailableFragment
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

class CreditAndDebitCardPaymentsFragment : Fragment(), View.OnClickListener {

    private var payMyAccountPresenter: PayMyAccountPresenterImpl? = null
    private var navController: NavController? = null
    private var layout: View? = null
    private val payMyAccountViewModel: PayMyAccountViewModel by activityViewModels()
    private var payMyAccountOption: PayMyAccount? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Prevent layout to reload when fragment refresh
        if (layout == null)
            layout = inflater.inflate(R.layout.credit_and_debit_card_payments_fragment, container, false)
        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? PayMyAccountActivity)?.apply {
            payMyAccountPresenter = getPayMyAccountPresenter()
            configureToolbar("")
            displayToolbarDivider(false)

            payMyAccountOption = WoolworthsApplication.getPayMyAccountOption()
            val isFeatureEnabled = payMyAccountOption?.isFeatureEnabled() == false
            val isCreditCardSection = when (payMyAccountPresenter?.getPayMyAccountSection()) {
                ApplyNowState.SILVER_CREDIT_CARD, ApplyNowState.GOLD_CREDIT_CARD, ApplyNowState.BLACK_CREDIT_CARD -> true
                else -> false
            }

            GlobalScope.doAfterDelay(10) {
                incDebitOrCreditCardButton?.visibility = when {
                    isFeatureEnabled || isCreditCardSection -> GONE
                    else -> VISIBLE
                }
            }
        }

        creditDebitCardPaymentsScrollView?.background = bindDrawable(R.drawable.black_white_gradient_bg)
        pmaBottomView?.visibility = VISIBLE

        navController = Navigation.findNavController(view)

        configureClickEvent()

        val payMyCardHeaderItem = payMyAccountPresenter?.getPayMyCardCardItem()
        payMyCardHeaderItem?.apply {
            payMyAccountTitleTextView?.text = bindString(title)
            payMyAccountDescTextView?.text = bindString(description)
            pmaCardImageView?.setImageResource(card)
        }

        setWhatsAppChatWithUsVisibility(payMyAccountPresenter?.getWhatsAppVisibility() ?: false)

        activity?.let { act ->
            ConnectionBroadcastReceiver.registerToFragmentAndAutoUnregister(act, this, object : ConnectionBroadcastReceiver() {
                override fun onConnectionChanged(hasConnection: Boolean) {
                    when (hasConnection) {
                        true -> {
                            if (payMyAccountViewModel.getPaymentMethodList()?.isNotEmpty() == true || payMyAccountOption?.isFeatureEnabled() == false) return

                            initShimmer()
                            startProgress()
                            payMyAccountViewModel.queryServiceGetPaymentMethod({ paymentMethodsResponse ->
                                with(paymentMethodsResponse) {
                                    when (httpCode) {
                                        200 -> {
                                            payMyAccountViewModel.setPaymentMethodsResponse(paymentMethodsResponse)
                                        }
                                        440 -> SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, response.stsParams
                                                ?: "", activity)

                                        else -> {
                                            activity?.let {
                                                Utils.showGeneralErrorDialog(it, response.desc
                                                        ?: "")
                                            }
                                        }
                                    }
                                    initShimmer()
                                    stopProgress()
                                }
                            }, { throwable ->
                                stopProgress()
                            })
                        }
                        else -> {
                            ErrorHandlerView(act).showToast()
                        }
                    }
                }
            })
        }

        initShimmer()
        stopProgress()
    }

    private fun configureClickEvent() {
        pmaCardImageView?.apply {
            setOnClickListener(this@CreditAndDebitCardPaymentsFragment)
            AnimationUtilExtension.animateViewPushDown(this)
        }

        incDebitOrCreditCardButton?.apply {
            setOnClickListener(this@CreditAndDebitCardPaymentsFragment)
            AnimationUtilExtension.animateViewPushDown(this)
        }

        incByElectronicFundTransferEFTButton?.apply {
            setOnClickListener(this@CreditAndDebitCardPaymentsFragment)
            AnimationUtilExtension.animateViewPushDown(this)
        }

        incAtAnyAbsaBranchButton?.apply {
            setOnClickListener(this@CreditAndDebitCardPaymentsFragment)
            AnimationUtilExtension.animateViewPushDown(this)
        }

        incAtYourNearestWoolworthsStoreButton?.apply {
            setOnClickListener(this@CreditAndDebitCardPaymentsFragment)
            AnimationUtilExtension.animateViewPushDown(this)
        }

        incPayAtAnyATMButton?.apply {
            setOnClickListener(this@CreditAndDebitCardPaymentsFragment)
            AnimationUtilExtension.animateViewPushDown(this)
        }

        incWhatsAppAnyQuestions?.apply {
            setOnClickListener(this@CreditAndDebitCardPaymentsFragment)
            AnimationUtilExtension.animateViewPushDown(this)
        }

        payByCardNowButton?.apply {
            setOnClickListener(this@CreditAndDebitCardPaymentsFragment)
            AnimationUtilExtension.animateViewPushDown(this)
        }

        viewBankingDetailButton?.apply {
            setOnClickListener(this@CreditAndDebitCardPaymentsFragment)
            AnimationUtilExtension.animateViewPushDown(this)
        }

        payAtAnyATMButton?.apply {
            setOnClickListener(this@CreditAndDebitCardPaymentsFragment)
            AnimationUtilExtension.animateViewPushDown(this)
        }

        findAWooliesStoreButton?.apply {
            setOnClickListener(this@CreditAndDebitCardPaymentsFragment)
            AnimationUtilExtension.animateViewPushDown(this)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.incDebitOrCreditCardButton, R.id.payByCardNowButton -> {

                val payMyAccountOption = WoolworthsApplication.getPayMyAccountOption()
                val payUMethodType = payMyAccountViewModel.getPaymentMethodType()
                val isFeatureEnabled = payMyAccountOption.isFeatureEnabled()

                val paymentMethod = Gson().toJson(payMyAccountViewModel.getPaymentMethodList())

                when {

                    (payUMethodType == PayMyAccountViewModel.PAYUMethodType.CREATE_USER) && isFeatureEnabled -> {
                        val account = payMyAccountPresenter?.getAccount() ?: Account()
                        val toEnterPaymentAmountDirection = CreditAndDebitCardPaymentsFragmentDirections.goToEnterPaymentAmountFragmentAction(account, true)
                        navController?.navigate(toEnterPaymentAmountDirection)
                    }
                    (payUMethodType == PayMyAccountViewModel.PAYUMethodType.CARD_UPDATE) && isFeatureEnabled -> {
                        val account = Gson().toJson(payMyAccountPresenter?.getAccount()
                                ?: Account())
                        val toDisplayCard = CreditAndDebitCardPaymentsFragmentDirections.actionCreditAndDebitCardPaymentsFragmentToDisplayVendorCardDetailFragment(paymentMethod, account)
                        navController?.navigate(toDisplayCard)
                    }
                    else -> return
                }
            }

            R.id.findAWooliesStoreButton, R.id.incAtYourNearestWoolworthsStoreButton -> {
                navController?.navigate(R.id.action_creditAndDebitCardPaymentsFragment_to_storesNearbyFragment1)
            }

            R.id.incByElectronicFundTransferEFTButton, R.id.viewBankingDetailButton -> {
                navController?.navigate(R.id.action_creditAndDebitCardPaymentsFragment_to_byElectronicFundTransferFragment)
            }

            R.id.incPayAtAnyATMButton, R.id.payAtAnyATMButton -> navController?.navigate(R.id.action_creditAndDebitCardPaymentsFragment_to_payMyAccountLearnMoreFragment)

            R.id.incWhatsAppAnyQuestions -> {
                if (!WhatsAppChatToUs().isCustomerServiceAvailable) {
                    val whatsAppUnavailableFragment = WhatsAppUnavailableFragment()
                    whatsAppUnavailableFragment.show(childFragmentManager, WhatsAppUnavailableFragment::class.java.simpleName)
                    return
                }
                Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.WHATSAPP_PAYMENT_OPTION)
                ScreenManager.presentWhatsAppChatToUsActivity(activity, WhatsAppChatToUs.FEATURE_WHATSAPP, payMyAccountPresenter?.getAppScreenName())
            }
        }
    }

    private fun setWhatsAppChatWithUsVisibility(isVisible: Boolean) {
        if (isVisible) {
            incWhatsAppAnyQuestions?.visibility = VISIBLE
            // Customer service availability
            if (WhatsAppChatToUs().isCustomerServiceAvailable) {
                whatsAppIconImageView?.setImageResource(R.drawable.icon_whatsapp)
                anyQuestionsTextView?.setTextColor(Color.BLACK)
                chatToUsOnWhatsAppTextView?.setTextColor(Color.BLACK)
                whatsAppNextIconImageView?.alpha = 1f
            } else {
                whatsAppIconImageView?.setImageResource(R.drawable.whatsapp_offline)
                whatsAppNextIconImageView?.alpha = 1.0f
                anyQuestionsTextView?.setTextColor(Color.BLACK)
                chatToUsOnWhatsAppTextView?.setTextColor(Color.BLACK)
            }
        } else {
            incWhatsAppAnyQuestions?.visibility = GONE
        }
    }

    fun initShimmer() {
        val shimmer = Shimmer.AlphaHighlightBuilder().build()
        payByCardNowButtonShimmerLayout?.setShimmer(shimmer)
    }

    fun startProgress() {
        payByCardNowButtonShimmerLayout?.startShimmer()
    }

    private fun stopProgress() {
        payByCardNowButtonShimmerLayout?.setShimmer(null)
        payByCardNowButtonShimmerLayout?.stopShimmer()
    }
}