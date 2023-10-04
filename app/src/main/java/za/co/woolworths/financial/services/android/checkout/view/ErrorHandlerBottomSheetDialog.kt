package za.co.woolworths.financial.services.android.checkout.view

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ErrorBottomSheetDialogBinding
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment

/**
 * Created by Kunal Uttarwar on 31/08/21.
 */
class ErrorHandlerBottomSheetDialog : WBottomSheetDialogFragment(),
    View.OnClickListener {

    private lateinit var binding: ErrorBottomSheetDialogBinding
    private var mclickListener: ClickListener? = null

    companion object {

        var errorType: Int? = 0
        const val ERROR_TITLE = "ERROR_TITLE"
        const val ERROR_DESCRIPTION = "ERROR_DESCRIPTION"
        const val ERROR_TYPE = "ERROR_TYPE"
        const val ERROR_TYPE_ADD_ADDRESS = 1034
        const val ERROR_TYPE_DELETE_ADDRESS = 1035
        const val ERROR_TYPE_PAYMENT_STATUS = 1033
        const val ERROR_TYPE_CONFIRM_DELIVERY_ADDRESS = 1036
        const val ERROR_TYPE_CONFIRM_COLLECTION_ADDRESS = 1037
        const val ERROR_TYPE_SHIPPING_DETAILS_COLLECTION = 1038
        const val ERROR_TYPE_CONNECT_ONLINE = 1039
        const val RESULT_ERROR_CODE_RETRY = "RESULT_ERROR_CODE_RETRY"

        fun newInstance(bundle: Bundle, clickListener: ClickListener) = ErrorHandlerBottomSheetDialog().apply {
            withArgs {
               putString(ERROR_TITLE, bundle.getString(ERROR_TITLE, ""))
               putString(ERROR_DESCRIPTION, bundle.getString(ERROR_DESCRIPTION, ""))
               putString(ERROR_TYPE, bundle.getString(ERROR_TYPE, ""))
            }
            mclickListener = clickListener
        }
    }
    

    interface  ClickListener{
        fun onRetryClick(errorType:Int)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ErrorBottomSheetDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.init()
        binding.initErrorView()
    }

    private fun ErrorBottomSheetDialogBinding.init() {
        cancelButton.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        cancelButton.setOnClickListener(this@ErrorHandlerBottomSheetDialog)
        actionButton.setOnClickListener(this@ErrorHandlerBottomSheetDialog)
    }

    private fun ErrorBottomSheetDialogBinding.initErrorView() {
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
        incSwipeCloseIndicator.root.visibility = View.VISIBLE
        if(errorType == ERROR_TYPE_CONNECT_ONLINE) {
            actionButton.text = getString(R.string.got_it)
            cancelButton.visibility = View.GONE
        }
        else {
            actionButton.text = getString(R.string.retry)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.actionButton -> {
                if(errorType != ERROR_TYPE_CONNECT_ONLINE) {
                    setFragmentResult(RESULT_ERROR_CODE_RETRY, bundleOf("bundle" to errorType))
                    errorType?.let {
                        mclickListener?.onRetryClick(it)
                    }
                }
                dismiss()
            }
            R.id.cancelButton -> {
                dismiss()
            }
        }
    }
}