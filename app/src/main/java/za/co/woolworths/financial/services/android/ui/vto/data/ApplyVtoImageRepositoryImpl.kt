package za.co.woolworths.financial.services.android.ui.vto.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.perfectcorp.perfectlib.*
import dagger.hilt.android.qualifiers.ApplicationContext
import za.co.woolworths.financial.services.android.ui.vto.ui.PfSDKInitialCallback
import za.co.woolworths.financial.services.android.ui.vto.ui.SdkUtility
import java.util.*
import javax.inject.Inject


class ApplyVtoImageRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context

) : ApplyVtoImageRepository {

    private val _context = context
    private var photoMakeup: PhotoMakeup? = null
    private var applier: VtoApplier? = null

    override fun setVtoApplier(uri: Uri?, productId: String?, sku: String?) {
        SdkUtility.initSdk(
            _context,
            object : PfSDKInitialCallback {
                override fun onInitialized() {

                    PhotoMakeup.create(object : PhotoMakeup.CreateCallback {
                        override fun onSuccess(makeup: PhotoMakeup) {
                            photoMakeup = makeup

                            VtoApplier.create(photoMakeup, object : VtoApplier.CreateCallback {
                                override fun onSuccess(applierVTO: VtoApplier) {
                                    applier = applierVTO

                                    loadPhoto(uri, productId, sku)

                                }

                                override fun onFailure(throwable: Throwable) {

                                }
                            })
                        }

                        override fun onFailure(throwable: Throwable) {

                        }
                    })

                }
                override fun onFailure(
                    throwable: Throwable?
                ) {

                }
            })

    }

    override fun loadPhoto(uri: Uri?, productId: String?, sku: String?): MutableLiveData<Boolean> {
        val data = MutableLiveData<Boolean>()
        if (uri == null) {
            data.value = true
            val message = "No valid photo path."
            return data
        }
        try {

            _context!!.contentResolver.openInputStream(uri).use { imageStream ->
                val bitmap = BitmapFactory.decodeStream(imageStream)
                val matrix: Matrix =
                    SdkUtility.getRotationMatrixByExif(
                        _context!!.contentResolver,
                        uri
                    )
                val selectedImage =
                    Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                if (bitmap != selectedImage) {
                    bitmap.recycle()
                }
                data.value = false

                detectFace(selectedImage, productId, sku)

            }
        } catch (e: Exception) {
            val message = "Decode photo failed."
            data.value = true
        }
        return data
    }

    override fun detectFace(image: Bitmap?, productId: String?, sku: String?) : MutableLiveData<Boolean>{
        val data = MutableLiveData<Boolean>()
        photoMakeup?.detectFace(image, object : PhotoMakeup.DetectFaceCallback {

            override fun onSuccess(faceList: List<FaceData>) {

                if (faceList.isEmpty()) {
                    data.value = true
                       return

                }
                // Select a face for applying effects.
                val faceIndex = Random().nextInt(faceList.size)
                val faceData = faceList[faceIndex]
                photoMakeup!!.setFace(faceData)

            }

            override fun onFailure(throwable: Throwable) {


            }
        })
     return data
    }

    override fun applyEffect(productId: String?, sku: String?): MutableLiveData<Any> {
        val data = MutableLiveData<Any>()
        val vtoSetting = VtoSetting.builder()
            .setProductGuid(productId)
            .setSkuGuid(sku)
            .build()

        PerfectLib.setDownloadCacheStrategy(DownloadCacheStrategy.CACHE_ONLY)
        applier?.apply(
            listOf(vtoSetting),
            EffectConfig.DEFAULT,
            object : VtoApplier.ApplyCallback {
                override fun onSuccess(bitmap: Bitmap) {
                    data.value = bitmap

                }

                override fun onFailure(throwable: Throwable) {
                    //can not get mapped id for productId
                    data.value = "IDMismatch"

                }

                override fun applyProgress(progress: Double) {

                }
            })
        return data

    }


    override fun clearEffect(): MutableLiveData<Bitmap> {
        val data = MutableLiveData<Bitmap>()
        applier?.clearAllEffects(object : VtoApplier.ApplyCallback {
            override fun onSuccess(bitmap: Bitmap?) {
                data.value = bitmap!!
            }

            override fun onFailure(throwable: Throwable) {
            }

            override fun applyProgress(progress: Double) {
            }
        })
        return data
    }


}