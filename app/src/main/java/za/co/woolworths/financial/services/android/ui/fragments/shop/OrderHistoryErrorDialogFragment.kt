package za.co.woolworths.financial.services.android.ui.fragments.shop

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.databinding.ErrorDialogFragmentBinding
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment

class OrderHistoryErrorDialogFragment : WBottomSheetDialogFragment() {

    private lateinit var binding: ErrorDialogFragmentBinding
    private var mDescription: String? = null
    private var listener: IOrderHistoryErrorDialogDismiss? = null

    interface IOrderHistoryErrorDialogDismiss {
        fun onErrorDialogDismiss()
    }

    companion object {
        private const val DESCRIPTION = "DESCRIPTION"
        fun newInstance(description: String) = OrderHistoryErrorDialogFragment().withArgs {
            putString(DESCRIPTION, description)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            mDescription = getString(DESCRIPTION)
        }
        try {
            listener = parentFragment as IOrderHistoryErrorDialogDismiss?
        } catch (e: ClassCastException) {
            throw ClassCastException("Calling fragment must implement Callback interface")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = ErrorDialogFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            tvDescription?.text = mDescription
            okButtonTapped?.setOnClickListener { dismissAllowingStateLoss() }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        listener?.onErrorDialogDismiss()
    }

}