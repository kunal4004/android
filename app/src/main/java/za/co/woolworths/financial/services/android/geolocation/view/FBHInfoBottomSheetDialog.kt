package za.co.woolworths.financial.services.android.geolocation.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.pargo_store_info_bottom_sheet_dialog.*
import za.co.woolworths.financial.services.android.ui.views.WMaterialShowcaseView
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.util.Utils

class FBHInfoBottomSheetDialog : WBottomSheetDialogFragment() ,
        View.OnClickListener{

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(
                R.layout.new_fbh_bottom_sheet_dialog,
                container,
                false
        )

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initClick()
    }

    private fun initClick() {
        gotItButton?.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.gotItButton -> {
                Utils.saveFeatureWalkthoughShowcase(WMaterialShowcaseView.Feature.NEW_FBH_CNC)
                dismiss()
            }
        }
    }
}