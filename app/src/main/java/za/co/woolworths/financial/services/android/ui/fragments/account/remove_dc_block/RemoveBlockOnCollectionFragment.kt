package za.co.woolworths.financial.services.android.ui.fragments.account.remove_dc_block

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.method.LinkMovementMethod
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.awfs.coordination.R
import com.google.gson.Gson
import kotlinx.android.synthetic.main.account_available_fund_overview_fragment.*
import kotlinx.android.synthetic.main.account_in_arrears_layout.*
import kotlinx.android.synthetic.main.remove_block_dc_fragment.*
import kotlinx.android.synthetic.main.remove_block_dc_fragment.incPayMyAccountButton
import kotlinx.android.synthetic.main.remove_block_dc_fragment.incRecentTransactionButton
import kotlinx.android.synthetic.main.remove_block_dc_fragment.incViewStatementButton
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.ui.activities.StatementActivity
import za.co.woolworths.financial.services.android.ui.activities.WTransactionsActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInPresenterImpl
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.information.CardInformationHelpActivity
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatExtensionFragment
import za.co.woolworths.financial.services.android.util.CurrencyFormatter
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension
import za.co.woolworths.financial.services.android.util.spannable.WSpannableStringBuilder
import za.co.woolworths.financial.services.android.util.wenum.LinkType
import java.util.*

class RemoveBlockOnCollectionFragment : Fragment(), View.OnClickListener {

    private var accountData: Pair<ApplyNowState, Account>? = null
    private var mAccountPresenter: AccountSignedInPresenterImpl? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.remove_block_dc_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAccountPresenter = (activity as? AccountSignedInActivity)?.mAccountSignedInPresenter
        accountData = mAccountPresenter?.getMyAccountCardInfo()

        when (accountData?.first) {
            ApplyNowState.PERSONAL_LOAN -> {
                removeBlockBackgroundConstraintLayout?.setBackgroundResource(R.drawable.store_card_background)
            }
            ApplyNowState.STORE_CARD -> {
                removeBlockBackgroundConstraintLayout?.setBackgroundResource(R.drawable.store_card_background)
            }
            else -> {
            }
        }

        val account = accountData?.second
        currentBalanceAmountTextview?.text =
            Utils.removeNegativeSymbol(CurrencyFormatter.formatAmountToRandAndCentWithSpace(account?.currentBalance))
        totalAmountDueAmountTextview?.text =
            Utils.removeNegativeSymbol(CurrencyFormatter.formatAmountToRandAndCentWithSpace(account?.totalAmountDue))

        setPushViewDownAnimation(incRecentTransactionButton)
        setPushViewDownAnimation(incViewStatementButton)
        setPushViewDownAnimation(incPayMyAccountButton)
        setPushViewDownAnimation(accountInArrearsTextView)
        setPushViewDownAnimation(navigateBackImageButton)
        setPushViewDownAnimation(toolbarTitleTextView)
        setPushViewDownAnimation(infoIconImageView)

        val contactCallCenter = WSpannableStringBuilder(bindString(R.string.contact_the_call_centre_now))
        contactCallCenter.makeStringInteractable("0861502020", LinkType.PHONE)
        contactCallCenter.makeStringUnderlined("0861502020")
        setUnderlineText(contactCallCenter.build(), contactCallCenterNowTextview)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.incRecentTransactionButton -> navigateToRecentTransactionActivity()
            R.id.incViewStatementButton -> navigateToStatementActivity()
            R.id.incPayMyAccountButton -> {
            }
            R.id.accountInArrearsTextView -> {
            }
            R.id.navigateBackImageButton -> activity?.onBackPressed()
            R.id.toolbarTitleTextView -> {
            }
            R.id.infoIconImageView -> navigateToCardInformation()
        }
    }

    private fun setPushViewDownAnimation(view: View?) {
        view?.apply {
            setOnClickListener(this@RemoveBlockOnCollectionFragment)
            AnimationUtilExtension.animateViewPushDown(this)
        }
    }

    fun navigateToRecentTransactionActivity() {
        activity?.let { activity ->
            val applyNowState = accountData?.first
            val propertyName = when (applyNowState) {
                ApplyNowState.PERSONAL_LOAN -> FirebaseManagerAnalyticsProperties.MYACCOUNTSPERSONALLOANTRANSACTIONS
                ApplyNowState.STORE_CARD -> FirebaseManagerAnalyticsProperties.MYACCOUNTSSTORECARDTRANSACTIONS
                else -> ""
            }
            Utils.triggerFireBaseEvents(propertyName)
            accountData?.second?.apply {
                val intent = Intent(activity, WTransactionsActivity::class.java)
                intent.putExtra("productOfferingId", productOfferingId.toString())
                intent.putExtra(
                    ChatExtensionFragment.ACCOUNTS,
                    Gson().toJson(Pair(applyNowState, this))
                )
                intent.putExtra("cardType", productGroupCode?.toUpperCase())
                activity.startActivityForResult(intent, 0)
                activity.overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
            }
        }
    }

    fun navigateToStatementActivity() {
        accountData?.apply {

            val applyNowState = accountData?.first
            val propertyName = when (applyNowState) {
                ApplyNowState.PERSONAL_LOAN -> FirebaseManagerAnalyticsProperties.MYACCOUNTSPERSONALLOANSTATEMENTS
                ApplyNowState.STORE_CARD -> FirebaseManagerAnalyticsProperties.MYACCOUNTSSTORECARDSTATEMENTS
                else -> ""
            }
            Utils.triggerFireBaseEvents(propertyName)

            activity?.apply {
                val openStatement = Intent(this, StatementActivity::class.java)
                openStatement.putExtra(
                    ChatExtensionFragment.ACCOUNTS,
                    Gson().toJson(
                        Pair(
                            applyNowState,
                            second
                        )
                    )
                )
                startActivity(openStatement)
                overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
            }
        }
    }

    private fun navigateToCardInformation() {
        activity ?: return
        val helpIcon = mAccountPresenter?.getCardProductInformation(true)

        val cardInformationHelpActivity = Intent(activity, CardInformationHelpActivity::class.java)
        cardInformationHelpActivity.putExtra(CardInformationHelpActivity.HELP_INFORMATION, Gson().toJson(helpIcon))
        activity?.startActivityForResult(
            cardInformationHelpActivity,
            AccountSignedInActivity.REQUEST_CODE_ACCOUNT_INFORMATION
        )
        activity?.overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
    }

    private fun setUnderlineText(howToUseSpannableContent: Spannable, textView: TextView?) {
        textView?.text = howToUseSpannableContent
        textView?.movementMethod = LinkMovementMethod.getInstance()
        textView?.highlightColor = Color.TRANSPARENT
    }

}