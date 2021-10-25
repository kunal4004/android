package za.co.woolworths.financial.services.android.ui.vto.data

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.MutableLiveData

interface ApplyVtoImageRepository {

    fun setVtoApplier(uri: Uri?, productId: String?, sku: String?)
    fun loadPhoto(uri: Uri?, productId: String?, sku: String?): MutableLiveData<Boolean>
    fun detectFace(image: Bitmap?, productId: String?, sku: String?): MutableLiveData<Boolean>
    fun applyEffect(productId: String?, sku: String?): MutableLiveData<Any>
    fun clearEffect() : MutableLiveData<Bitmap>

}