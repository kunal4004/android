package za.co.woolworths.financial.services.android.ui.fragments.card

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.permanent_card_block_dialog_fragment.*
import za.co.woolworths.financial.services.android.contracts.IPermanentCardBlock
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import android.app.Activity
import android.graphics.Paint
import android.graphics.Paint.UNDERLINE_TEXT_FLAG


class PermanentCardBlockDialogFragment : WBottomSheetDialogFragment() {

    private var mPermanentCardBlockCallback: IPermanentCardBlock? = null


    companion object {
        fun newInstance() = PermanentCardBlockDialogFragment().withArgs {
        }
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        activity?.apply {
            try {
                mPermanentCardBlockCallback = this as? IPermanentCardBlock
            } catch (e: ClassCastException) {
                throw ClassCastException("$this must implement MyInterface ")
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.permanent_card_block_dialog_fragment, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvCancel?.paintFlags = Paint.UNDERLINE_TEXT_FLAG;
        tvCancel?.setOnClickListener { dismiss() }
        btnBlockPermanentCard?.setOnClickListener {
            mPermanentCardBlockCallback?.onBlockPermanentCardPermissionGranted()
            dismiss()
        }
    }
}
