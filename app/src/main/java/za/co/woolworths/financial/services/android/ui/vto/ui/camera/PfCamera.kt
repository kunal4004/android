package za.co.woolworths.financial.services.android.ui.vto.ui.camera

import android.content.Context
import android.graphics.SurfaceTexture
import android.view.MotionEvent
import androidx.annotation.StringDef
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy


abstract class PfCamera internal constructor() {
    abstract val cameraInfo: CameraInfo?

    abstract fun getParameters(): Parameters?
    abstract fun setParameters(parameter: Parameters?)
    abstract fun setPreviewCallback(callback: PreviewCallback?)
    abstract fun setPreviewTexture(surfaceTexture: SurfaceTexture?)
    abstract fun startPreview()
    abstract fun stopPreview()
    abstract fun autoFocus(callback: AutoFocusCallback)
    abstract fun release()
    abstract fun zoom(context: Context, event: MotionEvent)
    class CameraInfo(facing: CameraFacing, orientation: Int) {
        val facing: CameraFacing

        /**
         * Degrees of clockwise rotation.
         *
         *
         * **Range of valid values:**<br></br>
         * 0, 90, 180, 270
         */
        val orientation: Int

        init {
            this.facing = facing
            this.orientation = orientation
        }
    }

    interface Parameters {
        @Retention(RetentionPolicy.SOURCE)
        @StringDef(
            FOCUS_MODE_AUTO,
            FOCUS_MODE_INFINITY,
            FOCUS_MODE_MACRO,
            FOCUS_MODE_FIXED,
            FOCUS_MODE_EDOF,
            FOCUS_MODE_CONTINUOUS_VIDEO,
            FOCUS_MODE_CONTINUOUS_PICTURE
        )
        annotation class FocusMode

        val supportedPreviewSizes: List<Size>?
        val previewSize: Size?
        fun setPreviewSize(width: Int, height: Int)
        val supportedFocusModes: List<String?>?
        fun getFocusMode(): String?
        fun setFocusMode(@FocusMode value: String?)

        companion object {
            const val FOCUS_MODE_AUTO = "auto"
            const val FOCUS_MODE_INFINITY = "infinity"
            const val FOCUS_MODE_MACRO = "macro"
            const val FOCUS_MODE_FIXED = "fixed"
            const val FOCUS_MODE_EDOF = "edof"
            const val FOCUS_MODE_CONTINUOUS_VIDEO = "continuous-video"
            const val FOCUS_MODE_CONTINUOUS_PICTURE = "continuous-picture"
        }
    }

    class Size(val width: Int, val height: Int)

    fun interface PreviewCallback {
        fun onPreviewFrame(data: ByteArray?, camera: PfCamera?)
    }

    fun interface AutoFocusCallback {
        fun onAutoFocus(success: Boolean, camera: PfCamera?)
    }

    companion object {
        fun open(context: Context, facing: CameraFacing): PfCamera {
            return PfCameraImpl.open(context, facing)
        }
    }
}
