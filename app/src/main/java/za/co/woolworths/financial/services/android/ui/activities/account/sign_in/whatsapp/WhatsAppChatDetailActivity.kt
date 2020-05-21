package za.co.woolworths.financial.services.android.ui.activities.account.sign_in.whatsapp

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.app_toolbar.*
import kotlinx.android.synthetic.main.whatsapp_chat_activity.*
import za.co.woolworths.financial.services.android.util.Utils

class WhatsAppChatDetailActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.updateStatusBarBackground(this)
        setContentView(R.layout.whatsapp_chat_activity)
        actionBar()

        with(WhatsAppConfig()) {
            whatsappNumberValueTextView?.text = whatsAppNumber
        }

        chatWithUsButton?.setOnClickListener(this)
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
               Utils.openBrowserWithUrl(WhatsAppConfig().whatsAppChatWithUsUrlBreakout)
            }
        }
    }
}