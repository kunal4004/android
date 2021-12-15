package za.co.woolworths.financial.services.android.ui.vto.ui.camera

import android.graphics.ImageFormat
import android.media.Image
import android.os.Build
import androidx.annotation.RequiresApi
import com.google.common.base.Preconditions


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
internal object CameraImageUtil {
    /**
     * For better performance, do color space conversion in JNI layer.
     */
    fun toNV21(image: Image): ByteArray {
        Preconditions.checkArgument(
            image.format == ImageFormat.YUV_420_888,
            "Unsupported image format " + image.format
        )
        val crop = image.cropRect
        val width = crop.width()
        val height = crop.height()
        val planes = image.planes
        val yuvStrideBuffer = ByteArray(planes[0].rowStride)
        val nv21Buffer = ByteArray(width * height * ImageFormat.getBitsPerPixel(image.format) / 8)
        var channelOffset = 0
        var outputStride = 1
        for (i in planes.indices) {
            when (i) {
                0 -> {
                    channelOffset = 0
                    outputStride = 1
                }
                1 -> {
                    channelOffset = width * height + 1
                    outputStride = 2
                }
                2 -> {
                    channelOffset = width * height
                    outputStride = 2
                }
            }
            val buffer = planes[i].buffer
            val rowStride = planes[i].rowStride
            val pixelStride = planes[i].pixelStride
            val shift = if (i == 0) 0 else 1
            val w = width shr shift
            val h = height shr shift
            buffer.position(rowStride * (crop.top shr shift) + pixelStride * (crop.left shr shift))
            for (row in 0 until h) {
                var length: Int
                if (pixelStride == 1 && outputStride == 1) {
                    length = w
                    buffer[nv21Buffer, channelOffset, length]
                    channelOffset += length
                } else {
                    length = (w - 1) * pixelStride + 1
                    buffer[yuvStrideBuffer, 0, length]
                    for (col in 0 until w) {
                        nv21Buffer[channelOffset] = yuvStrideBuffer[col * pixelStride]
                        channelOffset += outputStride
                    }
                }
                if (row < h - 1) {
                    buffer.position(buffer.position() + rowStride - length)
                }
            }
        }
        return nv21Buffer
    }
}
