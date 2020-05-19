package za.co.woolworths.financial.services.android.ui.activities.account.sign_in.whatsapp

import android.os.Bundle
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.base.ToolbarAppcompatActivity

class WhatsAppChatDetailActivity : ToolbarAppcompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.whatsapp_chat_activity)
        actionBar()
    }



}