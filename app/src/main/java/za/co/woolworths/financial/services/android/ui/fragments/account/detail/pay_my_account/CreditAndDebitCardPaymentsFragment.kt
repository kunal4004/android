package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.awfs.coordination.R
import com.awfs.coordination.databinding.CreditAndDebitCardPaymentsFragmentBinding
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.PMACardPopupModel
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.models.dto.app_config.ConfigPayMyAccount
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountPresenterImpl
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.whatsapp.WhatsAppChatToUs
import za.co.woolworths.financial.services.android.ui.extension.bindDrawable
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.extension.navigateSafelyWithNavController
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatBubbleVisibility
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ui.ChatFloatingActionButtonBubbleView
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WhatsAppUnavailableFragment
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.ScreenManager
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding
import java.net.ConnectException

class CreditAndDebitCardPaymentsFragment :
    BaseFragmentBinding<CreditAndDebitCardPaymentsFragmentBinding>(
        CreditAndDebitCardPaymentsFragmentBinding::inflate
    ), View.OnClickListener {

    private var mChatFloatingActionButtonBubbleView: ChatFloatingActionButtonBubbleView? = null
    private var payMyAccountPresenter: PayMyAccountPresenterImpl? = null
    private var navController: NavController? = null
    private val payMyAccountViewModel: PayMyAccountViewModel by activityViewModels()
    private var payMyAccountOption: ConfigPayMyAccount? = null
    private var isQueryPayUPaymentMethodComplete: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? PayMyAccountActivity)?.apply {
            payMyAccountPresenter = getPayMyAccountPresenter()
            configureToolbar("")
            displayToolbarDivider(false)
            payMyAccountOption = AppConfigSingleton.mPayMyAccount
            createCardOption()
            onRetry()
        }

        binding.incCreditCardButton.creditCardDescTextView.text = KotlinUtils.highlightText(
            bindString(R.string.credit_and_combination_desc),
            mutableListOf("Note:")
        )

        binding.creditDebitCardPaymentsScrollView.background =
            bindDrawable(R.drawable.black_white_gradient_bg)
        binding.pmaBottomView.visibility = VISIBLE

        navController = Navigation.findNavController(view)

        configureClickEvent()

        val payMyCardHeaderItem = payMyAccountPresenter?.getPayMyCardCardItem()
        payMyCardHeaderItem?.apply {
            binding.incStoreCardHeaderItem.apply {
                payMyAccountTitleTextView.text = bindString(title)
                payMyAccountDescTextView.text = bindString(description)
                pmaCardImageView.setImageResource(card)
            }
        }

        setWhatsAppChatWithUsVisibility(payMyAccountPresenter?.getWhatsAppVisibility() ?: false)

        if (payMyAccountViewModel.isAccountChargedOff()) {
            binding.chatCollectAgentFloatingButtonLayout.chatBubbleFloatingButton.visibility = GONE
        } else {
            val account = payMyAccountViewModel.getAccountWithApplyNowState()
            account?.apply { chatToCollectionAgent(first, mutableListOf(second)) }
        }
    }
    private fun setNoteTextsFromConfigIfAvailable() {
        binding.apply {
            AppConfigSingleton.mPayMyAccount?.payByCardFooterNote?.let {
                incDebitCardButton.debitOrCreditCardNoteTextView.text = it
                incCreditCardButton.creditCardNoteTextView.text = it
            }
        }
    }
    private fun onRetry() {
        payMyAccountViewModel.getNavigationResult().observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { result ->
                when (result) {
                    PayMyAccountViewModel.OnNavigateBack.Retry -> {
                        queryServicePaymentMethod()
                    }
                    else -> return@observe
                }
            }
        }
    }

    private fun createCardOption() {
        when (payMyAccountPresenter?.getPayMyAccountSection()) {

            ApplyNowState.STORE_CARD -> {
                binding.incAtAnyAbsaBranchButton.root.visibility = VISIBLE
                binding.incByElectronicFundTransferEFTButton.byElectronicFundTransferDescTextView.text =
                    bindString(R.string.by_electronic_fund_transfer_store_card_desc)
            }
            ApplyNowState.PERSONAL_LOAN -> {
                binding.incSetupMyDebitOrder.root.visibility = GONE
                binding.incAtAnyAbsaBranchButton.root.visibility = VISIBLE
                binding.incByElectronicFundTransferEFTButton.root.visibility = GONE
                binding.incPersonalLoanElectronicFundTransfer.root.visibility = VISIBLE
                binding.incByElectronicFundTransferEFTButton.byElectronicFundTransferDescTextView.text =
                    bindString(R.string.by_electronic_fund_trasfer_personal_loan_desc)
            }
            else -> {
                binding.incAtAnyAbsaBranchButton.root.visibility = VISIBLE
                binding.incByElectronicFundTransferEFTButton.byElectronicFundTransferDescTextView.text =
                    bindString(R.string.by_electronic_fund_transfer_store_card_desc)

                // Hide debit and credit card payment item  when  ABSA cards is null or empty
                if (payMyAccountViewModel.getAccount()?.cards?.isEmpty() == true) {
                    binding.apply {
                        incDebitCardButton.root.visibility = GONE
                        easilyPayYourWooliesAccountTextView.visibility = GONE
                        payYourWooliesAccountTextView.visibility = GONE
                        incCreditCardButton.root.visibility = GONE
                    }
                }
            }
        }

        // Disable payments if isFeatureEnabled is false
        if (payMyAccountOption?.isFeatureEnabled() == false) {
            hidePaymentMethod()
        }
    }

    private fun hidePaymentMethod() {
        binding.apply {
            incSetupMyDebitOrder.root.visibility = GONE
            easilyPayYourWooliesAccountTextView.visibility = GONE
            incDebitCardButton.root.visibility = GONE
            incCreditCardButton.root.visibility = GONE
            payYourWooliesAccountTextView.visibility = VISIBLE
            incAtAnyAbsaBranchButton.root.visibility = VISIBLE
        }

    }

    private fun queryServicePaymentMethod() {
        debitOrCreditButtonIsEnabled(false)
        val cardInfo = payMyAccountViewModel.getCardDetail()
        val account = cardInfo?.account
        val amountEntered = cardInfo?.amountEntered
        val payUMethodType = PayMyAccountViewModel.PAYUMethodType.CREATE_USER
        val paymentMethodList = cardInfo?.paymentMethodList
        val selectedCardPosition = cardInfo?.selectedCardPosition ?: 0
        val card = PMACardPopupModel(
            amountEntered,
            paymentMethodList,
            account,
            payUMethodType,
            selectedCardPosition = selectedCardPosition
        )
        payMyAccountViewModel.setPMACardInfo(card)

        payMyAccountViewModel.queryServicePayUPaymentMethod(
            { // onSuccessResult
                if (!isAdded) return@queryServicePayUPaymentMethod
                isQueryPayUPaymentMethodComplete = true
                debitOrCreditButtonIsEnabled(true)
            }, { onSessionExpired ->
                if (!isAdded) return@queryServicePayUPaymentMethod
                activity?.let {
                    isQueryPayUPaymentMethodComplete = true
                    debitOrCreditButtonIsEnabled(true)
                    SessionUtilities.getInstance()
                        .setSessionState(SessionDao.SESSION_STATE.INACTIVE, onSessionExpired, it)
                }
            }, { // on unknown http error / general error
                if (!isAdded) return@queryServicePayUPaymentMethod
                isQueryPayUPaymentMethodComplete = true

            }, { throwable ->
                if (!isAdded) return@queryServicePayUPaymentMethod
                activity?.runOnUiThread {
                    debitOrCreditButtonIsEnabled(true)
                }
                isQueryPayUPaymentMethodComplete = true
                if (throwable is ConnectException) {
                    isQueryPayUPaymentMethodComplete = false
                }
            })
    }

    private fun debitOrCreditButtonIsEnabled(isEnabled: Boolean) {
        binding.apply {
            incDebitCardButton.payByDebitCardNowButton.isEnabled = isEnabled
            incCreditCardButton.payByCreditCardNowButton.isEnabled = isEnabled
        }
    }

    private fun configureClickEvent() {
        binding.apply {
            setViewListener(incStoreCardHeaderItem.pmaCardImageView)
            setViewListener(incSetupMyDebitOrder.setUpDebitOrderButton)
            setViewListener(incSetupMyDebitOrder.setupMyDebitOrderContainer)
            setViewListener(incDebitCardButton.payByDebitCardNowButton)
            setViewListener(incCreditCardButton.payByCreditCardNowButton)
            setViewListener(incByElectronicFundTransferEFTButton.viewBankingDetailButton)
            setViewListener(incPayAtAnyATMButton.payAtAnyATMButton)
            setViewListener(incAtYourNearestWoolworthsStoreButton.findAWooliesStoreButton)
            setViewListener(incCreditCardButton.root)
            setViewListener(incSetupMyDebitOrder.root)
            setViewListener(incDebitCardButton.root)
            setViewListener(incByElectronicFundTransferEFTButton.root)
            setViewListener(incAtAnyAbsaBranchButton.root)
            setViewListener(incAtYourNearestWoolworthsStoreButton.root)
            setViewListener(incPayAtAnyATMButton.root)
            setViewListener(incWhatsAppAnyQuestions.root)
            setViewListener(incPersonalLoanElectronicFundTransfer.root)
            setViewListener(incPersonalLoanElectronicFundTransfer.plViewBankingDetailButton)
        }
    }

    override fun onClick(v: View?) {
        KotlinUtils.avoidDoubleClicks(v)
        when (v?.id) {
            R.id.incCreditCardButton,
            R.id.payByCreditCardNowButton,
            R.id.incDebitCardButton,
            R.id.payByDebitCardNowButton -> {
                val cardInfo = payMyAccountViewModel.getCardDetail()
                val payUMethodType = cardInfo?.payuMethodType
                activity?.let { payMyAccountPresenter?.setFirebaseEventForPayByCardNow(it) }

                payMyAccountViewModel.resetAmountEnteredToDefault()
                when {
                    (payUMethodType == PayMyAccountViewModel.PAYUMethodType.ERROR) -> {
                        navController?.navigate(R.id.payMyAccountRetryErrorFragment)
                    }
                    (payUMethodType == PayMyAccountViewModel.PAYUMethodType.CREATE_USER) -> {
                        navigateSafelyWithNavController(
                            CreditAndDebitCardPaymentsFragmentDirections.goToEnterPaymentAmountFragmentAction(
                                true
                            )
                        )
                    }

                    (payUMethodType == PayMyAccountViewModel.PAYUMethodType.CARD_UPDATE) -> {
                        navigateSafelyWithNavController(CreditAndDebitCardPaymentsFragmentDirections.actionCreditAndDebitCardPaymentsFragmentToDisplayVendorCardDetailFragment())

                    }
                }
            }

            R.id.findAWooliesStoreButton, R.id.incAtYourNearestWoolworthsStoreButton -> {
                navigateSafelyWithNavController(CreditAndDebitCardPaymentsFragmentDirections.actionCreditAndDebitCardPaymentsFragmentToStoresNearbyFragment1())
            }

            R.id.incPersonalLoanElectronicFundTransfer, R.id.plViewBankingDetailButton, R.id.incByElectronicFundTransferEFTButton, R.id.viewBankingDetailButton -> {
                navigateSafelyWithNavController(CreditAndDebitCardPaymentsFragmentDirections.actionCreditAndDebitCardPaymentsFragmentToByElectronicFundTransferFragment())
            }

            R.id.incPayAtAnyATMButton, R.id.payAtAnyATMButton -> navController?.navigate(R.id.action_creditAndDebitCardPaymentsFragment_to_payMyAccountLearnMoreFragment)

            R.id.incSetupMyDebitOrder, R.id.setUpDebitOrderButton -> {
                navigateSafelyWithNavController(CreditAndDebitCardPaymentsFragmentDirections.actionCreditAndDebitCardPaymentsFragmentToPMAPayByDebitOrderFragment())
            }

            R.id.incWhatsAppAnyQuestions -> {
                if (!WhatsAppChatToUs().isCustomerServiceAvailable) {
                    val whatsAppUnavailableFragment = WhatsAppUnavailableFragment()
                    whatsAppUnavailableFragment.show(
                        childFragmentManager,
                        WhatsAppUnavailableFragment::class.java.simpleName
                    )
                    return
                }
                Utils.triggerFireBaseEvents(
                    FirebaseManagerAnalyticsProperties.WHATSAPP_PAYMENT_OPTION,
                    activity
                )
                ScreenManager.presentWhatsAppChatToUsActivity(
                    activity,
                    WhatsAppChatToUs.FEATURE_WHATSAPP,
                    payMyAccountPresenter?.getAppScreenName()
                )
            }
        }
    }

    private fun setWhatsAppChatWithUsVisibility(isVisible: Boolean) {
        binding.apply {
            if (isVisible) {

                incWhatsAppAnyQuestions.root.visibility = VISIBLE
                // Customer service availability
                incWhatsAppAnyQuestions.apply {
                    if (WhatsAppChatToUs().isCustomerServiceAvailable) {
                        whatsAppIconImageView.setImageResource(R.drawable.icon_whatsapp)
                        anyQuestionsTextView.setTextColor(Color.BLACK)
                        chatToUsOnWhatsAppTextView.setTextColor(Color.BLACK)
                    } else {
                        whatsAppIconImageView.setImageResource(R.drawable.whatsapp_offline)
                        anyQuestionsTextView.setTextColor(Color.BLACK)
                        chatToUsOnWhatsAppTextView.setTextColor(Color.BLACK)
                    }
                    //TODO:: confirm usage and delete
//                    root.whatsAppNextIconImageView?.alpha = 1f

                }

            } else {
                incWhatsAppAnyQuestions.root.visibility = GONE
            }
        }

    }

    private fun setViewListener(view: View?) {
        view?.apply {
            setOnClickListener(this@CreditAndDebitCardPaymentsFragment)
            AnimationUtilExtension.animateViewPushDown(this)
        }
    }

    override fun onResume() {
        super.onResume()
        queryServicePaymentMethod()
        setNoteTextsFromConfigIfAvailable()
    }

    private fun chatToCollectionAgent(
        applyNowState: ApplyNowState,
        accountList: MutableList<Account>?
    ) {
        activity?.apply {
            mChatFloatingActionButtonBubbleView = ChatFloatingActionButtonBubbleView(
                activity = this as? PayMyAccountActivity,
                chatBubbleVisibility = ChatBubbleVisibility(accountList, this),
                floatingActionButton = binding.chatCollectAgentFloatingButtonLayout.chatBubbleFloatingButton,
                applyNowState = applyNowState,
                scrollableView = binding.creditDebitCardPaymentsScrollView,
                notificationBadge = binding.chatCollectAgentFloatingButtonLayout.badge,
                onlineChatImageViewIndicator = binding.chatCollectAgentFloatingButtonLayout.onlineIndicatorImageView,
                vocTriggerEvent = payMyAccountViewModel.getVocTriggerEventPaymentOptions()
            )

            mChatFloatingActionButtonBubbleView?.build()
        }
    }
}