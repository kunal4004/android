package za.co.woolworths.financial.services.android.ui.activities.account.sign_in.whatsapp

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.whatsapp_chat_activity.*
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.whatsapp.WhatsAppImpl.Companion.APP_SCREEN
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.whatsapp.WhatsAppImpl.Companion.FEATURE_NAME
import za.co.woolworths.financial.services.android.ui.extension.request
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension


class WhatsAppChatDetailActivity : AppCompatActivity(), View.OnClickListener {

    private var featureName: String? = null
    private var appScreen: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.updateStatusBarBackground(this)
        setContentView(R.layout.whatsapp_chat_activity)

        intent?.extras?.apply {
            featureName = getString(FEATURE_NAME, "")
            appScreen = getString(APP_SCREEN, "")
        }

        with(WhatsAppImpl()) {
            whatsappNumberValueTextView?.text = whatsAppNumber
        }

        chatWithUsButton?.apply {
            AnimationUtilExtension.animateViewPushDown(this)
            setOnClickListener(this@WhatsAppChatDetailActivity)
        }

        actionBar()
    }

    fun actionBar() {
        setSupportActionBar(whatsAppToolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
            setDisplayUseLogoEnabled(false)
            setHomeAsUpIndicator(R.drawable.back24)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.chatWithUsButton -> {
                request(OneAppService.queryServicePostEvent(featureName, appScreen))
                Utils.openBrowserWithUrl(WhatsAppImpl().whatsAppChatWithUsUrlBreakout)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            else -> false
        }
    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
    }
}