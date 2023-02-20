package za.co.woolworths.financial.services.android.ui.views.actionsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import com.awfs.coordination.databinding.WhatsappUnavailableFragmentBinding
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

class WhatsAppUnavailableFragment : WBottomSheetDialogFragment(), View.OnClickListener {

    private lateinit var binding: WhatsappUnavailableFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = WhatsappUnavailableFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.gotItButton?.apply {
            AnimationUtilExtension.animateViewPushDown(this)
            setOnClickListener(this@WhatsAppUnavailableFragment)
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.gotItButton -> dismiss()
        }
    }
}