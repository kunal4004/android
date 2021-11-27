package za.co.woolworths.financial.services.android.ui.vto.data

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import com.perfectcorp.perfectlib.MakeupCam


interface LiveCameraRepository {

    fun liveCameraVtoApplier(
        makeupCam: MakeupCam?,
        productId: String?,
        sku: String?
    ): MutableLiveData<Any>

    fun applyVtoEffectOnLiveCamera(
        productId: String?,
        sku: String?
    ): MutableLiveData<Any>

    fun takePhoto(): MutableLiveData<Any>
    fun clearEffect()
    fun getOriginalPicture(): MutableLiveData<Bitmap?>

}