package za.co.woolworths.financial.services.android.ui.fragments.account.whatsapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.whatsapp_chat_to_us_row_fragment.*
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.whatsapp.WhatsAppChatDetailActivity
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

class WhatsAppChatToUsRowFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.whatsapp_chat_to_us_row_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        chatToUsOnWhatsAppConstraintLayout?.apply {
            AnimationUtilExtension.animateViewPushDown(this)
            setOnClickListener {
                val navigateToWhatsAppChatDetailActivity = Intent(WoolworthsApplication.getAppContext(), WhatsAppChatDetailActivity::class.java)
                startActivity(navigateToWhatsAppChatDetailActivity)
            }
        }
    }
}