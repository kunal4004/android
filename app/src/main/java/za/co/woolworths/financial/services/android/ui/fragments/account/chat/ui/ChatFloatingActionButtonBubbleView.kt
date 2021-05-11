package za.co.woolworths.financial.services.android.ui.fragments.account.chat.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.google.gson.Gson
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.ui.activities.WChatActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatBubbleVisibility
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.helper.LiveChatDBRepository
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.helper.LiveChatExtraParams
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension


class ChatFloatingActionButtonBubbleView(
    var activity: Activity? = null,
    var chatBubbleVisibility: ChatBubbleVisibility? = null,
    var floatingActionButtonBadgeCounter: FloatingActionButtonBadgeCounter? = null,
    var applyNowState: ApplyNowState,
    var scrollableView: Any? = null
) {

    private var chatBubbleToolTip: Dialog? = null
    private var isLiveChatEnabled = false
    private var broadcaster: LocalBroadcastManager? = null

    init {
        isLiveChatEnabled = chatBubbleVisibility?.isChatBubbleVisible(applyNowState) == true
        floatingActionButtonBadgeCounter?.visibility = if (isLiveChatEnabled) VISIBLE else GONE

        broadcaster = activity?.let { LocalBroadcastManager.getInstance(it) }
    }

    @SuppressLint("InflateParams")
    private fun showChatToolTip() {
        if (chatBubbleVisibility?.isInAppChatTooltipVisible(applyNowState) == false || (scrollableView as? NestedScrollView)?.scrollY ?: 0 > 30) return
        chatBubbleToolTip = activity?.let { act -> Dialog(act) }
        chatBubbleToolTip?.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            val view = layoutInflater.inflate(R.layout.inapp_chat_tip_acknowledgement_dialog, null)
            val dismissChatTipImageView =
                view.findViewById<ImageButton>(R.id.dismissChatTipImageView)
            val greetingTextView = view.findViewById<TextView>(R.id.greetingTextView)
            val chatToUsNowTextView = view.findViewById<TextView>(R.id.chatToUsNowTextView)
            AnimationUtilExtension.animateViewPushDown(chatToUsNowTextView)
            val chatAccountProductLandingPage = if (chatBubbleVisibility?.isChatVisibleForAccountLanding() == true) chatBubbleVisibility?.getAccountInProductLandingPage() else chatBubbleVisibility?.getAccountForProductLandingPage(applyNowState)
            activity?.apply {
                greetingTextView?.text = bindString(
                    R.string.chat_greeting_label, chatBubbleVisibility?.getUsername()
                        ?: ""
                )
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
                val dialogPosition = dm.heightPixels.div(
                    when (activity) {
                        is BottomNavigationActivity -> 4.2f
                        else -> 7.0f
                    }
                )

                val windowManagerLayoutParams: WindowManager.LayoutParams = attributes
                windowManagerLayoutParams.y = dialogPosition.toInt()

                setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
                )
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
            is PayMyAccountActivity -> true
            else -> chatBubbleVisibility?.isChatVisibleForAccountProductsLanding(applyNowState) == true
        }

        if (!shouldAnimateChatIcon) return

        when (scrollableView) {
            is NestedScrollView -> {
                (scrollableView as? NestedScrollView)?.apply {
                    viewTreeObserver?.addOnScrollChangedListener {
                        val scrollViewHeight: Double =
                            getChildAt(0)?.bottom?.minus(height.toDouble())
                                ?: 0.0
                        val getScrollY: Double = scrollY.toDouble()
                        val scrollPosition = getScrollY / scrollViewHeight * 100.0
                        if (scrollPosition.toInt() > 30) {
                            floatingActionButtonBadgeCounter?.hide()
                            if (chatBubbleToolTip?.isShowing == true)
                                chatBubbleToolTip?.dismiss()
                        } else {
                            floatingActionButtonBadgeCounter?.show()
                        }
                    }
                }
            }

            is RecyclerView -> {
                (scrollableView as? RecyclerView)?.addOnScrollListener(object :
                    RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        if (dy > 0 || dy < 0 && floatingActionButtonBadgeCounter?.isShown == true) floatingActionButtonBadgeCounter?.hide()
                    }

                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        if (newState == RecyclerView.SCROLL_STATE_IDLE) floatingActionButtonBadgeCounter?.show()
                        super.onScrollStateChanged(recyclerView, newState)
                    }
                })
            }
        }
    }

    private fun floatingButtonListener() {
        activity?.apply {
            val chatAccountProductLandingPage: Account? =
                if (chatBubbleVisibility?.isChatVisibleForAccountLanding() == true) chatBubbleVisibility?.getAccountInProductLandingPage() else chatBubbleVisibility?.getAccountForProductLandingPage(
                    applyNowState
                )
            AnimationUtilExtension.animateViewPushDown(floatingActionButtonBadgeCounter)
            startLiveChatMessageCountReceiver(this)
            floatingActionButtonBadgeCounter?.setOnClickListener {
                navigateToChatActivity(activity, chatAccountProductLandingPage)
            }
        }
    }

    fun navigateToChatActivity(activity: Activity?, chatAccountProductLandingPage: Account?) {
        activity ?: return
        val initChatDetails =
            chatBubbleVisibility?.getProductOfferingIdAndAccountNumber(applyNowState)
        val liveChatDBRepository = LiveChatDBRepository()
        val liveChatParams = liveChatDBRepository.getLiveChatParams()
        liveChatDBRepository.resetUnReadMessageCount()
        floatingActionButtonBadgeCounter?.count = 0
        liveChatDBRepository.saveLiveChatParams(
            LiveChatExtraParams(
                initChatDetails?.first,
                initChatDetails?.second,
                chatBubbleVisibility?.getSessionType(),
                activity::class.java.simpleName,
                Gson().toJson(chatAccountProductLandingPage),
                true,
                liveChatParams?.conversation
            )
        )

        activity.startActivity(Intent(activity, WChatActivity::class.java))
    }

    fun build() {
        if (!isLiveChatEnabled) return
        activity?.runOnUiThread {
            animateChatIcon()
            showChatToolTip()
            floatingButtonListener()
        }
    }

    val messageCountBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            activity?.runOnUiThread {
                val liveChatDBRepository = LiveChatDBRepository()
                Log.e("messageCountReceiver", "messageCountReceiver")
                floatingActionButtonBadgeCounter?.count =
                    liveChatDBRepository.getUnReadMessageCount()
            }
        }
    }

    private fun startLiveChatMessageCountReceiver(activity: Activity?) {
        activity ?: return
        // handler for received Intents for the "UnreadMessageCount" event
        broadcaster?.registerReceiver(
            messageCountBroadcastReceiver,
            IntentFilter(LIVE_CHAT_UNREAD_MESSAGE_COUNT_PACKAGE)
        )
    }

    fun endChatMessageCountEnd(activity: Activity?) {
        activity ?: return
        // Unregister since the activity is not visible
        broadcaster?.unregisterReceiver(messageCountBroadcastReceiver)
    }

    companion object {
        const val UNREAD_MESSAGE_COUNT = "unread_message_count"
        const val LIVE_CHAT_SUBSCRIPTION_RESULT = "live_chat_subscription_result"
        const val LIVE_CHAT_PACKAGE = "live.chat.subscription.result.SUBSCRIBE.DATA"
        const val LIVE_CHAT_UNREAD_MESSAGE_COUNT_PACKAGE = "live.chat.message.COUNT.DATA"

    }


}