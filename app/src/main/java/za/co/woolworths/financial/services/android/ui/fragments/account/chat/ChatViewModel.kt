package za.co.woolworths.financial.services.android.ui.fragments.account.chat

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.IGenericAPILoaderView
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.models.dto.account.AccountsProductGroupCode
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.models.dto.chat.TradingHours
import za.co.woolworths.financial.services.android.models.dto.chat.amplify.SessionType
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.activities.AbsaStatementsActivity
import za.co.woolworths.financial.services.android.ui.activities.StatementActivity
import za.co.woolworths.financial.services.android.ui.activities.WTransactionsActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.extension.request
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.helper.LiveChatDBRepository
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.request.LiveChatAuthImpl
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.request.LiveChatListAllAgentConversationImpl
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.wenum.ActivityType

class ChatViewModel : ViewModel() {

    private var creditCardTokenAPI: Call<CreditCardTokenResponse>? = null
    private var awsAmplify: ChatAWSAmplify? = null

    var isCustomerSignOut: MutableLiveData<Boolean> = MutableLiveData()

    private var activityType: ActivityType? = null

    private var trackFirebaseEvent: ChatTrackFirebaseEvent = ChatTrackFirebaseEvent()
    private var chatTrackPostEvent: ChatTrackPostEvent = ChatTrackPostEvent()

    var liveChatDBRepository = LiveChatDBRepository()
    val liveChatAuthentication = LiveChatAuthImpl()
    val liveChatListAllAgentConversation = LiveChatListAllAgentConversationImpl()

    fun initAmplify() {
        this.awsAmplify = ChatAWSAmplify
    }

    fun isCreditCardAccount(): Boolean = liveChatDBRepository.isCreditCardAccount()

    fun getServiceUnavailableMessage(): Pair<SendEmailIntentInfo, String> {
        val inAppChatMessage = WoolworthsApplication.getInAppChat()
        return when (getSessionType()) {
            SessionType.Collections -> {
                val collections = inAppChatMessage?.collections
                val emailAddress = collections?.emailAddress ?: ""
                val subjectLine = collections?.emailSubjectLine ?: ""
                val serviceUnavailable =
                    collections?.serviceUnavailable?.replace("{{emailAddress}}", emailAddress)
                        ?: ""

                Pair(SendEmailIntentInfo(emailAddress, subjectLine), serviceUnavailable)
            }
            SessionType.CustomerService -> {
                val customerService = inAppChatMessage?.customerService
                val emailAddress = customerService?.emailAddress ?: ""
                val subjectLine = customerService?.emailSubjectLine ?: ""
                val serviceUnavailable =
                    customerService?.serviceUnavailable?.replace("{{emailAddress}}", emailAddress)
                        ?: ""

                Pair(SendEmailIntentInfo(emailAddress, subjectLine), serviceUnavailable)
            }
            SessionType.Fraud -> Pair(SendEmailIntentInfo(), "")
        }
    }

    private fun getSessionType(): SessionType {
        return liveChatDBRepository.getSessionType()
    }

    override fun onCleared() {
        creditCardTokenAPI?.apply {
            if (!isCanceled)
                cancel()

        }
        super.onCleared()
    }

    private fun getTradingHours(): MutableList<TradingHours>? {
        val inAppChat = WoolworthsApplication.getInAppChat()
        return when (getSessionType()) {
            SessionType.Collections -> inAppChat?.collections?.tradingHours
            SessionType.CustomerService -> inAppChat?.customerService?.tradingHours
            else -> inAppChat.tradingHours
        }
    }

    fun isOperatingHoursForInAppChat(): Boolean {
        return getTradingHours()?.let { KotlinUtils.isOperatingHoursForInAppChat(it) } ?: false
    }

