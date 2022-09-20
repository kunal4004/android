package za.co.woolworths.financial.services.android.onecartgetstream.channel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Filters.`in`
import io.getstream.chat.android.client.models.Filters.and
import za.co.woolworths.financial.services.android.onecartgetstream.common.State
import za.co.woolworths.financial.services.android.onecartgetstream.service.DashChatMessageListeningService

class ChannelListViewModel: ViewModel() {

    private val _state = MutableLiveData<State>()
    val state: LiveData<State> = _state

    private val chatClient: ChatClient by lazy { ChatClient.instance() }

     fun fetchChannel(context: Context, orderId: String?, channelId: String?) {
         if (channelId != null) {
             _state.postValue(State.RedirectToChat(channelId))
         } else if (orderId != null) {
             _state.postValue(State.Loading)
             DashChatMessageListeningService.getChannelForOrder(
                 context,
                 orderId,
                 onSuccess = { channel ->
                     _state.postValue(State.RedirectToChat(channel.cid))
                 },
                 onFailure = {
                     // Get first available channel as fallback
                     fetchChannel(context, null, null)
                 }
             )
         } else {
             val user = chatClient.getCurrentUser()

             if(user == null){
                 _state.postValue(State.Error(""))
                 return
             }

             _state.postValue(State.Loading)

             val filter = and(
                 `in`("members", listOf<String>(user.id))
             )
             val sort = QuerySort.desc<Channel>("created_at")
             val request = QueryChannelsRequest(
                 filter = filter,
                 querySort = sort,
                 limit = 10
             )

             // This will return the first channel available
             chatClient.queryChannels(request).enqueue { result ->
                 if (result.isSuccess && (!result.data().isNullOrEmpty())) {
                     val channelId = result.data().first().cid
                     _state.postValue(State.RedirectToChat(channelId))
                 } else {
                     _state.postValue(State.Error(""))
                 }
             }
         }

    }
}