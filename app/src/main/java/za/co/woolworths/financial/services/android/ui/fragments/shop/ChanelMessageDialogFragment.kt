package za.co.woolworths.financial.services.android.ui.fragments.shop

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.databinding.ChanelMessageDialogBinding
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment

class ChanelMessageDialogFragment : WBottomSheetDialogFragment() {

    private lateinit var binding: ChanelMessageDialogBinding
    private var listener: IChanelMessageDialogDismissListener? = null

    interface IChanelMessageDialogDismissListener {
        fun onDialogDismiss()
    }

    public companion object {
        fun newInstance() = ChanelMessageDialogFragment()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as IChanelMessageDialogDismissListener?
        } catch (e: ClassCastException) {
            throw ClassCastException("Calling Activity must implement Callback interface")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = ChanelMessageDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonContinue.setOnClickListener {
            dismissAllowingStateLoss()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        listener?.onDialogDismiss()
    }

}