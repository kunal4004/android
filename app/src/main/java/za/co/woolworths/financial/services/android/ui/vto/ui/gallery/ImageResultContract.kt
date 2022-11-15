package za.co.woolworths.financial.services.android.ui.vto.ui.gallery

import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContract
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.lang.Exception


class ImageResultContract : ActivityResultContract<Uri, Uri>() {

    companion object {
        const val SEND_URI = "SEND_URI"
        const val GET_URI = "GET_URI"
        fun getImageSizeFromUriInMegaByte(context: Context, uri: Uri): Int {
            val scheme = uri.scheme
            var dataSize = 0
            if (scheme == ContentResolver.SCHEME_CONTENT) {
                try {
                    val fileInputStream: InputStream? = context.contentResolver.openInputStream(uri)
                    if (fileInputStream != null) {
                        dataSize = fileInputStream.available()
                    }
                } catch (e: Exception) {

                }
            } else if (scheme == ContentResolver.SCHEME_FILE) {
                val path = uri.path
                var file: File? = null
                try {
                    file = File(path)
                } catch (e: Exception) {

                }
                if (file != null) {
                    dataSize = file.length().toInt()
                }
            }
            return dataSize / (1024 * 1024)
        }


        fun saveImageToStorage(context: Context,bitmap: Bitmap) {
            val filename = "${System.currentTimeMillis()}.jpg"
            var fos: OutputStream? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                context.contentResolver?.also { resolver ->
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                    }
                    val imageUri: Uri? =
                        resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    fos = imageUri?.let { resolver.openOutputStream(it) }
                }
            } else {
                val imagesDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val image = File(imagesDir, filename)
                fos = FileOutputStream(image)
            }
            fos?.use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)

            }
        }

    }

    override fun createIntent(context: Context, input: Uri): Intent =
        Intent(context, BrowseFullScreenImageActivity::class.java).apply {
            putExtra(SEND_URI, input)
        }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri = when {
        resultCode != Activity.RESULT_OK -> Uri.EMPTY
        else -> intent?.getParcelableExtra(GET_URI)!!
    }

}