package za.co.woolworths.financial.services.android.ui.vto.presentation


import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import za.co.woolworths.financial.services.android.ui.vto.data.ApplyVtoImageRepository
import javax.inject.Inject

@HiltViewModel
class VtoApplyEffectOnImageViewModel @Inject constructor(

    private val applyVtoImageRepository: ApplyVtoImageRepository

) : ViewModel() {

    private var _applyEffectImage = MutableLiveData<Any>()
    val applyEffectImage: LiveData<Any> get() = _applyEffectImage

    private var _applyEffectResult = MutableLiveData<Any>()
    val applyEffectResult: LiveData<Any> get() = _applyEffectResult

    private var _clearEffectImage = MutableLiveData<Bitmap>()
    val clearEffectImage: LiveData<Bitmap> get() = _clearEffectImage


    fun setApplier(uri: Uri?, productId: String?, sku: String?, captureLiveCameraImg : Bitmap?, isFromLiveCamera: Boolean) {

        _applyEffectResult =  applyVtoImageRepository.setVtoApplier(uri, productId, sku, captureLiveCameraImg, isFromLiveCamera)
    }

    fun applyEffect(productId: String?, sku: String?)
    {
        _applyEffectImage = applyVtoImageRepository.applyEffect(productId, sku)
    }

    fun clearEffect() {
        _clearEffectImage = applyVtoImageRepository.clearEffect()
    }


}