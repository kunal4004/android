package za.co.woolworths.financial.services.android.ui.vto.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.lifecycle.MutableLiveData
import com.perfectcorp.perfectlib.*
import dagger.hilt.android.qualifiers.ApplicationContext
import za.co.woolworths.financial.services.android.ui.vto.data.model.LiveCameraBitmapImages
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.VTO_COLOR_LIVE_CAMERA
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.VTO_COLOR_NOT_MATCH
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import javax.inject.Inject

class LiveCameraRepositoryImpl @Inject constructor(
    @ApplicationContext val context: Context

) : LiveCameraRepository {

    private var vtoApplier: VtoApplier? = null
    val getApplyResult = MutableLiveData<Any>()
    private var makeupCamera: MakeupCam? = null


    override fun liveCameraVtoApplier(
        makeupCam: MakeupCam?,
        productId: String?,
        sku: String?
    ): MutableLiveData<Any> {

        makeupCamera = makeupCam

        VtoApplier.create(makeupCam, object : VtoApplier.CreateCallback {

            override fun onSuccess(applier: VtoApplier) {
                vtoApplier = applier
                applier.setLipstickTransition(400, Color.WHITE)
                applyEffectFirstTime(productId, sku, getApplyResult)

            }

            override fun onFailure(
                throwable: Throwable
            ) {
                handleExceptionWithFireBase(throwable)
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
        vtoApplier?.apply(
            listOf(vtoSetting),
            EffectConfig.DEFAULT,
            object : VtoApplier.ApplyCallback {
                override fun onSuccess(ignored: Bitmap?) {
                   //Do Nothing
                }

                override fun onFailure(throwable: Throwable) {
                    getApplyResult.value = VTO_COLOR_NOT_MATCH
                }

                override fun applyProgress(progress: Double) {
                    // Do Nothing
                }
            })

    }

    override fun applyVtoEffectOnLiveCamera(
        productId: String?,
        sku: String?
    ): MutableLiveData<Any> {
        val selectedColorResult = MutableLiveData<Any>()
        val vtoSetting = VtoSetting.builder()
            .setSkuGuid(sku)
            .build()

       PerfectLib.setDownloadCacheStrategy(DownloadCacheStrategy.CACHE_ONLY)
        vtoApplier?.apply(
            listOf(vtoSetting),
            EffectConfig.DEFAULT,
            object : VtoApplier.ApplyCallback {
                override fun onSuccess(ignored: Bitmap?) {
                    selectedColorResult.value = VTO_COLOR_LIVE_CAMERA
                }

                override fun onFailure(throwable: Throwable) {
                    selectedColorResult.value = VTO_COLOR_NOT_MATCH
                }

                override fun applyProgress(progress: Double) {
                      //Do Nothing
                }
            })

       return selectedColorResult

    }

    override fun takePhoto(): MutableLiveData<LiveCameraBitmapImages> {
        val takenPicture = MutableLiveData<LiveCameraBitmapImages>()
        makeupCamera?.takePicture(
            true,
            true,
            false,
            true,
            object : MakeupCam.PictureCallback {
                override fun onPictureTaken(originalPicture: Bitmap?, resultPicture: Bitmap?) {
                    resultPicture?.let {
                        takenPicture.value= LiveCameraBitmapImages(originalPicture,resultPicture)
                        // avoid OOM (due to Bitmap)
                        takenPicture.value= LiveCameraBitmapImages(null,null)
                    }
                }

                override fun onFailure(t: Throwable) {
                    handleExceptionWithFireBase(t)
                }
            })
        return takenPicture
    }

    override fun clearEffect() {
        vtoApplier?.clearAllEffects(
            object : VtoApplier.ApplyCallback {
                override fun onSuccess(ignored: Bitmap?) {
                    //Do Nothing
                }
                override fun onFailure(throwable: Throwable) {
                    handleExceptionWithFireBase(throwable)
                }
                override fun applyProgress(progress: Double) {
                    //Do Nothing
                }
            })
    }
    private fun handleExceptionWithFireBase(t: Throwable) {
        FirebaseManager.logException(t)
    }


}