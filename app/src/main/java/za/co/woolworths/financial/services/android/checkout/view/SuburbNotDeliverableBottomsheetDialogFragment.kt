package za.co.woolworths.financial.services.android.checkout.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.fragment_suburb_not_deliverable_bottomsheet_dialog.*
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment

class SuburbNotDeliverableBottomsheetDialogFragment : WBottomSheetDialogFragment(),
    View.OnClickListener {

    companion object {
        const val ERROR_CODE_SUBURB_NOT_FOUND = "1162"
        const val RESULT_ERROR_CODE_SUBURB_NOT_FOUND = "1162"
        const val ERROR_CODE_SUBURB_NOT_DELIVERABLE = "1161"
        const val ERROR_CODE = "ERROR_CODE"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(
            R.layout.fragment_suburb_not_deliverable_bottomsheet_dialog,
            container,
            false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        when (arguments?.getString(ERROR_CODE, "")) {
            ERROR_CODE_SUBURB_NOT_FOUND -> {
                tvDescription?.visibility = View.GONE
                tvTitle?.text = context?.getString(R.string.suburb_not_found)
            }
            ERROR_CODE_SUBURB_NOT_DELIVERABLE -> {
                buttonChangeAddress.text =  context?.bindString(R.string.change_address)
            }
        }

        tvDismiss.setOnClickListener(this)
        buttonChangeAddress.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.buttonChangeAddress -> {
                val errorCode = arguments?.getString(ERROR_CODE, "")
                if (ERROR_CODE_SUBURB_NOT_FOUND.equals(errorCode, false)) {
                    setFragmentResult(RESULT_ERROR_CODE_SUBURB_NOT_FOUND, bundleOf())
                    dismiss()
                } else {
                    dismiss()
                }
            }
            else -> {
                dismiss()
            }
        }
    }
}