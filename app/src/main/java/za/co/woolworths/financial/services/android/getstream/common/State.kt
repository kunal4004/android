package za.co.woolworths.financial.services.android.getstream.common

sealed class State {
    object RedirectToChannels : State()
    data class RedirectToChat(val channelId: String) : State()
    object Loading : State()
    data class Error(val errorMessage: String?) : State()
}