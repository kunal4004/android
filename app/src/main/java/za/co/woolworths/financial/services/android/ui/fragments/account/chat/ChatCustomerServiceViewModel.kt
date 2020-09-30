package za.co.woolworths.financial.services.android.ui.fragments.account.chat

import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.chat.TradingHours
import za.co.woolworths.financial.services.android.models.dto.chat.amplify.Conversation
import za.co.woolworths.financial.services.android.models.dto.chat.amplify.SendMessageResponse
import za.co.woolworths.financial.services.android.models.dto.chat.amplify.SessionStateType
import za.co.woolworths.financial.services.android.models.dto.chat.amplify.SessionType
import za.co.woolworths.financial.services.android.util.KotlinUtils

class ChatCustomerServiceViewModel : ViewModel() {

    private var customerServiceAWSAmplify: ChatCustomerServiceAWSAmplify? = null
    private var conversation: Conversation? = null
    private var mAccount: MutableLiveData<Account?> = MutableLiveData()
    private var sessionStateType: MutableLiveData<SessionStateType?> = MutableLiveData()
    private var sessionType: MutableLiveData<SessionType?> = MutableLiveData()
    var isChatToCollectionAgent: MutableLiveData<Boolean> = MutableLiveData()
    var isCustomerSignOut: MutableLiveData<Boolean> = MutableLiveData()

    init {
        isChatToCollectionAgent.value = false
        isCustomerSignOut.value = false
        setSessionStateType(SessionStateType.DISCONNECT)
        setSessionType(SessionType.Collections)
    }

    fun initAmplify() {
        this.customerServiceAWSAmplify = ChatCustomerServiceAWSAmplify(getAccount())
    }

    fun getAmplify(): ChatCustomerServiceAWSAmplify? {
        return customerServiceAWSAmplify
    }

    fun setAccount(account: Account?) {
        mAccount.value = account
    }

    fun getAccount(): Account? {
        return mAccount.value
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

    fun getSessionType(): SessionType {
        return sessionType.value ?: SessionType.Collections
    }

    fun signIn(result: () -> Unit, failure: (Any) -> Unit) {
        customerServiceAWSAmplify?.apply {
            signIn({ conversation ->
                this@ChatCustomerServiceViewModel.conversation = conversation
                result()
            }, { failure -> failure(failure) })
        }
    }

    fun subscribeToMessageByConversationId(result: (SendMessageResponse?) -> Unit, failure: (Any) -> Unit) {
        customerServiceAWSAmplify?.subscribeToMessageByConversationId(getConversationMessageId(), getSessionType(), { data -> result(data) }, { failure(failure) })
    }

    private fun getConversationMessageId(): String {
        Log.e("subscribeToMessageId", conversation?.id)
        return conversation?.id ?: ""
    }

    override fun onCleared() {
        customerServiceAWSAmplify?.cancelSubscribeMessageByConversationId()
        super.onCleared()
    }

    fun userStartedTyping() {
        setSessionStateType(SessionStateType.TYPING)
        customerServiceAWSAmplify?.sendMessage(getConversationMessageId(), getSessionType(), getSessionStateType(), "")
    }

    //TODO:: how to stop typing
    fun userStoppedTyping() {
    }

    fun sendMessage(message: String) {
        customerServiceAWSAmplify?.sendMessage(getConversationMessageId(), getSessionType(), getSessionStateType(), message)
    }

    fun signOut(result: () -> Unit, error: () -> Unit) {
        customerServiceAWSAmplify?.queryServiceSignOut(getConversationMessageId(), SessionType.Collections, SessionStateType.DISCONNECT, "", { result() }, {
            error()
        })
    }


    private fun getTradingHours(): MutableList<TradingHours>? {
        return when (getSessionType()) {
            SessionType.Collections -> WoolworthsApplication.getPresenceInAppChat()?.collections?.tradingHours
            SessionType.CustomerService -> WoolworthsApplication.getPresenceInAppChat()?.customerService?.tradingHours
            else -> WoolworthsApplication.getPresenceInAppChat().tradingHours
        }
    }

    fun isOperatingHoursForInAppChat(): Boolean? {
        return getTradingHours()?.let { KotlinUtils.isOperatingHoursForInAppChat(it) } ?: false
    }

    fun getInAppTradingHoursForToday(): TradingHours? {
        return getTradingHours()?.let { KotlinUtils.getInAppTradingHoursForToday(it) }
    }

    fun offlineMessageTemplate(onClick: (Triple<String, String, String>) -> Unit): SpannableString {
        val presenceInAppChat = WoolworthsApplication.getPresenceInAppChat()
        return when (getSessionType()) {
            SessionType.Collections, SessionType.Fraud -> {
                val collections = presenceInAppChat.collections
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
                val customerService = presenceInAppChat.customerService
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
}