package za.co.woolworths.financial.services.android.ui.vto.ui.gallery

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract
import java.io.File
import java.io.InputStream
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
                    e.printStackTrace()
                }
            } else if (scheme == ContentResolver.SCHEME_FILE) {
                val path = uri.path
                var file: File? = null
                try {
                    file = File(path)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                if (file != null) {
                    dataSize = file.length().toInt()
                }
            }
            return dataSize / (1024 * 1024)
        }
    }

    override fun createIntent(context: Context, input: Uri?): Intent =
        Intent(context, BrowseFullScreenImageActivity::class.java).apply {
            putExtra(SEND_URI, input)
        }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? = when {
        resultCode != Activity.RESULT_OK -> null
        else -> intent?.getParcelableExtra(GET_URI)!!
    }



}