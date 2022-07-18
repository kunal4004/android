package za.co.woolworths.financial.services.android.getstream

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.awfs.coordination.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
@Suppress("JoinDeclarationAndAssignment")
class OCChatActivity : AppCompatActivity(R.layout.activity_one_cart_chat_activity) {

    private var orderID: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        orderID = checkNotNull(intent.getStringExtra(ORDER_ID))

        if (savedInstanceState != null && lastNonConfigurationInstance == null) {
            // the application process was killed by the OS
            startActivity(packageManager.getLaunchIntentForPackage(packageName))
            finishAffinity()
        }
    }

    internal fun getOrderId() = orderID

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    companion object {
        private const val ORDER_ID = "key:oid"
        fun newIntent(context: Context, orderID: String): Intent =
            Intent(context, OCChatActivity::class.java).putExtra(ORDER_ID, orderID)
    }
}