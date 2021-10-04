package za.co.woolworths.financial.services.android.ui.vto.ui

import android.annotation.SuppressLint

import android.content.ContentResolver
import android.content.Context

import android.graphics.Matrix
import android.net.Uri
import android.net.http.HttpResponseCache
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.exifinterface.media.ExifInterface
import com.google.common.collect.ImmutableList
import com.perfectcorp.perfectlib.*
import java.io.File
import java.io.IOException


@SuppressLint("SetTextI18n")
object SdkUtility {
    private const val TAG = "SdkUtility"
    private fun initAlreadyCalled(): Boolean {
        return PerfectLib.getState() == PerfectLib.State.INITIALIZING || PerfectLib.getState() == PerfectLib.State.INITIALIZED
    }


    private fun enableHttpCache(context: Context) {
        Log.d(TAG, "[enableHttpCache]")
        val httpCacheDir = File(context.cacheDir, "http")
        val httpCacheSize = (10 * 1024 * 1024).toLong() // 10 MiB
        try {
            HttpResponseCache.install(httpCacheDir, httpCacheSize)
            Log.d(TAG, "[enableHttpCache] succeed.")
        } catch (e: IOException) {
            Log.e(TAG, "[enableHttpCache] failed.", e)
        }
    }

    fun initSdk(context: Context, callback: PfSDKInitialCallback) {
        if (initAlreadyCalled()) {
            val msg =
                "[initSdk] SDK init method already called, new config will not be applied. current state=" + PerfectLib.getState().name
            Log.d(TAG, msg)
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            if (PerfectLib.getState() == PerfectLib.State.INITIALIZED) {
                Handler().post { callback.onInitialized() }
            }
            return
        }
        enableHttpCache(context)

        // [PF] Setup SDK log ability.
        setupLog()
        Log.d(TAG, "SDK version=" + PerfectLib.getVersion())
        // [PF] initialize SDK with model files.
        Log.d(TAG, "SDK initializing.")
        val wrappedCallback: PerfectLib.InitialCallback = object : PerfectLib.InitialCallback {
            override fun onInitialized(
                availableFunctionalities: Set<Functionality>,
                preloadError: Map<String, Throwable>
            ) {
                //PerfectLib.setMaxCacheSize(50)


                PerfectLib.setCountryCode("us")
                PerfectLib.setLocaleCode("en_US")
                Log.d(
                    TAG,
                    "[onInitialized] availableFunctionalities=$availableFunctionalities"
                )
                callback.onInitialized()
            }

            override fun onFailure(throwable: Throwable, preloadError: Map<String, Throwable>) {
                // Log.d(TAG, "[onFailure] preload error:$preloadError")
//                if (!preloadError.isEmpty()) {
//                }
                callback.onFailure(throwable)
            }
        }
        val configuration = Configuration
            .builder()
            .setModelPath(PerfectLib.ModelPath.assets("model"))
            .setPreloadPath(Path.assets("preloadTest"))
            .setImageSource(Configuration.ImageSource.FILE)
            .setUserId("USER_ID_FOR_TRACKING")
            .setDeveloperMode(true)
            .setMappingMode(true)
            .setPreviewMode(true)
            .setAdvancedFaceTrackingMode(true)
            .build()
        PerfectLib.init(context, configuration, wrappedCallback)
    }

    private fun setupLog() {
        val logFolder = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            "sdk_log"
        )
        if (!logFolder.exists()) {
            logFolder.mkdirs()
        } else if (!logFolder.isDirectory) {
            logFolder.delete()
            logFolder.mkdir()
        }
        PerfectLib.setDebugMode(
            DebugMode.builder()
                .enableLogcat(Log.VERBOSE)
                .enableFileLogger(logFolder, Log.ERROR)
                .build()
        )
    }

    fun makeArrayInfo(info: ProductInfo): Array<String> {
        return ImmutableList.builder<String>()
            .add("Guid: " + info.guid)
            .add("Name: " + info.name)
            .add("LongName: " + info.longName)
            .add("Vendor: " + info.vendor)
            .add("Thumbnail: " + info.thumbnailUrl)
            .build()
            .toTypedArray()
    }

    fun makeArrayInfo(info: SkuInfo): Array<String> {
        return ImmutableList.builder<String>()
            .add("Guid: " + info.guid)
            .add("ProductGuid: " + info.productGuid)
            .add("Name: " + info.name)
            .add("LongName: " + info.longName)
            .add("ThumbnailUrl: " + info.thumbnailUrl)
            .add("ActionUrl: " + info.actionUrl)
            .add("ActionUrlType: " + info.actionUrlType)
            .add("Hot: " + info.isHot)
            .add("CustomerInfo: " + info.customerInfo)
            .build()
            .toTypedArray()
    }

    fun makeArrayInfo(info: SkuSetInfo): Array<String> {
        return ImmutableList.builder<String>()
            .add("Guid: " + info.guid)
            .add("Name: " + info.name)
            .build()
            .toTypedArray()
    }

    fun makeArrayInfo(tagGroup: TagGroup): Array<String> {
        return ImmutableList.builder<String>()
            .add("ID: " + tagGroup.tagGroupId)
            .add("Name: " + tagGroup.name)
            .add("FreeTag: " + tagGroup.isFreeTag)
            .build()
            .toTypedArray()
    }

    fun makeArrayInfo(tag: Tag): Array<String> {
        return ImmutableList.builder<String>()
            .add("ID: " + tag.tagId)
            .add("Name: " + tag.name)
            .add("Thumbnail: " + tag.thumbnail)
            .build()
            .toTypedArray()
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


}
