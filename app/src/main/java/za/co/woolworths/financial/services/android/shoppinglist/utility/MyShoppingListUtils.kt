package za.co.woolworths.financial.services.android.shoppinglist.utility

import android.app.Activity
import android.content.Intent

fun shareListUrl(message:String, activity: Activity) {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, message)
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, null)
    activity.startActivity(shareIntent)
}