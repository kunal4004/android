package za.co.woolworths.financial.services.android.ui.fragments.account.chat

import android.annotation.SuppressLint
import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.awfs.coordination.R
import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.IGenericAPILoaderView
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.Card
import za.co.woolworths.financial.services.android.models.dto.ChatMessage
import za.co.woolworths.financial.services.android.models.dto.CreditCardTokenResponse
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.models.dto.chat.TradingHours
import za.co.woolworths.financial.services.android.models.dto.chat.amplify.Conversation
import za.co.woolworths.financial.services.android.models.dto.chat.amplify.SendMessageResponse
import za.co.woolworths.financial.services.android.models.dto.chat.amplify.SessionStateType
import za.co.woolworths.financial.services.android.models.dto.chat.amplify.SessionType
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.activities.AbsaStatementsActivity
import za.co.woolworths.financial.services.android.ui.activities.StatementActivity
import za.co.woolworths.financial.services.android.ui.activities.WTransactionsActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.AccountSignedInActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.payment_option.PaymentOptionActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.extension.request
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.wenum.ActivityType
import java.util.*

class ChatViewModel : ViewModel() {

    private var creditCardTokenAPI: Call<CreditCardTokenResponse>? = null
    private var awsAmplify: ChatAWSAmplify? = null
    private var conversation: Conversation? = null
    private var mAccount: MutableLiveData<Account?> = MutableLiveData()

    private var sessionStateType: MutableLiveData<SessionStateType?> = MutableLiveData()
    private var sessionType: MutableLiveData<SessionType?> = MutableLiveData()

    var isCustomerSignOut: MutableLiveData<Boolean> = MutableLiveData()
    var isChatToCollectionAgent: MutableLiveData<Boolean> = MutableLiveData()

    var mAbsaCard: MutableLiveData<MutableList<Card>?> = MutableLiveData()
    private var activityType: ActivityType? = null

    private var trackFirebaseEvent: ChatTrackFirebaseEvent = ChatTrackFirebaseEvent()
    private var chatTrackPostEvent: ChatTrackPostEvent = ChatTrackPostEvent()

    init {
        mAbsaCard.value = getAccount()?.cards
        isChatToCollectionAgent.value = false
        isCustomerSignOut.value = false
        setSessionStateType(SessionStateType.DISCONNECT)
        setSessionType(SessionType.Collections)
    }

    fun initAmplify() {
        this.awsAmplify = ChatAWSAmplify
    }

    fun setAccount(account: Account?) {
        mAccount.value = account
    }

    fun getAccount(): Account? {
        return mAccount.value
    }

    @SuppressLint("DefaultLocale")
    fun isCreditCardAccount(): Boolean {
        return getAccount()?.productGroupCode?.toLowerCase() == "cc"
    }

    fun setSessionStateType(type: SessionStateType) {
        sessionStateType.value = type
    }

    private fun getSessionStateType(): SessionStateType {
        return sessionStateType.value ?: SessionStateType.ONLINE
    }

    fun setSessionType(type: SessionType) {
        sessionType.value = type
    }

    private fun getSessionType(): SessionType {
        return sessionType.value ?: SessionType.Collections
    }

    fun signIn(result: () -> Unit, failure: (Any) -> Unit) {
        awsAmplify?.apply {
            signIn({ conversation ->
                this@ChatViewModel.conversation = conversation
                result()
            }, { failure -> failure(failure) })
        }
    }

    fun subscribeToMessageByConversationId(result: (SendMessageResponse?) -> Unit, failure: (Any) -> Unit) {
        awsAmplify?.subscribeToMessageByConversationId(
                getConversationMessageId(),
                getSessionType(),
                getSessionVars(),
                getCustomerInfo().getCustomerFamilyName(),
                getCustomerInfo().getCustomerEmail(),
                { data -> result(data) }, { failure(failure) })
    }

    private fun getConversationMessageId(): String = conversation?.id ?: ""

    override fun onCleared() {
        awsAmplify?.cancelSubscribeMessageByConversationId()
        creditCardTokenAPI?.apply {
            if (!isCanceled)
                cancel()

        }
        super.onCleared()
    }

    fun sendMessage(content: String) {
        awsAmplify?.sendMessage(
                getConversationMessageId(),
                getSessionType(),
                getSessionStateType(),
                content,
                getSessionVars(),
                getCustomerInfo().getCustomerFamilyName(),
                getCustomerInfo().getCustomerEmail())
    }

