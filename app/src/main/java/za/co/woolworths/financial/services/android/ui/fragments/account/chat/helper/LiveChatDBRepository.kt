package za.co.woolworths.financial.services.android.ui.fragments.account.chat.helper

import android.annotation.SuppressLint
import com.awfs.coordination.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.Card
import za.co.woolworths.financial.services.android.models.dto.account.AccountsProductGroupCode
import za.co.woolworths.financial.services.android.models.dto.chat.amplify.Conversation
import za.co.woolworths.financial.services.android.models.dto.chat.amplify.SessionType
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatAWSAmplify
import za.co.woolworths.financial.services.android.util.db.DatabaseManager
import java.util.*

class LiveChatDBRepository : DatabaseManager() {

    companion object {
        private val KEY_LIVE_CHAT_DB = SessionDao.KEY.LIVE_CHAT_EXTRAS
    }

    fun saveLiveChatParams(liveChatExtraParams: LiveChatExtraParams?) =
        saveToDB(KEY_LIVE_CHAT_DB, liveChatExtraParams)

    fun saveConversation(conversation: Conversation) {
        val liveChatParams = getLiveChatParams()
        liveChatParams?.conversation = conversation
        saveLiveChatParams(liveChatParams)
    }

    fun saveABSACardsList(cardLists: List<Card?>?) {
        val liveChatParams = getLiveChatParams()
        liveChatParams?.absaCardList = Gson().toJson(cardLists)
        saveLiveChatParams(liveChatParams)
    }

    fun resetUnReadMessageCount() {
        val liveChatParams = getLiveChatParams()
        liveChatParams?.unReadMessageCount = 0
        saveLiveChatParams(liveChatParams)
    }

    fun updateUnreadMessageCount(count : Int  = 1) {
        val liveChatParams = getLiveChatParams()
        val unreadCount = liveChatParams?.unReadMessageCount?.plus(count) ?: 0
        liveChatParams?.unReadMessageCount = unreadCount
        saveLiveChatParams(liveChatParams)
    }

    fun getUnReadMessageCount(): Int {
        val liveChatParams = getLiveChatParams()
        return liveChatParams?.unReadMessageCount ?: 0
    }

    fun getLiveChatParams() = getDataFromDB(KEY_LIVE_CHAT_DB, LiveChatExtraParams::class.java)

    fun getSessionType() = getLiveChatParams()?.sessionType ?: SessionType.Collections

    fun getAccount(): Account? =
        Gson().fromJson(getLiveChatParams()?.chatAccountProductLandingPage, Account::class.java)

    fun isCreditCardAccount(): Boolean = getAccount()?.productGroupCode.equals(
        AccountsProductGroupCode.CREDIT_CARD.groupCode,
        ignoreCase = true
    )

    @SuppressLint("DefaultLocale")
    fun getSessionVars(): String {

        val customerInfo = ChatCustomerInfo
        val account = getAccount()

        val prsAccountNumber = account?.accountNumber ?: ""
        val productGroupCode = account?.productGroupCode?.toLowerCase(Locale.getDefault())
        val isCreditCard =
            productGroupCode == AccountsProductGroupCode.CREDIT_CARD.groupCode.toLowerCase()
        val prsCardNumber = if (isCreditCard) getABSACardToken() else null
        val prsC2id = customerInfo.getCustomerC2ID()
        val prsFirstname = customerInfo.getCustomerUsername()
        val prsSurname = customerInfo.getCustomerFamilyName()
        val prsProductOfferingId = account?.productOfferingId?.toString() ?: "0"
        val prsProductOfferingDescription =
            when (productGroupCode?.let { AccountsProductGroupCode.getEnum(it) }) {
                AccountsProductGroupCode.STORE_CARD -> "StoreCard"
                AccountsProductGroupCode.PERSONAL_LOAN -> "PersonalLoan"
                AccountsProductGroupCode.CREDIT_CARD -> "CreditCard"
                else -> ""
            }

        return bindString(
            R.string.chat_send_message_session_var_params,
            prsAccountNumber,
            prsCardNumber,
            prsC2id,
            prsFirstname,
            prsSurname,
            prsProductOfferingId,
            prsProductOfferingDescription
        )
    }

    fun getABSACardToken(): String? {
        val account = getAccount()
        val absaCardList = getLiveChatParams()?.absaCardList
        val absaCard = if (!absaCardList.isNullOrEmpty()) Gson().fromJson(absaCardList, object : TypeToken<List<Card>>() {}.type) else null
        val result: List<Card>? = account?.cards ?: absaCard
        return if(result?.isNotEmpty() == true) result[0].absaCardToken else null
    }

    fun clearData() {
        var liveChatParams = getLiveChatParams()
        liveChatParams?.conversation = null
        ChatAWSAmplify.isLiveChatActivated = false
        liveChatParams = null
        saveLiveChatParams(liveChatParams)
    }

    fun getConversationMessageId(): String = getConversation()?.id ?: ""

    fun getConversation() = getLiveChatParams()?.conversation

}