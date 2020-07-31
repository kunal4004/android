package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.awfs.coordination.R
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
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountPresenterImpl
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.whatsapp.WhatsAppChatToUs
import za.co.woolworths.financial.services.android.ui.extension.bindColor
import za.co.woolworths.financial.services.android.ui.extension.bindDrawable
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.extension.doAfterDelay
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WhatsAppUnavailableFragment
import za.co.woolworths.financial.services.android.util.ScreenManager
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

class CreditAndDebitCardPaymentsFragment : Fragment(), View.OnClickListener {

    private var payMyAccountPresenter: PayMyAccountPresenterImpl? = null
    private var navController: NavController? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.credit_and_debit_card_payments_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? PayMyAccountActivity)?.apply {
            payMyAccountPresenter = getPayMyAccountPresenter()
            configureToolbar("")
        }

        GlobalScope.doAfterDelay(100) {
            payMyAccountTitleTextView?.visibility = VISIBLE
            payMyAccountDescTextView?.visibility = VISIBLE
            creditDebitCardPaymentsScrollView?.background = bindDrawable(R.drawable.black_white_gradient_bg)
            incDebitOrCreditCardButton?.visibility = VISIBLE
            incByElectronicFundTransferEFTButton?.visibility = VISIBLE
            incAtAnyAbsaBranchButton?.visibility = VISIBLE
            incAtYourNearestWoolworthsStoreButton?.visibility = VISIBLE
            incPayAtAnyATMButton?.visibility = VISIBLE
            incWhatsAppAnyQuestions?.visibility = VISIBLE
            pmaBottomView?.visibility = VISIBLE
        }

        navController = Navigation.findNavController(view)
        configureClickEvent()

        val payMyCardHeaderItem = payMyAccountPresenter?.getPayMyCardCardItem()
        payMyCardHeaderItem?.apply {
            payMyAccountTitleTextView?.text = bindString(title)
            payMyAccountDescTextView?.text = bindString(description)
            pmaCardImageView?.setImageResource(card)
        }
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

    fun setWhatsAppChatWithUsVisibility(isVisible: Boolean) {
        if (isVisible) {
            incWhatsAppAnyQuestions?.visibility = VISIBLE
            // Customer service availability
            if (WhatsAppChatToUs().isCustomerServiceAvailable) {
                whatsAppIconImageView?.setImageResource(R.drawable.icon_whatsapp)
                anyQuestionsTextView?.setTextColor(Color.BLACK)
                chatToUsOnWhatsAppTextView?.setTextColor(Color.BLACK)
                whatsAppNextIconImageView?.alpha = 1f
            } else {
                whatsAppIconImageView?.setImageResource(R.drawable.icon_whatsapp_grey)
                whatsAppNextIconImageView?.alpha = 0.4f
                anyQuestionsTextView?.setTextColor(bindColor(R.color.unavailable))
                chatToUsOnWhatsAppTextView?.setTextColor(bindColor(R.color.unavailable))
            }
        } else {
            incWhatsAppAnyQuestions?.visibility = View.GONE
        }
    }
}