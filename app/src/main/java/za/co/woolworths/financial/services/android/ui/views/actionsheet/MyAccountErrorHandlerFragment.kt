package za.co.woolworths.financial.services.android.ui.views.actionsheet

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.account_error_handler_fragment.*
import za.co.woolworths.financial.services.android.contracts.IMyAccountInterface
import za.co.woolworths.financial.services.android.ui.extension.withArgs

class MyAccountErrorHandlerFragment : WBottomSheetDialogFragment() {

    private var mDescription: String? = null
    private var myAccountInterface: IMyAccountInterface? = null

    companion object {
        private const val DESCRIPTION = "DESCRIPTION"
        fun newInstance(description: String) = MyAccountErrorHandlerFragment().withArgs {
            putString(DESCRIPTION, description)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            if (activity is IMyAccountInterface)
                myAccountInterface = context as? IMyAccountInterface
        } catch (e: ClassCastException) {
            throw ClassCastException("$this must implement MyInterface ")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            mDescription = getString(DESCRIPTION)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.account_error_handler_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvDescription?.text = mDescription

        retryButton?.setOnClickListener {
            myAccountInterface?.onRetryMyAccountCall()
            dismiss()
        }
        cancelButton?.setOnClickListener { dismiss() }
    }
}