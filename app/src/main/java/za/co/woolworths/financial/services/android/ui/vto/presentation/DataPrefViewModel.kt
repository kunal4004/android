package za.co.woolworths.financial.services.android.ui.vto.presentation


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

    val isLightingTipsGallery = prefsStore.isLightingTipsGallery().asLiveData()
    val isLightingTipsFiles = prefsStore.isLightingTipsFiles().asLiveData()
    val isLightingTipsTakePhoto = prefsStore.isLightingTipsTakePhoto().asLiveData()
    val isLightingTipsCamera = prefsStore.isLightingTipsLiveCamera().asLiveData()
    val isTryItOn = prefsStore.isTryItOnFirstTime().asLiveData()


    fun disableLightingGallery(disable: Boolean) {
        viewModelScope.launch {
            prefsStore.disableLightingTipsGallery(disable)
        }
    }
    fun disableLightingFiles(disable: Boolean) {
        viewModelScope.launch {
            prefsStore.disableLightingFiles(disable)
        }
    }
    fun disableLightingTipsTakePhoto(disable: Boolean) {
        viewModelScope.launch {
            prefsStore.disableLightingTipsTakePhoto(disable)
        }
    }
    fun disableLightingTipsLiveCamera(disable: Boolean) {
        viewModelScope.launch {
            prefsStore.disableLightingTipsLiveCamera(disable)
        }
    }

    fun disableTryItOn(disable: Boolean) {
        viewModelScope.launch {
            prefsStore.disableTryItOnMode(disable)
        }
    }

}