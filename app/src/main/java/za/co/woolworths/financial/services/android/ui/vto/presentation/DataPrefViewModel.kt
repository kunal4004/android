package za.co.woolworths.financial.services.android.ui.vto.presentation

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.ui.vto.prefstore.PrefsStore


class DataPrefViewModel @ViewModelInject constructor(
    private val prefsStore: PrefsStore

) : ViewModel() {

    val isLightingTips = prefsStore.isLightingTipsFirstTime().asLiveData()
    val isTryItOn = prefsStore.isTryItOnFirstTime().asLiveData()


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