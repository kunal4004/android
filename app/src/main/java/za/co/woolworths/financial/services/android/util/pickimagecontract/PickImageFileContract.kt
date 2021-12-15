package za.co.woolworths.financial.services.android.util.pickimagecontract

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract

class PickImageFileContract : ActivityResultContract<String, Uri?>() {

    override fun createIntent(context: Context, input: String) =

        Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            val mimeTypes = arrayOf("image/png", "image/jpg")
            type = "image/*"
            putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            type = input

        }

    override fun parseResult(resultCode: Int, intent: Intent?) =
        intent?.data?.takeIf { resultCode == Activity.RESULT_OK }
}