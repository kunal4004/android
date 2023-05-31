package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_message.data


import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import za.co.woolworths.financial.services.android.models.dto.MessageResponse
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.ViewState
import za.co.woolworths.financial.services.android.ui.wfs.core.NetworkStatusUI
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.feature_message.data.network.MessageRemoteDataSource
import javax.inject.Inject

interface IMessage {
    suspend fun queryMessageRemoteService(_messageState: MutableStateFlow<NetworkStatusUI<MessageResponse>>)
}

class IMessageImpl @Inject constructor(private val remote : MessageRemoteDataSource) : IMessage {
    override suspend fun queryMessageRemoteService(_messageState: MutableStateFlow<NetworkStatusUI<MessageResponse>>) {
        remote.fetchAllMessages().collect { result ->
            when(result) {
                is ViewState.Loading ->  _messageState.update {
                    it.copy(
                        isLoading = result.isLoading,
                        hasError = false)
                }
                is ViewState.RenderSuccess ->   _messageState.update {
                    it.copy(
                        data = result.output,
                        isLoading = false,
                        hasError = false)
                }
                else -> Unit
            }
        }
    }
}