package za.co.woolworths.financial.services.android.ui.vto.data

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.MutableLiveData

interface ApplyVtoImageRepository {

    fun setVtoApplier(uri: Uri?, productId: String?, sku: String?, isFromLiveCamera: Boolean): MutableLiveData<Any>
    fun applyEffect(productId: String?, sku: String?): MutableLiveData<Any>
    fun clearEffect(): MutableLiveData<Bitmap>

}