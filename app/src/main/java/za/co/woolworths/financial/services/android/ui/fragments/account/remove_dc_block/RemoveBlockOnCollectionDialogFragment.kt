package za.co.woolworths.financial.services.android.ui.fragments.account.remove_dc_block

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.account_in_arrears_alert_dialog_fragment.*
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

class RemoveBlockOnCollectionDialogFragment : AppCompatDialogFragment(), View.OnClickListener {

    private val mClassName = RemoveBlockOnCollectionDialogFragment::class.java.simpleName

    companion object {
        const val ARREARS_PAY_NOW_BUTTON = "payNowButton"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.remove_block_on_collection_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        payNowButton?.apply {
            setOnClickListener(this@RemoveBlockOnCollectionDialogFragment)
            AnimationUtilExtension.animateViewPushDown(this)
        }


        closeIconImageButton?.apply {
            setOnClickListener(this@RemoveBlockOnCollectionDialogFragment)
            AnimationUtilExtension.animateViewPushDown(this)
        }

    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.payNowButton -> {
                dismiss()
                setFragmentResult(mClassName, bundleOf(mClassName to ARREARS_PAY_NOW_BUTTON))
            }
        }
    }
}