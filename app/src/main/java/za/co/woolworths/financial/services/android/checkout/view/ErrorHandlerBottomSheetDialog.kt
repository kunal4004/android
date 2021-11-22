package za.co.woolworths.financial.services.android.checkout.view

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.error_bottom_sheet_dialog.*
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment

/**
 * Created by Kunal Uttarwar on 31/08/21.
 */
class ErrorHandlerBottomSheetDialog : WBottomSheetDialogFragment(),
    View.OnClickListener {

    companion object {

        var errorType: Int? = 0
        const val ERROR_TITLE = "ERROR_TITLE"
        const val ERROR_DESCRIPTION = "ERROR_DESCRIPTION"
        const val ERROR_TYPE = "ERROR_TYPE"
        const val ERROR_TYPE_ADD_ADDRESS = 1034
        const val ERROR_TYPE_DELETE_ADDRESS = 1035
        const val ERROR_TYPE_CONFIRM_DELIVERY_ADDRESS = 1036
        const val ERROR_TYPE_CONFIRM_COLLECTION_ADDRESS = 1037
        const val ERROR_TYPE_SHIPPING_DETAILS_COLLECTION = 1038
        const val RESULT_ERROR_CODE_RETRY = "RESULT_ERROR_CODE_RETRY"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            R.layout.error_bottom_sheet_dialog,
            container,
            false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        initErrorView()
    }

    private fun init() {
        cancelButton.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        cancelButton.setOnClickListener(this)
        actionButton.setOnClickListener(this)
    }

    private fun initErrorView() {
        arguments?.apply {
            if (containsKey(ERROR_TYPE)){
                errorType = arguments?.getInt(ERROR_TYPE, ERROR_TYPE_ADD_ADDRESS)
            }
            if (containsKey(ERROR_TITLE)) {
                errorTitle.text = arguments?.getString(ERROR_TITLE, "")
            }

            if (containsKey(ERROR_DESCRIPTION)) {
                errorDescription?.text = getString(ERROR_DESCRIPTION, "")
            }
        }

        errorLogo.setImageResource(R.drawable.ic_error_icon)
        cancelButton?.visibility = View.VISIBLE
        incSwipeCloseIndicator.visibility = View.VISIBLE
        actionButton.text = getString(R.string.retry)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.actionButton -> {
                setFragmentResult(RESULT_ERROR_CODE_RETRY, bundleOf("bundle" to errorType))
                dismiss()
            }
            R.id.cancelButton -> {
                dismiss()
            }
        }
    }
}