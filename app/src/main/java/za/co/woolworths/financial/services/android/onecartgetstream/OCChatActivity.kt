package za.co.woolworths.financial.services.android.onecartgetstream

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ActivityOneCartChatActivityBinding
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.common.ClickOnDialogButton
import za.co.woolworths.financial.services.android.common.CommonErrorBottomSheetDialog
import za.co.woolworths.financial.services.android.util.Utils
import javax.inject.Inject

@AndroidEntryPoint
@Suppress("JoinDeclarationAndAssignment")
class OCChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOneCartChatActivityBinding
    private var orderID: String? = ""
    private var channelId: String? = null

    @Inject
    lateinit var errorBottomSheetDialog: CommonErrorBottomSheetDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOneCartChatActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Utils.updateStatusBarBackground(this)
        orderID = checkNotNull(intent.getStringExtra(ORDER_ID))
        channelId = intent.getStringExtra(CHANNEL_ID)

        if (savedInstanceState != null && lastNonConfigurationInstance == null) {
            // the application process was killed by the OS
            startActivity(packageManager.getLaunchIntentForPackage(packageName))
            finishAffinity()
        }
    }

    internal fun getOrderId() = orderID

    internal fun getChannelId() = channelId

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

                override fun onDismiss() {
                }
            },
            this,
            getString(R.string.generic_error_something_wrong_newline),
            getString(R.string.one_cart_chat_error_disc),
            getString(R.string.got_it),
            false
        )
    }


    companion object {
        private const val ORDER_ID = "key:oid"
        private const val CHANNEL_ID = "key:cid"

        @JvmStatic
        fun newIntent(context: Context, orderID: String, channelId: String? = null): Intent =
            Intent(context, OCChatActivity::class.java).apply {
                putExtra(ORDER_ID, orderID)
                channelId?.let { channelId ->
                    putExtra(CHANNEL_ID, channelId)
                }
            }
    }
}