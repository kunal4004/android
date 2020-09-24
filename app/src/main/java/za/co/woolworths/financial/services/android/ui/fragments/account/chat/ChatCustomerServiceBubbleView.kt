package za.co.woolworths.financial.services.android.ui.fragments.account.chat

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.Window
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import com.awfs.coordination.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.models.dto.chat.amplify.SessionType
import za.co.woolworths.financial.services.android.ui.activities.StatementActivity
import za.co.woolworths.financial.services.android.ui.activities.WChatActivity
import za.co.woolworths.financial.services.android.ui.activities.WTransactionsActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.payment_option.PaymentOptionActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatCustomerServiceExtensionFragment.Companion.ACCOUNTS
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatCustomerServiceExtensionFragment.Companion.ACCOUNT_NUMBER
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatCustomerServiceExtensionFragment.Companion.PRODUCT_OFFERING_ID
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatCustomerServiceExtensionFragment.Companion.SESSION_TYPE
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.WhatsAppChatToUsVisibility.Companion.CHAT_TO_COLLECTION_AGENT
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

class ChatCustomerServiceBubbleView(private var activity: Activity?,
                                    private val chatCustomerServiceBubbleVisibility: ChatCustomerServiceBubbleVisibility? = null,
                                    private var floatingActionButton: FloatingActionButton?,
                                    private var applyNowState: ApplyNowState,
                                    private var isAppScreenPaymentOptions: Boolean = false,
                                    private var scrollView: NestedScrollView? = null) {

    private fun getSessionType(): SessionType {
        val collectionsList = mutableListOf(AccountSignedInActivity::class.java.simpleName, BottomNavigationActivity::class.java.simpleName, PaymentOptionActivity::class.java.simpleName)
        val customerServicesList = mutableListOf(WTransactionsActivity::class.java.simpleName, StatementActivity::class.java.simpleName)
        val name = activity?.javaClass?.simpleName

        return when {
            collectionsList.contains(name) -> {
                SessionType.Collections
            }
            customerServicesList.contains(name) -> {
                SessionType.CustomerService
            }
            else -> {
                SessionType.Fraud
            }
        }
    }

    @SuppressLint("InflateParams")
    private fun showChatToolTip() {
        if (chatCustomerServiceBubbleVisibility?.shouldPresentChatTooltip(applyNowState, isAppScreenPaymentOptions) == false) return
        val tooltip = activity?.let { act -> Dialog(act) }
        tooltip?.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            val view = layoutInflater.inflate(R.layout.inapp_chat_tip_acknowledgement_dialog, null)
            val dismissChatTipImageView = view.findViewById<ImageButton>(R.id.dismissChatTipImageView)
            val greetingTextView = view.findViewById<TextView>(R.id.greetingTextView)
            greetingTextView?.text = bindString(R.string.chat_greeting_label, chatCustomerServiceBubbleVisibility?.getUsername()
                    ?: "")
            dismissChatTipImageView?.setOnClickListener {
                chatCustomerServiceBubbleVisibility?.saveInAppChatTooltip(applyNowState, isAppScreenPaymentOptions)
                dismiss()
            }
            setContentView(view)

            window?.apply {

                val dm = DisplayMetrics()
                windowManager.defaultDisplay.getMetrics(dm)

                val dialogPosition = dm.heightPixels.div(if (applyNowState == ApplyNowState.ACCOUNT_LANDING) 5f else 7.8f)

                val windowManagerLayoutParams: WindowManager.LayoutParams = attributes
                windowManagerLayoutParams.y = dialogPosition.toInt()

                setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
                setBackgroundDrawableResource(R.color.transparent)
                setGravity(Gravity.BOTTOM)
            }
            setTitle(null)
            setCancelable(true)
            show()
        }
    }

    private fun showChatIcon() {
        floatingActionButton?.visibility = when (applyNowState) {
            ApplyNowState.ACCOUNT_LANDING -> if (chatCustomerServiceBubbleVisibility?.isChatVisibleForAccountLanding() == true) VISIBLE else GONE
            ApplyNowState.STORE_CARD, ApplyNowState.PERSONAL_LOAN, ApplyNowState.BLACK_CREDIT_CARD, ApplyNowState.GOLD_CREDIT_CARD, ApplyNowState.SILVER_CREDIT_CARD -> if (chatCustomerServiceBubbleVisibility?.isChatVisibleForAccountDetailProduct(applyNowState) == true) VISIBLE else GONE
        }
    }

    fun build() {
        activity?.runOnUiThread {
            chatIconAnimation()
            showChatIcon()
            showChatToolTip()
            floatingActionButtonEvent()
        }
    }

    private fun chatIconAnimation() {
        val shouldAnimateChatIcon = when (applyNowState) {
            ApplyNowState.ACCOUNT_LANDING -> chatCustomerServiceBubbleVisibility?.isChatVisibleForAccountLanding() == true
            ApplyNowState.STORE_CARD, ApplyNowState.PERSONAL_LOAN, ApplyNowState.BLACK_CREDIT_CARD, ApplyNowState.GOLD_CREDIT_CARD, ApplyNowState.SILVER_CREDIT_CARD -> chatCustomerServiceBubbleVisibility?.isChatVisibleForAccountDetailProduct(applyNowState) == true
        }

        if (!shouldAnimateChatIcon) return

        scrollView?.apply {
            viewTreeObserver?.addOnScrollChangedListener {
                val scrollViewHeight: Double =
                        getChildAt(0)?.bottom?.minus(height.toDouble()) ?: 0.0

                val getScrollY: Double = scrollY.toDouble()
                val scrollPosition = getScrollY / scrollViewHeight * 100.0
                if (scrollPosition.toInt() > 30) {
                    floatingActionButton?.hide()
                } else {
                    floatingActionButton?.show()
                }
            }
        }
    }

    private fun floatingActionButtonEvent() {
        activity?.apply {
            Log.e("floatingActionButton",Gson().toJson(getSessionType()))
            val chatAccountProductLandingPage = if (chatCustomerServiceBubbleVisibility?.isChatVisibleForAccountLanding() == true) chatCustomerServiceBubbleVisibility.getAccountInProductLandingPage() else chatCustomerServiceBubbleVisibility?.getAccountForProductLandingPage(applyNowState)
            AnimationUtilExtension.animateViewPushDown(floatingActionButton)
            floatingActionButton?.setOnClickListener {
                val initChatDetails = chatCustomerServiceBubbleVisibility?.getProductOfferingIdAndAccountNumber(applyNowState)
                Utils.triggerFireBaseEvents(if (Utils.isOperatingHoursForInAppChat()) FirebaseManagerAnalyticsProperties.MY_ACCOUNTS_CHAT_ONLINE else FirebaseManagerAnalyticsProperties.MY_ACCOUNTS_CHAT_OFFLINE)
                val intent = Intent(this, WChatActivity::class.java)
                intent.putExtra(PRODUCT_OFFERING_ID, initChatDetails?.first)
                intent.putExtra(ACCOUNT_NUMBER, initChatDetails?.second)
                intent.putExtra(SESSION_TYPE , getSessionType())
                intent.putExtra(ACCOUNTS, Gson().toJson(chatAccountProductLandingPage))
                intent.putExtra(CHAT_TO_COLLECTION_AGENT, true)
                startActivity(intent)
            }
        }
    }
}