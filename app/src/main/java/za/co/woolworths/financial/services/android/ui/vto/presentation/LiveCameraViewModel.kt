package za.co.woolworths.financial.services.android.ui.vto.presentation


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.perfectcorp.perfectlib.MakeupCam
import dagger.hilt.android.lifecycle.HiltViewModel
import za.co.woolworths.financial.services.android.ui.vto.data.LiveCameraRepository
import za.co.woolworths.financial.services.android.ui.vto.data.model.LiveCameraBitmapImages
import javax.inject.Inject

@HiltViewModel
class LiveCameraViewModel @Inject constructor(

    private val liveCameraRepository: LiveCameraRepository

) : ViewModel() {

    private var _colorMappedResult = MutableLiveData<Any>()
    val colorMappedResult: LiveData<Any> get() = _colorMappedResult

    private var _takenPicture = MutableLiveData<LiveCameraBitmapImages>()
    val takenPicture: LiveData<LiveCameraBitmapImages> get() = _takenPicture

    private var _selectedSkuResult = MutableLiveData<Any>()
    val selectedSkuResult: LiveData<Any> get() = _selectedSkuResult


    fun liveCameraVtoApplier(
        makeupCam: MakeupCam?,
        productId: String?,
        sku: String?
    ) {
        _colorMappedResult = liveCameraRepository.liveCameraVtoApplier(makeupCam, productId, sku)

    }

    fun clearLiveCameraEffect() = liveCameraRepository.clearEffect()

    fun takenPicture() {
        _takenPicture = liveCameraRepository.takePhoto()
    }

    fun applyVtoEffectOnLiveCamera(
        productId: String?,
        sku: String?
    ) {
        _selectedSkuResult = liveCameraRepository.applyVtoEffectOnLiveCamera(productId, sku)

    }


}