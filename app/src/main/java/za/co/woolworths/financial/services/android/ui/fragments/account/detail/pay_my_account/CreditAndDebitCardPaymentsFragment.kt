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
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.gson.Gson
import kotlinx.android.synthetic.main.card_payment_option_header_item.*
import kotlinx.android.synthetic.main.credit_and_debit_card_payments_fragment.*
import kotlinx.android.synthetic.main.payment_options_activity.*
import kotlinx.android.synthetic.main.payment_options_activity.whatsAppIconImageView
import kotlinx.android.synthetic.main.pma_at_your_nearest_woolies_store_item.*
import kotlinx.android.synthetic.main.pma_by_electronic_fund_transfer_eft_item.*
import kotlinx.android.synthetic.main.pma_by_electronic_fund_transfer_eft_item.byElectronicFundTransferDescTextView
import kotlinx.android.synthetic.main.pma_credit_card_item.*
import kotlinx.android.synthetic.main.pma_debit_card_item.*
import kotlinx.android.synthetic.main.pma_pay_at_any_atm.*
import kotlinx.android.synthetic.main.pma_pay_by_debit_order_item.*
import kotlinx.android.synthetic.main.pma_personal_loan_electronic_fund_transfer.*
import kotlinx.android.synthetic.main.pma_whatsapp_chat_with_us.*
import kotlinx.coroutines.GlobalScope
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.PayMyAccount
import za.co.woolworths.financial.services.android.models.dto.PaymentAmountCard
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
import java.net.ConnectException

class CreditAndDebitCardPaymentsFragment : Fragment(), View.OnClickListener {

