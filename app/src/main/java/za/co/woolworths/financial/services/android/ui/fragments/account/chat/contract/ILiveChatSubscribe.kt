package za.co.woolworths.financial.services.android.ui.fragments.account.chat.contract

import com.amplifyframework.api.ApiException
import za.co.woolworths.financial.services.android.models.dto.chat.amplify.SendMessageResponse

interface ILiveChatSubscribe {
    fun onSubscribe(onSuccess: (SendMessageResponse?) -> Unit, onFailure: (ApiException) -> Unit)
    fun onCancel()
}