package za.co.woolworths.financial.services.android.onecartgetstream.channel

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

class ChannelListViewModel: ViewModel() {

    private val _state = MutableLiveData<State>()
    val state: LiveData<State> = _state

    private val chatClient: ChatClient by lazy { ChatClient.instance() }

     fun fetchChannels() {
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

         // TODO: Confirm if this is right. Seem to be using only the first channelId, which gives a high risk of getting the wrong channel if there is more than 1 channel. Needs to be fixed - TBC
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