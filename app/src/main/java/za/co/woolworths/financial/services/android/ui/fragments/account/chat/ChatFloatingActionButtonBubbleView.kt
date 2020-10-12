package za.co.woolworths.financial.services.android.ui.fragments.account.chat

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.Window
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.ui.activities.WChatActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatExtensionFragment.Companion.ACCOUNTS
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatExtensionFragment.Companion.ACCOUNT_NUMBER
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatExtensionFragment.Companion.FROM_ACTIVITY
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatExtensionFragment.Companion.PRODUCT_OFFERING_ID
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatExtensionFragment.Companion.SESSION_TYPE
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.WhatsAppChatToUsVisibility.Companion.CHAT_TO_COLLECTION_AGENT
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

class ChatFloatingActionButtonBubbleView(var activity: Activity? = null,
                                         var chatBubbleVisibility: ChatBubbleVisibility? = null,
                                         var floatingActionButton: FloatingActionButton? = null,
                                         var applyNowState: ApplyNowState,
                                         var scrollableView: Any? = null) {

    private var isLiveChatEnabled = false

    init {
        isLiveChatEnabled = chatBubbleVisibility?.isChatBubbleVisible(applyNowState) == true
        floatingActionButton?.visibility = if (isLiveChatEnabled) VISIBLE else GONE
    }

    @SuppressLint("InflateParams")
    private fun showChatToolTip() {
        if (chatBubbleVisibility?.isInAppChatTooltipVisible(applyNowState) == false) return
        val tooltip = activity?.let { act -> Dialog(act) }
        tooltip?.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            val view = layoutInflater.inflate(R.layout.inapp_chat_tip_acknowledgement_dialog, null)
            val dismissChatTipImageView = view.findViewById<ImageButton>(R.id.dismissChatTipImageView)
            val greetingTextView = view.findViewById<TextView>(R.id.greetingTextView)
            val chatToUsNowTextView = view.findViewById<TextView>(R.id.chatToUsNowTextView)
            AnimationUtilExtension.animateViewPushDown(chatToUsNowTextView)
            val chatAccountProductLandingPage = if (chatBubbleVisibility?.isChatVisibleForAccountLanding() == true) chatBubbleVisibility?.getAccountInProductLandingPage() else chatBubbleVisibility?.getAccountForProductLandingPage(applyNowState)
            activity?.apply {
                greetingTextView?.text = bindString(R.string.chat_greeting_label, chatBubbleVisibility?.getUsername()
                        ?: "")
                dismissChatTipImageView?.setOnClickListener {
                    chatBubbleVisibility?.saveInAppChatTooltip(applyNowState)
                    dismiss()
                }

                chatToUsNowTextView?.setOnClickListener {
                    chatBubbleVisibility?.saveInAppChatTooltip(applyNowState)
                    navigateToChatActivity(activity, chatAccountProductLandingPage)
                    dismiss()
                }
            }
            setContentView(view)

            window?.apply {

                val dm = DisplayMetrics()
                windowManager.defaultDisplay.getMetrics(dm)

                val dialogPosition = dm.heightPixels.div(when (activity) {
                    is BottomNavigationActivity -> 4.2f
                    else -> 7.0f
                })

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

    private fun animateChatIcon() {
        val shouldAnimateChatIcon = when (activity) {
            is BottomNavigationActivity -> chatBubbleVisibility?.isChatVisibleForAccountLanding() == true
            else -> chatBubbleVisibility?.isChatVisibleForAccountProductsLanding(applyNowState) == true
        }

        if (!shouldAnimateChatIcon) return

        when (scrollableView) {
            is NestedScrollView -> {
                (scrollableView as? NestedScrollView)?.apply {
                    viewTreeObserver?.addOnScrollChangedListener {
                        val scrollViewHeight: Double = getChildAt(0)?.bottom?.minus(height.toDouble())
                                ?: 0.0
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

            is RecyclerView -> {
                (scrollableView as? RecyclerView)?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        if (dy > 0 || dy < 0 && floatingActionButton?.isShown == true) floatingActionButton?.hide()
                    }

                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        if (newState == RecyclerView.SCROLL_STATE_IDLE) floatingActionButton?.show()
                        super.onScrollStateChanged(recyclerView, newState)
                    }
                })
            }
        }
    }

    private fun floatingButtonListener() {
        activity?.apply {
            val chatAccountProductLandingPage : Account? = if (chatBubbleVisibility?.isChatVisibleForAccountLanding() == true) chatBubbleVisibility?.getAccountInProductLandingPage() else chatBubbleVisibility?.getAccountForProductLandingPage(applyNowState)
            AnimationUtilExtension.animateViewPushDown(floatingActionButton)
            floatingActionButton?.setOnClickListener {
                navigateToChatActivity(activity, chatAccountProductLandingPage)
            }
        }
    }

    fun navigateToChatActivity(activity: Activity?, chatAccountProductLandingPage: Account?) {
        activity?.apply {
            val initChatDetails = chatBubbleVisibility?.getProductOfferingIdAndAccountNumber(applyNowState)
            val intent = Intent(this, WChatActivity::class.java)
            intent.putExtra(PRODUCT_OFFERING_ID, initChatDetails?.first)
            intent.putExtra(ACCOUNT_NUMBER, initChatDetails?.second)
            intent.putExtra(SESSION_TYPE, chatBubbleVisibility?.getSessionType())
            intent.putExtra(FROM_ACTIVITY, this::class.java.simpleName)
            intent.putExtra(ACCOUNTS, Gson().toJson(chatAccountProductLandingPage))
            intent.putExtra(CHAT_TO_COLLECTION_AGENT, true)
            startActivity(intent)
        }
    }

    fun build() {
        if (!isLiveChatEnabled) return
        activity?.runOnUiThread {
            animateChatIcon()
            showChatToolTip()
            floatingButtonListener()
        }
    }
}