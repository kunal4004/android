package za.co.woolworths.financial.services.android.ui.fragments.account.chat.helper

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import za.co.woolworths.financial.services.android.models.dto.chat.amplify.Conversation
import za.co.woolworths.financial.services.android.models.dto.chat.amplify.SessionType

@Parcelize
data class LiveChatExtraParams(val productOfferingId: String?,
                               val accountNumber: String?,
                               var sessionType: SessionType?,
                               var fromActivity: String,
                               var chatAccountProductLandingPage: String,
                               var chatCollectionAgent: Boolean = false,
                               var userShouldSignIn: Boolean = true,
                               var conversation: Conversation?,
                               var absaCardList: String? = null,
                               var unReadMessageCount: Int = 0) : Parcelable