    private var payMyAccountPresenter: PayMyAccountPresenterImpl? = null
    private var navController: NavController? = null
    private var layout: View? = null
    private val payMyAccountViewModel: PayMyAccountViewModel by activityViewModels()
    private var payMyAccountOption: PayMyAccount? = null
    private var isQueryPayUPaymentMethodComplete: Boolean = false

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
            createCardOption()
            onRetry()
        }

        creditCardDescTextView?.text = KotlinUtils.highlightText(bindString(R.string.pay_by_credit_card_desc), "Note:")

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

        initShimmer()
        stopProgress()
    }

    private fun onRetry() {
        payMyAccountViewModel.getNavigationResult().observe(viewLifecycleOwner) { result ->
            when (result) {
                PayMyAccountViewModel.OnBackNavigation.RETRY -> {
                    queryServicePaymentMethod()
                }
                else -> return@observe
            }
        }
    }

    private fun createCardOption() {
        when (payMyAccountPresenter?.getPayMyAccountSection()) {
            ApplyNowState.SILVER_CREDIT_CARD, ApplyNowState.GOLD_CREDIT_CARD, ApplyNowState.BLACK_CREDIT_CARD -> {
                hidePaymentMethod()
            }
            ApplyNowState.STORE_CARD -> {
                incAtAnyAbsaBranchButton?.visibility = VISIBLE
                byElectronicFundTransferDescTextView?.text = bindString(R.string.by_electronic_fund_transfer_store_card_desc)
            }
            ApplyNowState.PERSONAL_LOAN -> {
                incSetupMyDebitOrder?.visibility = GONE
                incAtAnyAbsaBranchButton?.visibility = VISIBLE
                incByElectronicFundTransferEFTButton?.visibility = GONE
                incPersonalLoanElectronicFundTransfer?.visibility = VISIBLE
                byElectronicFundTransferDescTextView?.text = bindString(R.string.by_electronic_fund_trasfer_personal_loan_desc)
            }
        }

        // Disable payments if isFeatureEnabled is false
        if (payMyAccountOption?.isFeatureEnabled() == false) {
            hidePaymentMethod()
        }
    }

    private fun hidePaymentMethod() {
        incSetupMyDebitOrder?.visibility = GONE
        easilyPayYourWooliesAccountTextView?.visibility = GONE
        incDebitCardButton?.visibility = GONE
        incCreditCardButton?.visibility = GONE
        payYourWooliesAccountTextView?.visibility = VISIBLE
        incAtAnyAbsaBranchButton?.visibility = VISIBLE
    }

    private fun queryServicePaymentMethod() {
        initShimmer()
        startProgress()

        val cardInfo = payMyAccountViewModel.getCardDetail()
        val account = cardInfo?.account
        val amountEntered = account?.second?.totalAmountDue?.let { amountDue -> Utils.removeNegativeSymbol(WFormatter.newAmountFormat(amountDue)) }
        val payUMethodType = PayMyAccountViewModel.PAYUMethodType.CREATE_USER
        val paymentMethodList = cardInfo?.paymentMethodList

        val card = PaymentAmountCard(amountEntered, paymentMethodList, account, payUMethodType)
        payMyAccountViewModel.setPMACardInfo(card)

        payMyAccountViewModel.queryServicePayUPaymentMethod(
                { // onSuccessResult
                    if (!isAdded) return@queryServicePayUPaymentMethod
                    stopProgress()
                    isQueryPayUPaymentMethodComplete = true

                }, { onSessionExpired ->
            if (!isAdded) return@queryServicePayUPaymentMethod
            activity?.let {
                stopProgress()
                isQueryPayUPaymentMethodComplete = true
                SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, onSessionExpired, it)

            }
        }, { // on unknown http error / general error
            if (!isAdded) return@queryServicePayUPaymentMethod
            stopProgress()
            isQueryPayUPaymentMethodComplete = true

        }, { throwable ->
            if (!isAdded) return@queryServicePayUPaymentMethod
            activity?.runOnUiThread {
                stopProgress()
            }
            isQueryPayUPaymentMethodComplete = true
            if (throwable is ConnectException) {
                isQueryPayUPaymentMethodComplete = false
            }
        })
    }

    private fun initShimmer() {
        initShimmer(payByDebitCardNowButtonShimmerLayout)
        initShimmer(payByCreditCardNowButtonShimmerLayout)
    }

    private fun startProgress() {
        startProgress(payByDebitCardNowButtonShimmerLayout)
        startProgress(payByCreditCardNowButtonShimmerLayout)
    }

    private fun stopProgress() {
        stopProgress(payByDebitCardNowButtonShimmerLayout)
        stopProgress(payByCreditCardNowButtonShimmerLayout)
    }

    private fun configureClickEvent() {
        setViewListener(pmaCardImageView)
        setViewListener(setUpDebitOrderButton)
        setViewListener(setupMyDebitOrderContainer)
        setViewListener(payByDebitCardNowButton)
        setViewListener(payByCreditCardNowButton)
        setViewListener(viewBankingDetailButton)
        setViewListener(payAtAnyATMButton)
        setViewListener(findAWooliesStoreButton)
        setViewListener(incCreditCardButton)
        setViewListener(incSetupMyDebitOrder)
        setViewListener(incDebitCardButton)
        setViewListener(incByElectronicFundTransferEFTButton)
        setViewListener(incAtAnyAbsaBranchButton)
        setViewListener(incAtYourNearestWoolworthsStoreButton)
        setViewListener(incPayAtAnyATMButton)
        setViewListener(incWhatsAppAnyQuestions)
        setViewListener(incPersonalLoanElectronicFundTransfer)
        setViewListener(plViewBankingDetailButton)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.incCreditCardButton,
            R.id.payByCreditCardNowButton,
            R.id.incDebitCardButton,
            R.id.payByDebitCardNowButton -> {
                if (payByDebitCardNowButtonShimmerLayout?.isShimmerStarted == true) return

                val cardInfo = payMyAccountViewModel.getCardDetail()
                val payMyAccountOption: PayMyAccount? = WoolworthsApplication.getPayMyAccountOption()
                val payUMethodType = cardInfo?.payuMethodType
                val isFeatureEnabled = payMyAccountOption?.isFeatureEnabled() ?: false
                val paymentMethod = Gson().toJson(cardInfo?.paymentMethodList)
                payMyAccountPresenter?.setFirebaseEventForPayByCardNow()

                when {
                    (payUMethodType == PayMyAccountViewModel.PAYUMethodType.ERROR) -> {
                        navController?.navigate(R.id.payMyAccountRetryErrorFragment)
                    }
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
                }
            }

            R.id.findAWooliesStoreButton, R.id.incAtYourNearestWoolworthsStoreButton -> {
                navController?.navigate(R.id.action_creditAndDebitCardPaymentsFragment_to_storesNearbyFragment1)
            }

            R.id.incPersonalLoanElectronicFundTransfer, R.id.plViewBankingDetailButton, R.id.incByElectronicFundTransferEFTButton, R.id.viewBankingDetailButton -> {
                navController?.navigate(R.id.action_creditAndDebitCardPaymentsFragment_to_byElectronicFundTransferFragment)
            }

            R.id.incPayAtAnyATMButton, R.id.payAtAnyATMButton -> navController?.navigate(R.id.action_creditAndDebitCardPaymentsFragment_to_payMyAccountLearnMoreFragment)

            R.id.incSetupMyDebitOrder, R.id.setUpDebitOrderButton -> {
                GlobalScope.doAfterDelay(200) {
                    navController?.navigate(R.id.action_creditAndDebitCardPaymentsFragment_to_PMAPayByDebitOrderFragment)
                }
            }

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

    private fun setViewListener(view: View?) {
        view?.apply {
            setOnClickListener(this@CreditAndDebitCardPaymentsFragment)
            AnimationUtilExtension.animateViewPushDown(this)
        }
    }

    private fun initShimmer(view: ShimmerFrameLayout?) {
        val shimmer = Shimmer.AlphaHighlightBuilder().build()
        view?.setShimmer(shimmer)
    }

    private fun startProgress(view: ShimmerFrameLayout?) {
        view?.startShimmer()
    }

    private fun stopProgress(view: ShimmerFrameLayout?) {
        view?.setShimmer(null)
        view?.stopShimmer()
    }
}