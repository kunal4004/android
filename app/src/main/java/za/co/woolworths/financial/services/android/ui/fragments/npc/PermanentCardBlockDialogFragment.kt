package za.co.woolworths.financial.services.android.ui.fragments.npc

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.npc_permanent_card_block_layout.*
import za.co.woolworths.financial.services.android.contracts.IPermanentCardBlock
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import android.content.Context
import android.graphics.Paint


class PermanentCardBlockDialogFragment : WBottomSheetDialogFragment() {

    private var mPermanentCardBlockCallback: IPermanentCardBlock? = null


    companion object {
        fun newInstance() = PermanentCardBlockDialogFragment().withArgs {
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.apply {
            try {
                mPermanentCardBlockCallback = this as? IPermanentCardBlock
            } catch (e: ClassCastException) {
                throw ClassCastException("$this must implement MyInterface ")
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.npc_permanent_card_block_layout, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvCancel?.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        tvCancel?.setOnClickListener { dismiss() }
        btnBlockPermanentCard?.setOnClickListener {
            mPermanentCardBlockCallback?.onBlockPermanentCardPermissionGranted()
            dismiss()
        }
    }
}
