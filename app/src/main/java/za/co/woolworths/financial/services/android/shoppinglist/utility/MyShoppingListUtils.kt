package za.co.woolworths.financial.services.android.shoppinglist.utility

import android.app.Activity
import android.content.Intent


/*todo need to move in config file once finalized*/
const val BASE_SHARE_URL = "www-win-qa.woolworths.co.za/dashboard/shopping-lists/shoppinglist"
fun shareListUrl(message:String, activity: Activity) {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, message)
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, null)
    activity.startActivity(shareIntent)
}

fun prepareUrl(listId: String, selectedOption: String) = BASE_SHARE_URL.plus("/").plus(listId).plus("/").plus(selectedOption)