    fun signOut(result: () -> Unit) {
        awsAmplify?.queryServiceSignOut(
                getConversationMessageId(),
                getSessionType(),
                SessionStateType.DISCONNECT,
                "",
                getSessionVars(),
                getCustomerInfo().getCustomerFamilyName(),
                getCustomerInfo().getCustomerEmail(),
                { result() }, { result() })
    }


    private fun getTradingHours(): MutableList<TradingHours>? {
        val inAppChat = WoolworthsApplication.getInAppChat()
        return when (getSessionType()) {
            SessionType.Collections -> inAppChat?.collections?.tradingHours
            SessionType.CustomerService ->  inAppChat?.customerService?.tradingHours
            else -> inAppChat.tradingHours
        }
    }

    fun isOperatingHoursForInAppChat(): Boolean? {
        return getTradingHours()?.let { KotlinUtils.isOperatingHoursForInAppChat(it) } ?: false
    }

    fun getInAppTradingHoursForToday(): TradingHours? {
        return getTradingHours()?.let { KotlinUtils.getInAppTradingHoursForToday(it) }
    }

    fun offlineMessageTemplate(onClick: (Triple<String, String, String>) -> Unit): SpannableString {
        val inAppChat = WoolworthsApplication.getInAppChat()
        when (getSessionType()) {
            SessionType.Collections, SessionType.Fraud -> {
                val collections = inAppChat.collections
                val emailAddress = collections.emailAddress

                var offlineMessageTemplate = collections.offlineMessageTemplate.replace("{{emailAddress}}", emailAddress)
                offlineMessageTemplate = offlineMessageTemplate.replace("{{emailAddress}}", emailAddress)
                val spannableOfflineMessageTemplate = SpannableString(offlineMessageTemplate)
                spannableOfflineMessageTemplate.setSpan(object : ClickableSpan() {
                    override fun updateDrawState(ds: TextPaint) {
                        ds.color = Color.WHITE
                        ds.isUnderlineText = true
                    }

                    override fun onClick(textView: View) {
                        val emailSubjectLine = collections.emailSubjectLine
                        val emailMessage = collections.emailMessage
                        onClick(Triple(emailAddress, emailSubjectLine, emailMessage))
                    }
                }, spannableOfflineMessageTemplate.indexOf(emailAddress), spannableOfflineMessageTemplate.indexOf(emailAddress) + emailAddress.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)


                return spannableOfflineMessageTemplate
            }

            SessionType.CustomerService -> {
                val customerService = inAppChat.customerService
                val emailAddress = customerService.emailAddress

                var offlineMessageTemplate = customerService.offlineMessageTemplate.replace("{{emailAddress}}", emailAddress)
                offlineMessageTemplate = offlineMessageTemplate.replace("{{emailAddress}}", emailAddress)
                val spannableOfflineMessageTemplate = SpannableString(offlineMessageTemplate)
                spannableOfflineMessageTemplate.setSpan(object : ClickableSpan() {
                    override fun updateDrawState(ds: TextPaint) {
                        ds.color = Color.WHITE
                        ds.isUnderlineText = true
                    }

                    override fun onClick(textView: View) {
                        val emailSubjectLine = customerService.emailSubjectLine
                        val emailMessage = customerService.emailMessage
                        onClick(Triple(emailAddress, emailSubjectLine, emailMessage))
                    }
                }, spannableOfflineMessageTemplate.indexOf(emailAddress), spannableOfflineMessageTemplate.indexOf(emailAddress) + emailAddress.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                return spannableOfflineMessageTemplate
            }
        }
    }

    fun getCreditCardToken(result: (CreditCardTokenResponse?) -> Unit, error: (Throwable?) -> Unit) {
        creditCardTokenAPI = request(OneAppService.getCreditCardToken(), object : IGenericAPILoaderView<Any> {

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

    private fun getSessionVars(): String {

        val account = getAccount()

        val prsAccountNumber = account?.accountNumber ?: ""
        val productGroupCode = account?.productGroupCode?.toLowerCase(Locale.getDefault())
        val isCreditCard = productGroupCode == "cc"
        val prsCardNumber = if (isCreditCard) getABSACardToken() ?: "" else "0"
        val prsC2id = getCustomerInfo().getCustomerC2ID()
        val prsFirstname = getCustomerInfo().getCustomerUsername()
        val prsSurname = getCustomerInfo().getCustomerFamilyName()
        val prsProductOfferingId = account?.productOfferingId?.toString() ?: "0"
        val prsProductOfferingDescription = when (productGroupCode) {
            "sc" -> "StoreCard"
            "pl" -> "PersonalLoan"
            "cc" -> "CreditCard"
            else -> ""
        }

        return bindString(R.string.chat_send_message_session_var_params,
                prsAccountNumber,
                prsCardNumber,
                prsC2id,
                prsFirstname,
                prsSurname,
                prsProductOfferingId,
                prsProductOfferingDescription)

    }

    fun getABSACardToken(): String? = getAccount()?.cards?.get(0)?.absaCardToken
            ?: mAbsaCard.value?.get(0)?.absaCardToken

    fun getCustomerInfo() = ChatCustomerInfo

    fun getMessagesListByConversation(result: ((MutableList<ChatMessage>?) -> Unit)) {

        awsAmplify?.getMessagesListByConversation(getConversationMessageId()) { message ->

            val messageList: MutableList<ChatMessage> = mutableListOf()
            message?.items?.forEach { item ->
                val chatMessage = ChatMessage(if (item.sender == "AGENT") ChatMessage.Type.RECEIVED else ChatMessage.Type.SENT, item.content)
                messageList.add(chatMessage)
            }

            var messagesSize = messageList.size
            if (messagesSize > 1) {
                messagesSize -= 1
                for (i in 1..messagesSize) {
                    if (messageList[i].type == messageList[i - 1].type) {
                        messageList[i].isWoolworthIconVisible = false
                    }
                }
            }

            result(messageList)
        }
    }

    @Throws(RuntimeException::class)
    fun setScreenType(fromActivity: String?) {
        activityType = when (fromActivity) {
            BottomNavigationActivity::class.java.simpleName -> ActivityType.ACCOUNT_LANDING
            AccountSignedInActivity::class.java.simpleName -> ActivityType.PRODUCT_LANDING
            PaymentOptionActivity::class.java.simpleName -> ActivityType.PAYMENT_OPTIONS
            WTransactionsActivity::class.java.simpleName -> ActivityType.TRANSACTION
            StatementActivity::class.java.simpleName -> ActivityType.STATEMENT
            AbsaStatementsActivity::class.java.simpleName -> ActivityType.ABSA_STATEMENT
            else -> throw RuntimeException("$fromActivity value not supported")
        }
    }

    @SuppressLint("DefaultLocale")
    fun getApplyNowState(): ApplyNowState {
        return when (getAccount()?.productGroupCode?.toLowerCase()) {
            "sc" -> ApplyNowState.STORE_CARD
            "pl" -> ApplyNowState.PERSONAL_LOAN
            "cc" -> when (getAccount()?.accountNumberBin) {
                Utils.SILVER_CARD -> ApplyNowState.SILVER_CREDIT_CARD
                Utils.BLACK_CARD -> ApplyNowState.BLACK_CREDIT_CARD
                Utils.GOLD_CARD -> ApplyNowState.GOLD_CREDIT_CARD
                else -> ApplyNowState.STORE_CARD
            }
            else -> ApplyNowState.STORE_CARD
        }
    }

    fun postEventChatOffline() = chatTrackPostEvent.onChatOffline(applyNowState = getApplyNowState())

    fun postChatEventInitiateSession() {
        if (isOperatingHoursForInAppChat() == false) return
        val applyNowState = getApplyNowState()
        with(chatTrackPostEvent) {
            when (getSessionType()) {
                SessionType.Collections -> {
                    when (activityType) {
                        ActivityType.ACCOUNT_LANDING -> onChatCollectionsLandingInitiateSession(applyNowState)
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

    fun triggerFirebaseOnlineOfflineChatEvent() {
        if (isOperatingHoursForInAppChat() == true)
            trackFirebaseEvent.chatOnline(getApplyNowState(), activityType)
        else
            trackFirebaseEvent.chatOffline(getApplyNowState(), activityType)
    }

    fun triggerFirebaseEventChatBreak() {
        trackFirebaseEvent.chatBreak(getApplyNowState(), activityType)
    }

    fun triggerFirebaseEventEndSession() {
        trackFirebaseEvent.chatEnd(getApplyNowState(), activityType)
    }
}