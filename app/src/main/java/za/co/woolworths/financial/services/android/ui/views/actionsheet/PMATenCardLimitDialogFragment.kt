package za.co.woolworths.financial.services.android.ui.views.actionsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.databinding.PmaTenCardLimitExceedFragmentBinding
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

class PMATenCardLimitDialogFragment : WBottomSheetDialogFragment() {

    private lateinit var binding: PmaTenCardLimitExceedFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = PmaTenCardLimitExceedFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.okButtonTapped?.apply {
            AnimationUtilExtension.animateViewPushDown(this)
            setOnClickListener { dismiss() }
        }
    }
}