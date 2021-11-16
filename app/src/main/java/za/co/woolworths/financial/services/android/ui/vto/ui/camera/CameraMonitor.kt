package za.co.woolworths.financial.services.android.ui.vto.ui.camera

import android.content.Context
import android.graphics.SurfaceTexture
import android.view.MotionEvent
import androidx.lifecycle.*
import com.perfectcorp.perfectlib.CameraFrame
import com.perfectcorp.perfectlib.MakeupCam
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.lang.Exception

private var camera: PfCamera? = null

class CameraMonitor constructor(
    private val context: Context,
    private val makeupCamera: MakeupCam?,
    private val lifecycle: Lifecycle
) : LifecycleObserver {

    private lateinit var job: Job
    private lateinit var coroutineScope: CoroutineScope
    private var surfaceTexture: SurfaceTexture? = null
    private var started = false

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun init() {
        startCamera()
    }
    
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    internal fun startCamera() {
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            if (!started) {
                started = true
                makeupCamera?.onCreated()
                makeupCamera?.onStarted()
                initCoroutine()
                startPfCamera()
                startPreview()
            }
        } else if (lifecycle.currentState.isAtLeast(Lifecycle.State.DESTROYED)) {
            stopLiveCamera()
        }
    }

   private fun stopLiveCamera() {
        makeupCamera?.onPaused()
        makeupCamera?.onStopped()
        makeupCamera?.onDestroyed()
        stopCamera()
        job?.cancel()
        lifecycle.removeObserver(this)
    }

    private fun initCoroutine() {
        job = Job()
        coroutineScope = CoroutineScope(Dispatchers.IO + job)
    }

    private fun startPfCamera() {
        var isFrontCamera = false
        var cameraOrientation = 0
        var previewWidth = 0
        var previewHeight = 0

        try {
            camera = PfCamera.open(context, CameraFacing.FRONT)
            if (camera != null) {
                tryToSetPreviewSize(
                    camera!!,
                    1280,
                    400
                ) // Need app to have an arbitration approach to select performance matched preview size.
                camera!!.setPreviewCallback(
                    CameraPreviewCallback(
                        camera!!, makeupCamera
                    )
                )
                surfaceTexture = SurfaceTexture(10)
                camera!!.setPreviewTexture(surfaceTexture)
                val previewSize: PfCamera.Size = camera!!.getParameters()!!.previewSize!!
                previewWidth = previewSize.width
                previewHeight = previewSize.height
                val cameraInfo: PfCamera.CameraInfo = camera!!.cameraInfo!!

                coroutineScope.launch {
                    makeupCamera?.onCameraOpened(
                        (cameraInfo.facing === CameraFacing.FRONT).also {
                            isFrontCamera = it
                        },
                        cameraInfo.orientation.also {
                            cameraOrientation = it
                        },
                        previewSize.width,
                        previewSize.height
                    )
                }

                tryToSetAutoFocus(camera!!)
            }
        } catch (t: Throwable) {
             //Do Nothing
        }
    }

    private fun tryToSetAutoFocus(camera: PfCamera) {
        val cameraParameters = camera.getParameters()
        val cameraFocusModes: List<String> =
            (cameraParameters!!.supportedFocusModes as List<String>?)!!
        if (cameraFocusModes.contains(PfCamera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            cameraParameters.setFocusMode(PfCamera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)
        } else if (cameraFocusModes.contains(PfCamera.Parameters.FOCUS_MODE_AUTO)) {
            cameraParameters.setFocusMode(PfCamera.Parameters.FOCUS_MODE_AUTO)
        }
        camera.setParameters(cameraParameters)
    }

    private val autoFocusCallback =
        PfCamera.AutoFocusCallback { _, _ -> }

    private fun startPreview() {

        if (camera != null) {
            try {
                camera!!.startPreview()
                camera!!.autoFocus(autoFocusCallback)

            } catch (t: Throwable) {
                  //Do Nothing
            }
        }
    }

    private fun tryToSetPreviewSize(camera: PfCamera, width: Int, height: Int) {
        val cameraParameters = camera.getParameters()
        val previewSizes: List<PfCamera.Size> = (cameraParameters?.supportedPreviewSizes)!!

        for (size in previewSizes)
            if (width == size.width && height == size.height) {
                cameraParameters!!.setPreviewSize(
                    width,
                    height
                ) // Need app to have an arbitration approach to select performance matched preview size.
                camera.setParameters(cameraParameters)

                return
            }
    }

    private class CameraPreviewCallback(
        camera: PfCamera,
        makeupCamera: MakeupCam?
    ) :
        PfCamera.PreviewCallback {
        private val previewWidth: Int
        private val previewHeight: Int
        private var isFirstFrame = true
        private var makeup = makeupCamera

        override fun onPreviewFrame(data: ByteArray?, camera: PfCamera?) {
            if (data == null) {
                return
            }
            // [PF] feed camera buffer into SDK
            val cameraFrame = CameraFrame(data, previewWidth, previewHeight, isFirstFrame)
            val cameraFrameOrientation: Int = -1
            if (cameraFrameOrientation >= 0) {
                cameraFrame.setFrameOrientation(cameraFrameOrientation)
            }
            makeup?.sendCameraBuffer(cameraFrame)
            isFirstFrame = false
        }

        init {
            val previewSize: PfCamera.Size = camera.getParameters()!!.previewSize!!
            previewWidth = previewSize.width
            previewHeight = previewSize.height
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun stopCamera() {
        if (camera != null) {
            camera!!.stopPreview()
            surfaceTexture = null
            try {
                camera!!.setPreviewTexture(null)
            } catch (e: Exception) {
              //Do Nothing
            }
            camera!!.setPreviewCallback(null)
            camera!!.release()
            camera = null
        }
    }

    internal fun pinchZoom(context: Context, event: MotionEvent) {
        try {
            camera!!.zoom(context, event)
        } catch (e: Exception) {
            // Do Nothing
        }
    }

}