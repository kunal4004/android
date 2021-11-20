package za.co.woolworths.financial.services.android.ui.vto.utils

import android.annotation.SuppressLint

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.net.http.HttpResponseCache
import android.os.Handler
import androidx.exifinterface.media.ExifInterface
import com.perfectcorp.perfectlib.*
import za.co.woolworths.financial.services.android.ui.vto.ui.PfSDKInitialCallback
import java.io.File
import java.io.IOException


@SuppressLint("SetTextI18n")
object SdkUtility {

    private fun initAlreadyCalled(): Boolean {
        return PerfectLib.getState() == PerfectLib.State.INITIALIZING || PerfectLib.getState() == PerfectLib.State.INITIALIZED
    }

    private fun enableHttpCache(context: Context) {

        val httpCacheDir = File(context.cacheDir, "http")
        val httpCacheSize = (10 * 1024 * 1024).toLong() // 10 MiB
        try {
            HttpResponseCache.install(httpCacheDir, httpCacheSize)

        } catch (e: IOException) {

        }
    }

    @JvmStatic
    fun initSdk(context: Context, callback: PfSDKInitialCallback) {
        if (initAlreadyCalled()) {

            if (PerfectLib.getState() == PerfectLib.State.INITIALIZED) {
                Handler().post { callback.onInitialized() }
            }
            return
        }
        enableHttpCache(context)

        val wrappedCallback: PerfectLib.InitialCallback = object : PerfectLib.InitialCallback {
            override fun onInitialized(
                availableFunctionalities: Set<Functionality>,
                preloadError: Map<String, Throwable>
            ) {
                PerfectLib.setMaxCacheSize(500)
                PerfectLib.setCountryCode("ZA")
                PerfectLib.setLocaleCode("en_ZA")
                callback.onInitialized()
            }

            override fun onFailure(throwable: Throwable, preloadError: Map<String, Throwable>) {
                callback.onFailure(throwable)
            }
        }
        val configuration = Configuration
            .builder()
            .setModelPath(PerfectLib.ModelPath.assets("model"))
            .setImageSource(Configuration.ImageSource.FILE)
            .setUserId("USER_ID_FOR_TRACKING")
            .setDeveloperMode(true)
            .setMappingMode(true)
            .setPreviewMode(true)
            .setAdvancedFaceTrackingMode(false)
            .build()
        PerfectLib.init(context, configuration, wrappedCallback)
    }

    fun getRotationMatrixByExif(contentResolver: ContentResolver, imageUri: Uri?): Matrix {
        val matrix = Matrix()
        try {
            contentResolver.openInputStream(imageUri!!).use { imageStream ->
                val orientation = ExifInterface(
                    imageStream!!
                ).getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)
                val rotate: Int
                rotate = when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    else -> 0
                }
                matrix.postRotate(rotate.toFloat())
            }
        } catch (throwable: Throwable) {

        }
        return matrix
    }

    fun Bitmap.rotate(degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }


}
