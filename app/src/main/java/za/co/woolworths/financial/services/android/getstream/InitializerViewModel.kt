
package za.co.woolworths.financial.services.android.getstream

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.ChatDomain
import za.co.woolworths.financial.services.android.getstream.common.State
import za.co.woolworths.financial.services.android.models.WoolworthsApplication

class InitializerViewModel: ViewModel() {

    private val _state = MutableLiveData<State>()
    val state: LiveData<State> = _state

//                        App.instance.userRepository.setUser(
//                                SampleUser(
//                                        apiKey = loginCredentials.apiKey,
//                                        id = loginCredentials.userId,
//                                        name = loginCredentials.userName,
//                                        token = loginCredentials.userToken,
//                                        image = "https://getstream.io/random_png?id=${loginCredentials.userId}&name=${loginCredentials.userName}&size=200"
//                                )
//                        )

    private val atgId: String //retrieved from the JWT Token
    private val displayName: String //retrieved from the JWT Token, firstName + lastName
    private val userId: String //retrieved from OneCart Authentication API
    private val token: String //retrieved from OneCart Authentication API

    init {
        atgId = "262820175"
        displayName = "Eesa Jacobs"
        userId = "CUST-WWO-DEV-262820175"
        token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiQ1VTVC1XV08tREVWLTI2MjgyMDE3NSIsImV4cCI6MTY1NzIzNDQyN30.N-5f8kofgSiboYt4UEs2J4xcWufBWI5bENF8vmRWehM"

        initChatSdk()
        initChatUser()
    }

    /**
     * This snippet should be placed in the WoolworthsApplication
     * class and be initialised once.
     */
    private fun initChatSdk() {
        val client = ChatClient.Builder("94v4edc6mnn8", WoolworthsApplication.getAppContext())
                .logLevel(ChatLogLevel.ALL)
                .build()

        val domain = ChatDomain.Builder(client, WoolworthsApplication.getAppContext())
                .userPresenceEnabled()
                .offlineEnabled()
                .build()
    }

    private fun initChatUser() {
        /*
        * WARNING
        * You shouldn't call connectUser if the user is already set! You can use ChatClient.instance().getCurrentUser() to verify if the user is already connected.
        * */

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
}