    fun offlineMessageTemplate(onClick: (Triple<String, String, String>) -> Unit): SpannableString {
        val inAppChat = WoolworthsApplication.getInAppChat()
        when (getSessionType()) {
            SessionType.Collections, SessionType.Fraud -> {
                val collections = inAppChat.collections
                val emailAddress = collections.emailAddress

                var offlineMessageTemplate =
                    collections.offlineMessageTemplate.replace("{{emailAddress}}", emailAddress)
                offlineMessageTemplate =
                    offlineMessageTemplate.replace("{{emailAddress}}", emailAddress)
                val spannableOfflineMessageTemplate = SpannableString(offlineMessageTemplate)
                spannableOfflineMessageTemplate.setSpan(
                    object : ClickableSpan() {
                        override fun updateDrawState(ds: TextPaint) {
                            ds.color = Color.WHITE
                            ds.isUnderlineText = true
                        }

                        override fun onClick(textView: View) {
                            val emailSubjectLine = collections.emailSubjectLine
                            val emailMessage = collections.emailMessage
                            onClick(Triple(emailAddress, emailSubjectLine, emailMessage))
                        }
                    },
                    spannableOfflineMessageTemplate.indexOf(emailAddress),
                    spannableOfflineMessageTemplate.indexOf(emailAddress) + emailAddress.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )


                return spannableOfflineMessageTemplate
            }

            SessionType.CustomerService -> {
                val customerService = inAppChat.customerService
                val emailAddress = customerService.emailAddress

                var offlineMessageTemplate =
                    customerService.offlineMessageTemplate.replace("{{emailAddress}}", emailAddress)
                offlineMessageTemplate =
                    offlineMessageTemplate.replace("{{emailAddress}}", emailAddress)
                val spannableOfflineMessageTemplate = SpannableString(offlineMessageTemplate)
                spannableOfflineMessageTemplate.setSpan(
                    object : ClickableSpan() {
                        override fun updateDrawState(ds: TextPaint) {
                            ds.color = Color.WHITE
                            ds.isUnderlineText = true
                        }

                        override fun onClick(textView: View) {
                            val emailSubjectLine = customerService.emailSubjectLine
                            val emailMessage = customerService.emailMessage
                            onClick(Triple(emailAddress, emailSubjectLine, emailMessage))
                        }
                    },
                    spannableOfflineMessageTemplate.indexOf(emailAddress),
                    spannableOfflineMessageTemplate.indexOf(emailAddress) + emailAddress.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                return spannableOfflineMessageTemplate
            }
        }
    }

    fun getCreditCardToken(
        result: (CreditCardTokenResponse?) -> Unit,
        error: (Throwable?) -> Unit
    ) {
        creditCardTokenAPI =
            request(OneAppService.getCreditCardToken(), object : IGenericAPILoaderView<Any> {

                override fun onSuccess(response: Any?) {

                    (response as? CreditCardTokenResponse)?.apply {
                        cards?.apply { initAmplify() }
                        result(this)
                    }
                }

                override fun onFailure(error: Throwable?) {
                    super.onFailure(error)
                    error(error)
                }
            })
    }

    @Throws(RuntimeException::class)
    fun setScreenType() {
        activityType = when (liveChatDBRepository.getLiveChatParams()?.fromActivity) {
            BottomNavigationActivity::class.java.simpleName -> ActivityType.ACCOUNT_LANDING
            AccountSignedInActivity::class.java.simpleName -> ActivityType.PRODUCT_LANDING
            PayMyAccountActivity::class.java.simpleName -> ActivityType.PAYMENT_OPTIONS
            WTransactionsActivity::class.java.simpleName -> ActivityType.TRANSACTION
            StatementActivity::class.java.simpleName -> ActivityType.STATEMENT
            AbsaStatementsActivity::class.java.simpleName -> ActivityType.ABSA_STATEMENT
            else -> throw RuntimeException("${liveChatDBRepository.getLiveChatParams()?.fromActivity} value not supported")
        }
    }

    @SuppressLint("DefaultLocale")
    fun getApplyNowState(): ApplyNowState {
        return when (liveChatDBRepository.getAccount()?.productGroupCode?.toLowerCase()
            ?.let { AccountsProductGroupCode.getEnum(it) }) {
            AccountsProductGroupCode.STORE_CARD -> ApplyNowState.STORE_CARD
            AccountsProductGroupCode.PERSONAL_LOAN -> ApplyNowState.PERSONAL_LOAN
            AccountsProductGroupCode.CREDIT_CARD -> when (liveChatDBRepository.getAccount()?.accountNumberBin) {
                Utils.SILVER_CARD -> ApplyNowState.SILVER_CREDIT_CARD
                Utils.BLACK_CARD -> ApplyNowState.BLACK_CREDIT_CARD
                Utils.GOLD_CARD -> ApplyNowState.GOLD_CREDIT_CARD
                else -> ApplyNowState.STORE_CARD
            }
            else -> ApplyNowState.STORE_CARD
        }
    }

    fun postEventChatOffline() =
        chatTrackPostEvent.onChatOffline(applyNowState = getApplyNowState())

    fun postChatEventInitiateSession() {
        if (!isOperatingHoursForInAppChat()) return
        val applyNowState = getApplyNowState()
        with(chatTrackPostEvent) {
            when (getSessionType()) {
                SessionType.Collections -> {
                    when (activityType) {
                        ActivityType.ACCOUNT_LANDING -> onChatCollectionsLandingInitiateSession(
                            applyNowState
                        )
                        ActivityType.PAYMENT_OPTIONS -> onPayOptionsInitiateSession(applyNowState)
                        else -> return
                    }
                }
                SessionType.CustomerService -> {
                    when (activityType) {
                        ActivityType.TRANSACTION -> onTransactionsInitiateSession(applyNowState)
                        ActivityType.STATEMENT -> onStatementsInitiateSession(applyNowState)
                        else -> return
                    }
                }
                SessionType.Fraud -> return
            }
        }
    }

    fun postChatEventEndSession() {
        val applyNowState = getApplyNowState()
        with(chatTrackPostEvent) {
            when (getSessionType()) {
                SessionType.Collections -> onChatCollectionsEndSession(applyNowState)
                SessionType.CustomerService -> onChatCustomerServicesEndSession(applyNowState)
                SessionType.Fraud -> return
            }
        }
    }

    fun triggerFirebaseOnlineOfflineChatEvent(activity: Activity) {
        activity?.apply {
            if (isOperatingHoursForInAppChat())
                trackFirebaseEvent.chatOnline(getApplyNowState(), activityType, this)
            else
                trackFirebaseEvent.chatOffline(getApplyNowState(), activityType, this)
        }
    }

    fun triggerFirebaseEventChatBreak(activity: Activity) {
        activity?.apply { trackFirebaseEvent.chatBreak(getApplyNowState(), activityType, this) }
    }

    fun triggerFirebaseEventEndSession(activity: Activity) {
        activity?.apply { trackFirebaseEvent.chatEnd(getApplyNowState(), activityType, this) }
    }

    fun isChatServiceRunning(activity: Activity?): Boolean {
        activity ?: return false
        return ChatAWSAmplify.isLiveChatBackgroundServiceRunning
    }
}