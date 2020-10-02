package za.co.woolworths.financial.services.android.ui.fragments.account.chat

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
import za.co.woolworths.financial.services.android.models.JWTDecodedModel
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.Card
import za.co.woolworths.financial.services.android.models.dto.CreditCardTokenResponse
import za.co.woolworths.financial.services.android.models.dto.chat.TradingHours
import za.co.woolworths.financial.services.android.models.dto.chat.amplify.Conversation
import za.co.woolworths.financial.services.android.models.dto.chat.amplify.SendMessageResponse
import za.co.woolworths.financial.services.android.models.dto.chat.amplify.SessionStateType
import za.co.woolworths.financial.services.android.models.dto.chat.amplify.SessionType
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.extension.request
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.SessionUtilities
import java.util.*

class ChatCustomerServiceViewModel : ViewModel() {

    private var jWTDecodedModel: JWTDecodedModel? = SessionUtilities.getInstance().jwt
    private var creditCardTokenAPI: Call<CreditCardTokenResponse>? = null
    private var customerServiceAWSAmplify: ChatCustomerServiceAWSAmplify? = null
    private var conversation: Conversation? = null
    private var mAccount: MutableLiveData<Account?> = MutableLiveData()
    private var sessionStateType: MutableLiveData<SessionStateType?> = MutableLiveData()
    private var sessionType: MutableLiveData<SessionType?> = MutableLiveData()
    var isChatToCollectionAgent: MutableLiveData<Boolean> = MutableLiveData()
    var isCustomerSignOut: MutableLiveData<Boolean> = MutableLiveData()
    var absaCreditCard: MutableLiveData<MutableList<Card>?> = MutableLiveData()

    init {
        absaCreditCard.value = getAccount()?.cards
        isChatToCollectionAgent.value = false
        isCustomerSignOut.value = false
        setSessionStateType(SessionStateType.DISCONNECT)
        setSessionType(SessionType.Collections)
    }

    fun initAmplify() {
        this.customerServiceAWSAmplify = ChatCustomerServiceAWSAmplify()
    }

    fun setAccount(account: Account?) {
        mAccount.value = account
    }

    fun getAccount(): Account? {
        return mAccount.value
    }


    fun isCreditCardAccount(): Boolean {
        return getAccount()?.productGroupCode == "cc"
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
        customerServiceAWSAmplify?.apply {
            signIn({ conversation ->
                this@ChatCustomerServiceViewModel.conversation = conversation
                result()
            }, { failure -> failure(failure) })
        }
    }

    fun subscribeToMessageByConversationId(result: (SendMessageResponse?) -> Unit, failure: (Any) -> Unit) {
        customerServiceAWSAmplify?.subscribeToMessageByConversationId(
                getConversationMessageId(),
                getSessionType(),
                getSessionVars(),
                getCustomerFamilyName(),
                getCustomerEmail(),
                { data -> result(data) }, { failure(failure) })
    }

    private fun getConversationMessageId(): String = conversation?.id ?: ""

    override fun onCleared() {
        customerServiceAWSAmplify?.cancelSubscribeMessageByConversationId()
        creditCardTokenAPI?.apply {
            if (!isCanceled)
                cancel()

        }
        super.onCleared()
    }


    fun sendMessage(content: String) {
        customerServiceAWSAmplify?.sendMessage(
                getConversationMessageId(),
                getSessionType(),
                getSessionStateType(),
                content,
                getSessionVars(),
                getCustomerFamilyName(),
                getCustomerEmail())
    }

    fun signOut(result: () -> Unit) {
        customerServiceAWSAmplify?.queryServiceSignOut(
                getConversationMessageId(),
                getSessionType(),
                SessionStateType.DISCONNECT,
                "",
                getSessionVars(),
                getCustomerFamilyName(),
                getCustomerEmail(),
                { result() }, { result() })
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
        when (getSessionType()) {
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
        val prsC2id = getCustomerC2ID()
        val prsFirstname = getCustomerUsername()
        val prsSurname = getCustomerFamilyName()
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

    private fun getCustomerFamilyName(): String {
        val familyName = jWTDecodedModel?.family_name?.get(0)
        return KotlinUtils.firstLetterCapitalization(familyName) ?: ""
    }

    internal fun getCustomerUsername(): String {
        val username = jWTDecodedModel?.name?.get(0)
        return KotlinUtils.firstLetterCapitalization(username) ?: ""
    }

    private fun getCustomerEmail() = jWTDecodedModel?.email?.get(0) ?: ""

    private fun getCustomerC2ID() = jWTDecodedModel?.C2Id ?: ""

    fun getABSACardToken(): String? = getAccount()?.cards?.get(0)?.absaCardToken
            ?: absaCreditCard.value?.get(0)?.absaCardToken
}