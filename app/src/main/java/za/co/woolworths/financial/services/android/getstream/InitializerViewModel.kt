
package za.co.woolworths.financial.services.android.getstream

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.ChatDomain
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import za.co.woolworths.financial.services.android.getstream.common.State
import za.co.woolworths.financial.services.android.getstream.network.OCAuthenticationDto
import za.co.woolworths.financial.services.android.getstream.network.OCAuthenticationResponse
import za.co.woolworths.financial.services.android.getstream.network.OneCartService
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.util.SessionUtilities

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

    private lateinit var atgId: String //retrieved from the JWT Token
    private lateinit var displayName: String //retrieved from the JWT Token, firstName + lastName
    private lateinit var userId: String //retrieved from OneCart Authentication API
    private lateinit var token: String //retrieved from OneCart Authentication API

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

        val jwtToken = SessionUtilities.getInstance().jwt
        jwtToken.AtgId?.apply {
            atgId = if (this.isJsonArray) this.asJsonArray.first().asString else this.asString

            displayName = (jwtToken.name.first() + " " + jwtToken.family_name.first())
        }

        OneCartService.instance.api.authenticate(OCAuthenticationDto(atgId, displayName)).enqueue(
                object : Callback<OCAuthenticationResponse>{
                    override fun onResponse(call: Call<OCAuthenticationResponse>, response: Response<OCAuthenticationResponse>) {
                        val responseBody: OCAuthenticationResponse = response.body() ?: return

                        userId = responseBody.details.userId
                        token = responseBody.details.token

                        initChatUser()
                    }

                    override fun onFailure(call: Call<OCAuthenticationResponse>, t: Throwable) {
                        _state.postValue(State.Error(t.message))
                    }
                }
        )
    }

    private fun initChatSdk() {
        val client = ChatClient.Builder("94v4edc6mnn8", WoolworthsApplication.getAppContext())
                .logLevel(ChatLogLevel.ALL)
                .build()

        ChatDomain.Builder(client, WoolworthsApplication.getAppContext())
                .userPresenceEnabled()
                .offlineEnabled()
                .build()
    }

    private fun initChatUser() {
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
}