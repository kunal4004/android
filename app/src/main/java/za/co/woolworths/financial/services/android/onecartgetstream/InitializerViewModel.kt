
package za.co.woolworths.financial.services.android.onecartgetstream

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.ChatDomain
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.onecartgetstream.common.State
import za.co.woolworths.financial.services.android.onecartgetstream.model.OCAuthenticationResponse
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.network.Event
import za.co.woolworths.financial.services.android.models.network.Resource
import za.co.woolworths.financial.services.android.onecartgetstream.repository.OCAuthRepository
import za.co.woolworths.financial.services.android.util.NetworkManager
import javax.inject.Inject

@HiltViewModel
class InitializerViewModel @Inject constructor(
private val ocAuthRepository: OCAuthRepository
) : ViewModel() {

    private val _state = MutableLiveData<State>()
    val state: LiveData<State> = _state
    private val _ocAuthData = MutableLiveData<Event<Resource<OCAuthenticationResponse>>>()
    val ocAuthData: LiveData<Event<Resource<OCAuthenticationResponse>>> = _ocAuthData

    init {
        initChatSdk()
        authenticateUserIfNeeded()
    }

    private fun authenticateUserIfNeeded(){
        val currentUser = ChatClient.instance().getCurrentUser()
        currentUser?.let {
            _state.postValue(State.RedirectToChannels)
            return
        }
        viewModelScope.launch {
            val response = ocAuthRepository.getOCAuthToken()
            _ocAuthData.value = Event(response)

       }
    }

    private fun initChatSdk() {
        val client = ChatClient.Builder(AppConfigSingleton.dashConfig?.inAppChat?.apiKey.toString(), WoolworthsApplication.getAppContext())
                .logLevel(ChatLogLevel.ALL)
                .build()

        ChatDomain.Builder(client, WoolworthsApplication.getAppContext())
                .userPresenceEnabled()
                .offlineEnabled()
                .build()
    }

   internal fun initChatUser(userId: String, displayName: String, token: String) {
        /*
        * WARNING
        * You shouldn't call connectUser if the user is already set! You can use ChatClient.instance().getCurrentUser() to verify if the user is already connected.
        * */
        val currentUser = ChatClient.instance().getCurrentUser()
        currentUser?.let {
            _state.postValue(State.RedirectToChannels)
            return
        }

        val chatUser = User().apply {
            id = userId
            name = displayName
        }

        ChatClient.instance().connectUser(chatUser, token)
                .enqueue { result ->
                    if (result.isSuccess) {
                        _state.postValue(State.RedirectToChannels)
                    } else {
                        _state.postValue(State.Error(result.error().message))
                    }
                }
    }

    fun isConnectedToInternet(context: Context) =
        NetworkManager.getInstance().isConnectedToNetwork(context)
}