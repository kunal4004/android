package za.co.woolworths.financial.services.android.ui.fragments.npc

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.npc_permanent_card_block_layout.*
import za.co.woolworths.financial.services.android.contracts.IStoreCardListener
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import android.content.Context
import android.graphics.Paint
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.util.Utils


class BlockMyCardReasonConfirmationFragment : WBottomSheetDialogFragment() {

    private var mStoreCardListenerCallback: IStoreCardListener? = null


    companion object {
        fun newInstance() = BlockMyCardReasonConfirmationFragment().withArgs {
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.apply {
            try {
                mStoreCardListenerCallback = this as? IStoreCardListener
            } catch (e: ClassCastException) {
                throw ClassCastException("$this must implement MyInterface ")
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.npc_permanent_card_block_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.apply {
            tvCancel?.paintFlags = Paint.UNDERLINE_TEXT_FLAG
            tvCancel?.setOnClickListener {
                Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.BLOCK_CARD_CANCEL, this)
                dismiss()
            }
            btnBlockPermanentCard?.setOnClickListener {
                Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.BLOCK_CARD_CONFIRM, this)
                mStoreCardListenerCallback?.onBlockPermanentCardPermissionGranted()
                dismiss()
            }
        }
    }
}
