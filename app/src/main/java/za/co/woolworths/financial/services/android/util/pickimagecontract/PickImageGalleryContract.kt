package za.co.woolworths.financial.services.android.util.pickimagecontract

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract


class PickImageGalleryContract() : ActivityResultContract<String, Uri?>() {

    override fun createIntent(context: Context, input: String) =

        Intent(
            Intent.ACTION_PICK,
            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        ).apply { type = input }

    override fun parseResult(resultCode: Int, intent: Intent?) =
        intent?.data?.takeIf { resultCode == Activity.RESULT_OK }
}