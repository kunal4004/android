package za.co.woolworths.financial.services.android.ui.activities.deep_link

import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import android.content.Intent



class DynamicLinkActivity : AppCompatActivity() {

    companion object {
        val TAG: String = DynamicLinkActivity::class.java.toString()
    }

    enum class DeepLinkingType {
        CATEGORY,
        PRODUCT_LISTING,
        WTODAY,
        BARCODE_SCAN,
        TIPS_AND_TRICK
    }

    override fun onStart() {
        super.onStart()
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(intent)
                .addOnSuccessListener(this) { pendingDynamicLinkData ->
                    // Get deep link from result (may be null if no link is found)
                    var deepLink: Uri? = null

                    Log.e("deepLinkdata", pendingDynamicLinkData.link?.toString()?:"---")

                    // Handle the deep link. For example, open the linked
                    // content, or apply promotional credit to the user's
                    // account.
                    // ...

                    // ...
                }
                .addOnFailureListener(this) { e -> Log.w(TAG, "getDynamicLink:onFailure", e) }

    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        Log.e("deepLinkdata", intent.toString())

    }
}