package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.remove_dc_block

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.databinding.RemoveBlockDcMainFragmentBinding
import za.co.woolworths.financial.services.android.ui.base.ViewBindingFragment

class RemoveBlockOnCollectionFragment : ViewBindingFragment<RemoveBlockDcMainFragmentBinding>() {

    override fun inflateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): RemoveBlockDcMainFragmentBinding {
        return RemoveBlockDcMainFragmentBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

}