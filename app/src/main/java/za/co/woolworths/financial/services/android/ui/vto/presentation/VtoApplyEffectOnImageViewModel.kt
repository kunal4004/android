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

    private var _isFaceNotDetect = MutableLiveData<Boolean>()
    val isFaceDetect: LiveData<Boolean> get() = _isFaceNotDetect

    private var _isImagePathNotValid = MutableLiveData<Boolean>()
    val isImagePathValid: LiveData<Boolean> get() = _isImagePathNotValid

    private var _clearEffectImage = MutableLiveData<Bitmap>()
    val clearEffectImage: LiveData<Bitmap> get() = _clearEffectImage


    fun setApplier(uri: Uri?, productId: String?, sku: String?) = applyVtoImageRepository.setVtoApplier(uri, productId, sku)


    fun loadPhoto(uri: Uri?, productId: String?, sku: String?) {
        _isImagePathNotValid = applyVtoImageRepository.loadPhoto(uri, productId, sku)
    }


    fun detectFace(uri: Uri?, productId: String?, sku: String?) {

       // applyVtoImageRepository.detectFace(uri,productId,sku)

    }


    fun applyEffect(productId: String?, sku: String?)
    {
        _applyEffectImage = applyVtoImageRepository.applyEffect(productId, sku)
    }


  //  fun getFaceDetection() = _isFaceNotDetect.postValue(applyVtoImageRepository.faceNotDetect())

    fun clearEffect() {
        _clearEffectImage = applyVtoImageRepository.clearEffect()
    }


}