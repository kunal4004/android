package za.co.woolworths.financial.services.android.ui.vto.ui.camera

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.hardware.camera2.CameraCaptureSession.CaptureCallback
import android.media.ImageReader
import android.media.ImageReader.OnImageAvailableListener
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.text.TextUtils
import android.view.MotionEvent
import androidx.annotation.GuardedBy
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSet
import com.google.common.collect.Lists
import com.google.common.util.concurrent.SettableFuture
import com.google.common.util.concurrent.Uninterruptibles
import za.co.woolworths.financial.services.android.ui.vto.ui.camera.PfCamera.Parameters.Companion.FOCUS_MODE_AUTO
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import java.lang.Long.signum
import java.util.*


@SuppressLint("MissingPermission")
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class PfCameraImpl private constructor(builder: Builder) : PfCamera() {

    internal class Builder(val context: Context) {
        var manager: CameraManager? = null
        var cameraId: String? = null
        var defaultPreviewSize: Size? = null
        fun setCameraManager(manager: CameraManager): Builder {
            this.manager = manager
            return this
        }

        fun setCameraId(cameraId: String): Builder {
            this.cameraId = cameraId
            return this
        }

        fun setDefaultPreviewSize(defaultPreviewSize: Size): Builder {
            this.defaultPreviewSize = defaultPreviewSize
            return this
        }

        fun build(): PfCameraImpl {
            return PfCameraImpl(this)
        }
    }

    private class CompareSizesByArea : Comparator<android.util.Size?> {

        override fun compare(lhs: android.util.Size?, rhs: android.util.Size?): Int {
            // Do cast here to ensure the multiplications won't overflow.
            return signum(lhs?.width?.toLong()!! * lhs?.height!! - rhs?.width?.toLong()!! * rhs?.height!!)

        }
    }

    private val context: Context
    private val manager: CameraManager
    private val cameraId: String
    private var characteristics: CameraCharacteristics? = null
    private val lock = Any()
    private val backgroundThread: HandlerThread?
    private val backgroundHandler: Handler
    private var fingerSpacing = 0f
    private var zoomLevel = 1

    @GuardedBy("lock")
    private var cameraDevice: CameraDevice? = null

    @GuardedBy("lock")
    private var previewImageReader: ImageReader? = null

    @GuardedBy("lock")
    private var previewRequestBuilder: CaptureRequest.Builder? = null

    @GuardedBy("lock")
    private var captureSession: CameraCaptureSession? = null

    @GuardedBy("lock")
    private var previewSize: Size? = null

    @GuardedBy("lock")
    @Parameters.FocusMode
    private var focusMode: String? = null

    @GuardedBy("lock")
    private var previewCallback: PreviewCallback? = null

    override val cameraInfo: CameraInfo?
        get() = try {
            val characteristics = manager.getCameraCharacteristics(cameraId)
            val cameraFacing =
                Objects.requireNonNull(characteristics.get(CameraCharacteristics.LENS_FACING))
            val orientation =
                Objects.requireNonNull(characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION))
            orientation?.let { cameraFacing?.let { getPfCameraFacing(it) }?.let { it1 ->
                CameraInfo(
                    it1, it)
            } }
        } catch (t: Throwable) {
            val characteristics = manager.getCameraCharacteristics(cameraId)
            val cameraFacing =
                Objects.requireNonNull(characteristics.get(CameraCharacteristics.LENS_FACING))
            val orientation =
                Objects.requireNonNull(characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION))
            orientation?.let { cameraFacing?.let { getPfCameraFacing(it) }?.let { it1 ->
                CameraInfo(
                    it1, it)
            } }
        }

    internal class Camera2Parameters(pfCamera2: PfCameraImpl) : Parameters {
        override val supportedPreviewSizes: List<Size>
        override val supportedFocusModes: List<String>
        override lateinit var previewSize: Size
            private set

        @Parameters.FocusMode
        private var focusMode: String?
        override fun setPreviewSize(width: Int, height: Int) {
            previewSize = Size(width, height)
        }

        override fun getFocusMode(): String? {
            return focusMode
        }

        override fun setFocusMode(@Parameters.FocusMode value: String?) {
            focusMode = value
        }

        companion object {
            fun buildFocusModes(characteristics: CameraCharacteristics): List<String> {
                val builder = ImmutableSet.builder<String>()
                val focuses = characteristics.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES)
                if (focuses != null) {
                    for (focus in focuses) {
                        when (focus) {
                            CaptureRequest.CONTROL_AF_MODE_AUTO -> builder.add(FOCUS_MODE_AUTO)
                            CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE -> builder.add(
                                Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
                            )
                            CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO -> builder.add(
                                Parameters.FOCUS_MODE_CONTINUOUS_VIDEO
                            )
                            CaptureRequest.CONTROL_AF_MODE_EDOF -> builder.add(Parameters.FOCUS_MODE_EDOF)
                            CaptureRequest.CONTROL_AF_MODE_MACRO -> builder.add(Parameters.FOCUS_MODE_MACRO)
                            CaptureRequest.CONTROL_AF_MODE_OFF -> builder.add(Parameters.FOCUS_MODE_INFINITY)
                        }
                    }
                }
                return ImmutableList.copyOf(builder.build())
            }
        }

        init {
            val map =
                pfCamera2.characteristics?.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            val preview = Objects.requireNonNull(map)
                ?.getOutputSizes(PREVIEW_IMAGE_FORMAT)
            supportedPreviewSizes = Lists.transform(
                Lists.newArrayList(*preview)
            ) { input: android.util.Size? ->
                input?.let {
                    Size(
                        it.width, it.height
                    )
                }
            }
            supportedFocusModes = buildFocusModes(
                pfCamera2.characteristics!!
            )
            previewSize =
                pfCamera2.previewSize?.width?.let { pfCamera2.previewSize?.height?.let { it1 ->
                    Size(it,
                        it1
                    )
                } }!!
            focusMode = pfCamera2.focusMode
        }
    }

    override fun getParameters(): Parameters {
        synchronized(lock) { return Camera2Parameters(this) }
    }


    override fun setParameters(parameter: Parameters?) {
        synchronized(lock) {
            previewSize = parameter!!.previewSize
            var updateCaptureRequest = false
            val newFocusMode = parameter!!.getFocusMode()
            if (!TextUtils.equals(newFocusMode, focusMode)) {
                focusMode = newFocusMode
                updateCaptureRequest = true
                setFocus(focusMode, previewRequestBuilder)
            }
            if (updateCaptureRequest && previewRequestBuilder != null) {
                try {
                    previewRequestBuilder?.build()?.let {
                        captureSession?.setRepeatingRequest(
                            it,
                            null,
                            backgroundHandler
                        )
                    }
                } catch (t: Throwable) {
                    //throw Unchecked.of(t)
                }
            }
        }
    }

    override fun setPreviewCallback(callback: PreviewCallback?) {
        synchronized(lock) { previewCallback = callback }
    }

    override fun setPreviewTexture(surfaceTexture: SurfaceTexture?) {
        // NOP
    }

    private val onPreviewImageAvailableListener =
        OnImageAvailableListener { reader ->
            synchronized(lock) {
                if (previewCallback != null) {
                    val image = reader.acquireNextImage()
                    previewCallback?.onPreviewFrame(
                        CameraImageUtil.toNV21(image),
                        this@PfCameraImpl
                    )
                    image.close()
                }
            }
        }

    override fun startPreview() {
        try {
            val future =
                SettableFuture.create<Void>() // Let it be synchronized call like camera 1 API behavior.
            synchronized(lock) {
                previewImageReader = previewSize?.let {
                    ImageReader.newInstance(
                        it.width,
                        it.height,
                        PREVIEW_IMAGE_FORMAT,
                        1
                    )
                }
                previewImageReader?.setOnImageAvailableListener(
                    onPreviewImageAvailableListener,
                    backgroundHandler
                )
                val outputs =
                    listOf(previewImageReader?.surface)
                previewRequestBuilder =
                    cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                previewRequestBuilder?.addTarget(previewImageReader!!.surface)
                cameraDevice?.createCaptureSession(
                    outputs,
                    object : CameraCaptureSession.StateCallback() {
                        override fun onConfigured(session: CameraCaptureSession) {
                            synchronized(lock) {
                                try {
                                    // Start displaying the camera preview.
                                    captureSession = session
                                    setFocus(focusMode, previewRequestBuilder)
                                    val previewRequest =
                                        previewRequestBuilder?.build()
                                    previewRequest?.let {
                                        session.setRepeatingRequest(
                                            it,
                                            null,
                                            backgroundHandler
                                        )
                                    }
                                } catch (e: CameraAccessException) {
                                    val message = "configure camera failed"

                                }
                            }
                            future.set(null)
                        }

                        override fun onConfigureFailed(session: CameraCaptureSession) {
                            val message = "configure camera failed"
                            future.setException(IllegalStateException(message))
                        }
                    },
                    backgroundHandler
                )
            }
            Uninterruptibles.getUninterruptibly(future)
        } catch (t: Throwable) {

        }
    }

    private fun setFocus(@Parameters.FocusMode value: String?, builder: CaptureRequest.Builder?) {
        if (builder != null) {
            when (value) {
                Parameters.FOCUS_MODE_AUTO -> builder.set(
                    CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_AUTO
                )
                Parameters.FOCUS_MODE_CONTINUOUS_PICTURE -> builder.set(
                    CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                )
                Parameters.FOCUS_MODE_CONTINUOUS_VIDEO -> builder.set(
                    CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO
                )
                Parameters.FOCUS_MODE_EDOF -> builder.set(
                    CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_EDOF
                )
                Parameters.FOCUS_MODE_FIXED, Parameters.FOCUS_MODE_INFINITY -> if (isHardwareLevelSupported(
                        CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY
                    )
                ) {
                    builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF)
                    builder.set(CaptureRequest.LENS_FOCUS_DISTANCE, 0.0f)
                }
                Parameters.FOCUS_MODE_MACRO -> builder.set(
                    CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_MACRO
                )
            }
        }
    }

    private fun isHardwareLevelSupported(requiredLevel: Int): Boolean {
        var result = false
        val deviceLevel = characteristics?.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)
        if (null != deviceLevel) {
            result =
                if (deviceLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
                    requiredLevel == deviceLevel
                } else {
                    // deviceLevel is not LEGACY, can use numerical sort
                    requiredLevel <= deviceLevel
                }
        }
        return result
    }

    override fun stopPreview() {
        synchronized(lock) {
            previewCallback = null
            if (captureSession != null) {
                captureSession?.close()
                captureSession = null
            }
            if (previewImageReader != null) {
                previewImageReader?.close()
                previewImageReader = null
            }
        }
    }

    override fun autoFocus(callback: AutoFocusCallback) {
        Objects.requireNonNull<Any>(callback)
        synchronized(lock) {
            previewRequestBuilder?.set(
                CaptureRequest.CONTROL_AF_TRIGGER,
                CameraMetadata.CONTROL_AF_TRIGGER_CANCEL
            )
            previewRequestBuilder?.set(
                CaptureRequest.CONTROL_AF_MODE,
                CaptureRequest.CONTROL_AF_MODE_OFF
            )
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                    captureSession?.stopRepeating();
                }
                captureSession?.capture(previewRequestBuilder!!.build(), null, backgroundHandler)
            } catch (e: CameraAccessException) {
                callback.onAutoFocus(false, this)

            }
            previewRequestBuilder?.set(
                CaptureRequest.CONTROL_AF_TRIGGER,
                CameraMetadata.CONTROL_AF_TRIGGER_START
            )
            previewRequestBuilder?.set(
                CaptureRequest.CONTROL_MODE,
                CameraMetadata.CONTROL_AF_MODE_AUTO
            )
            previewRequestBuilder?.set(
                CaptureRequest.CONTROL_AF_MODE,
                CameraMetadata.CONTROL_AF_MODE_AUTO
            )
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                    captureSession?.stopRepeating()
                }
                previewRequestBuilder?.build()?.let { it ->
                    captureSession?.capture(
                        it,
                        object : CaptureCallback() {
                            override fun onCaptureCompleted(
                                session: CameraCaptureSession,
                                request: CaptureRequest,
                                result: TotalCaptureResult
                            ) {
                                synchronized(lock) {
                                    callback.onAutoFocus(true, this@PfCameraImpl)
                                    disableAutoFocus()
                                }
                            }

                            override fun onCaptureFailed(
                                session: CameraCaptureSession,
                                request: CaptureRequest,
                                failure: CaptureFailure
                            ) {
                                synchronized(lock) {
                                    callback.onAutoFocus(false, this@PfCameraImpl)
                                    disableAutoFocus()
                                }
                            }

                            private fun disableAutoFocus() {
                                previewRequestBuilder?.set(
                                    CaptureRequest.CONTROL_AF_TRIGGER,
                                    CameraMetadata.CONTROL_AF_TRIGGER_IDLE
                                )
                                try {
                                    previewRequestBuilder?.build()?.let {
                                        captureSession?.setRepeatingRequest(
                                            it,
                                            null,
                                            backgroundHandler
                                        )
                                    }
                                } catch (e: CameraAccessException) {
                                    //DO Nothing
                                }
                            }
                        },
                        backgroundHandler
                    )
                }
            } catch (e: CameraAccessException) {
                callback.onAutoFocus(false, this)

            }
        }
    }

    override fun release() {
        closeCameraDevice()
        stopBackgroundThread()
    }



    private fun closeCameraDevice() {
        synchronized(lock) {
            previewCallback = null
            if (previewImageReader != null) {
                previewImageReader?.close()
                previewImageReader = null
            }
            if (cameraDevice != null) {
                cameraDevice?.close()
                cameraDevice = null
            }
        }
    }

    private fun stopBackgroundThread() {
        if (backgroundThread != null) {
            backgroundThread.quitSafely()
            Uninterruptibles.joinUninterruptibly(backgroundThread)
        }
    }


    init {
        context = Objects.requireNonNull(builder.context)
        manager = Objects.requireNonNull(builder.manager)!!
        cameraId = Objects.requireNonNull(builder.cameraId).toString()
        synchronized(lock) {
            previewSize = Objects.requireNonNull(builder.defaultPreviewSize)
        }
        backgroundThread = HandlerThread("Camera2Background")
        backgroundThread.start()
        backgroundHandler = Handler(backgroundThread.looper)
        try {
            characteristics = manager.getCameraCharacteristics(cameraId)
            val supportedFocusModes = Camera2Parameters.buildFocusModes(characteristics!!)
            if (supportedFocusModes != null && !supportedFocusModes.isEmpty()) {
                synchronized(lock) {
                    focusMode =
                        if (supportedFocusModes.contains(FOCUS_MODE_AUTO)) FOCUS_MODE_AUTO else supportedFocusModes[0]
                }
            }
            val stateCallback: CameraDevice.StateCallback = object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    synchronized(lock) { cameraDevice = camera }
                }

                override fun onDisconnected(camera: CameraDevice) {
                    closeCameraDevice()
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    closeCameraDevice()
                }
            }
            manager.openCamera(cameraId, stateCallback, backgroundHandler)
        } catch (t: Throwable) {

        }

    }

    companion object {
        private const val PREVIEW_IMAGE_FORMAT = ImageFormat.YUV_420_888
        fun open(context: Context, facing: CameraFacing): PfCameraImpl {
            check(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { "camera2 requires at least API level 21" }
            return createBuilder(context, facing).build()
        }

        private val CAMERA_FACING_MAPPING_TABLE: Map<Int?, CameraFacing> =
            ImmutableMap.builder<Int?, CameraFacing>()
                .put(CameraCharacteristics.LENS_FACING_FRONT, CameraFacing.FRONT)
                .put(CameraCharacteristics.LENS_FACING_BACK, CameraFacing.BACK)
                .build()

        private fun createBuilder(context: Context, facing: CameraFacing): Builder {
            try {
                val cameraManager =
                    context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
                for (cameraId in cameraManager.cameraIdList) {
                    val cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId)
                    val cameraFacing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING)
                    val mappedCameraFacing = CAMERA_FACING_MAPPING_TABLE[cameraFacing]
                    if (mappedCameraFacing == null || mappedCameraFacing !== facing) {
                        continue
                    }
                    val map =
                        cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                            ?: continue
                    val defaultPreviewSize = Collections.max(
                        Arrays.asList(*map.getOutputSizes(PREVIEW_IMAGE_FORMAT)),
                        CompareSizesByArea()
                    )
                    return Builder(context)
                        .setCameraManager(cameraManager)
                        .setCameraId(cameraId)
                        .setDefaultPreviewSize(
                            Size(
                                defaultPreviewSize.width,
                                defaultPreviewSize.height
                            )
                        )
                }
                throw IllegalStateException("Can't find available camera for facing $facing")
            } catch (t: Throwable) {
                throw IllegalStateException("Can't find available camera for facing $facing")
            }
        }

        private fun getPfCameraFacing(cameraFacing: Int): CameraFacing {
            return when (cameraFacing) {
                CameraCharacteristics.LENS_FACING_FRONT -> CameraFacing.FRONT
                CameraCharacteristics.LENS_FACING_BACK -> CameraFacing.BACK
                else -> throw IllegalArgumentException("Only supports front and back camera.")
            }
        }
    }

    override fun zoom(context: Context, event: MotionEvent) {

        try {
            val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val characteristics = manager.getCameraCharacteristics(cameraId)
            val maxzoom =
                characteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM)!! * 4
            val m = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE)
            val action = event.action
            val current_finger_spacing: Float
            if (event.pointerCount == 2) {
                // Multi touch
                current_finger_spacing = getFingerSpacing(event)
                if (fingerSpacing != 0f) {
                    if (current_finger_spacing > fingerSpacing && maxzoom > zoomLevel) {
                        zoomLevel++
                    } else if (current_finger_spacing < fingerSpacing && zoomLevel > 1) {
                        zoomLevel--
                    }
                    val minW = (m!!.width() / maxzoom).toInt()
                    val minH = (m.height() / maxzoom).toInt()
                    val difW = m.width() - minW
                    val difH = m.height() - minH
                    var cropW = difW / 100 * zoomLevel
                    var cropH = difH / 100 * zoomLevel
                    cropW -= cropW and 3
                    cropH -= cropH and 3
                    val zoom = Rect(cropW, cropH, m.width() - cropW, m.height() - cropH)
                    previewRequestBuilder?.set(CaptureRequest.SCALER_CROP_REGION, zoom)
                }
                fingerSpacing = current_finger_spacing
            } else {
                if (action == MotionEvent.ACTION_UP) {
                    //single touch
                }
            }
            try {
                previewRequestBuilder?.let {
                    captureSession
                        ?.setRepeatingRequest(it.build(),null, null)
                }

            } catch (e: CameraAccessException) {
               //Do Nothing
            } catch (ex: java.lang.NullPointerException) {
                //Do Nothing
            }
        } catch (e: CameraAccessException) {
            //Do Nothing
        }

    }

    private fun getFingerSpacing(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return Math.sqrt((x * x + y * y).toDouble()).toFloat()
    }
}
