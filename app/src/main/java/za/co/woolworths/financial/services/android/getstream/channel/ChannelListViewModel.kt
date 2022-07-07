package za.co.woolworths.financial.services.android.getstream.channel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Filters.`in`
import io.getstream.chat.android.client.models.Filters.and
import io.getstream.chat.android.client.models.Filters.eq
import za.co.woolworths.financial.services.android.getstream.common.State

class ChannelListViewModel: ViewModel() {

    private val _state = MutableLiveData<State>()
    val state: LiveData<State> = _state

    private val chatClient: ChatClient by lazy { ChatClient.instance() }

    public fun fetchChannels() {
        val user = chatClient.getCurrentUser()

        if(user == null){
            _state.postValue(State.Error("Unable to retrieve channels when connection is not established."))
            return
        }

        _state.postValue(State.Loading)

        // 1. Get the first 30 channels to which thierry belongs
        val filter = and(
                eq("type", "messaging"),
                `in`("members", listOf<String>(user.id))
        )
        val sort = QuerySort.desc<Channel>("created_at")
        val request = QueryChannelsRequest(
                filter = filter,
                querySort = sort,
                limit = 10
        )

        chatClient.queryChannels(request).enqueue { result ->
            if (result.isSuccess) {
                val channelId = result.data().first().cid
                _state.postValue(State.RedirectToChat(channelId))
            } else {
                _state.postValue(State.Error(result.error().message))
            }
        }
    }
}