package za.co.woolworths.financial.services.android.ui.fragments.mypreferences

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.delete_account_bottom_sheet_dialog.*
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.DELETE_ACCOUNT
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.DELETE_ACCOUNT_CONFIRMATION


class DeleteAccountBottomSheetDialog : WBottomSheetDialogFragment() ,
    View.OnClickListener{

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(
            R.layout.delete_account_bottom_sheet_dialog,
            container,
            false
        )

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initClick()
    }

    private fun initClick() {
        cancelButton.setOnClickListener(this)
        actionButton.setOnClickListener(this)
    }




    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.actionButton -> {
                setFragmentResult(DELETE_ACCOUNT_CONFIRMATION, bundleOf(DELETE_ACCOUNT to DELETE_ACCOUNT))
                dismiss()
            }
            R.id.cancelButton -> {
                dismiss()
            }
        }
    }


}