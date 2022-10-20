package za.co.woolworths.financial.services.android.ui.fragments.wreward

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.databinding.VirtualCardNumberInfoDialogBinding
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment

class VirtualCardNumberInfoDialogFragment : WBottomSheetDialogFragment() {

    companion object {
        fun newInstance() = VirtualCardNumberInfoDialogFragment()
    }

    private lateinit var binding: VirtualCardNumberInfoDialogBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = VirtualCardNumberInfoDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.gotItButton.setOnClickListener {
            dismissAllowingStateLoss()
        }
    }

}