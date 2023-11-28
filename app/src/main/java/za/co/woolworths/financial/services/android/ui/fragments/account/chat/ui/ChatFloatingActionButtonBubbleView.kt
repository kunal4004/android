package za.co.woolworths.financial.services.android.ui.fragments.account.chat.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.Window
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import kotlinx.coroutines.GlobalScope
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.models.dto.chat.amplify.SessionStateType
import za.co.woolworths.financial.services.android.ui.activities.WChatActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.extension.bindDrawable
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.extension.doAfterDelay
import za.co.woolworths.financial.services.android.ui.extension.setSafeOnClickListener
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatAWSAmplify
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatBubbleVisibility
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.helper.LiveChatDBRepository
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.helper.LiveChatExtraParams
import za.co.woolworths.financial.services.android.ui.views.NotificationBadge
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension
import za.co.woolworths.financial.services.android.util.wenum.VocTriggerEvent


class ChatFloatingActionButtonBubbleView(
    var activity: AppCompatActivity? = null,
    var chatBubbleVisibility: ChatBubbleVisibility? = null,
    var floatingActionButton: FloatingActionButton? = null,
    var applyNowState: ApplyNowState,
    var scrollableView: Any? = null,
    var notificationBadge: NotificationBadge? = null,
    var onlineChatImageViewIndicator: ImageView? = null,
    val vocTriggerEvent: VocTriggerEvent? = null
) : LifecycleObserver {

    private var chatBubbleToolTip: Dialog? = null
    private var isLiveChatEnabled = false
    private var receiverManager: ReceiverManager? = null
    private val liveChatDBRepository = LiveChatDBRepository()

    init {
        isLiveChatEnabled = chatBubbleVisibility?.isChatBubbleVisible(applyNowState) == true
        floatingActionButton?.visibility = if (isLiveChatEnabled) VISIBLE else GONE

        receiverManager = activity?.let { ReceiverManager.init(it) }
    }

    @SuppressLint("InflateParams")
    private fun showChatToolTip() {
        if (chatBubbleVisibility?.isInAppChatTooltipVisible(applyNowState) == false || ((scrollableView as? NestedScrollView)?.scrollY
                        ?: 0) > 30) return
        chatBubbleToolTip = activity?.let { act -> Dialog(act) }
        chatBubbleToolTip?.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            val view = layoutInflater.inflate(R.layout.inapp_chat_tip_acknowledgement_dialog, null)
            val dismissChatTipImageView =
                view.findViewById<ImageButton>(R.id.dismissChatTipImageView)
            val greetingTextView = view.findViewById<TextView>(R.id.greetingTextView)
            val chatToUsNowTextView = view.findViewById<TextView>(R.id.chatToUsNowTextView)
            AnimationUtilExtension.animateViewPushDown(chatToUsNowTextView)
            val chatAccountProductLandingPage =
                if (chatBubbleVisibility?.isChatVisibleForAccountLanding() == true) chatBubbleVisibility?.getAccountInProductLandingPage() else chatBubbleVisibility?.getAccountForProductLandingPage(
                    applyNowState
                )
            activity?.apply {
                greetingTextView?.text = bindString(
                    R.string.chat_greeting_label,
                    chatBubbleVisibility?.getUsername() ?: ""
                )
                dismissChatTipImageView?.setOnClickListener {
                    chatBubbleVisibility?.saveInAppChatTooltip(applyNowState)
                    dismiss()
                }

                chatToUsNowTextView?.setSafeOnClickListener {
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
            if (!this.isShowing)
                show()
        }
    }

    private  fun isUserInPaymentOptionScreen(): Boolean  {
        val creditDebitCardNestedScrollView = activity?.findViewById<NestedScrollView>(R.id.creditDebitCardPaymentsScrollView)
        val nestedScrollView = scrollableView as? NestedScrollView
        return (nestedScrollView?.id == creditDebitCardNestedScrollView?.id)
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
                        val scrollViewHeight: Double = getChildAt(0)?.bottom?.minus(height.toDouble()) ?: 0.0
                        val getScrollY: Double = scrollY.toDouble()
                        val scrollPosition = getScrollY / scrollViewHeight * 100.0
                        if (scrollPosition.toInt() > 30) {
                            if (!isUserInPaymentOptionScreen()) {
                                floatingActionButton?.hide()
                            }
                        } else {
                            floatingActionButton?.show()
                        }
                    }
                }
            }

            is RecyclerView -> {
                (scrollableView as? RecyclerView)?.addOnScrollListener(object :
                    RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        if (dy > 0 || dy < 0 && floatingActionButton?.isShown == true) {
                            floatingActionButton?.hide()
                        }
                    }

                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                            floatingActionButton?.show()
                        }
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
            AnimationUtilExtension.animateViewPushDown(floatingActionButton)
            floatingActionButton?.setSafeOnClickListener {
                navigateToChatActivity(activity, chatAccountProductLandingPage)
            }
        }
    }

    fun navigateToChatActivity(activity: Activity?, chatAccountProductLandingPage: Account?) {
        activity ?: return
        val initChatDetails =
            chatBubbleVisibility?.getProductOfferingIdAndAccountNumber(applyNowState)
        val liveChatParams = liveChatDBRepository.getLiveChatParams()
        liveChatDBRepository.resetUnReadMessageCount()
        GlobalScope.doAfterDelay(DelayConstant.DELAY_300_MS) {
            notificationBadge?.setNumber(0)
        }
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

        activity.sendBroadcast(Intent(LIVE_CHAT_TOAST))
        Intent(activity, WChatActivity::class.java).apply {
            putExtra(WChatActivity.EXTRA_VOC_TRIGGER_EVENT, vocTriggerEvent)
            activity.startActivity(this)
        }
    }

    fun build() {
        if (!isLiveChatEnabled) return
        activity?.runOnUiThread {
            animateChatIcon()
            showChatToolTip()
            floatingButtonListener()
            messageCountObserver()
            addLifeCycleObserver()
            onFABVisibilityChangeListener()
        }
    }

    private fun onFABVisibilityChangeListener() {
        floatingActionButton?.addOnHideAnimationListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                showOnlineIconIndicator(false)
                super.onAnimationStart(animation)
            }
        })

        floatingActionButton?.addOnShowAnimationListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                super.onAnimationStart(animation)
                showOnlineIconIndicator(true)
            }
        })
    }

    private fun addLifeCycleObserver() {
        activity?.lifecycle?.addObserver(object : LifecycleObserver {

            @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
            fun onResume() {
                // handler for received Intents for the "UnreadMessageCount" event
                if (activity?.lifecycle?.currentState?.isAtLeast(Lifecycle.State.STARTED) == true) {
                    receiverManager?.registerReceiver(
                        messageCountBroadcastReceiver,
                        IntentFilter(LIVE_CHAT_UNREAD_MESSAGE_COUNT_PACKAGE)
                    )
                    showOnlineIconIndicator(true)
                }
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            fun onPause() {
                unregisterReceiver()
            }
        })
    }

    private fun showOnlineIconIndicator(isVisible: Boolean) {
        val isServiceRunning = ChatAWSAmplify.isLiveChatBackgroundServiceRunning
        val messageCount = liveChatDBRepository.getUnReadMessageCount()
        val isUserAuthenticated = SessionUtilities.getInstance().isUserAuthenticated

        // notificationBadge
        if (ChatAWSAmplify.sessionStateType != null) {
            val badgeBackgroundDrawable = when (ChatAWSAmplify.sessionStateType) {
                SessionStateType.DISCONNECT -> bindDrawable(R.drawable.nb_badge_disconnect_bg)
                else -> bindDrawable(R.drawable.nb_badge_bg)
            }

            notificationBadge?.badgeBackgroundDrawable = badgeBackgroundDrawable
            onlineChatImageViewIndicator?.background = badgeBackgroundDrawable
        }

        if (isUserAuthenticated && isServiceRunning && messageCount > 0) {
            notificationBadge?.setNumber(messageCount, true)
            notificationBadge?.visibility = VISIBLE
            onlineChatImageViewIndicator?.visibility = GONE
            if (!isVisible) {
                notificationBadge?.visibility = GONE
            }
        } else {
            notificationBadge?.visibility = GONE
            notificationBadge?.setNumber(0, true)
            val isLiveChatServiceVisible =
                isVisible && isServiceRunning && messageCount == 0 && isUserAuthenticated
            if (isLiveChatServiceVisible) {
                onlineChatImageViewIndicator?.visibility = VISIBLE
            } else {
                onlineChatImageViewIndicator?.visibility = GONE
            }
        }

        if (!isUserAuthenticated) {
            floatingActionButton?.visibility = GONE
        }
    }

    private fun messageCountObserver() {
        activity?.lifecycle?.addObserver(this@ChatFloatingActionButtonBubbleView)
    }

    var messageCountBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            activity?.runOnUiThread {
                if (floatingActionButton?.isShown == true)
                    showOnlineIconIndicator(true)
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        unregisterReceiver()
    }

    // Unregister since the activity is not visible
    private fun unregisterReceiver() {
        try {
            receiverManager?.unregisterReceiver(messageCountBroadcastReceiver)
        } catch (ex: IllegalArgumentException) {
            FirebaseManager.logException("unregisterReceiver messageCountBroadcastReceiver $ex")
        }
    }

    companion object {
        const val UNREAD_MESSAGE_COUNT = "unread_message_count"
        const val LIVE_CHAT_SUBSCRIPTION_RESULT = "live_chat_subscription_result"
        const val LIVE_CHAT_NO_INTERNET_RESULT = "live_chat_no_internet_result"
        const val LIVE_CHAT_PACKAGE = "live.chat.subscription.result.SUBSCRIBE.DATA"
        const val LIVE_CHAT_UNREAD_MESSAGE_COUNT_PACKAGE = "live.chat.message.COUNT.DATA"
        const val LIVE_CHAT_TOAST = "live.chat.TOAST.DATA"
    }
}
