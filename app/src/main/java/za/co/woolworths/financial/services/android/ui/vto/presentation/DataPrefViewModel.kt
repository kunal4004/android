package za.co.woolworths.financial.services.android.ui.vto.presentation


import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import za.co.woolworths.financial.services.android.ui.vto.data.prefstore.PrefsStore

@HiltViewModel
class DataPrefViewModel @Inject constructor(
    private val prefsStore: PrefsStore

) : ViewModel() {

    val isLightingTips = prefsStore.isLightingTipsFirstTime().asLiveData()
    val isTryItOn = prefsStore.isTryItOnFirstTime().asLiveData()
    val userImage = MutableLiveData<Uri>()

    fun disableLightingTips(disable: Boolean) {
        viewModelScope.launch {
            prefsStore.disableLightingTips(disable)
        }
    }

    fun disableTryItOn(disable: Boolean) {
        viewModelScope.launch {
            prefsStore.disableTryItOnMode(disable)
        }
    }

}