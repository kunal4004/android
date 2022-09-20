package za.co.woolworths.financial.services.android.onecartgetstream

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.awfs.coordination.R
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.common.ClickOnDialogButton
import za.co.woolworths.financial.services.android.common.CommonErrorBottomSheetDialog
import za.co.woolworths.financial.services.android.util.Utils
import javax.inject.Inject

@AndroidEntryPoint
@Suppress("JoinDeclarationAndAssignment")
class OCChatActivity : AppCompatActivity(R.layout.activity_one_cart_chat_activity) {

    private var orderID: String? = ""
    @Inject
    lateinit var errorBottomSheetDialog: CommonErrorBottomSheetDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.updateStatusBarBackground(this)
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

    internal fun showErrorDialog() {
        errorBottomSheetDialog.showCommonErrorBottomDialog(
            object : ClickOnDialogButton {
                override fun onClick() {
                    finish()
                }
            },
            this,
            getString(R.string.generic_error_something_wrong_newline),
            getString(R.string.one_cart_chat_error_disc),
            getString(R.string.got_it)
        )
    }


    companion object {
        private const val ORDER_ID = "key:oid"
        @JvmStatic
        fun newIntent(context: Context, orderID: String): Intent =
            Intent(context, OCChatActivity::class.java).putExtra(ORDER_ID, orderID)
    }
}