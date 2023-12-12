package za.co.woolworths.financial.services.android.geolocation.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import com.awfs.coordination.databinding.NewFbhBottomSheetDialogBinding
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.ui.views.tooltip.TooltipDialog
import za.co.woolworths.financial.services.android.util.Utils

class FBHInfoBottomSheetDialog : WBottomSheetDialogFragment() ,
        View.OnClickListener{
    private lateinit var binding: NewFbhBottomSheetDialogBinding

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = NewFbhBottomSheetDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initClick()
    }

    private fun initClick() {
        binding.gotItButton?.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.gotItButton -> {
                Utils.saveFeatureWalkthoughShowcase(TooltipDialog.Feature.NEW_FBH_CNC)
                dismiss()
            }
        }
    }
}