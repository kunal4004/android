package za.co.woolworths.financial.services.android.ui.views.actionsheet

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import com.awfs.coordination.R
import com.awfs.coordination.databinding.GotItDescInfoDialogFragmentBinding
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

class InfoDialogFragment : WBottomSheetDialogFragment(), View.OnClickListener {

    private lateinit var binding: GotItDescInfoDialogFragmentBinding
    val args: InfoDialogFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = GotItDescInfoDialogFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            tvTitleTextView?.text = bindString(args.infoWindowTitle)
            tvDescriptionTextView?.text = bindString(args.infoWindowDesc)

            gotItButton?.apply {
                AnimationUtilExtension.animateViewPushDown(this)
                setOnClickListener(this@InfoDialogFragment)
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.gotItButton -> {
                dismiss()
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        setFragmentResult(InfoDialogFragment::class.java.simpleName, bundleOf())
        super.onDismiss(dialog)
    }
}