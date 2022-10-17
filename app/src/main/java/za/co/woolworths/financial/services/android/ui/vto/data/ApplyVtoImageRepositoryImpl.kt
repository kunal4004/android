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
import za.co.woolworths.financial.services.android.ui.vto.utils.SdkUtility
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.SDK_INIT_FAIL
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.VTO_COLOR_NOT_MATCH
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.VTO_FACE_NOT_DETECT
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.VTO_FAIL_IMAGE_LOAD
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.VTO_INVALID_IMAGE_PATH
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import java.util.*
import javax.inject.Inject


class ApplyVtoImageRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context

) : ApplyVtoImageRepository {

    private val context = context
    private var photoMakeup: PhotoMakeup? = null
    private var applier: VtoApplier? = null
    private val getApplyResult = MutableLiveData<Any>()

    override fun setVtoApplier(
        uri: Uri?, productId: String?,
        sku: String?,
        captureLiveCameraImg: Bitmap?,
        isFromLiveCamera: Boolean
    ): MutableLiveData<Any> {

        SdkUtility.initSdk(
            context,
            object : PfSDKInitialCallback {
                override fun onInitialized() {
                    PhotoMakeup.create(object : PhotoMakeup.CreateCallback {
                        override fun onSuccess(makeup: PhotoMakeup) {
                            photoMakeup = makeup

                            VtoApplier.create(photoMakeup, object : VtoApplier.CreateCallback {
                                override fun onSuccess(applierVTO: VtoApplier) {
                                    applier = applierVTO
                                    loadPhoto(uri, captureLiveCameraImg, productId, sku)
                                }

                                override fun onFailure(throwable: Throwable) {
                                    getApplyResult.value = VTO_FAIL_IMAGE_LOAD
                                }
                            })
                        }

                        fun loadPhoto(
                            uri: Uri?,
                            captureLiveCameraImg: Bitmap?,
                            productId: String?,
                            sku: String?
                        ) {
                            if (captureLiveCameraImg != null) {
                                detectFace(captureLiveCameraImg, productId, sku)
                            } else {
                                if (uri == null) {
                                    getApplyResult.value = VTO_INVALID_IMAGE_PATH

                                }
                                try {
                                    context?.contentResolver?.openInputStream(uri!!)
                                        .use { imageStream ->
                                            val bitmap = BitmapFactory.decodeStream(imageStream)
                                            val matrix: Matrix? =
                                                context?.contentResolver?.let { contentResolver ->
                                                    SdkUtility.getRotationMatrixByExif(
                                                        contentResolver,
                                                        uri
                                                    )
                                                }
                                            val selectedImage =
                                                Bitmap.createBitmap(
                                                    bitmap,
                                                    0,
                                                    0,
                                                    bitmap.width,
                                                    bitmap.height,
                                                    matrix,
                                                    true
                                                )
                                            if (bitmap != selectedImage) {
                                                bitmap.recycle()
                                            }
                                            detectFace(selectedImage, productId, sku)

                                        }
                                } catch (e: Exception) {
                                    getApplyResult.value = VTO_INVALID_IMAGE_PATH
                                }
                            }
                        }

                        fun detectFace(
                            image: Bitmap?,
                            productId: String?,
                            sku: String?
                        ) {
                            photoMakeup?.detectFace(image, object : PhotoMakeup.DetectFaceCallback {

                                override fun onSuccess(faceList: List<FaceData>) {
                                    if (faceList.isEmpty()) {
                                        getApplyResult.value = VTO_FACE_NOT_DETECT
                                        return

                                    }
                                    // Select a face for applying effects.
                                    val faceIndex = Random().nextInt(faceList.size)
                                    val faceData = faceList[faceIndex]
                                    photoMakeup?.setFace(faceData)
                                    if (!isFromLiveCamera) {
                                        applyEffectFirstTime(productId, sku, getApplyResult)
                                    }
                                }

                                override fun onFailure(throwable: Throwable) {
                                    getApplyResult.value = VTO_FAIL_IMAGE_LOAD
                                }
                            })
                        }

                        override fun onFailure(throwable: Throwable) {
                            getApplyResult.value = VTO_FAIL_IMAGE_LOAD
                        }
                    })
                }

                override fun onFailure(
                    throwable: Throwable?
                ) {
                    getApplyResult.value = SDK_INIT_FAIL
                }
            })
        return getApplyResult
    }

    private fun applyEffectFirstTime(
        productId: String?,
        sku: String?,
        getApplyResult: MutableLiveData<Any>
    ) {

        val vtoSetting = VtoSetting.builder()
            .setSkuGuid(sku)
            .build()

        PerfectLib.setDownloadCacheStrategy(DownloadCacheStrategy.CACHE_ONLY)
        applier?.apply(
            listOf(vtoSetting),
            EffectConfig.DEFAULT,
            object : VtoApplier.ApplyCallback {
                override fun onSuccess(bitmap: Bitmap) {

                    getApplyResult.value = bitmap

                }

                override fun onFailure(throwable: Throwable) {
                    getApplyResult.value = VTO_COLOR_NOT_MATCH
                }

                override fun applyProgress(progress: Double) {
                    // Do Nothing
                }
            })

    }

    override fun applyEffect(productId: String?, sku: String?): MutableLiveData<Any> {
        val data = MutableLiveData<Any>()
        val vtoSetting = VtoSetting.builder()
            .setSkuGuid(sku)
            .build()

        PerfectLib.setDownloadCacheStrategy(DownloadCacheStrategy.CACHE_ONLY)
        applier?.apply(
            listOf(vtoSetting),
            EffectConfig.DEFAULT,
            object : VtoApplier.ApplyCallback {
                override fun onSuccess(bitmap: Bitmap) {
                    data.value = bitmap
                    data.value = "" // avoid OOM (Bitmap)

                }

                override fun onFailure(throwable: Throwable) {
                    //can not get mapped id for productId
                    data.value = VTO_COLOR_NOT_MATCH

                }

                override fun applyProgress(progress: Double) {
                    // Do Nothing
                }
            })
        return data

    }


    override fun clearEffect(): MutableLiveData<Bitmap> {
        val data = MutableLiveData<Bitmap>()
        applier?.clearAllEffects(object : VtoApplier.ApplyCallback {
            override fun onSuccess(bitmap: Bitmap) {
                data.value = bitmap
            }

            override fun onFailure(throwable: Throwable) {
                FirebaseManager.logException(throwable)
            }

            override fun applyProgress(progress: Double) {
                // Do Nothing
            }
        })
        return data
    }


}