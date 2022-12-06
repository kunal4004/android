package za.co.woolworths.financial.services.android.ui.fragments.mypreferences

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import com.awfs.coordination.databinding.DeletedSuccessBottomSheetDialogBinding
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment

class DeletedSuccessBottomSheetDialog : WBottomSheetDialogFragment(),
    View.OnClickListener {

    private lateinit var binding: DeletedSuccessBottomSheetDialogBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DeletedSuccessBottomSheetDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.initClick()
    }

    private fun DeletedSuccessBottomSheetDialogBinding.initClick() {
        gotItButton.setOnClickListener(this@DeletedSuccessBottomSheetDialog)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.gotItButton -> {
                dismiss()
            }
        }
    }

    companion object {
        fun newInstance(): DeletedSuccessBottomSheetDialog {

            return DeletedSuccessBottomSheetDialog()
        }
        const val TAG = "DeletedSuccessBottomSheetDialog"
    }
}
