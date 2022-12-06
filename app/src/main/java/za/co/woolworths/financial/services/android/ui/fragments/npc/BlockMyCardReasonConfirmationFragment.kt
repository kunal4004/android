package za.co.woolworths.financial.services.android.ui.fragments.npc

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.contracts.IStoreCardListener
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import android.content.Context
import android.graphics.Paint
import com.awfs.coordination.databinding.NpcPermanentCardBlockLayoutBinding
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.util.Utils

class BlockMyCardReasonConfirmationFragment : WBottomSheetDialogFragment(), View.OnClickListener {

    private lateinit var binding: NpcPermanentCardBlockLayoutBinding
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
        binding = NpcPermanentCardBlockLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            tvCancel?.paintFlags = Paint.UNDERLINE_TEXT_FLAG
            tvCancel?.setOnClickListener(this@BlockMyCardReasonConfirmationFragment)
            yesBlockCardButton?.setOnClickListener(this@BlockMyCardReasonConfirmationFragment)
        }

    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.yesBlockCardButton -> {
                Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.BLOCK_CARD_CONFIRM, activity)
                mStoreCardListenerCallback?.onBlockPermanentCardPermissionGranted()
                dismiss()
            }
            R.id.tvCancel -> {
                Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.BLOCK_CARD_CANCEL, activity)
                dismiss()
            }
        }
    }
}
