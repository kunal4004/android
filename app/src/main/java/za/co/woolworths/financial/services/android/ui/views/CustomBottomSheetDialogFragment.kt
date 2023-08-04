package za.co.woolworths.financial.services.android.ui.views

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.awfs.coordination.R
import com.awfs.coordination.databinding.FragmentCustomBottomsheetDialogBinding
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment

/**
 * Created by Kunal Uttarwar on 13/04/22.
 */
/**
 * This class can be used anywhere to show bottom dialog with button and image.
 */
class CustomBottomSheetDialogFragment : WBottomSheetDialogFragment(),
    View.OnClickListener {

    companion object {
        const val DIALOG_TITLE = "dialog_title"
        const val DIALOG_SUB_TITLE = "dialog_sub_title"
        const val DIALOG_BUTTON_TEXT = "dialog_button_text"
        const val DIALOG_TITLE_IMG = "dialog_title_img"
        const val DIALOG_BUTTON_CLICK_RESULT = "dialog_button_click_result"
        const val DIALOG_BUTTON_DISMISS_RESULT = "dialog_button_dismiss_result"
        const val DIALOG_DISMISS_LINK_TEXT = "dialog_dismiss_link_text"

        fun newInstance(
            title: String,
            subTitle: CharSequence,
            dialog_button_text: String,
            dialog_title_img: Int,
            dismissLinkText: String?,
        ) =
            CustomBottomSheetDialogFragment().withArgs {
                putString(DIALOG_TITLE, title)
                putCharSequence(DIALOG_SUB_TITLE, subTitle)
                putString(DIALOG_BUTTON_TEXT, dialog_button_text)
                putInt(DIALOG_TITLE_IMG, dialog_title_img)
                putString(DIALOG_DISMISS_LINK_TEXT, dismissLinkText)
            }

        fun newInstance(
            title: String,
            subTitle: CharSequence,
            dialog_button_text: String,
            dismissLinkText: String?,
            dialogResultCode: String,
        ) =
            CustomBottomSheetDialogFragment().withArgs {
                putString(DIALOG_TITLE, title)
                putCharSequence(DIALOG_SUB_TITLE, subTitle)
                putString(DIALOG_BUTTON_TEXT, dialog_button_text)
                putString(DIALOG_DISMISS_LINK_TEXT, dismissLinkText)
                putString(DIALOG_BUTTON_CLICK_RESULT, dialogResultCode)
            }
    }

    private lateinit var binding: FragmentCustomBottomsheetDialogBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentCustomBottomsheetDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.init()
    }

    private fun FragmentCustomBottomsheetDialogBinding.init() {
        arguments?.apply {
            val title = getString(DIALOG_TITLE, "")
            if (title.isNullOrEmpty()) {
                tvTitle?.visibility = View.GONE
            } else {
                tvTitle?.visibility = View.VISIBLE
                tvTitle?.text = title
            }
            val subTitle = getCharSequence(DIALOG_SUB_TITLE, "")
            if (subTitle.isNullOrEmpty()) {
                tvDescription?.visibility = View.GONE
            } else {
                tvDescription?.visibility = View.VISIBLE
                tvDescription?.text = subTitle
            }
            val buttonText = getString(DIALOG_BUTTON_TEXT, "")
            if (buttonText.isNullOrEmpty()) {
                buttonAction?.visibility = View.GONE
            } else {
                buttonAction?.visibility = View.VISIBLE
                buttonAction?.text = buttonText
            }
            val dialogImg = getInt(DIALOG_TITLE_IMG)
            if (dialogImg != null) {
                imgView.visibility = View.VISIBLE
                imgView.setImageResource(dialogImg)
            } else
                imgView.visibility = View.GONE

            val linkText = getString(DIALOG_DISMISS_LINK_TEXT)
            if (linkText == null) {
                tvDismiss?.text = getText(R.string.cancel_underline)
            } else if (linkText.isEmpty()) {
                tvDismiss?.visibility = View.GONE
            } else {
                tvDismiss?.text = linkText
            }
        }

        tvDismiss.setOnClickListener(this@CustomBottomSheetDialogFragment)
        buttonAction.setOnClickListener(this@CustomBottomSheetDialogFragment)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.buttonAction -> {
                setFragmentResult(arguments?.getString(DIALOG_BUTTON_CLICK_RESULT)
                    ?: DIALOG_BUTTON_CLICK_RESULT, bundleOf())
                dismiss()
            }
            R.id.tvDismiss -> {
                val bundle = Bundle()
                bundle.putString(DIALOG_BUTTON_CLICK_RESULT, arguments?.getString(DIALOG_BUTTON_CLICK_RESULT))
                setFragmentResult(DIALOG_BUTTON_DISMISS_RESULT, bundle)
                dismiss()
            }
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        val bundle = Bundle()
        bundle.putString(DIALOG_BUTTON_CLICK_RESULT, arguments?.getString(DIALOG_BUTTON_CLICK_RESULT))
        setFragmentResult(DIALOG_BUTTON_DISMISS_RESULT, bundle)
        super.onCancel(dialog)
    }
}