package za.co.woolworths.financial.services.android.ui.views.actionsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.account_in_arrears_fragment_dialog.*
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

class WhatsAppUnavailableFragment : WBottomSheetDialogFragment(), View.OnClickListener {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.whatsapp_unavailable_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        gotITButton?.apply {
            AnimationUtilExtension.animateViewPushDown(this)
            setOnClickListener(this@WhatsAppUnavailableFragment)
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.gotITButton -> dismiss()
        }
    }
}